package hobby;

import hobby.config.JokeConfigRecord;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;

import static hobby.constants.MessageBusConstants.DAD_JOKE_GET;

public class JokeVerticle extends AbstractVerticle {

    static final String ENDPOINT_URI = "/";
    private HttpRequest<JsonObject> request;

    private final JokeConfigRecord config;

    public JokeVerticle(final JokeConfigRecord config) {
        this.config = config;
    }

    @Override
    public void start() {
        setHttpRequest(config);
        initConsumers();
    }

    /* package private */ void initConsumers() {
        vertx.eventBus().consumer(DAD_JOKE_GET, this::fetchJoke);
    }

    private void fetchJoke(final Message<JokeConfigRecord> msg) {

        Future<HttpResponse<JsonObject>> future = request.send();

        future.onSuccess(s -> {
                System.out.println(future.result().body().getString("joke") + "\n🤣");
                System.out.println();
                msg.reply(0);
            })
            .onFailure(f -> msg.fail(1, "ERROR: Attempt to fetch Joke was unsuccessful.\n" + future.cause().getMessage()));
    }

    protected void setHttpRequest(final JokeConfigRecord config) {
        request = WebClient.create(vertx)
            .get(config.port(), config.host(), config.endpointUri())
            .putHeader("Accept", "application/json")
            .as(BodyCodec.jsonObject())
            .expect(ResponsePredicate.SC_OK);
    }
}
