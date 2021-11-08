import java.nio.ByteBuffer;
import java.util.Arrays;

public class HandshakeMessage {
    int PeerId;

    public HandshakeMessage(byte[] msgBytes) {
        byte[] peerId = Arrays.copyOfRange(msgBytes, msgBytes.length - 4, msgBytes.length);
        PeerId = ByteBuffer.wrap(peerId).getInt();
    }
}