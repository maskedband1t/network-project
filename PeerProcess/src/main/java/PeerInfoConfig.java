import java.util.List;

public class PeerInfoConfig {
    List<PeerTrackerInfo> peerTrackerInfo;

    public PeerInfoConfig(List<PeerTrackerInfo> peerTrackerInfo) {
        this.peerTrackerInfo = peerTrackerInfo;
    }

    public void debugPrint() {
        System.out.println("Peer Info Config");
        System.out.println("----------------");
        for (int i = 0; i < peerTrackerInfo.size(); i++) {
            peerTrackerInfo.get(i).debugPrint();
        }
    }
}

