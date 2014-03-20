package markboydcode.socketproxy;

import java.io.*;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handler of a single Socket connection. Is responsible for setting up spooling, loggin, and graceful termination
 * when the socket connection terminates from either side.
 *
 *
 */
public class ConnectionHandler implements Runnable
{
    private static final AtomicInteger connIdGen = new AtomicInteger(0);
    private static final DecimalFormat secFormat = new DecimalFormat("#.###");

    private final String destHost;
    private final int destPort;
    private final Date startTS;

    private ConnectionState state;
    private String id;
    private OutputStream logStream;

    private EndPoint client;
    private Socket clientSocket;
    private Spooler clientToDest;
    private ConnectionLogger clientLogger;

    private EndPoint dest;
    private Socket destSocket;
    private Spooler destToClient;
    private ConnectionLogger destLogger;
    private long lastActivityTS = 0;

    /**
     * Creates a handler for the incoming socket connection to handle connecting to the destination and spooling the
     * conversation.
     *
     * @param s
     * @param destHost
     * @param destPort
     * @param logPrefix
     */
    public ConnectionHandler(Socket s, String destHost, int destPort, String logPrefix) {
        this.state  = ConnectionState.RECEIVED;
        this.destHost = destHost;
        this.destPort = destPort;
        this.clientSocket = s;
        this.id = logPrefix + "-" + connIdGen.incrementAndGet();
        this.startTS = new Date();
    }

    @Override
    public void run() {
        System.out.println(this.id + " created @ " + this.startTS);

        // set up logging
        try {
            this.logStream = new FileOutputStream(this.id + ".log");
        } catch (Exception e) {
            terminate("Unable to open file for logging.", e);
            return;
        }
        clientLogger = new ConnectionLogger(LogMoniker.TODEST, this.logStream); // logs what went from client to dest
        destLogger = new ConnectionLogger(LogMoniker.TOCLNT, this.logStream); // logs what went from dest to client

        // get client i/o streams
        this.state = ConnectionState.CLIENT_IO;
        this.client = getStreams(clientSocket, "client");

        if (client.failed) {
            terminate(client.failureReason, client.exception);
            return;
        }

        // connect to destination
        this.state = ConnectionState.CONNECTING;
        try {
            destSocket = new Socket(destHost, destPort);
        } catch (Exception e) {
            terminate(id + " Unable to connect to destination.", e);
            return;
        }

        // get destination i/o streams
        this.state = ConnectionState.DEST_IO;
        this.dest = getStreams(destSocket, "destination");

        if (dest.failed) {
            terminate(dest.failureReason, dest.exception);
            return;
        }

        // start spooling
        destToClient = Spooler.create(LogMoniker.TOCLNT, this, destLogger, dest.in, client.out);
        clientToDest = Spooler.create(LogMoniker.TODEST, this, clientLogger, client.in, dest.out);
    }

    /**
     * Changes state to {@link markboydcode.socketproxy.ConnectionState#TERMINATING}, closes client and destination
     * end points if they exist and changes state to {@link markboydcode.socketproxy.ConnectionState#TERMINATED}
     */
    public synchronized void terminate(String reason, Exception e) {
        if (state != ConnectionState.TERMINATING &&
                state != ConnectionState.TERMINATED) {
            System.out.println(id + " terminating: " + reason);
            if (e != null) {
                e.printStackTrace();
            }
            if (clientToDest != null && destToClient != null) {
                logClosedEvent();
            }

            _terminate();
        }
    }

    /**
     * Formats and logs the closed event for a connection.
     */
    private void logClosedEvent() {
        boolean noBytesSpooled = clientToDest.getByteCount() == 0 && destToClient.getByteCount() == 0;

        // extra space prefixing @ char aligns @ of created line.
        System.out.println(id + " closed  @ " + new Date() + ", bytes["
                + (noBytesSpooled ? "none" : (LogMoniker.TODEST.getString() + clientToDest.getByteCount()
                + ", " + LogMoniker.TOCLNT.getString() + destToClient.getByteCount()))
                + "] over "
                + showDeltaSeconds(this.startTS.getTime()) + "s"
                + (this.lastActivityTS > 0 ? ", last @ -" + showDeltaSeconds(lastActivityTS) + "s" : ""));
    }

    /**
     * Takes the passed in millis timestamp which should originally be obtained from System.currentTimeMillis(), and
     * subtracts it from System.currentTimeMillis(), divides by 1000.0 to convert to seconds, then formats to three
     * decimal places.
     *
     * @param millisTS
     * @return
     */
    private String showDeltaSeconds(long millisTS) {
        return secFormat.format((System.currentTimeMillis() - millisTS)/1000.0);
    }

    private void _terminate() {
        this.state = ConnectionState.TERMINATING;
        if (client != null) {
            client.terminate();
        }
        if (dest != null) {
            dest.terminate();
        }
        if (destToClient != null) {
            destToClient.terminate();
        }
        if (clientToDest != null) {
            clientToDest.terminate();
        }

        try {
            this.logStream.flush();
        } catch (Exception e1) {
            // ignore since we are shutting down
        }
        try {
            this.logStream.close();
        } catch (Exception e1) {
            // ignore since we are shutting down
        }

        this.state = ConnectionState.TERMINATED;
    }

    /**
     * Creates and returns a named holder of the input stream and output stream for an end point or returns null if
     * unable to obtain either stream. Takes no effort to close streams or the socket if unable to obtain streams.
     *
     * @param s
     * @param name
     * @return
     */
    private EndPoint getStreams(Socket s, String name) {
        EndPoint p = new EndPoint(s, name);

        try {
            p.in = s.getInputStream();
        } catch (IOException e) {
            p.failed(id + " Unable to get " + name + " input stream.", e);
            return p;
        }

        try {
            p.out = s.getOutputStream();
        } catch (IOException e) {
            p.failed(id + " Unable to get " + name + " output stream.", e);
        }
        return p;
    }


    /**
     * Sets a unique identifier for this connection.
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    /**
     * Returns the number of bytes sent from the client.
     * @return
     */
    public long getBytesFromClient() {
        return clientToDest.getByteCount();
    }

    /**
     * Returns the number of butes sent to the client.
     * @return
     */
    public long getBytesToClient() {
        return destToClient.getByteCount();
    }

    /**
     * Terminates this connection as expected like when a client or server properly disconnected.
     */
    public synchronized void close() {
        if (state != ConnectionState.TERMINATING &&
                state != ConnectionState.TERMINATED) {
            logClosedEvent();
            _terminate();

        }
    }

    /**
     * Sets the timestamp of the end of the last time bytes passed through the connection in either direction.
     */
    public void setLastActivity() {
        this.lastActivityTS = System.currentTimeMillis();
    }
}
