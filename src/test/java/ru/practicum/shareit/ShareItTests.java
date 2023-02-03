package ru.practicum.shareit;

import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
class ShareItTests {

    private static final String HOST = "http://localhost:8080";
    private static final String USERS = "/users";

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
}
