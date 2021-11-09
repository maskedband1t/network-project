import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PeerManager {
    private static PeerManager instance = null;
    private final Collection<PeerTrackerInfo> _preferredPeers = new HashSet<>();
    private final List<PeerTrackerInfo> _chokedNeighbors = new ArrayList<>();
    final Collection<PeerTrackerInfo> _optmisticallyUnchokedPeers =
            Collections.newSetFromMap(new ConcurrentHashMap<PeerTrackerInfo, Boolean>());

    public static PeerManager getInstance() {
        if (instance == null) {
            throw new AssertionError("PeerManager not yet initialized. Try PeerManager.init().");
        }
        return instance;
    }

    public static void init() {
        if (instance != null) {
            throw new AssertionError("PeerManager is already initialized!");
        }
        instance = new PeerManager();
    }

    public boolean CanUploadToPeer(PeerInfo info) {
        return (_preferredPeers.contains(info) ||
                _optmisticallyUnchokedPeers.contains(info));
    }
}
