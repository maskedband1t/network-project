import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Bitfield {
    private BitSet bits;

    // Default constructor, init bits with number of pieces defined in CommonConfig
    public Bitfield() {
        bits = new BitSet(CommonConfig.getInstance().numPieces);
    }

    // Init bits with number of pieces defined in CommonConfig as all true or false
    public Bitfield(boolean allTrue) {
        bits = new BitSet(CommonConfig.getInstance().numPieces);
        if (allTrue)
            bits.flip(0, CommonConfig.getInstance().numPieces);
    }

    // Init bits with a byte array
    public Bitfield(byte[] set) {
        bits = BitSet.valueOf(set.clone());
    }

    public Bitfield(BitSet set) {
        bits = set;
    }

    public Bitfield clone(){ // provides clone capabilities for Bitfield using underlying bitset clone
        BitSet new_bitset = (BitSet)this.getBits().clone();
        Bitfield new_bitfield = new Bitfield(new_bitset);
        return new_bitfield;
    }

    public Bitfield andNot(Bitfield b){ // provides andNot capabilities for Bitfield using underlying bitset andNot
        BitSet new_bitset = (BitSet) getBits().clone();
        new_bitset.andNot(b.getBits());
        return new Bitfield(new_bitset);
    }

    // Get bits as byte array
    public byte[] toByteArray() {
        return ((BitSet)bits.clone()).toByteArray();
    }

    // Get bits as a string
    public String asString() {
        String str = "";
        for (int i = 0; i < CommonConfig.getInstance().numPieces; i++) {
            str += i == CommonConfig.getInstance().numPieces - 2 ? Helpers.boolToBit(bits.get(i)) : Helpers.boolToBit(bits.get(i)) + ",";
        }
        return str;
    }

    // Get bits
    public BitSet getBits() {
        return bits;
    }

    // TODO: Can probably remove
    public int getRandomUnsetIndex() {
        List<Integer> unsets = new ArrayList<Integer>();
        for (int i = bits.nextSetBit(0); i != -1; i = bits.nextSetBit(i + 1)) {
            unsets.add(i);
        }
        return (int) Math.random() * unsets.size();
    }
    
    // Check if bit at index is == 1
    public boolean hasPiece(int pieceIndex) {
        try {
            return bits.get(pieceIndex);
        }
        catch (IndexOutOfBoundsException e) {
            Helpers.printerr("Unable to get this piece: index out of bounds.");
            e.printStackTrace();
            return false;
        }
    }

    // Check if piece is successfully set to 1
    public boolean setPiece(int pieceIndex) {
        try {
            bits.set(pieceIndex);
            return true;
        }
        catch (IndexOutOfBoundsException e) {
            Helpers.printerr("Unable to set this piece: index out of bounds.");
            e.printStackTrace();
            return false;
        }
    }

    // Check if bits are all set to 0
    public boolean empty() {
        return bits.cardinality() == 0;
    }

    // Debugging function to print the bitset into the command line
    public void debugPrint() {
        Helpers.print("{");
        for (int i = 0; i < CommonConfig.getInstance().numPieces; i++)
            Helpers.print(bits.get(i) + ",");
        Helpers.print("}");
        Helpers.println("");
    }
}