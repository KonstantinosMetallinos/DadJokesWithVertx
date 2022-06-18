package hobby;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;

import java.util.concurrent.TimeUnit;

public class JokeVerticle extends AbstractVerticle {
    private HttpRequest<JsonObject> request;
    private static final int HTTPS_PORT = 443;
    private static final String HOST = "icanhazdadjoke.com";
    private static final String REQUEST_URI = "/";
    private static final long JOKES_INTERVAL = TimeUnit.SECONDS.toMillis(5);


    // TODO(K): 1 - commit project as is
    // TODO(K): 2 - fix checkstyle rules and keep commit changes to figure out what the template changes are needed.

    @Override
    public void start() {

        request = WebClient.create(vertx)
                .get(HTTPS_PORT, HOST, REQUEST_URI)
                .ssl(true)
                .putHeader("Accept", "application/json")
                .as(BodyCodec.jsonObject())
                .expect(ResponsePredicate.SC_OK);

        vertx.setPeriodic(JOKES_INTERVAL, id -> fetchJoke());
    }

    private void fetchJoke() {
        request.send(asyncResult -> {
            if (asyncResult.succeeded()) {
                System.out.println(asyncResult.result().body().getString("joke"));
                System.out.println("🤣");
                System.out.println();
            }
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new JokeVerticle());
    }
}
