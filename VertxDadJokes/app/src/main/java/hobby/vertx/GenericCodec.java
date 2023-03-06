package hobby.vertx;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Credits to Levi Doron: https://levidoro.medium.com/vert-x-event-bus-send-any-object-with-generic-codec-t-a0bc1feab13a Bulk of this Generic Code logic comes
 * from above blog.
 *
 * @param <T> Class type we wish to append into the Codec.
 */
public class GenericCodec<T> implements MessageCodec<T, T> {

    private static final Logger LOG = LoggerFactory.getLogger(GenericCodec.class);
    private static final int BYTES_IN_INT = 4;

    private final Class<T> cls;

    public GenericCodec(final Class<T> cls) {
        this.cls = cls;
    }

    @Override
    public void encodeToWire(Buffer buffer, T s) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(s);
            out.flush();
            byte[] yourBytes = bos.toByteArray();
            buffer.appendInt(yourBytes.length);
            buffer.appendBytes(yourBytes);
            out.close();
        } catch (IOException e) {
            // pass
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T decodeFromWire(int startPositionOfBuffer, Buffer buffer) {
        // Length of JSON
        int length = buffer.getInt(startPositionOfBuffer);

        // Jump 4 because getInt() == 4 bytes
        int start = startPositionOfBuffer + BYTES_IN_INT;
        int end = start + length;
        byte[] yourBytes = buffer.getBytes(start, end);

        try (ByteArrayInputStream bis = new ByteArrayInputStream(yourBytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOG.error("Listen failed {}", e.getMessage());
        }
        return null;
    }

    /**
     * If a message is sent *locally* across the event bus. This example sends message just as is
     *
     * @param customMessage Message to send
     * @return Transformed message
     */
    @Override
    public T transform(T customMessage) {
        return customMessage;
    }

    /**
     * Each codec must have a unique name. This is used to identify a codec when sending a message and for unregistering codecs.
     *
     * @return Codec name identifier
     */
    @Override
    public String name() {
        return cls.getSimpleName() + "Codec";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
