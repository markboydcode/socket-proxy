package markboydcode.socketproxy;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Named holder of a socket and its input and output streams.
 */
public class EndPoint {

    public String name = null;
    public Socket socket = null;
    public InputStream in = null;
    public OutputStream out = null;

    private boolean closed = false;

    public boolean failed = false;
    public String failureReason;
    public Exception exception;

    public EndPoint(Socket s, String name) {
        this.name = name;
        this.socket = s;
    }

    /**
     * Closes streams and socket of this end point removing them one closed so that they can't be closed more than once.
     */
    public synchronized void terminate() {
        if (!closed) {
            closeUnconditionally(in);
            closeUnconditionally(out);
            closeUnconditionally(socket);
            closed = true;
        }
    }

    /**
     * Closes a closeable ignoring exceptions.
     *
     * @param c
     */
    private void closeUnconditionally(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException e) {
            // ignore it
        }
    }

    /**
     * Indicates that starting up this endpoint (ie: getting i/ostreams) failed.
     *
     * @param reason
     * @param e
     */
    public void failed(String reason, Exception e) {
        failed = true;
        failureReason = reason;
        exception = e;
    }
}
