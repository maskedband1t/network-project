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
        final List<PeerInfo> _optimisticallyUnchokedPeers = Collections.newSetFromMap(new ConcurrentHashMap<PeerInfo, Boolean>);
        
        OptimisticUnchoker(){
            _num_optimistic_unchoked_neighbors = 1;    // hardcoded for now (maybe don't need to change)
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

    private final List<PeerInfo> _peers = new Arraylist<>();
    private final OptimisticUnchoker _optimisticUnchoker;
    private int _unchokingInterval;
    private int _num_Preffered_Neighbors;
    private int _numPieces;

    public PeerManager(int peerId, List<PeerInfo> peers) {
        _optimisticUnchoker = new OptimisticUnchoker();
        _unchokingInterval = CommonConfig.getInstance().unchokingInterval;
        _num_Preffered_Neighbors = CommonConfig.getInstance().numPrefNeighbors;
        _numPieces = (int)Math.ceil(CommonConfig.getInstance().fileSize/CommonConfig.getInstance().pieceSize);
        _peers.addAll(peers);
    }

    public boolean CanUploadToPeer(PeerInfo info) {
        return (_preferredPeers.contains(info.getId()) ||
                _optimisticallyUnchokedPeers.contains(info.getId()));
    }

    public void addPeerInterested(int peerId) {
        for (PeerInfo peer : _peers) {
            if (peer.getPeerId() == peerId) {
                if(peer != null){
                    peer.setIfInterested(true);
                }
            }
        }
    }

    public void removePeerInterested(int peerId) {
        for (PeerInfo peer : _peers) {
            if (peer.getPeerId() == peerId) {
                if(peer != null){
                    peer.setIfInterested(false);
                }
            }
        } 
    }

    public void handleHave(int peerId, int pieceIdx) {
    }

    public void handleBitfield(int peerId, BitSet bitset) {
    }

    public boolean canUploadToPeer(int peerId) {
        return false;
    }

    public void receivedPiece(int peerId, int pieceContentLength) {
    }

    public BitSet getReceivedPieces(int peerId) {
        return null;
    }
}
