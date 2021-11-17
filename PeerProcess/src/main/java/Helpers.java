import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Helpers {
    /*
    Helper functions
    */
    public static byte[] intToByte(int i, int size) {
        return ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN).putInt(i).array();
    }

    public static int getPieceIndexFromByteArray(byte[] payload) {
        return ByteBuffer.wrap(Arrays.copyOfRange(payload, 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    public static byte[] getPieceContentFromByteArray(byte[] payload) {
        // null check
        if (payload == null || payload.length <= 4)
            return null;

        return Arrays.copyOfRange(payload, 4, payload.length);
    }

    /* MESSAGE TYPES */
    public static final byte CHOKE = 0;
    public static final byte UNCHOKE = 1;
    public static final byte INTERESTED = 2;
    public static final byte NOTINTERESTED = 3;
    public static final byte HAVE = 4;
    public static final byte BITFIELD = 5;
    public static final byte REQUEST = 6;
    public static final byte PIECE = 7;
}
