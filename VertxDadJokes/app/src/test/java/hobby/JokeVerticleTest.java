package hobby;

import hobby.config.JokeConfigRecord;
import hobby.constants.Utils;
import hobby.vertx.CodecUtil;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import static hobby.JokeVerticle.ENDPOINT_URI;
import static hobby.constants.MessageBusConstants.DAD_JOKE_GET;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(VertxExtension.class)
class JokeVerticleTest {

    private static final Logger LOG = LoggerFactory.getLogger(JokeVerticleTest.class);
    private static final int TIME_OUT = 10;
    private static final int TEST_PORT_WITH_DATA = 3000;
    private static final int TEST_ERROR_PORT = 3001;
    private static final int JOKE_INTERVALS_SECONDS = 5;
    private static final int BLOCKED_THREAD_INTERVAL = 5;
    private static final String LOCAL_HOST = "localhost";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String HEADER_JSON = "application/json";

    private static Vertx vertx;
    private static JokeVerticle jokeVerticle;
    private static JokeConfigRecord config;

    @BeforeAll
    static void setup() throws InterruptedException {
        Utils.setLoggingLevelToInfo();

        vertx = Vertx.vertx(new VertxOptions()
            .setBlockedThreadCheckInterval(BLOCKED_THREAD_INTERVAL)
            .setBlockedThreadCheckIntervalUnit(TimeUnit.MINUTES));
        vertx.exceptionHandler(ex -> LOG.error("GLOBAL UNCAUGHT EXCEPTION: {}", ex.getMessage(), ex));
        CodecUtil.registerEventBusCodecs(vertx);


        jokeVerticle = new JokeVerticle(config);
        VertxTestContext ctxDeploy = new VertxTestContext();

        Future<CompositeFuture> f1 = setupMocks(vertx).onFailure(ctxDeploy::failNow);
        Future<String> f2 = vertx.deployVerticle(jokeVerticle)
            .onFailure(ctxDeploy::failNow);

        CompositeFuture.all(f1, f2).onSuccess(unused -> ctxDeploy.completeNow()).onFailure(ctxDeploy::failNow);
        assertThat(ctxDeploy.awaitCompletion(TIME_OUT, TimeUnit.SECONDS), equalTo(true));
        jokeVerticle.initConsumers();
    }

    @AfterAll
    static void teardown() {
        vertx.close();
    }

    @Test
    @DisplayName("Hit local endpoint and confirm we are getting the data we expect.")
    void test_fetchJoke_withData() throws InterruptedException {
        // Given
        VertxTestContext blocker = new VertxTestContext();
        JokeConfigRecord localConfig = new JokeConfigRecord(LOCAL_HOST, TEST_PORT_WITH_DATA, ENDPOINT_URI, JOKE_INTERVALS_SECONDS);

        jokeVerticle.setHttpRequest(localConfig);

        // When
        vertx.eventBus().request(DAD_JOKE_GET, config)
            .onComplete(s -> blocker.completeNow())
            // We expect a 200 Response.
            .onFailure(f -> Assertions.fail());

        //Then
        assertThat(blocker.awaitCompletion(TIME_OUT, TimeUnit.SECONDS), equalTo(true));
    }

    @Test
    @DisplayName("Hit local endpoint and confirm we can handle error messages.")
    void test_fetchJoke_withHttpErrorResponse() throws InterruptedException {
        // Given
        VertxTestContext blocker = new VertxTestContext();
        JokeConfigRecord localConfig = new JokeConfigRecord(LOCAL_HOST, TEST_ERROR_PORT, ENDPOINT_URI, JOKE_INTERVALS_SECONDS);

        jokeVerticle.setHttpRequest(localConfig);

        // When
        vertx.eventBus().request(DAD_JOKE_GET, config)
            // We expect a non 2XX Response.
            .onSuccess(s -> Assertions.fail())
            .onFailure(f -> blocker.completeNow());

        //Then
        assertThat(blocker.awaitCompletion(TIME_OUT, TimeUnit.SECONDS), equalTo(true));
    }

    // ===============================================================
    // ============================ Utils ============================
    // ===============================================================

    private static CompositeFuture setupMocks(Vertx vertx) {
        Future<HttpServer> f0 = mockHttpCall(vertx);
        Future<HttpServer> f1 = mockHttpCall500Response(vertx);

        return CompositeFuture.all(f0, f1);
    }

    private static Future<HttpServer> mockHttpCall(Vertx vertx) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        //        String payloadResponse = "{[\"joke\" : \"What did the green grape say to the purple grape?\\nBREATH!!\"]}";
        //        String payloadResponse = "{\"joke\":\"How was the snow globe feeling after the storm? A little shaken.\"}";
        String payloadResponse = "{\"id\":\"M7ElyAQCAd\",\"joke\":\"How was the snow globe feeling after the storm? A little shaken.\",\"status\":200}";

        router.get(ENDPOINT_URI).handler(ctx -> ctx.response()
            .putHeader(CONTENT_TYPE, HEADER_JSON)
            .setStatusCode(HttpURLConnection.HTTP_OK)
            .end(String.format("[%s]", payloadResponse)));

        return vertx.createHttpServer().requestHandler(router).listen(TEST_PORT_WITH_DATA);
    }

    private static Future<HttpServer> mockHttpCall500Response(Vertx vertx) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        String payloadResponse = """
            <html>
                <body>
                    <h1>Whitelabel Error Page</h1>
                    <p>This application has no explicit mapping for /error, so you are seeing this as a fallback.</p>
                    <div id='created'>Mon Jul 04 22:45:19 UTC 2022></div>
                    <div> There was an unexpected error (type=Not Found, status=404).</div>
                    <div></div>
                </body>
            </html>
            """;

        router.get(ENDPOINT_URI).handler(ctx -> ctx.response()
            .putHeader(CONTENT_TYPE, HEADER_JSON)
            .setStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
            .end(payloadResponse));

        return vertx.createHttpServer().requestHandler(router).listen(TEST_ERROR_PORT);
    }

}
