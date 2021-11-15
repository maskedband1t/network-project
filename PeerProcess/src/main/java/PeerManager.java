import java.util.BitSet;
import java.util.List;

public class PeerManager {
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
