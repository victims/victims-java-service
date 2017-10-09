package com.redhat.victims;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class ServerTest {

	protected static final String TEST_RESOURCES = "src/test/resources/";
    private Vertx vertx;
    private Integer port;
    private HttpClient client;



    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();

        // Let's configure the verticle to listen on the 'test' port (randomly
        // picked).
        // We create deployment options and set the _configuration_ json object:
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();

        DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("http.port", port));

        vertx.deployVerticle(Server.class.getName(), options, context.asyncAssertSuccess());

        client = vertx.createHttpClient(getHttpClientOptions());

    }

    protected HttpServerOptions getHttpServerOptions() {
        return new HttpServerOptions().setPort(port).setHost("localhost");
    }

    protected HttpClientOptions getHttpClientOptions() {
        return new HttpClientOptions().setDefaultPort(port);
    }

    /**
     * This method, called after our test, just cleanup everything by closing
     * the vert.x instance
     *
     * @param context
     *            the test context
     */
    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testHealthz(TestContext context) {
        final Async async = context.async();

        vertx.createHttpClient().getNow(port, "localhost", "/health", response -> {
            context.assertEquals(response.statusCode(), 200);
            async.complete();
        });

    }

    @Test
    public void sendCamelSnakeUploadUpsertRequest(TestContext context) throws Exception {
        final Async async = context.async();
        sendFile("camel-snakeyaml-2.17.4.jar", 200, "OK");
        async.complete();

    }

    @Test
    public void send2Struts2UploadRequest(TestContext context) throws Exception {
        final Async async = context.async();
        sendFile("struts2-core-2.5.12.jar", 200, "OK");
        async.complete();

    }

    @Test
    public void sendUnsupportedType(TestContext context) throws Exception {
        final Async async = context.async();
        sendFile("freckles-0.2.1.tar.gz", 501, "Not Implemented");
        async.complete();
    }

    private void sendFile(final String fileName, int expectedStatusCode, String expectedBody)
            throws Exception {
        testRequest(HttpMethod.POST, "/hash", req -> {
            fileUploadRequest(fileName, req);
        }, expectedStatusCode, expectedBody, null);
    }

    private void fileUploadRequest(final String fileName, HttpClientRequest req) {
        Buffer fileData = vertx.fileSystem().readFileBlocking(TEST_RESOURCES + fileName);
        String contentType = "application/octet-stream";
        String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
        Buffer buffer = Buffer.buffer();
        String header = "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"" + fileName
                + "\"; filename=\"" + fileName + "\"\r\n" + "Content-Type: " + contentType + "\r\n"
                + "Content-Transfer-Encoding: binary\r\n" + "\r\n";
        buffer.appendString(header);
        buffer.appendBuffer(fileData);

        String footer = "\r\n--" + boundary + "--\r\n";
        buffer.appendString(footer);
        req.headers().set("content-length", String.valueOf(buffer.length()));
        req.headers().set("content-type", "multipart/form-data; boundary=" + boundary);
        String encodedCredentials = Base64.getEncoder().encodeToString("testuser:testpass".getBytes());
        req.headers().set("Authorization", "Basic " + encodedCredentials);
        req.write(buffer);
    }

    protected void testRequest(HttpMethod method, String path, Consumer<HttpClientRequest> requestAction,
            int statusCode, String statusMessage, String responseBody) throws Exception {
        testRequestBuffer(method, path, requestAction, statusCode, statusMessage,
                responseBody != null ? Buffer.buffer(responseBody) : null);
    }

    protected void testRequestBuffer(HttpMethod method, String path, Consumer<HttpClientRequest> requestAction,
            int statusCode, String statusMessage, Buffer responseBodyBuffer) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        HttpClientRequest req = client.request(method, port, "localhost", path, resp -> {
            assertEquals(statusCode, resp.statusCode());
            assertEquals(statusMessage, resp.statusMessage());
            if (responseBodyBuffer == null) {
                latch.countDown();
            } else {
                resp.bodyHandler(buff -> {
                    assertEquals(responseBodyBuffer, buff);
                    latch.countDown();
                });
            }
        });
        if (requestAction != null) {
            requestAction.accept(req);
        }
        req.end();
        awaitLatch(latch);
    }

    protected void awaitLatch(CountDownLatch latch) throws InterruptedException {
        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }
}
