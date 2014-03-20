package markboydcode.socketproxy;

/**
 * Defines key monikers/delimiters of log file taking an approach similar to chunked transfer encoding but enabling a
 * human to read the file with some understanding.
 *
 * Created by markboyd on 3/19/14.
 */
public enum LogMoniker {
    CRLF("" + ((char) 13) + ((char) 10)),
    TODEST(">>>> "),
    TOCLNT("<<<< "),
    STROBLK(" ["),
    ENDOBLK("]" + CRLF.getString());

    /**
     * Holds the String version of the moniker.
     */
    private final String moniker;

    LogMoniker(String moniker) {
        this.moniker = moniker;
    }

    /**
     * Returns the string version of the moniker.
     *
     * @return
     */
    public String getString() {
        return this.moniker;
    }

    /**
     * Returns the byte array version of the moniker suitable for streaming into the log file.
     *
     * @return
     */
    public byte[] getBytes() {
        return this.moniker.getBytes();
    }
}
