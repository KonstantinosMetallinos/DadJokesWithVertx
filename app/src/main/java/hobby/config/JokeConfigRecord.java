package hobby.config;

import java.util.concurrent.TimeUnit;

public record JokeConfigRecord(String host, int port, String endpointUri, long jokeFrequency) {

    public JokeConfigRecord(final String host, final int port, final String endpointUri, final long jokeFrequency) {
        if (port < 0) {
            throw new IllegalArgumentException("Provided port value is negative: " + port);
        }
        if (jokeFrequency <= 0) {
            throw new IllegalArgumentException("jokeFrequency needs to be a positive value. Provided: " + jokeFrequency);
        }
        this.host = host;
        this.port = port;
        this.endpointUri = endpointUri;
        this.jokeFrequency = TimeUnit.SECONDS.toMillis(jokeFrequency);
    }
}
