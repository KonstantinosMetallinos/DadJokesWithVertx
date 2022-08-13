package hobby.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.concurrent.TimeUnit;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "host",
        "port",
        "endpointUri",
        "jokeFrequency"
})
public class JokeConfig {

    @JsonProperty("host")
    private String host;
    @JsonProperty("port")
    private int port;
    @JsonProperty("endpointUri")
    private String endpointUri;
    @JsonProperty("jokeFrequency")
    private long jokeFrequency;

    public JokeConfig() {

    }

    public JokeConfig(final String host, final int port, final String endpointUri, final long jokeFrequency) {
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

    public void setJokeFrequency(long jokeFrequency) {
        this.jokeFrequency = TimeUnit.SECONDS.toMillis(jokeFrequency);
    }

}
