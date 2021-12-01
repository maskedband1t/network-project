public class PeerInfo {
    private int peerId;
    private String hostname;
    private int port;
    private Bitfield bitfield;
    private boolean hasFile;

    // Construct a default PeerInfo
    public PeerInfo() {
        this.peerId = -1;
        this.hostname = "";
        this.port = -1;
        this.bitfield = null;
        this.hasFile = false;
    }

    // Construct a PeerInfo
    public PeerInfo(int peerId, String hostname, int port, boolean hasFile) {
        this.peerId = peerId;
        this.hostname = hostname;
        this.port = port;
        this.hasFile = hasFile;
        this.bitfield = new Bitfield(hasFile);
    }

    // Get the peer id
    public int getId() {
        return peerId;
    }

    // Get the host name
    public String getHost() {
        return hostname;
    }

    // Get the port
    public int getPort() {
        return port;
    }

    // Get the bitfield
    public Bitfield getBitfield() { return bitfield; }

    // Get the has file bool
    public boolean getHasFile() { return hasFile; }

    // Set the peer id
    public void setId(int peerId) {
        this.peerId = peerId;
    }

    // Set the host name
    public void setHost(String hostname) {
        this.hostname = hostname;
    }

    // Set the port
    public void setPort(int port) {
        this.port = port;
    }

    // Set the has file bool
    public void setHasFile(boolean bool) { this.hasFile = bool; }

    // Initialize the bitfield
    public void initBitfield() { bitfield = new Bitfield(hasFile);}

    // Debugging purposes, print this peer's info
    public void debugPrint() {
        System.out.println(getId() + " " + getHost() + " " + getPort() + " " + getHasFile());
    }
}