package ru.practicum.shareit;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.ErrorResponse;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ShareItGatewayIntegrationTest {
    private static final String USERS = "/users";
    private static final String ITEMS = "/items";
    private static final String REQUEST = "/requests";
    private String HOST = "http://localhost:8080";

    @BeforeAll
    private static void run() {
        SpringApplication.run(ShareItGateway.class);
    }

    @Test
    public void contextLoads() {
    }

    @Test
    public void createUser_whenEmailIsNull_thenReturnException() {
        List<ErrorResponse> errors =
                given().log().all()
                        .contentType(ContentType.JSON)
                        .body("{\n   " +
                                " \"name\": \"userUpd\"" +
                                "\n}")
                        .when().post(HOST + USERS)
                        .then().log().all()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .extract().as(new TypeRef<>() {
                        });

        assertEquals("'email' must not be nul", errors.get(0).getError());
    }

    @Test
    public void addComment_textIsBlank_thenException() {
        given().log().all()
                .contentType(ContentType.JSON)
                .body("{\n" +
                        "\"text\": \"\"" +
                        "\n}")
                .header("X-Sharer-User-Id", 1)
                .when().post(HOST + ITEMS + "/{itemId}/comment", 1L)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("error", is("'text' must not be blank"));
    }

    @Test
    public void addComment_textDoesNotExist_thenException() {
        given().log().all()
                .contentType(ContentType.JSON)
                .body("{ }")
                .header("X-Sharer-User-Id", 1)
                .when().post(HOST + ITEMS + "/{itemId}/comment", 1L)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("error", is("'text' must not be blank"));
    }

    @Test
    void createItemRequest_whenEmptyBody_then400() {

        given().log().all()
                .contentType(ContentType.JSON)
                .body("{ }")
                .header("X-Sharer-User-Id", 1)
                .when().post(HOST + REQUEST)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("error", is("'description' must not be blank"))
                .body("errorInfo.type", is("validation"));
    }

    @Test
    void createItemRequest_whenDescriptionIsNull_then400() {
        given().log().all()
                .contentType(ContentType.JSON)
                .body("{\n" +
                        "    \"description\": null \n" +
                        "}")
                .header("X-Sharer-User-Id", 1)
                .when().post(HOST + REQUEST)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("error", is("'description' must not be blank"))
                .body("errorInfo.type", is("validation"));
    }
}
