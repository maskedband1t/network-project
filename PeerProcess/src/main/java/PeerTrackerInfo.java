public class PeerTrackerInfo {
    PeerInfo peerInfo;
    boolean hasFile;

    public PeerTrackerInfo(int id, String host, int port, boolean has) {
        peerInfo = new PeerInfo(id, host, port);
        hasFile = has;
    }

    public PeerTrackerInfo(PeerInfo info, boolean has) {
        peerInfo = info;
        hasFile = has;
    }

    public void debugPrint() {
        System.out.println(peerInfo.getId() + " " + peerInfo.getHost() + " " + peerInfo.getPort() + " " + hasFile);
    }
}