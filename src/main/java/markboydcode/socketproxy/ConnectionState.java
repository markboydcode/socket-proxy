package markboydcode.socketproxy;

/**
 * States of a ConnectionHandler.
 *
 * Created by markboyd on 3/18/14.
 */
public enum ConnectionState {

    /**
     * State of a ConnectionHandler upon receipt of underlying TCP connection before any client i/o streams are opened.
     */
    RECEIVED,

    /**
     * State of a ConnectionHandler while setting up client i/o streams.
     */
    CLIENT_IO,

    /**
     * State of a ConnectionHandler while opening TCP connection to destination.
     */
    CONNECTING,

    /**
     * State of ConnectionHandler while opening destination i/o streams.
     */
    DEST_IO,

    /**
     * State of ConnectionHandler while spooling bytes between client and destination.
     */
    SPOOLING,

    /**
     * State of ConnectionHandler from first closed stream to all streams and sockets closed and immediately prior to
     * thread exit.
     */
    TERMINATING,

    /**
     * State of ConnectionHandler after thread exits.
     */
    TERMINATED
}
