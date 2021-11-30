public class HandshakeMessage {
    private int _peerId;

    // Construct a handshake message with the id of the sender
    public HandshakeMessage (int id) {
        _peerId = id;
    }

    // Get the id of the sender
    public int getPeerId() { return _peerId; }
}