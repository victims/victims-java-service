package com.redhat.victims;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;
import io.vertx.core.buffer.Buffer;

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
		String contentType = "application/octet-stream";
		String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
		Buffer buffer = Buffer.buffer();
		String header = "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"" + SNAKEYAML
				+ "\"; filename=\"" + SNAKEYAML + "\"\r\n" + "Content-Type: " + contentType + "\r\n"
				+ "Content-Transfer-Encoding: binary\r\n" + "\r\n";
		buffer.appendString(header);
		Path path = Paths.get(ServerTest.TEST_RESOURCES + SNAKEYAML);
		try {
			buffer.appendBytes(Files.readAllBytes(path));
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		String footer = "\r\n--" + boundary + "--\r\n";
		buffer.appendString(footer);
		given().body(buffer.toString()).post("/hash")
		.then().assertThat().statusCode(200);
    }
}
