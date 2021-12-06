public class MessageHandler {
    private boolean _choked;
    private int _remotePeerId;
    private FileManager _fileManager;
    private PeerManager _peerManager;

    // Construct the Message Handler with the remote peer we are handling messages for
    MessageHandler(int remotePeerId, FileManager fileManager, PeerManager peerManager) {
        _choked = true;
        _remotePeerId = remotePeerId;
        _fileManager = fileManager;
        _peerManager = peerManager;
    }

    // Handle an incoming message
    public Message handle(Message msg) {
        System.out.println("Handling message of type: " + Helpers.GetMessageType(msg.getType()));

        // Handle depending on message type
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

    // Handle a message of type Choke
    private void handleChokeMsg(Message msg) {
        // NO packet payload

        // Choked by the remote peer
        _choked = true;

        // Log
        Logger.getInstance().chokedBy(_remotePeerId);
    }

    // Handle a message of type Unchoke
    private Message handleUnchokedMsg(Message msg) {
        // NO packet payload

        // Unchoked by the remote peer
        _choked = false;

        // Log
        Logger.getInstance().unchokedBy(_remotePeerId);

        // Return Request msg if applicable
        if (!_choked) {
            int newPieceIdx = _fileManager.getPieceToRequest(_peerManager.getReceivedPieces(_remotePeerId));
            byte[] newPieceIdxByteArray = Helpers.intToBytes(newPieceIdx, 4);
            System.out.println("We want piece with index: " + newPieceIdx);
            if (newPieceIdx >= 0)
                return new Message(Helpers.REQUEST, newPieceIdxByteArray);
        }

        return null;
    }

    // Handle a message of type Interested
    private void handleInterestedMsg(Message msg) {
        // NO packet payload

        // Interested in remote peer
        _peerManager.addPeerInterested(_remotePeerId);

        // Log
        Logger.getInstance().receivedInterestedFrom(_remotePeerId);
    }

    // Handle a message of type NotInterested
    private void handleNotInterestedMsg(Message msg) {
        // NO packet payload

        // Uninterested in remote peer
        _peerManager.removePeerInterested(_remotePeerId);

        // Log
        Logger.getInstance().receivedNotInterestedFrom(_remotePeerId);
    }

    // Handle a message of type Have
    private Message handleHaveMsg(Message msg) {
        // HAS packet payload: 4 byte piece index field

        // Get piece index field
        int pieceIdx = Helpers.getPieceIndexFromByteArray(msg.getPayload());

        // Log
        Logger.getInstance().receivedHaveFrom(_remotePeerId, pieceIdx);

        // Update the bitfield for the peer that sent this message
        _peerManager.handleHave(_remotePeerId, pieceIdx);

        // Send message back based on whether or not bitfield has this piece
        if (_fileManager.getReceivedPieces().getBits().get(pieceIdx))
            return new Message(Helpers.NOTINTERESTED, new byte[]{});
        else
            return new Message(Helpers.INTERESTED, new byte[]{});
    }

    // Handle a message of type Bitfield
    private Message handleBitfieldMsg(Message msg) {
        // HAS packet payload: bitfield structure, which tracks the pieces of the file the peer has
        // Ex: If there are 32 pieces of the file, and the peer has all of them, it will send a payload of 32 bits

        // Initialize the bitfield for the peer that sent this message
        // Note: We only handle this bitfield message once per peer
        Bitfield bf = new Bitfield(msg.getPayload());
        _peerManager.handleBitfield(_remotePeerId, bf);

        // Log
        Logger.getInstance().receivedBitfieldFrom(_remotePeerId);

        // TODO: Debug print - can remove later
        System.out.println("Setting Bitfield for peer " + _remotePeerId);// + " to: ");
        //bf.debugPrint();

        // Clears all bits that are set
        bf.getBits().andNot(_fileManager.getReceivedPieces().getBits());

        // Send message back based on whether or not bitfield has this piece
        if (bf.empty())
            return new Message(Helpers.NOTINTERESTED, new byte[]{});
        else
            return new Message(Helpers.INTERESTED, new byte[]{});
    }

    // Handle a message of type Request
    private Message handleRequestMsg(Message msg) {
        // HAS packet payload: 4 byte piece index field
        // Ex: The peer has requested for us to send the piece corresponding to the 4 byte piece index field in the payload

        // Get piece index field
        int pieceIdx = Helpers.getPieceIndexFromByteArray(msg.getPayload());


        // Log
        Logger.getInstance().receivedRequestFrom(_remotePeerId, pieceIdx);

        // Make sure we can send to remotePeer
        if (_peerManager.canUploadToPeer(_remotePeerId)) {
            System.out.println("We can upload piece " + pieceIdx + " to peer " + _remotePeerId);
            // Get the piece
            byte[] piece = _fileManager.getPiece(pieceIdx);

            byte[] concat = new byte[4 + (piece == null ? 0 : piece.length)];
            System.arraycopy( Helpers.intToBytes(pieceIdx,4), 0, concat, 0, 4);
            System.arraycopy(piece, 0, concat, 4, piece.length);


            //System.out.println(piece);

            Logger.getInstance().dangerouslyWrite("SENDING " + pieceIdx);

            // Send the piece
            if (concat != null)
                return new Message(Helpers.PIECE, concat);
        }

        return null;
    }

    // Handle a message of type Piece
    private Message handlePieceMsg(Message msg) {
        // HAS packet payload: 4 byte piece index field AND piece content
        // Ex: The peer sent the whole piece including its index field

        // We add this check here to ensure that the remote peer has not choked us before sending a requested piece over
        if (!_choked) {
            // Get piece index field
            int pieceIdx = Helpers.getPieceIndexFromByteArray(msg.getPayload());
            System.out.println("THE PIECE INDEX is " + pieceIdx);

            // Get piece content
            byte[] pieceContent = Helpers.getPieceContentFromByteArray(msg.getPayload());
            int pieceContentLength = pieceContent.length;

            Logger.getInstance().dangerouslyWrite("STARTING TO ADD PIECE");

            // Add piece content to pieces directory
            _fileManager.addPiece(pieceIdx, pieceContent);
            Logger.getInstance().dangerouslyWrite("(1) Added Piece");

            Logger.getInstance().dangerouslyWrite("ADDED PIECE");

            // Mark that we received piece
            _peerManager.updateDownloadRate(_remotePeerId, pieceContentLength);
            Logger.getInstance().dangerouslyWrite("(2) Updating Download Rate");

            Logger.getInstance().dangerouslyWrite("UPDATED DOWNLOAD RATE");

            // Mark that we received piece
            _peerManager.receivedPiece(_remotePeerId, pieceIdx);
            Logger.getInstance().dangerouslyWrite("(3) Marking Piece as Received");

            // Log
            Logger.getInstance().downloadedPiece(_remotePeerId, pieceIdx, pieceContentLength);

            // Return Request msg if applicable
            int newPieceIdx = _fileManager.getPieceToRequest(_peerManager.getReceivedPieces(_remotePeerId));
            byte[] newPieceIdxByteArray = Helpers.intToBytes(newPieceIdx, 4);
            if (newPieceIdx >= 0) {
                Logger.getInstance().dangerouslyWrite("(5) Decided to request piece: " + newPieceIdx);
                return new Message(Helpers.REQUEST, newPieceIdxByteArray);
            }
            else
                Logger.getInstance().dangerouslyWrite("We could not find a new piece to request (after receiving a piece).");
        }

        return null;
    }
}
