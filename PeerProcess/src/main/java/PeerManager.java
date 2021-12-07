import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class PeerManager implements Runnable {

    // PeerManager has OptimisticUnchoker as a separate thread
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
                    Thread.sleep(_optimistic_unchoking_interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // TODO: handle exception
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

    private int _peerId;
    public Collection<PeerInfo> _preferredPeers = new HashSet<>();
    public List<PeerInfo> _peers = new ArrayList<>();
    private final OptimisticUnchoker _optimisticUnchoker;
    private int _unchokingInterval;
    private int _num_Preferred_Neighbors;
    private final AtomicBoolean _fileDone = new AtomicBoolean(false);
    CommonConfig config = CommonConfig.getInstance();

    Set<Integer> _chokedPeerIDs = new HashSet<>();
    Set<Integer> _preferredPeerIDs = new HashSet<>();
    private Process _process = null;

    // Construct the PeerManager for peerId
    public PeerManager(int peerId) {
        _peerId = peerId;
        _optimisticUnchoker = new OptimisticUnchoker();
        _unchokingInterval = config.unchokingInterval;
        _num_Preferred_Neighbors = config.numPrefNeighbors;
        // our peers are everyone but us
        _peers = PeerInfoConfig.getInstance().peerInfos.stream().filter(ele -> ele.getId() != peerId).collect(Collectors.toList());
        Helpers.println("Peers: ");
        for (PeerInfo p : _peers)
            Helpers.print(p.getBitfield() + ",");
        Helpers.println("");
    }

    // Checks if we can upload to remote peer with peerId
    synchronized boolean canUploadToPeer(int peerId) {
        List<Integer> opt = _optimisticUnchoker._optimisticallyUnchokedPeers.stream().map(p -> p.getId()).collect(Collectors.toList());
        boolean x = _preferredPeerIDs.contains(peerId) ||
                opt.contains(peerId);
        Helpers.println("Peer " + peerId + " is preferred or opt unchoke: " + x);
        return x;
    }

    // Checks if we are interested in remote peer with @peerId
     public synchronized void addPeerInterested(int peerId) {
        for (PeerInfo peer : _peers) {
            if (peer.getId() == peerId) {
                peer.setIfInterested(true);
            }
        }
    }

    // Sets Interested bool for remote peer with peerId to false
    synchronized void removePeerInterested(int peerId) {
        for (PeerInfo peer : _peers) {
            if (peer.getId() == peerId) {
                if(peer != null){
                    peer.setIfInterested(false);
                }
            }
        } 
    }

    // Increment download_rate of remote peer with peerId by size
    void updateDownloadRate(int peerId, int size){
        for (PeerInfo peer : _peers) {
            if (peer.getId() == peerId) {
                if(peer != null){
                    peer.set_download_rate(peer.get_download_rate() + size); 
                }
            }
        } 
    }

    // Checks if we should be interested in peer with peerId
    // return true if has interesting parts in bitfield
    synchronized boolean isPeerInteresting(int peerId, Bitfield b){
        for (PeerInfo peer : _peers) {
            if (peer.getId() == peerId) {
                if(peer != null){
                    Bitfield clone_bitfield = peer.getBitfield().clone(); // clone peers bitfield
                    clone_bitfield = clone_bitfield.andNot(b);
                    return !clone_bitfield.empty();
                }
            }
        }
        return false;
    }

    // Get list of peers we are interested in
    synchronized List<PeerInfo> getInterestedPeers(){
        List<PeerInfo> interestedPeers = new ArrayList<PeerInfo>();

        for(PeerInfo peer : _peers){
            if(peer.isInterested()){
                interestedPeers.add(peer);
            }
        }
        return interestedPeers;
    }

    // Sets _fileDone to true
    public synchronized void fileCompleted() {
        _fileDone.set(true);
    }

    // Initializes the bitfield for remote peer with id peerId
    public synchronized void handleBitfield(int peerId, Bitfield bitfield) {
        for (PeerInfo peer : _peers) {
            if (peer.getId() == peerId) {
                peer.setBitfield(bitfield);
                if (bitfield.getBits().cardinality() == CommonConfig.getInstance().numPieces)
                    peer.set_file_complete(true);
                Logger.getInstance().dangerouslyWrite("Updated bitfield for " + peerId + " to: " + peer.getBitfield().getBits().toString());
                download_finished();
            }
        } 
    }

    // Updates the bitfield at index pieceIdx to 1 for remote peer with id peerId
    public synchronized void handleHave(int peerId, int pieceIdx) {
        for (PeerInfo peer : _peers) {
            if (peer.getId() == peerId) {
                if (!peer.getFileComplete())
                    peer.getBitfield().getBits().set(pieceIdx);
                download_finished();
            }
        } 
    }

    // Handles logic for finishing the file if necessary
    private synchronized void download_finished(){
        for (PeerInfo peer : _peers) {
            if (peer.getBitfield().getBits().cardinality() < CommonConfig.getInstance().numPieces){
                Logger.getInstance().dangerouslyWrite("(download_finished) Peer " + peer.getId() + " is NOT done. Cardinality (" + peer.getBitfield().getBits().cardinality() + ")");
                return;
            }
            else
                Logger.getInstance().dangerouslyWrite("(download_finished) Peer " + peer.getId() + " IS done.");
        }
        _process.neighborsComplete();
    }

    // Set _chokedPeerIds
    private synchronized void update_choked_peers(Set<Integer> c_peers){
        Set<Integer> choked_peers = new HashSet<>();
        choked_peers.addAll(c_peers);
        _chokedPeerIDs = choked_peers;
    }

    // Set _preferredPeerIds
    private synchronized void update_preferred_peers(Set<Integer> c_peers){
        Set<Integer> preferred_peers = new HashSet<>();
        preferred_peers.addAll(c_peers);
        _preferredPeerIDs = preferred_peers;
    }

    // Get _chokedPeerIDs
    Set<Integer> get_choked_peer_ids(){
        return _chokedPeerIDs;
    }

    // Set _preferredPeerIDs
    Set<Integer> get_preferred_peer_ids(){
        return _preferredPeerIDs;
    }

    // Get a copy of the BitSet of remote peer with id peerId
    synchronized Bitfield getReceivedPieces(int peerId) {
        for (PeerInfo peer : _peers) {
            if (peer.getId() == peerId) {
                if(peer != null){
                    //Bitfield clone_bitfield = peer.getBitfield().clone(); // clone peers bitfield
                    return peer.getBitfield().clone(); //clone_bitfield.getBits();
                }
            }
        }
        return new Bitfield();
    }

    // Sets bit at pieceIdx of BitSet of remote peer with id peerId to 1
    synchronized void receivedPiece(int peerId, int pieceIdx) {
        for (PeerInfo peer : _peers) {
            if (peer.getId() == peerId) {
                if(peer != null){
                    peer.getBitfield().getBits().set(pieceIdx);
                }
            }
        }
    }

    // register the process
    synchronized void registerProcess(Process proc) {
        this._process = proc;
    }

    // choke peers
    void choke_peers(Set<Integer> peers) throws IOException {
        if (this._process != null)
            this._process.choke_peers(peers);
    }

    // unchoke peers
    void unchoke_peers(Set<Integer> peers) throws IOException {
        if (this._process != null) {
            this._process.unchoke_peers(peers);
        }
    }

    // The entry point for this thread
    @Override
    public void run(){
        // Start the optimisticUnchoker on its own thread
        _optimisticUnchoker.start();

        while(!Process.shutdown){
            try {
                Thread.sleep(_unchokingInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
                // TODO: handle exception
            }

            List<PeerInfo> _interestedPeers = getInterestedPeers();

            /*
            // Debugging purposes
            if (_interestedPeers.size() > 0) {
                Helpers.print("Interested Peers: ");
                for(PeerInfo p : _interestedPeers)
                    Helpers.print(p.getId() + ",");
                Helpers.println();
            }*/

            // Here we randomly shuffle neighbors
            if(_fileDone.get()){
                Collections.shuffle(_interestedPeers);
            }
            // Sort by preference (depends on download rate of peer in previous interval)
            else {
                Collections.sort(_interestedPeers , new Comparator() {
                    @Override
                    public int compare(Object o1, Object o2){
                        PeerInfo p1 = (PeerInfo)(o1);
                        PeerInfo p2 = (PeerInfo)(o2);

                        return(p1.get_download_rate() - p2.get_download_rate());
                    }
                });
            }

            Collection<PeerInfo> optimistically_unchokable_peers;
            Set<Integer> chokedPeerIDs = new HashSet<>();
            Set<Integer> preferredPeerIDs = new HashSet<>();
            Map<Integer, Long> downloadedBytes = new HashMap<>();

            synchronized(this){
                for(PeerInfo p : _peers){
                    downloadedBytes.put(p.getId() , p.get_download_rate_atomic().longValue()); // store so we can run calculations
                    p.set_download_rate(0); //reset
                }

                // Select highest ranked peers
                _preferredPeers.clear();
                _preferredPeers.addAll(_interestedPeers.subList(0, Math.min(_num_Preferred_Neighbors, _interestedPeers.size())));
                if(_preferredPeers.size() > 0){
                    // logs
                }

                Collection <PeerInfo> _choked_peers = new LinkedList<>(_peers);
                _choked_peers.removeAll(_preferredPeers); // remove unchoked ones
                chokedPeerIDs.addAll(PeerInfo.toIdList(_choked_peers));

                if(_num_Preferred_Neighbors >= _interestedPeers.size())
                    optimistically_unchokable_peers = new ArrayList<PeerInfo>();
                else
                    optimistically_unchokable_peers = _interestedPeers.subList(_num_Preferred_Neighbors, _interestedPeers.size());

                preferredPeerIDs.addAll(PeerInfo.toIdList(_preferredPeers));

                /*Helpers.print("Preferred peers: ");
                for(int id:preferredPeerIDs)
                    Helpers.print(id);
                Helpers.println();*/
            }

            // could log here the state of every peer if helpful
            // ex. if choked, unchoked, or interested
            // anything helpful/needed to log every thread run

            // Update _chokedPeerIDs and _preferredPeerIDs
            // TODO: hand chokedPeerIds and preferredPeerIds to process
            update_choked_peers(chokedPeerIDs);
            update_preferred_peers(preferredPeerIDs);

            // choke/unchoke peers
            try {
                choke_peers(chokedPeerIDs);
                unchoke_peers(preferredPeerIDs);
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            if(optimistically_unchokable_peers != null)
                _optimisticUnchoker.setChokedPeers(optimistically_unchokable_peers); // pass new unchokable peers to unchoker
        }
    }
}
