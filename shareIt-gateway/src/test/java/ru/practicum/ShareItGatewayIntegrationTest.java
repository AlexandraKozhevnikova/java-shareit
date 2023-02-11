package ru.practicum;

import io.restassured.http.ContentType;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;

@SpringBootTest
//@RunWith(SpringRunner.class)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ShareItGatewayIntegrationTest {

    public static final String REQUEST = "/requests";
    private static final String USERS = "/users";
    private static final String ITEM = "/items";
    private static final String BOOKING = "/bookings";
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
        given().log().all()
                .contentType(ContentType.JSON)
                .body("{\n    \"name\": \"userUpd\"" +
                        "\n}")
                .when().post(HOST + USERS)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
