package markboydcode.socketproxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Spools byest received from the input stream to the output stream.
 *
 * Created by markboyd on 3/18/14.
 */
public class Spooler implements Runnable {
    private ConnectionHandler handler;
    private OutputStream out;
    private InputStream in;
    private String name;
    private byte[] buffer;
    private ConnectionLogger logger;
    private boolean terminated = false;
    private long byteCount = 0;
    private Thread executor;

    private Spooler(ConnectionHandler connectionHandler, ConnectionLogger logger, String name, InputStream in, OutputStream out) {
        this.name = name;
        this.handler = connectionHandler;
        this.logger = logger;
        this.in = in;
        this.out = out;
        this.buffer = new byte[4096];

        // start spooling
        executor = new Thread(this);
        executor.setName(this.name);
        executor.start();
    }

    /**
     * Create spooler with underlying suitably named thread for spooling bytes between input and output streams and
     * logging such activity for a given direction.
     *
     * @param dir
     * @param handler
     * @param logger
     * @param in
     * @param out
     * @return
     */
    public static final Spooler create(LogMoniker dir, ConnectionHandler handler, ConnectionLogger logger, InputStream in, OutputStream out) {
        return new Spooler(handler, logger, handler.getId() + "_spooler_" + dir.getString(), in, out);
    }

    /**
     * Implements the spooling.
     */
    @Override
    public void run() {
        int bytes = 0;
        // use stable local handles to these guys to avoid NPEs during termination
        ConnectionHandler handler = this.handler;
        InputStream in = this.in;
        OutputStream out = this.out;


        while(!terminated) {
            try {
                 bytes = in.read(this.buffer);
            } catch (Exception e) {
                handler.terminate(name + " incurred reading exception.", e);
                return;
            }
            if (bytes == -1) {
                handler.close();
                return;
            }
            try {
                out.write(buffer, 0, bytes);
            } catch (Exception e) {
                handler.terminate(name + " incurred writing exception.", e);
                return;
            }
            this.byteCount += bytes;
            this.handler.setLastActivity();
            try {
                this.logger.log(bytes, buffer);
            } catch (Exception e) {
                handler.terminate(name + " incurred logging exception.", e);
            }
        }
    }

    /**
     * Frees handles on embedded objects.
     */
    public void terminate() {
        this.terminated = true;
        executor.interrupt();
        this.handler = null;
        this.out = null;
        this.in = null;
        this.logger = null;
        this.buffer = null;
    }

    /**
     * Returns the total number of bytes that this spooler has passed between streams. Includes only those that have
     * successfully been written to the output stream.
     *
     * @return
     */
    public long getByteCount() {
        return byteCount;
    }
}
