package markboydcode.socketproxy;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 * Entrypoint into cli for Socket Proxy. Contains and enforces the command line interface.
 *
 * Created by markboyd on 3/14/14.
 */
public class Main {

    static final Option portParam = OptionBuilder.withArgName("port")
            .hasArg().withDescription("The local port on which to listen for connections").create("p");

    static final Option targetParam = OptionBuilder.withArgName("target")
            .hasArg().withDescription("The destination to connect to format: <host>:<port>").create("t");

    /**
     * Entrypoint into app.
     *
     * @param args
     */
    public static void Main(String[] args) {

    }

    /**
     * Constructor of this guy.
     *
     * @param args
     */
    Main(String[] args) {

    }
}
