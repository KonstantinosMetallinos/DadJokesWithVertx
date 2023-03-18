package hobby;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hobby.config.JokeConfigRecord;
import hobby.constants.Utils;
import hobby.vertx.CodecUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static hobby.constants.MessageBusConstants.DAD_JOKE_GET;

public final class MainVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class);

    private static final int BLOCKED_THREAD_CHECK_INTERVAL = 5;
    private static final long FIRST_CALL_DELAY = TimeUnit.MILLISECONDS.toMillis(100);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String CONFIG_LOCATION = "./app/src/main/resources/config-local.json";
    private static final JokeConfigRecord JOKE_CONFIG = setupConfigs();

    private MainVerticle() { }

    public static void main(String[] args) {
        Utils.setLoggingLevelToInfo();

        Vertx vertx = Vertx.vertx(new VertxOptions()
                .setBlockedThreadCheckInterval(BLOCKED_THREAD_CHECK_INTERVAL)
                .setBlockedThreadCheckIntervalUnit(TimeUnit.MINUTES));
        vertx.exceptionHandler(ex -> LOG.error("GLOBAL UNCAUGHT EXCEPTION: {}", ex.getMessage(), ex));

        CodecUtil.registerEventBusCodecs(vertx);

        Future<String> setupFuture = vertx.deployVerticle(new JokeVerticle(JOKE_CONFIG));
        setupFuture.onSuccess(h -> LOG.info("JokeVerticle setup completed successfully."));

        vertx.setTimer(FIRST_CALL_DELAY, r -> vertx.eventBus().request(DAD_JOKE_GET, JOKE_CONFIG));
        vertx.setPeriodic(JOKE_CONFIG.jokeFrequency(), r -> vertx.eventBus().request(DAD_JOKE_GET, JOKE_CONFIG));
    }

    private static JokeConfigRecord setupConfigs() {
        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(new File(CONFIG_LOCATION));
            return OBJECT_MAPPER.readValue(rootNode.get("jokeConfig").toString(), JokeConfigRecord.class);
        } catch (IOException e) {
            LOG.error(String.format("Unable to find or parse config file %s", CONFIG_LOCATION), e);
            System.exit(1);
        }
        // unreachable state.
        System.exit(2);
        return null;
    }
}
