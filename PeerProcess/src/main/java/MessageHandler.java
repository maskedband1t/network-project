import java.util.BitSet;

public class MessageHandler {
    private boolean _choked;
    private int _remotePeerId;
    private FileManager _fileManager;
    private PeerManager _peerManager;
    private Logger _logger;

    MessageHandler(int remotePeerId, FileManager fileManager, PeerManager peerManager, Logger logger) {
        _choked = true;
        _remotePeerId = remotePeerId;
        _fileManager = fileManager;
        _peerManager = peerManager;
        _logger = logger;
    }

    public Message handle(HandshakeMessage msg) {
        BitSet bitset = _fileManager.getReceivedPieces();
        if (!bitset.isEmpty()) {
            return (new Message(Helpers.BITFIELD, bitset.toByteArray()));
        }
        return null;
    }

    public Message handle(Message msg) {
        switch (msg.getType()) {
            case Helpers.CHOKE: {
                handleChokeMsg(msg);
                return null;
            }
            case Helpers.UNCHOKE: {
                return handleUnchokedMsg(msg);
            }
            case Helpers.INTERESTED: {
                handleInterestedMsg(msg);
                return null;
            }
            case Helpers.NOTINTERESTED: {
                handleNotInterestedMsg(msg);
                return null;
            }
            case Helpers.HAVE: {
                return handleHaveMsg(msg);
            }
            case Helpers.BITFIELD: {
                return handleBitfieldMsg(msg);
            }
            case Helpers.REQUEST: {
                return handleRequestMsg(msg);
            }
            case Helpers.PIECE: {
                return handlePieceMsg(msg);
            }
        }
        return null;
    }

    private void handleChokeMsg(Message msg) {
        // NO packet payload

        _choked = true;

        // Log
        Logger.getInstance().chokedBy(_remotePeerId);
    }

    private Message handleUnchokedMsg(Message msg) {
        // NO packet payload

        _choked = false;

        // Log
        Logger.getInstance().unchokedBy(_remotePeerId);

        // Return Request msg if applicable
        if (!_choked) {
            int newPieceIdx = _fileManager.getPieceToRequest(_peerManager.getReceivedPieces(_remotePeerId));
            byte[] newPieceIdxByteArray = Helpers.intToByte(newPieceIdx, 4);
            if (newPieceIdx >= 0)
                return new Message(Helpers.REQUEST, newPieceIdxByteArray);
        }

        return null;
    }

    private void handleInterestedMsg(Message msg) {
        // Log
        Logger.getInstance().receivedInterestedFrom(_remotePeerId);
        _peerManager.addPeerInterested(_remotePeerId);
    }

    private void handleNotInterestedMsg(Message msg) {
        // Log
        Logger.getInstance().receivedNotInterestedFrom(_remotePeerId);
        _peerManager.removePeerInterested(_remotePeerId);
    }

    private Message handleHaveMsg(Message msg) {
        // HAS packet payload: 4 byte piece index field

        // Get piece index field
        int pieceIdx = Helpers.getPieceIndexFromByteArray(msg.getPayload());

        // Log
        Logger.getInstance().receivedHaveFrom(_remotePeerId, pieceIdx);

        // Update the bitfield for the peer that sent this message
        _peerManager.handleHave(_remotePeerId, pieceIdx);

        // Send message back based on whether or not bitfield has this piece
        if (_fileManager.getReceivedPieces().get(pieceIdx)) {
            return new Message(Helpers.NOTINTERESTED, new byte[]{});
        }
        else {
            return new Message(Helpers.INTERESTED, new byte[]{});
        }
    }

    private Message handleBitfieldMsg(Message msg) {
        // HAS packet payload: bitfield structure, which tracks the pieces of the file the peer has
        // Ex: If there are 32 pieces of the file, and the peer has all of them, it will send a payload of 32 1 bits

        // Initialize the bitfield for the peer that sent this message
        // Note: We only handle this bitfield message once per peer
        BitSet bitset = new BitSet();
        bitset.valueOf(msg.getPayload());
        _peerManager.handleBitfield(_remotePeerId, bitset);

        // clears all bits that are set
        bitset.andNot(_fileManager.getReceivedPieces());

        // Send message back based on whether or not bitfield has this piece
        if (bitset.isEmpty()) {
            return new Message(Helpers.NOTINTERESTED, new byte[]{});
        } else {
            return new Message(Helpers.INTERESTED, new byte[]{});
        }
    }

    private Message handleRequestMsg(Message msg) {
        // HAS packet payload: 4 byte piece index field
        // Ex: The peer has requested for us to send the piece corresponding to the 4 byte piece index field in the payload

        // make sure we can send to remotePeer
        if (_peerManager.canUploadToPeer(_remotePeerId)) {
            // get the piece
            byte[] piece = _fileManager.getPiece(Helpers.getPieceIndexFromByteArray(msg.getPayload()));

            // send the piece
            if (piece != null) {
                return new Message(Helpers.PIECE, piece);
            }
        }

        return null;
    }

    private Message handlePieceMsg(Message msg) {
        // HAS packet payload: 4 byte piece index field AND piece content
        // Ex: The peer sent the whole piece including its index field

        int pieceIdx = Helpers.getPieceIndexFromByteArray(msg.getPayload());
        byte[] pieceContent = Helpers.getPieceContentFromByteArray(msg.getPayload());
        int pieceContentLength = pieceContent.length;

        // Add piece to file
        _fileManager.addPiece(pieceIdx, pieceContent);

        // Mark that we receieved piece
        _peerManager.receivedPiece(_remotePeerId, pieceContentLength);

        // Log
        Logger.getInstance().downloadedPiece(_remotePeerId, pieceIdx, pieceContentLength);

        // Return Request msg if applicable
        if (!_choked) {
            int newPieceIdx = _fileManager.getPieceToRequest(_peerManager.getReceivedPieces(_remotePeerId));
            byte[] newPieceIdxByteArray = Helpers.intToByte(newPieceIdx, 4);
            if (newPieceIdx >= 0)
                return new Message(Helpers.REQUEST, newPieceIdxByteArray);
        }

        return null;
    }
}
