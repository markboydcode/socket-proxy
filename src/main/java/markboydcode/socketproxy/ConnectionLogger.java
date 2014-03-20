package markboydcode.socketproxy;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Handles writing blocks of bytes to the log stream with the following delineation. A direction indicator, the
 * number of bytes in this block as a decimal string like "27" for twenty seven bytes, , a start of block indicator,
 * the block of bytes, and an end of block indicator. Although the bytes of the stream are considered opaque to
 * the proxy this enables a user to read the files and percieve how much data is passing through and in which
 * direction.
 *
 * Created by markboyd on 3/19/14.
 */
public class ConnectionLogger {

    private final OutputStream logStream;
    private LogMoniker direction;

    /**
     * Creates a logger responsible for writing into the log stream blocks of characters headed in the indicated direction.
     *
     * @param direction
     * @param logStream
     */
    public ConnectionLogger(LogMoniker direction, OutputStream logStream) {
        this.direction = direction;
        this.logStream = logStream;
    }

    /**
     * Write a block of characters to the log stream delineated appropriately.
     *
     * @param bytes
     * @param buffer
     */
    public void log(int bytes, byte[] buffer) throws IOException {
        synchronized (logStream) {
            logStream.write(direction.getBytes());
            logStream.write(Integer.toString(bytes, 10).getBytes());
            logStream.write(LogMoniker.STROBLK.getBytes());
            logStream.write(buffer, 0, bytes);
            logStream.write(LogMoniker.ENDOBLK.getBytes());
            logStream.flush();
        }
    }
}
