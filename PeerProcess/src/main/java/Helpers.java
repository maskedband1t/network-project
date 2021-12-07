import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class Helpers {
    // Get boolean as bit
    public static int boolToBit(boolean b) { return b ? 1 : 0; }

    // Get an int as a byte array
    public static byte[] intToBytes(int i, int size) {
        return ByteBuffer.allocate(size).putInt(i).array();
    }

    // Get a byte array as an int
    public static int bytesToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    // Get a piece index field from a byte array
    public static int getPieceIndexFromByteArray(byte[] payload) {
        return ByteBuffer.wrap(Arrays.copyOfRange(payload, 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    // Get piece content from a byte array
    public static byte[] getPieceContentFromByteArray(byte[] payload) {
        // Validate not null
        if (payload == null || payload.length <= 4)
            return null;

        return Arrays.copyOfRange(payload, 4, payload.length);
    }

    // Copy a byte array into another
    public static void copyBytes(byte[] source, int srcBegin, int srcEnd, byte[] destination,
                                 int dstBegin) {
        System.arraycopy(source, srcBegin, destination, dstBegin, srcEnd - srcBegin);
    }

    // Get a subset of a byte array
    public static byte[] getByteSubset(byte[] source, int srcBegin, int srcEnd) {
        byte destination[] = new byte[srcEnd - srcBegin];
        copyBytes(source, srcBegin, srcEnd, destination, 0);
        return destination;
    }

    // Translate Message Types to friendly name
    public static String GetMessageType(byte b) {
        switch(b) {
            case 0:
                return "CHOKE";
            case 1:
                return "UNCHOKE";
            case 2:
                return "INTERESTED";
            case 3:
                return "NOTINTERESTED";
            case 4:
                return "HAVE";
            case 5:
                return "BITFIELD";
            case 6:
                return "REQUEST";
            case 7:
                return "PIECE";
            default:
                return "";
        }
    }

    // Message Types
    public static final byte CHOKE = 0;
    public static final byte UNCHOKE = 1;
    public static final byte INTERESTED = 2;
    public static final byte NOTINTERESTED = 3;
    public static final byte HAVE = 4;
    public static final byte BITFIELD = 5;
    public static final byte REQUEST = 6;
    public static final byte PIECE = 7;

    // Relative string path to where the built resources file is
    public static final String pathToResourcesFolder = "./../../../resources/main/";

    // Debugging purposes
    public static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
}
