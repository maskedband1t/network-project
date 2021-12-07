import java.util.ArrayList;
import java.util.List;

public class PeerInfoConfig {
    private static PeerInfoConfig instance = null;
    List<PeerInfo> peerInfos;

    // Get the singleton
    public static PeerInfoConfig getInstance() {
        if (instance == null) {
            throw new AssertionError("PeerInfoConfig not yet initialized. Try PeerInfoConfig.init(List<PeerTrackerInfo>).");
        }
        return instance;
    }

    // Init the singleton
    public static void init(List<PeerInfo> infos) {
        if (instance != null) {
            throw new AssertionError("PeerInfoConfig is already initialized!");
        }
        instance = new PeerInfoConfig(infos);
    }

    // Constructor used by init(...)
    public PeerInfoConfig(List<PeerInfo> infos) {
        this.peerInfos = infos;
    }

    // Check if we have information on the given peer id
    public boolean HasFile(int id) {
        return peerInfos.stream().filter(info -> info.getId() == id).findFirst().get().getFileComplete();
    }

    // Get the PeerInfo for the given peer id
    public PeerInfo GetPeerInfo(int id) {
        return peerInfos.stream().filter(info -> info.getId() == id).findFirst().get();
    }

    // Get the PeerInfo for the given host and port combination
    public PeerInfo GetPeerInfo(String host, int port) {
        return peerInfos.stream().filter(info -> info.getHost().equals(host) && info.getPort() == port).findFirst().get();
    }

    // Get the list of PeerInfos to connect for a given peer id
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

    // Debugging purposes, print out the configuration file contents
    public void debugPrint() {
        Helpers.println("Peer Info Config");
        Helpers.println("----------------");
        for (int i = 0; i < peerInfos.size(); i++) {
            peerInfos.get(i).debugPrint();
        }
    }
}

