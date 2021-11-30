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
        bits = BitSet.valueOf(set);
    }

    // Get bits as byte array
    public byte[] toByteArray() {
        return bits.toByteArray();
    }

    // Get bits as a string
    public String toString() {
        return bits.toString();
    }

    // Get bits
    public BitSet getBits() {
        return bits;
    }

    // Get number of bits
    public int getSize() {
        return bits.size();
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
            System.err.println("Unable to get this piece: index out of bounds.");
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
            System.err.println("Unable to set this piece: index out of bounds.");
            e.printStackTrace();
            return false;
        }
    }

    // Check if bits are all set to 0
    public boolean empty() {
        return bits.cardinality() == 0;
    }

    synchronized int getPieceIndexToRequest(BitSet piecesNotRequested) {
        // logic for which piece to choose is detailed in the proj description
        piecesNotRequested.andNot(bits);
        if (!piecesNotRequested.isEmpty()) {
            // Request a piece that we do not have, that we haven't requested yet
            // Random Selection Strategy if there are multiple choices
            // Ex: We are Peer A, and are requesting a Piece from Peer B
            //     We will randomly select a Piece to request from Peer B
            //     of the pieces we do not have, and have not requested

            // {1,0,1,1} -> "1,0,1,1"
            String str = piecesNotRequested.toString();
            // "1,0,1,1" -> ["1","0","1" "1"]
            String[] indexes = str.substring(1, str.length()-1).split(",");
            // Get random index, trim commas off, parse into int
            int pieceIndex = Integer.parseInt(indexes[(int)(Math.random()*(indexes.length-1))].trim());

            // since we're going to return this value, update that we will request this index
            bits.set(pieceIndex);

            // make the part requestable again in _timeoutInMillis
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            synchronized (bits) {
                                bits.clear(pieceIndex);
                            }
                        }
                    },
                    2
            );
            // return the index of the piece to request
            return pieceIndex;
        }
        // default
        return -1;
    }

    // Debugging function to print the bitset into the command line
    public void debugPrint() {
        int[] arr = bits.stream().toArray();
        System.out.print("{");
        for (int i = 0; i < bits.length(); i++)
            System.out.print(bits.get(i) + ",");
        System.out.print("}");
        System.out.println();
    }
}