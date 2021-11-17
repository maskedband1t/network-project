import java.util.ArrayList;
import java.util.List;

public class PeerInfoConfig {
    private static PeerInfoConfig instance = null;
    List<PeerTrackerInfo> peerTrackerInfo;

    public static PeerInfoConfig getInstance() {
        if (instance == null) {
            throw new AssertionError("PeerInfoConfig not yet initialized. Try PeerInfoConfig.init(List<PeerTrackerInfo>).");
        }
        return instance;
    }

    public static void init(List<PeerTrackerInfo> peerTrackerInfo) {
        if (instance != null) {
            throw new AssertionError("PeerInfoConfig is already initialized!");
        }
        instance = new PeerInfoConfig(peerTrackerInfo);
    }

    public PeerInfoConfig(List<PeerTrackerInfo> peerTrackerInfo) {
        this.peerTrackerInfo = peerTrackerInfo;
    }

    public boolean HasFile(int id) {
        return peerTrackerInfo.stream().filter(info -> info.peerInfo.getId() == id).findFirst().get().hasFile;
    }

    public PeerTrackerInfo GetPeerTrackerInfo(int id) {
        return peerTrackerInfo.stream().filter(info -> info.peerInfo.getId() == id).findFirst().get();
    }

    public PeerTrackerInfo GetPeerTrackerInfo(String host, int port) {
        return peerTrackerInfo.stream().filter(info -> info.peerInfo.getHost().equals(host) && info.peerInfo.getPort() == port).findFirst().get();
    }

    public PeerInfo GetPeerInfo(int id){
        return GetPeerTrackerInfo(id).peerInfo;
    }

    public PeerInfo GetPeerInfo(String host, int port){
        return GetPeerTrackerInfo(host, port).peerInfo;
    }

    public List<PeerInfo> GetPeersToConnectToFor(int peerId) {
        List<PeerInfo> peersBefore = new ArrayList<PeerInfo>();
        for (int i = 0; i < peerTrackerInfo.size(); i++) {
            if (peerTrackerInfo.get(i).peerInfo.getId() != peerId)
                peersBefore.add(peerTrackerInfo.get(i).peerInfo);
            else
                return peersBefore;
        }
        return new ArrayList<PeerInfo>();
    }

    public void debugPrint() {
        System.out.println("Peer Info Config");
        System.out.println("----------------");
        for (int i = 0; i < peerTrackerInfo.size(); i++) {
            peerTrackerInfo.get(i).debugPrint();
        }
    }
}

