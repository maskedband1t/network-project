public class PeerInfo {
    private int peerId;
    private String hostname;
    private int port;
    private Bitfield bitfield;
    private boolean hasFile;

    public PeerInfo() {
        this.peerId = -1;
        this.hostname = "";
        this.port = -1;
        this.bitfield = null;
        this.hasFile = false;
    }

    public PeerInfo(int peerId, String hostname, int port, boolean hasFile) {
        this.peerId = peerId;
        this.hostname = hostname;
        this.port = port;
        this.hasFile = hasFile;
        this.bitfield = new Bitfield(hasFile);
    }

    public int getId() {
        return peerId;
    }

    public String getHost() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public Bitfield getBitfield() { return bitfield; }

    public boolean getHasFile() { return hasFile; }

    public void setId(int peerId) {
        this.peerId = peerId;
    }

    public void setHost(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHasFile(boolean bool) { this.hasFile = bool; }

    public void initBitfield() { bitfield = new Bitfield(hasFile);}

    public void debugPrint() {
        System.out.println(getId() + " " + getHost() + " " + getPort() + " " + getHasFile());
    }
}