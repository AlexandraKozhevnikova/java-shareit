package ru.practicum.shareit;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@SpringBootTest //может тут каждый тест новые данные чтобы не пересекались
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
class ShareItTests {

    private static final String HOST = "http://localhost:8080";
    private static final String USERS = "/users";
    private static final String ITEM = "/items";
    private static final String BOOKING = "/bookings";

    @BeforeAll
    private static void run() {
        SpringApplication.run(ShareItApp.class);
    }

    @BeforeEach
    private void clear() {


    }

    @Test
    void contextLoads() {
    }

    @Test
    @Transactional
    @Rollback(value = true)
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
        doDataPreparation_createItem();
        doDataPreparation_createBooking(2L, 1L, LocalDateTime.now().plusSeconds(1L));
        doDataPreparation_createBooking(2L, 1L, LocalDateTime.parse("2040-01-31T19:53:19.363093"));

        Thread.sleep(3000L); //чтобы бронирование стало прошедшим
        doDataPreparation_addComment(2);

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
    void getOwnersItems_whenItemGetByOwner_thenReturnItemInfo() throws Exception {
        doDataPreparation_createUser();
        doDataPreparation_createUser();
        doDataPreparation_createItem();
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
    void searchItem_whenItemsMatch_thenReturnListItems() throws Exception {
        doDataPreparation_createUser();
        doDataPreparation_createItem();
        doDataPreparation_createItem();

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

    private void doDataPreparation_createUser() {
        given().log().all()
                .contentType(ContentType.JSON)
                .body("{\n    \"name\": \"user\",\n    \"email\": \""
                        + RandomUtils.nextLong() + "user@user.com\"\n}")
                .when().post(HOST + USERS)
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    private void doDataPreparation_createItem() {
        given().log().all()
                .contentType(ContentType.JSON)
                .body("{\n" +
                        "    \"name\": \"Дрель\",\n" +
                        "    \"description\": \"Простая дрель\",\n" +
                        "    \"available\": true\n" +
                        "}")
                .header("X-Sharer-User-Id", 1)
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

    void doDataPreparation_addComment(long userId) {
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
}
