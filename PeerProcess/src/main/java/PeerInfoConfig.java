import java.util.ArrayList;
import java.util.List;

public class PeerInfoConfig {
    private static PeerInfoConfig instance = null;
    List<PeerInfo> peerInfos;
    
    public static PeerInfoConfig getInstance() {
        if (instance == null) {
            throw new AssertionError("PeerInfoConfig not yet initialized. Try PeerInfoConfig.init(List<PeerTrackerInfo>).");
        }
        return instance;
    }

    public static void init(List<PeerInfo> infos) {
        if (instance != null) {
            throw new AssertionError("PeerInfoConfig is already initialized!");
        }
        instance = new PeerInfoConfig(infos);
    }

    public PeerInfoConfig(List<PeerInfo> infos) {
        this.peerInfos = infos;
    }

    public boolean HasFile(int id) {
        return peerInfos.stream().filter(info -> info.getId() == id).findFirst().get().getFileComplete();
    }

    public PeerInfo GetPeerInfo(int id) {
        return peerInfos.stream().filter(info -> info.getId() == id).findFirst().get();
    }

    public PeerInfo GetPeerInfo(String host, int port) {
        return peerInfos.stream().filter(info -> info.getHost().equals(host) && info.getPort() == port).findFirst().get();
    }

    public List<PeerInfo> GetPeersToConnectToFor(int peerId) {
        List<PeerInfo> peersBefore = new ArrayList<PeerInfo>();
        for (int i = 0; i < peerInfos.size(); i++) {
            if (peerInfos.get(i).getId() != peerId)
                peersBefore.add(peerInfos.get(i));
            else
                return peersBefore;
        }
        return new ArrayList<PeerInfo>();
    }

    public void debugPrint() {
        System.out.println("Peer Info Config");
        System.out.println("----------------");
        for (int i = 0; i < peerInfos.size(); i++) {
            peerInfos.get(i).debugPrint();
        }
    }
}

