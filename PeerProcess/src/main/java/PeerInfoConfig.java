import java.util.List;

public class PeerInfoConfig {
    List<PeerTrackerInfo> peerTrackerInfo;

    public PeerInfoConfig(List<PeerTrackerInfo> peerTrackerInfo) {
        this.peerTrackerInfo = peerTrackerInfo;
    }

    public static PeerInfo GetPeerInfo(String host, int port){
        return peerTrackerInfo.stream()
                .filter(info -> info.peerInfo.hostname == host && info.peerInfo.port == port)
                .findFirst()
                .get().peerInfo;
    }

    public void debugPrint() {
        System.out.println("Peer Info Config");
        System.out.println("----------------");
        for (int i = 0; i < peerTrackerInfo.size(); i++) {
            peerTrackerInfo.get(i).debugPrint();
        }
    }
}

