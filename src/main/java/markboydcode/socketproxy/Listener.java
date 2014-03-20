package markboydcode.socketproxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * Opens the ServerSocket and creates and launches ConnectionHandler instances per incoming TCP connection.
 *
 * Created by markboyd on 3/14/14.
 */
public class Listener implements Runnable {

    private final int port;
    private final int destPort;
    private final String destHost;
    private final String logPrefix;

    /**
     * Listener for socket proxy meaning it opens the ServerSocket and for each connection launches a ConnectionHandler.
     *
     * @param port
     * @param destHost
     * @param destPort
     * @param logPrefix
     */
    public Listener(int port, String destHost, int destPort, String logPrefix) {
        System.out.println("Listener started, port: " + port + " ---> dest: " + destHost + ":" + destPort + " @ " + new Date());
        
        this.port = port;
        this.destHost = destHost;
        this.destPort = destPort;
        this.logPrefix = logPrefix;
    }

    @Override
    public void run() {
        ServerSocket ss = null;
        try {
             ss = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Unable to create connection listener");
            e.printStackTrace();
            System.exit(1);
            return;
        }
        boolean terminated = false;
        Socket s = null;

        while(!terminated) {
            try {
                s = ss.accept();
            } catch (IOException e) {
                System.err.println("Unable to receive connection. Terminating...");
                e.printStackTrace();
                System.exit(1);
                return;
            }
            ConnectionHandler handler = new ConnectionHandler(s, this.destHost, this.destPort, logPrefix);

            //connections.add(handler);
            Thread t = new Thread(handler);
            t.setName(handler.getId() + "_startup");
            t.start();
        }
    }
}
