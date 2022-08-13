package hobby.constants;

import ch.qos.logback.classic.Level;
import org.slf4j.LoggerFactory;

public final class Utils {

    private Utils() { }

    public static void setLoggingLevelToInfo() {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
    }

}
