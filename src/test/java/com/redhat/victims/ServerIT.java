package com.redhat.victims;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;
import com.redhat.victims.ServerTest;

public class ServerIT {

	private static final String SNAKEYAML = "camel-snakeyaml-2.17.4.jar";

	@BeforeClass
	public static void configureRestAssured() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = Integer.getInteger("http.port", 8080);
	}

	@AfterClass
	public static void unconfigureRestAssured() {
		RestAssured.reset();
	}

    
    @Test
    public void testHealthz() {
        get("health").then().assertThat().statusCode(200);
    }
    
    @Test
    public void checkThatWeCanAUpload() {
		Path path = Paths.get(ServerTest.TEST_RESOURCES + SNAKEYAML);
		given().multiPart(path.toFile()).post("/hash")
		.then().assertThat().statusCode(200).and()
		.body("hash", hasItem("3cfc3c06a141ba3a43c6c0a01567dbb7268839f75e6367ae0346bab90507ea09c9ecd829ecec3f030ed727c0beaa09da8c08835a8ddc27054a03f800fa049a0a"));
    }
}
