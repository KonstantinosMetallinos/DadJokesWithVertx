package hobby.vertx;

import hobby.config.JokeConfig;
import hobby.config.JokeConfigRecord;
import io.vertx.core.Vertx;

import java.util.List;

public final class CodecUtil {

    public static final List<Class<?>> CODECS = List.of(JokeConfig.class, JokeConfigRecord.class);

    private CodecUtil() { }

    /**
     * Method that Registers Codecs for provided Vertx instance.<br> Codecs that will be registered will be read from {@link CodecUtil#CODECS}
     *
     * @param vertx Vertx instance we wish to set up with Codecs.
     */
    public static void registerEventBusCodecs(Vertx vertx) {
        for (Class<?> eachClass : CODECS) {
            registerEventBusCodec(vertx, eachClass);
        }
    }

    private static <T> void registerEventBusCodec(Vertx vertx, Class<T> clazz) {
        vertx.eventBus().registerDefaultCodec(clazz, new GenericCodec<>(clazz));
    }
}
