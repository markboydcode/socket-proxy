package markboydcode.socketproxy;

import markboydcode.socketproxy.udp.UdpListener;
import org.apache.commons.cli.*;

/**
 * Entry point cli for Socket Proxy. Contains and enforces the command line interface.
 *
 * Created by markboyd on 3/14/14.
 */
public class Main {

    /**
     * command line option: -p <port>    use file-system based config file
     */
    static final Option portParam = OptionBuilder.withArgName("port")
            .hasArg().withDescription("Local port on which to listen for connections. Integer value.").create("p");

    /**
     * command line option: -d <host:port>    use file-system based config file
     */
    static final Option destParam = OptionBuilder.withArgName("host:port")
            .hasArg().withDescription("Destination to which to proxy connections").create("d");

    /**
     * command line option: -x <log-files-prefix>    use some other connection prefix than default of 'C' for connection identifiers and associated log files
     */
    static final Option idPrefixParam = OptionBuilder.withArgName("log-files-prefix")
            .isRequired(false).hasArg().withDescription("Prefix of connection identifiers and hence their log files. Defaults to 'C'").create("x");

    /**
     * command line option: -t <'udp' | 'tcp'> proxy UDP rather than the default of TCP
     */
    static final Option proxyTypeParam = OptionBuilder.withArgName("proxy-type")
            .isRequired(false).hasArg().withDescription("Type of traffic to proxy. Defaults to 'tcp'").create("t");

    /**
     * Entry point into app.
     *
     * @param args
     */
    public static void main(String[] args) {
        new Main(args);
    }

    /**
     * Constructor of this guy which validates command line interface parameters and then fires up the listener.
     *
     * @param args
     */
    Main(String[] args) {
        Options opts = new Options();

        opts.addOption(portParam);
        opts.addOption(destParam);
        opts.addOption(idPrefixParam);
        opts.addOption(proxyTypeParam);

        CommandLineParser clp = new GnuParser();
        CommandLine cl = null;
        try {
            cl = clp.parse(opts, args, false);
        } catch (ParseException e) {
            System.err.println( "Parsing command line failed.  Reason: " + e.getMessage() );
            System.out.println();
            showHelpAndExit(opts);
            return;
        }

        int port = -1;
        String destHost = null;
        String logPrefix = "C";
        String proxyType = "tcp";
        int destPort = -1;

        if (cl.hasOption(portParam.getOpt())) {
            String val = cl.getOptionValue(portParam.getOpt());
            try {
                port = Integer.parseInt(val);
            } catch(NumberFormatException nfe) {
                System.err.println("Specified port '" + val + "' is not an integer.");
                this.showHelpAndExit(opts);
                return;
            }
        }
        if (cl.hasOption(destParam.getOpt())) {
            String val = cl.getOptionValue(destParam.getOpt());

            String[] vals = val.split("\\:");
            if (vals.length < 2) {
                System.err.println("Specified destination '" + val + "' does not contain a colon.");
                this.showHelpAndExit(opts);
                return;
            }
            destHost = vals[0];

            try {
                destPort = Integer.parseInt(vals[1]);
            } catch(NumberFormatException nfe) {
                System.err.println("Specified destination port '" + vals[1] + "' is not an integer.");
                this.showHelpAndExit(opts);
                return;
            }
        }
        if (cl.hasOption(idPrefixParam.getOpt())) {
            logPrefix = cl.getOptionValue(idPrefixParam.getOpt());
        }
        if (cl.hasOption(proxyTypeParam.getOpt())) {
            proxyType = cl.getOptionValue(proxyTypeParam.getOpt()).toLowerCase();
        }

        if(port == -1 || destPort == -1 || destHost == null || destHost.equals("")) {
            showHelpAndExit(opts);
            return;
        }

        // we've got valid parameters, fire it up

        if ("udp".equals(proxyType)) {
            UdpListener udpListener = new UdpListener(port, destHost, destPort, logPrefix);
            udpListener.run();
        }
        else {
            System.out.println("TCP Proxy starting");
            Listener l = new Listener(port, destHost, destPort, logPrefix);
            l.run();
        }
    }


    /**
     * Prints the usage help on the command line.
     *
     * @param opts
     */
    protected void showHelpAndExit(Options opts) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar <jar-name> [options] ", opts );
        exitJvm();
    }

    /**
     * Exists the JVM.
     */
    protected void exitJvm() {
        System.exit(1);
    }
}
