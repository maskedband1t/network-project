public class PeerInfo {
    private int peerId;
    private String hostname;
    private int port;

    public PeerInfo(int peerId, String hostname, int port) {
        this.peerId = peerId;
        this.hostname = hostname;
        this.port = port;
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

    public void setId(int peerId) {
        this.peerId = peerId;
    }

    public void setHost(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }
}