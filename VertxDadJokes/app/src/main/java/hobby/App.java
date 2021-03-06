package hobby;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Java source file was generated by the Gradle 'init' task.
 */
public final class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    static final String HELLO_WORLD = "Hello World!";

    private App() {
    }

    public static void main(String[] args) {
        log.info(HELLO_WORLD);
    }
}
