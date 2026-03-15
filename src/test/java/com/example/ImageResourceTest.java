package com.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class ImageResourceTest {

    @Test
    void rootReturnsGotIt() {
        given()
            .when().get("/webapi/myresource")
            .then()
            .statusCode(200)
            .body(is("Got it"));
    }

    @Test
    void missingImageReturns404() {
        given()
            .when().get("/webapi/myresource/images/nonexistent.jpg")
            .then()
            .statusCode(404);
    }

    @Test
    void missingMovieImageReturns404() {
        given()
            .when().get("/webapi/myresource/images/movies/nonexistent.jpg")
            .then()
            .statusCode(404);
    }

    @Test
    void pathTraversalIsRejected() {
        given()
            .when().get("/webapi/myresource/images/movies/..%2F..%2Fetc%2Fpasswd")
            .then()
            .statusCode(404);
    }
}

