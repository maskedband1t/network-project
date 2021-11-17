import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class HandshakeMessage {
    private byte[] _peerIdByteArray = new byte[4];
    private int _peerId;

    public HandshakeMessage (int id) {
        _peerId = id;
        byte[] _peerIdByteArray = Helpers.intToByte(id, 4);
        int i = 0;
        for (byte b : _peerIdByteArray) {
            _peerIdByteArray[i++] = b;
        }
    }

    public int getPeerId() { return _peerId; }
    public byte[] getPeerIdPayload() {
        return _peerIdByteArray;
    }
}