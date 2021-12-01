import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PeerManager implements Runnable {

    class OptimisticUnchoker extends Thread {
        private final int _num_optimistic_unchoked_neighbors;
        private final int _optimistic_unchoking_interval;
        private final List<PeerInfo> _chokedPeers = new ArrayList<>();
        // TODO: set up collection of optimistically unchoked peers
        final List<PeerInfo> _optimisticallyUnchokedPeers = Collections.newSetFromMap(new ConcurrentHashMap<PeerInfo, Boolean>());
        
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





    public Collection<PeerInfo> _preferredPeers = new HashSet<>();

    private final List<PeerInfo> _peers = new ArrayList<>();
    private final OptimisticUnchoker _optimisticUnchoker;
    private int _unchokingInterval;
    private int _num_Preffered_Neighbors;
    private final AtomicBoolean _fileDone = new AtomicBoolean(false);
    CommonConfig config = CommonConfig.getInstance();

    public PeerManager(int peerId, List<PeerInfo> peers) {
        _optimisticUnchoker = new OptimisticUnchoker();
        _unchokingInterval = config.unchokingInterval;
        _num_Preffered_Neighbors = config.numPrefNeighbors;
        _peers.addAll(peers);
    }

    synchronized boolean CanUploadToPeer(PeerInfo info) {
        return (_preferredPeers.contains(info.getId()) ||
                _optimisticallyUnchokedPeers.contains(info.getId()));
    }

    synchronized void addPeerInterested(int peerId) {
        for (PeerInfo peer : _peers) {
            if (peer.getPeerId() == peerId) {
                if(peer != null){
                    peer.setIfInterested(true);
                }
            }
        }
    }

    synchronized void removePeerInterested(int peerId) {
        for (PeerInfo peer : _peers) {
            if (peer.getPeerId() == peerId) {
                if(peer != null){
                    peer.setIfInterested(false);
                }
            }
        } 
    }
    synchronized void updateDownloadRate(int peerId, int size){
        for (PeerInfo peer : _peers) {
            if (peer.getPeerId() == peerId) {
                if(peer != null){
                    peer.set_download_rate(peer.get_download_rate() + size); 
                }
            }
        } 
    }

    synchronized boolean isPeerInteresting(int peerId, Bitfield b){ // return true if has interesting parts in bitfield 
        for (PeerInfo peer : _peers) {
            if (peer.getPeerId() == peerId) {
                if(peer != null){
                    Bitfield clone_bitfield = peer.getBitfield().clone(); // clone peers bitfield
                    clone_bitfield = clone_bitfield.andNot(b);
                    return !clone_bitfield.empty();
                }
            }
        }
        return false; 
    }
    synchronized List<PeerInfo> getInterestedPeers(){
        List<PeerInfo> interestedPeers = new ArrayList<>();

        for(PeerInfo peer : _peers){
            if(peer.isInterested()){
                interestedPeers.add(peer);
            }
        }
        return interestedPeers;
    }

    synchronized void fileCompleted() {
        _fileDone.set(true);
    }
    public void handleBitfield(int remotePeerId, Bitfield bitfield) {
        for (PeerInfo peer : _peers) {
            if (peer.getPeerId() == peerId) {
                if(peer != null){
                    peer.setBitfield(bitfield);
                }
                download_finished();
            }
        } 
    }

    public void handleHave(int peerId, int pieceIdx) {
        for (PeerInfo peer : _peers) {
            if (peer.getPeerId() == peerId) {
                if(peer != null){
                    peer.getBitfield().getBits().set(pieceIdx);
                }
                download_finished();
            }
        } 
    }

    synchronized download_finished(){
        for (PeerInfo peer : _peers) {
            if (peer.getPeerId() == peerId) {
                if(peer != null){
                    if(peer.getBitfield().getBits().cardinality() < CommonConfig.getInstance().numPieces){
                        // log that a neighbor hasnt finished
                        return;
                    }
                }
            }
        } 
    }

    synchronized BitSet getReceivedPieces(int peerId) {
        for (PeerInfo peer : _peers) {
            if (peer.getPeerId() == peerId) {
                if(peer != null){
                    Bitfield clone_bitfield = peer.getBitfield().clone(); // clone peers bitfield
                    return clone_bitfield.getBits();
                }
            }
        }
        return new BitSet();
    }

    @Override
    public void run(){
        _optimisticUnchoker.start();

        while(true){
            try {
                Thread.sleep(_unchokingInterval);
            } catch (InterruptedException e) {
                //TODO: handle exception
            }

            _interestedPeers = getInterestedPeers();

            if(_fileDone.get()){ // here we randomly shuffle neighbors
                Collections.shuffle(_interestedPeers);
            }
            else{ // sort by preference (depends on download rate of peer in previous interval)
                Collections.sort(_interestedPeers , new Comparator() {
                    @Override
                    public int compare(Object o1, Object o2){
                        PeerInfo p1 = (PeerInfo)(o1);
                        PeerInfo p2 = (PeerInfo)(o2);

                        return(p1.get_download_rate() - p2.get_download_rate());
                    }
                });
            }

            Set<PeerInfo> optimistically_unchokable_peers = null;
            Set<Integer> chokedPeerIDs = new HashSet<>();
            Set<Integer> preferredPeerIDs = new HashSet<>();
            Map<Integer, Long> downloadedBytes = new HashMap<>();

            synchronized(this){
                for(PeerInfo p : _peers){
                    downloadedBytes.put(p.getId() , p.set_download_rate(p.get_download_rate_atomic().longValue())); // store so we can run calculations
                    p.set_download_rate(0); //reset
                }

                // select highest ranked peers
                _preferredPeers.clear();
                _preferredPeers.addAll(_interestedPeers.subList(0, Math.min(_num_Preffered_Neighbors, _interestedPeers.size())));
                if(_preferredPeers.size() > 0){
                    // logs
                }

                Collection <PeerInfo> _choked_peers = new LinkedList<>(_peers);
                _choked_peers.removeAll(_preferredPeers); // remove unchoked ones
                chokedPeerIDs.addAll(PeerInfo.toIdList(_choked_peers)); // adding List to Set

                if(_num_Preffered_Neighbors >= _interestedPeers.size()){
                    optimistically_unchokable_peers = new ArrayList<>();
                }
                else{
                    optimistically_unchokable_peers = _interestedPeers.subList(_num_Preffered_Neighbors , _interestedPeers.size());
                }

                preferredPeerIDs.addAll(PeerInfo.toIdList(_preferredPeers));
            }

            // could log here the state of every peer if helpful
            // ex. if choked, unchoked, or interested
            // anything helpful/needed to log every thread run

            // TODO: hand chokedPeerIds and preferredPeerIds to process

            if(optimistically_unchokable_peers != null){
                _optimisticUnchoker.setChokedPeers(optimistically_unchokable_peers); // pass new unchokable peers to unchoker
            }

        }
    }
}
