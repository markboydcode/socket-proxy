package markboydcode.socketproxy.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handler of a single udp packet request. We assume a response will be coming back to us but won't wait forever.
 *
 *
 */
public class UdpServerToClientHandler implements Runnable
{
    private static final AtomicInteger reqIdGen = new AtomicInteger(0);
    private static final DecimalFormat secFormat = new DecimalFormat("#.###");

    private final DatagramChannel serverChannel;
    private final ByteBuffer buffer;

    private DatagramChannel clientChannel;
    private InetSocketAddress clientAddr;


    /**
     * Creates a handler for the incoming socket connection to handle connecting to the destination and spooling the
     * conversation.
     *
     */
    public UdpServerToClientHandler(DatagramChannel clientChannel, InetSocketAddress clientAddr, DatagramChannel serverChannel) {
        this.serverChannel = serverChannel;
        this.clientChannel = clientChannel;
        this.clientAddr = clientAddr;
        this.buffer = ByteBuffer.allocate(4096);
        this.buffer.order(ByteOrder.BIG_ENDIAN);

        Thread t = new Thread(this);
        t.setName(UdpServerToClientHandler.class.getSimpleName() + "-thread");
        t.start();
    }

    @Override
    public void run() {
        //while (true) {
            try {
                buffer.clear(); // get buffer ready for writing
                System.out.println("<<< awaiting from server");
                InetSocketAddress serverAddr = (InetSocketAddress) serverChannel.receive(buffer);
                System.out.println("<<< received [" + buffer.position() + "] from server " + serverAddr.toString());
            } catch (IOException e) {
                System.out.println("Exception receiving from server.");
                e.printStackTrace();
                return;
            }
            buffer.flip(); // so channel can read out

            try {
                System.out.println("<<< sending to client");
                clientChannel.send(buffer, clientAddr);
                System.out.println("<<< sent to client");
                System.out.println("------------------");
            } catch (IOException e) {
                System.out.println("Unable to send response to client. Dropping.");
                e.printStackTrace();
            }
        //}
    }
}
