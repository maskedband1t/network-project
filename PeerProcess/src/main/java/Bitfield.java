import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Bitfield {
    private BitSet bits;

    // if you want to calculate the piece count yourself
    public Bitfield(int pieceCount) {
        bits = new BitSet(pieceCount);
    }

    // if you want to just throw the file size and piece size in
    public Bitfield(long fileSize, long pieceSize) {
        bits = new BitSet(Math.toIntExact(fileSize / pieceSize + (fileSize % pieceSize == 0 ? 0 : 1)));
    }
    
    public byte[] toByteArray() {
        return bits.toByteArray();
    }

    public String toString() {
        return bits.toString();
    }

    public BitSet getBits() {
        return bits;
    }
    
    public int getSize() {
        return bits.size();
    }

    public int getRandomUnsetIndex() {
        List<Integer> unsets = new ArrayList<Integer>();
        for (int i = bits.nextSetBit(0); i != -1; i = bits.nextSetBit(i + 1)) {
            unsets.add(i);
        }
        return (int) Math.random() * unsets.size();
    }
    
    // return true if has piece (i.e. bit at index is 1)
    public boolean hasPiece(int pieceIndex) {
        try {
            return bits.get(pieceIndex);
        }
        catch (IndexOutOfBoundsException e) {
            System.err.println("Unable to get this piece: index out of bounds.");
            e.printStackTrace();
            return false;
        }
    }

    // return true if piece is successfully set
    public boolean setPiece(int pieceIndex) {
        try {
            bits.set(pieceIndex);
            return true;
        }
        catch (IndexOutOfBoundsException e) {
            System.err.println("Unable to set this piece: index out of bounds.");
            e.printStackTrace();
            return false;
        }
    }
}