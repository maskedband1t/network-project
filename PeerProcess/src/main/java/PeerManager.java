import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

public class PeerManager implements Runnable {

    class OptimisticUnchoker extends Thread {
        private final int _num_optimistic_unchoked_neighbors;
        private final int _optimistic_unchoking_interval;
        private final List<PeerInfo> _chokedPeers = new ArrayList<>();
        // TODO: set up collection of optimistically unchoked peers
        final Collection<PeerInfo> _optimisticallyUnchokedPeers = Collection.newSetFromMap(); // not done
        
        OptimisticUnchoker(){
            _num_optimistic_unchoked_neighbors = 1; // hardcoded for now (maybe don't need to change)
            _optimistic_unchoking_interval = CommonConfig.getInstance().optimisticUnchokingInterval;
        }

        synchronized void setChokedPeers(Collection<PeerInfo> chokedPeers) { // shouldn't be able to access this while running
            _chokedPeers.clear();
            _chokedPeers.addAll(chokedPeers);
        }

        @Override
        public void run(){
            while(true){
                try {
                    Thread.sleep(_optimistic_unchoking_interval); // constantly sleeping for interval and reshuffling once out
                } catch (InterruptedException e) {
                    //TODO: handle exception
                }

                synchronized(this){
                        if(!_chokedPeers.isEmpty()){
                            Collections.shuffle(_chokedPeers); // shuffle to pick new unchoked peers 
                            _optimisticallyUnchokedPeers.clear(); // clear to put new ones in
                            _minPeers = Math.min(_chokedPeers.size() ,_num_optimistic_unchoked_neighbors);
                            _optimisticallyUnchokedPeers.addAll(0, _minPeers); // since already shuffled, this is fine
                        }
                }
            }
        }
    }





    public List<Integer> _interestedPeers;
    public List<Integer> _preferredPeers;
    public List<Integer> _optimisticallyUnchokedPeers;

    public PeerManager() {
    }

    public boolean CanUploadToPeer(PeerInfo info) {
        return (_preferredPeers.contains(info.getId()) ||
                _optimisticallyUnchokedPeers.contains(info.getId()));
    }

    public void addPeerInterested(int remotePeerId) {
        if (!_interestedPeers.contains(remotePeerId))
            _interestedPeers.add(remotePeerId);
    }

    public void removePeerInterested(int remotePeerId) {
        if (_interestedPeers.contains(remotePeerId))
            _interestedPeers.remove(remotePeerId);
    }

    public void handleHave(int remotePeerId, int pieceIdx) {
    }

    public void handleBitfield(int remotePeerId, BitSet bitset) {
    }

    public boolean canUploadToPeer(int remotePeerId) {
        return false;
    }

    public void receivedPiece(int remotePeerId, int pieceContentLength) {
    }

    public BitSet getReceivedPieces(int peerId) {
        return null;
    }
}
