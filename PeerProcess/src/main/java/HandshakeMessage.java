import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class HandshakeMessage {
    private int _peerId;

    public HandshakeMessage (int id) {
        _peerId = id;
    }

    public int getPeerId() { return _peerId; }
}