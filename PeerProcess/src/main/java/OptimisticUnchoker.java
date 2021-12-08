import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

class OptimisticUnchoker extends Thread {
    private final int _num_optimistic_unchoked_neighbors;
    private final int _optimistic_unchoking_interval;
    private final List<PeerInfo> _chokedPeers = new ArrayList<>();
    // TODO: set up collection of optimistically unchoked peers
    final Collection<PeerInfo> _optimisticallyUnchokedPeers = Collections.newSetFromMap(new ConcurrentHashMap<PeerInfo, Boolean>());

    // Construct an Optimistic Unchoker
    OptimisticUnchoker(){
        _num_optimistic_unchoked_neighbors = 1;    // hardcoded for now (maybe don't need to change)
        _optimistic_unchoking_interval = CommonConfig.getInstance().optimisticUnchokingInterval;
    }

    // Set _chokedPeers
    void setChokedPeers(Collection<PeerInfo> chokedPeers) { // shouldn't be able to access this while running
        _chokedPeers.clear();
        _chokedPeers.addAll(chokedPeers);
    }

    // The entry point for this thread
    @Override
    public void run(){
        while(!Process.shutdown){
            try {
                // constantly sleeping for interval and reshuffling once out
                Thread.sleep(_optimistic_unchoking_interval * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized(this){
                if(!_chokedPeers.isEmpty()){
                    // shuffle to pick new unchoked peers
                    Collections.shuffle(_chokedPeers);

                    // clear to put new ones in
                    _optimisticallyUnchokedPeers.clear();

                    // since already shuffled, this is fine
                    int _minPeers = Math.min(_chokedPeers.size() ,_num_optimistic_unchoked_neighbors);
                    _optimisticallyUnchokedPeers.addAll(_chokedPeers.subList(0, _minPeers));
                }
            }
        }
    }
}