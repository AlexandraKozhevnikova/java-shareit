package shareit;

import io.restassured.common.mapper.TypeRef;
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
import shareit.request.dto.ItemRequestGetResponse;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
class ShareItServerIntegrationTests {

    public static final String REQUEST = "/requests";
    private static final String USERS = "/users";
    private static final String ITEM = "/items";
    private static final String BOOKING = "/bookings";
    private static final String HOST = "http://localhost:9090";

    @BeforeAll
    private static void run() {
        SpringApplication.run(ShareItServerApp.class);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void createUser_whenUserValid_thenReturnOkWithUser() {
        given().log().all()
                .when().get(HOST + USERS)
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
                .when().get(HOST + USERS)
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
                .when().get(HOST + ITEM + "/{itemId}")
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
                .when().get(HOST + ITEM)
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
                .when().get(HOST + ITEM + "/search")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("", hasSize(2))
                .body("[0].id", is(1))
                .body("[0].name", is("Дрель"))
                .body("[0].description", is("Простая дрель"))
                .body("[0].available", is(true));
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
                .when().get(HOST + REQUEST + "/{requestId}")
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
                .when().get(HOST + REQUEST + "/{requestId}")
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
                .when().get(HOST + REQUEST + "/{requestId}")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("id", is(1))
                .body("description", is("что-то для сверления стен"))
                .body("items", emptyIterable())
                .extract().body().path("created");

        assertThat(LocalDateTime.parse(created), within(13, ChronoUnit.HOURS, LocalDateTime.now()));
    }

    @Test
    void getItemRequestByAuthor_whenItemRequestAndItemOfferIsExist_thenReturnResponseWithItems() {
        doDataPreparation_createUser();
        doDataPreparation_createUser();
        doDataPreparation_createUser();
        doDataPreparation_createItemRequest(1L);
        doDataPreparation_createItemRequest(1L);
        doDataPreparation_createItemRequest(1L);
        doDataPreparation_createItemRequest(3L);
        doDataPreparation_createItem(1L, 2L);
        doDataPreparation_createItem(3L, 2L);
        doDataPreparation_createItem(1L, 3L);
        doDataPreparation_createItem(1L, 2L);
        doDataPreparation_createItem(null, 3L);

        List<ItemRequestGetResponse> list = given().log().all()
                .header("X-Sharer-User-Id", 1)
                .when().get(HOST + REQUEST)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<>() {
                });

        assertThat(list, hasSize(3));
        assertThat(list.get(0).getItemOfferDtoList(), hasSize(1));
        assertThat(list.get(1).getItemOfferDtoList(), emptyIterable());
        assertThat(list.get(2).getItemOfferDtoList(), hasSize(3));
        assertThat(list.get(2).getId(), is(1L));
        assertThat(list.get(2).getItemOfferDtoList().get(0).getId(), is(1L));
        assertThat(list.get(2).getItemOfferDtoList().get(1).getId(), is(3L));
        assertThat(list.get(2).getItemOfferDtoList().get(2).getId(), is(4L));
    }

    @Test
    void getAllOtherItemRequest_whenItemRequestAndItemOfferIsExist_thenReturnResponseWithItems() {
        doDataPreparation_createUser();
        doDataPreparation_createUser();
        doDataPreparation_createUser();
        doDataPreparation_createItemRequest(1L);
        doDataPreparation_createItemRequest(1L);
        doDataPreparation_createItemRequest(1L);
        doDataPreparation_createItemRequest(3L);
        doDataPreparation_createItem(1L, 2L);
        doDataPreparation_createItem(3L, 2L);
        doDataPreparation_createItem(1L, 3L);
        doDataPreparation_createItem(1L, 2L);
        doDataPreparation_createItem(null, 3L);

        List<ItemRequestGetResponse> listFromPageOne = given().log().all()
                .header("X-Sharer-User-Id", 2)
                .queryParam("from", 0)
                .queryParam("size", 3)
                .when().get(HOST + REQUEST + "/all")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<>() {
                });

        List<ItemRequestGetResponse> listFromPageTwo = given().log().all()
                .header("X-Sharer-User-Id", 2)
                .queryParam("from", 1)
                .queryParam("size", 3)
                .when().get(HOST + REQUEST + "/all")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<>() {
                });

        assertThat(listFromPageOne, hasSize(3));
        assertThat(listFromPageOne.get(0).getId(), is(4L));
        assertThat(listFromPageOne.get(0).getItemOfferDtoList(), emptyIterable());
        assertThat(listFromPageOne.get(1).getId(), is(3L));
        assertThat(listFromPageOne.get(1).getItemOfferDtoList(), hasSize(1));
        assertThat(listFromPageOne.get(1).getItemOfferDtoList().get(0).getId(), is(2L));
        assertThat(listFromPageOne.get(2).getId(), is(2L));
        assertThat(listFromPageOne.get(2).getItemOfferDtoList(), emptyIterable());

        assertThat(listFromPageTwo, hasSize(1));
        assertThat(listFromPageTwo.get(0).getItemOfferDtoList(), hasSize(3));
        assertThat(listFromPageTwo.get(0).getItemOfferDtoList().get(0).getId(), is(1L));
        assertThat(listFromPageTwo.get(0).getItemOfferDtoList().get(1).getId(), is(3L));
        assertThat(listFromPageTwo.get(0).getItemOfferDtoList().get(2).getId(), is(4L));

    }

    @Test
    void getAllOtherItemRequest_whenFromIsLessThenZero_thenReturnException() {
        doDataPreparation_createUser();
        given().log().all()
                .header("X-Sharer-User-Id", 1)
                .queryParam("from", -1)
                .queryParam("size", 3)
                .when().get(HOST + REQUEST + "/all")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void getAllOtherItemRequest_whenWithoutPages_thenReturnException() {
        doDataPreparation_createUser();
        given().log().all()
                .header("X-Sharer-User-Id", 1)
                .when().get(HOST + REQUEST + "/all")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("", emptyIterable());
    }

    @Test
    void updateUser_whenRequestValid_thenReturnUser() {
        doDataPreparation_createUser();

        given().log().all()
                .contentType(ContentType.JSON)
                .body("{\n    \"name\": \"userUpd\"" +
                        "\n}")
                .pathParam("userId", 1)
                .when().patch(HOST + USERS + "/{userId}")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void updateUser_whenEmailIsDuplicated_thenReturnException() {
        given().log().all()
                .contentType(ContentType.JSON)
                .body("{\n    \"name\": \"user\",\n " +
                        "   \"email\": \"user@user.com\"\n}")
                .when().post(HOST + USERS)
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        given().log().all()
                .contentType(ContentType.JSON)
                .body("{\n    \"name\": \"user\",\n " +
                        "   \"email\": \"user@user.com\"\n}")
                .when().post(HOST + USERS)
                .then().log().all()
                .statusCode(HttpStatus.CONFLICT.value());
    }


    @Test
    void deleteUser_whenUserExist_thenReturnSuccess() {
        doDataPreparation_createUser();
        given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("userId", 1)
                .when().delete(HOST + USERS + "/{userId}")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("userId", 2)
                .when().get(HOST + USERS + "/{userId}")
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
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
