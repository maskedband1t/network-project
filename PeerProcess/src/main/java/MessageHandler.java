import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.BitSet;

public class MessageHandler {
    /* MESSAGE TYPES */
    public static final byte CHOKE = 0;
    public static final byte UNCHOKE = 1;
    public static final byte INTERESTED = 2;
    public static final byte NOTINTERESTED = 3;
    public static final byte HAVE = 4;
    public static final byte BITFIELD = 5;
    public static final byte REQUEST = 6;
    public static final byte PIECE = 7;

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
        BitSet bitset = _fileManager.getReceivedParts();
        if (!bitset.isEmpty()) {
            return (new Message(BITFIELD, bitset.toByteArray()));
        }
        return null;
    }

    public Message handle(Message msg) {
        switch (msg.getType()) {
            case CHOKE: {
                handleChokeMsg(msg);
                return null;
            }
            case UNCHOKE: {
                return handleUnchokedMsg(msg);
            }
            case INTERESTED: {
                handleInterestedMsg(msg);
                return null;
            }
            case NOTINTERESTED: {
                handleNotInterestedMsg(msg);
                return null;
            }
            case HAVE: {
                return handleHaveMsg(msg);
            }
            case BITFIELD: {
                return handleBitfieldMsg(msg);
            }
            case REQUEST: {
                return handleRequestMsg(msg);
            }
            case PIECE: {
                return handlePieceMsg(msg);
            }
        }
        return null;
    }

    private void handleChokeMsg(Message msg) {
        // NO packet payload

        // Psuedocode
        // Unchoked = false
        // We should no longer be sending RequestMessages to the peer
        _choked = true;
        Logger.getInstance().chokedBy(_remotePeerId);
    }

    private Message handleUnchokedMsg(Message msg) {
        // NO packet payload

        // Psuedocode
        // While(Unchoked):
        //     PieceToRequest = Random Selection Strategy to select a piece that the peer has that we do not and have not requested
        //     SendRequestMessageTo(peerInfo, forPieceWithHeader=PieceToRequest.Header)
        /**
         * Message msg = new Message();
         * peerConn.send(msg);
         * **/
        //     // Handle the edge case in which we request the piece but never recieve it bc the peer chokes us
        //     If NotReceiveForRequest():
        //         break

        choked = false;
        Logger.getInstance().unchokedBy(peerConn.GetInfo().getId());

        while (!choked) { // need to choose an actual conditional - where to keep track of choked?
            int index = 0; // int index = Bitfield.getRandomUnsetIndex();
            byte[] length = ByteBuffer.allocate(4).putInt(4).array();
            byte[] pieceIndex = ByteBuffer.allocate(4).putInt(index).array();
            Message requestMsg = new Message(length, Process.REQUEST.byteValue(), pieceIndex);
            try {
                peerConn.send(requestMsg);
            } catch (IOException e) {
                System.err.println("IO Error on request.");
                e.printStackTrace();
            }
        }
        // handle not received
    }

    private void handleInterestedMsg(Message msg) {
        Logger.getInstance().receivedInterestedFrom(_remotePeerId);
        _peerManager.addPeerInterested(_remotePeerId);
    }

    private void handleNotInterestedMsg(Message msg) {
        Logger.getInstance().receivedNotInterestedFrom(_remotePeerId);
        _peerManager.removePeerInterested(_remotePeerId);
    }

    private Message handleHaveMsg(Message msg) {
        // HAS packet payload: 4 byte piece index field

        // Get piece index field
        int pieceIdx = getPieceIndexFromByteArray(msg.getPayload());

        // Log
        Logger.getInstance().receivedHaveFrom(_remotePeerId, pieceIdx);

        // Update the bitfield for the peer that sent this message
        _peerManager.handleHave(_remotePeerId, pieceIdx);

        // Send message back based on whether or not bitfield has this piece
        if (_fileManager.getReceievedParts().get(pieceIdx)) {
            return new Message(NOTINTERESTED, new byte[]{});
        }
        else {
            return new Message(INTERESTED, new byte[]{});
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

        // clears all bits that are set in getReceievedParts()
        bitset.andNot(_fileManager.getReceivedParts());

        // Send message back based on whether or not bitfield has this piece
        if (bitset.isEmpty()) {
            return new Message(NOTINTERESTED, new byte[]{});
        } else {
            return new Message(INTERESTED, new byte[]{});
        }
    }

    private Message handleRequestMsg(Message msg) {
        // HAS packet payload: 4 byte piece index field
        // Ex: The peer has requested for us to send the piece corresponding to the 4 byte piece index field in the payload

        // make sure we can send to remotePeer
        if (_peerManager.canUploadToPeer(_remotePeerId)) {
            // get the piece
            byte[] piece = _fileManager.getPiece(getPieceIndexFromByteArray(msg.getPayload()));

            // send the piece
            if (piece != null) {
                return new Message(PIECE, piece);
            }
        }

        return null;
    }

    private Message handlePieceMsg(Message msg) {
        // HAS packet payload: 4 byte piece index field AND piece content
        // Ex: The peer sent the whole piece including its index field

        int pieceIdx = getPieceIndexFromByteArray(msg.getPayload());
        byte[] pieceContent = getPieceContentFromByteArray(msg.getPayload());
        int pieceContentLength = pieceContent.length;

        // Add piece to file
        _fileManager.addPiece(pieceIdx, pieceContent);

        // Mark that we receieved piece
        _peerManager.receievedPiece(_remotePeerId, pieceContentLength);

        // Log
        Logger.getInstance().downloadedPiece(_remotePeerId, pieceIdx, pieceContentLength);

        // Return Request msg if applicable
        if (!_choked) {
            int newPieceIdx = _fileManager.getPieceToRequest(_peerManager.getReceivedPIeces(_remotePeerId));
            byte[] newPieceIdxByteArray = intToByte(newPieceIdx, 4);
            if (newPieceIdx >= 0)
                return new Message(REQUEST, newPieceIdxByteArray);
        }

        return null;
    }

    /*
    Helper functions
    */
    private byte[] intToByte(int i, int size) {
        return ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN).putInt(i).array();
    }

    private int getPieceIndexFromByteArray(byte[] payload) {
        return ByteBuffer.wrap(Arrays.copyOfRange(payload, 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    private byte[] getPieceContentFromByteArray(byte[] payload) {
        // null check
        if (payload == null || payload.length <= 4)
            return null;

        return Arrays.copyOfRange(payload, 4, payload.length);
    }
}
