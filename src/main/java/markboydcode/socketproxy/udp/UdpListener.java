package markboydcode.socketproxy.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.Date;

/**
 * Opens the ServerSocket and creates and launches ConnectionHandler instances per incoming TCP connection.
 *
 * Created by markboyd on 3/14/14.
 */
public class UdpListener implements Runnable {

    private int port;
    private int destPort;
    private String destHost;
    private boolean responseHandlerInstalled = false;


    /**
     * Listener for socket proxy meaning it opens the ServerSocket and for each connection launches a ConnectionHandler.
     *
     * @param port
     * @param destHost
     * @param destPort
     * @param logPrefix
     */
    public UdpListener(int port, String destHost, int destPort, String logPrefix) {
        System.out.println("UDP Listener started, port: " + port + " ---> dest: " + destHost + ":" + destPort + " @ " + new Date());
        
        this.port = port;
        this.destHost = destHost;
        this.destPort = destPort;
    }

    @Override
    public void run() {

        // open our listening channel
        DatagramChannel clientChannel = null;
        try {
            clientChannel = DatagramChannel.open();
            clientChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        } catch (IOException e) {
            System.out.println("Unable to open incoming datagram channel for listening. Exiting...");
            e.printStackTrace();
            return;
        }

        // bind listener to port
        try {
            clientChannel.socket().bind(new InetSocketAddress(this.port));
        } catch (SocketException e) {
            System.out.println("Unable to bind to port " + this.port + ". Exiting...");
            e.printStackTrace();
            return;
        }

        // open destination channel
        DatagramChannel serverChannel = null;
        try {
            serverChannel = DatagramChannel.open();
        } catch (IOException e) {
            System.out.println("Unable to open outgoing datagram channel for sending. Exiting...");
            e.printStackTrace();
            return;
        }

        if (serverChannel == null) {
            // couldn't create but message already dumped. so exit.
            return;
        }

        // buffer for client to server packages
        ByteBuffer clientToServer = ByteBuffer.allocate(4096);
        clientToServer.order(ByteOrder.BIG_ENDIAN);
        InetSocketAddress clientAddr = null;
        InetSocketAddress serverAddr = new InetSocketAddress(this.destHost, this.destPort);

        while (true) {
            try {
                clientToServer.clear();
                System.out.println(" >>> awaiting from client");
                clientAddr = (InetSocketAddress) clientChannel.receive(clientToServer);
                System.out.println(" >>> received [" + clientToServer.position() + "] from client " + clientAddr.toString());
            } catch (IOException e) {
                System.out.println("Exception receiving from client.");
                e.printStackTrace();
                continue;
            }
            clientToServer.flip(); // so we can read out

            try {
                System.out.println(" >>> forwarding to server");
                serverChannel.send(clientToServer, serverAddr);
                System.out.println(" >>> sent to server");
            } catch (IOException e) {
                System.out.println("Exception sending to server.");
                e.printStackTrace();
                return;
            }
            //if (! responseHandlerInstalled) {
                System.out.println("--- launching server2client handler for " + clientAddr.toString());
                new UdpServerToClientHandler(clientChannel, clientAddr, serverChannel);
                responseHandlerInstalled = true;
            //}
        }
    }
}
