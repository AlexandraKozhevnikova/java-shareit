package ru.practicum.shareit;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static io.restassured.RestAssured.given;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ShareItTests {

    private static final String HOST = "http://localhost:8080";
    private static final String USERS = "/users";
    private static final String ITEM = "/items";
    private static final String BOOKING = "/bookings";
    public static final String REQUEST = "/requests";


    @BeforeAll
    private static void run() {
        SpringApplication.run(ShareItApp.class);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void createUser_whenUserValid_thenReturnOkWithUser() {
        given().log().all()
                .when().get(USERS)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("", Matchers.empty());

        given().log().all()
                .contentType(ContentType.JSON)
                .body("{\n    \"name\": \"user\",\n    \"email\": \"user@user.com\"\n}")
                .when().post(HOST + USERS)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("id", is(1))
                .body("name", is("user"))
                .body("email", is("user@user.com"));

        given().log().all()
                .when().get(USERS)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("", hasSize(1))
                .body("[0].id", is(1))
                .body("[0].name", is("user"))
                .body("[0].email", is("user@user.com"));
    }

    @Test
    void getItem_whenItemWithCommentAndGetByOwner_thenReturnFullItemInfo() throws Exception {
        doDataPreparation_createUser();
        doDataPreparation_createUser();
        doDataPreparation_createItem(null, 1L);
        doDataPreparation_createBooking(2L, 1L, LocalDateTime.now().plusSeconds(1L));
        doDataPreparation_createBooking(2L, 1L, LocalDateTime.parse("2040-01-31T19:53:19.363093"));

        Thread.sleep(3000L); //чтобы бронирование стало прошедшим
        doDataPreparation_createComment(2);

        given().log().all()
                .pathParam("itemId", 1)
                .header("X-Sharer-User-Id", 1)
                .when().get(ITEM + "/{itemId}")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("id", is(1))
                .body("name", is("Дрель"))
                .body("description", is("Простая дрель"))
                .body("available", is(true))
                .body("lastBooking.id", is(1))
                .body("lastBooking.bookerId", is(2))
                .body("nextBooking.id", is(2))
                .body("nextBooking.bookerId", is(2))
                .body("comments", hasSize(1));
    }

    @Test
    void getOwnersItems_whenItemGetByOwner_thenReturnItemInfo() {
        doDataPreparation_createUser();
        doDataPreparation_createUser();
        doDataPreparation_createItem(null, 1L);
        doDataPreparation_createBooking(2L, 1L, LocalDateTime.parse("2040-01-31T19:53:19.363093"));

        given().log().all()
                .header("X-Sharer-User-Id", 1)
                .when().get(ITEM)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("", hasSize(1))
                .body("[0].id", is(1))
                .body("[0].name", is("Дрель"))
                .body("[0].description", is("Простая дрель"))
                .body("[0].available", is(true))
                .body("[0].lastBooking", is(nullValue()))
                .body("[0].nextBooking.id", is(1))
                .body("[0].nextBooking.bookerId", is(2))
                .body("[0].comments", is(nullValue()));
    }

    @Test
    void searchItem_whenItemsMatch_thenReturnListItems() {
        doDataPreparation_createUser();
        doDataPreparation_createItem(null, 1L);
        doDataPreparation_createItem(null, 1L);

        given().log().all()
                .header("X-Sharer-User-Id", 1)
                .queryParam("text", "дрЕл")
                .when().get(ITEM + "/search")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("", hasSize(2))
                .body("[0].id", is(1))
                .body("[0].name", is("Дрель"))
                .body("[0].description", is("Простая дрель"))
                .body("[0].available", is(true));
    }

    @Test
    void createItemRequest_whenEmptyBody_then400() {
        doDataPreparation_createUser();

        given().log().all()
                .contentType(ContentType.JSON)
                .body("{ }")
                .header("X-Sharer-User-Id", 1)
                .when().post(HOST + REQUEST)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("error", is("'text' must not be blank"))
                .body("errorInfo.type", is("validation"));
    }

    @Test
    void createItemRequest_whenDescriptionIsNull_then400() {
        doDataPreparation_createUser();

        given().log().all()
                .contentType(ContentType.JSON)
                .body("{\n" +
                        "    \"description\": null \n" +
                        "}")
                .header("X-Sharer-User-Id", 1)
                .when().post(HOST + REQUEST)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("error", is("'text' must not be blank"))
                .body("errorInfo.type", is("validation"));
    }

    @Test
    void createItemRequest_whenUserNotExist_then404() {
        given().log().all()
                .contentType(ContentType.JSON)
                .body("{\n" +
                        "    \"description\": \"some abc\" \n" +
                        "}")
                .header("X-Sharer-User-Id", 1)
                .when().post(HOST + REQUEST)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("error", is("Пользователя с 'id' = 1 не существует"))
                .body("errorInfo.type", is("logic"));
    }

    @Test
    void getItemRequestById_whenRequestIdIsNotLong_then404() {
        given().log().all()
                .pathParam("requestId", "abc")
                .header("X-Sharer-User-Id", 1)
                .when().get(REQUEST + "/{requestId}")
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("error", containsString("Failed to convert value of type"))
                .body("errorInfo.type", is("common"));
    }

    @Test
    void getItemRequest_whenItemOfferIsExist_thenReturnResponseWithItems() {
        doDataPreparation_createUser();
        doDataPreparation_createUser();
        doDataPreparation_createItemRequest(1L);
        doDataPreparation_createItem(1L, 2L);

        String created = given().log().all()
                .pathParam("requestId", 1)
                .header("X-Sharer-User-Id", 1)
                .when().get(REQUEST + "/{requestId}")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("id", is(1))
                .body("description", is("что-то для сверления стен"))
                .body("items", hasSize(1))
                .body("items[0].id", is(1))
                .body("items[0].name", is("Дрель"))
                .body("items[0].description", is("Простая дрель"))
                .body("items[0].requestId", is(1))
                .body("items[0].available", is(true))
                .extract().body().path("created");

        assertThat(LocalDateTime.parse(created), within(13, ChronoUnit.HOURS, LocalDateTime.now()));
    }

    @Test
    void getItemRequest_whenItemOfferIsNotExist() {
        doDataPreparation_createUser();
        doDataPreparation_createUser();
        doDataPreparation_createItemRequest(1L);
        doDataPreparation_createItem(null, 2L);

        String created = given().log().all()
                .pathParam("requestId", 1)
                .header("X-Sharer-User-Id", 1)
                .when().get(REQUEST + "/{requestId}")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("id", is(1))
                .body("description", is("что-то для сверления стен"))
                .body("items", emptyIterable())
                .extract().body().path("created");

        assertThat(LocalDateTime.parse(created), within(13, ChronoUnit.HOURS, LocalDateTime.now()));
    }

    private void doDataPreparation_createUser() {
        given().log().all()
                .contentType(ContentType.JSON)
                .body("{\n    \"name\": \"user\",\n    \"email\": \""
                        + RandomUtils.nextLong() + "user@user.com\"\n}")
                .when().post(HOST + USERS)
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    private void doDataPreparation_createItem(Long requestId, long ownerId) {
        given().log().all()
                .contentType(ContentType.JSON)
                .body("{\n" +
                        "    \"name\": \"Дрель\",\n" +
                        "    \"description\": \"Простая дрель\",\n" +
                        "    \"available\": true, \n" +
                        "    \"requestId\":" + requestId +
                        "}")
                .header("X-Sharer-User-Id", ownerId)
                .when().post(HOST + ITEM)
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    private void doDataPreparation_createBooking(long userId, long itemId, LocalDateTime start) {
        ValidatableResponse createBookingResponse = given().log().all()
                .contentType(ContentType.JSON)
                .body("{\n" +
                        "    \"itemId\": " + itemId + ",\n" +
                        "    \"start\": \"" + start + "\",\n" +
                        "    \"end\": \"" + start.plusSeconds(2) + "\"\n" +
                        "}")
                .header("X-Sharer-User-Id", userId)
                .when().post(HOST + BOOKING)
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        given().log().all()
                .pathParam("bookingId", createBookingResponse.extract().body().path("id"))
                .header("X-Sharer-User-Id", 1L)
                .queryParam("approved", true)
                .when().patch(HOST + BOOKING + "/{bookingId}")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    private void doDataPreparation_createComment(long userId) {
        given().log().all()
                .contentType(ContentType.JSON)
                .body(" {\n" +
                        "    \"text\": \"Add comment\"\n" +
                        "}")
                .header("X-Sharer-User-Id", userId)
                .pathParam("itemId", 1)
                .when().post(HOST + ITEM + "/{itemId}/comment")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    private void doDataPreparation_createItemRequest(long userId) {
        given().log().all()
                .contentType(ContentType.JSON)
                .body("{\n" +
                        "    \"description\": \"что-то для сверления стен\" \n" +
                        "}")
                .header("X-Sharer-User-Id", userId)
                .when().post(HOST + REQUEST)
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

}


//TODO  штуки с удалением каскад или сет нал
