package hobby;

import org.junit.jupiter.api.Test;

import static hobby.App.HELLO_WORLD;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * This Java source file was generated by the Gradle 'init' task.
 */
class AppTest {
    @Test
    void appHasAGreeting() {
        assertNotNull(HELLO_WORLD, "app should have a greeting");
    }
}
