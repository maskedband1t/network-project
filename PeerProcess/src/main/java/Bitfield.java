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
        return bits.toByteArray();
    }

    // Get bits as a string
    public String asString() {
        String str = "";
        for (int i = 0; i < bits.length()-1; i++) {
            str += i == bits.length() - 2 ? Helpers.boolToBit(bits.get(i)) : Helpers.boolToBit(bits.get(i)) + ",";
        }
        return str;
    }

    // Get bits
    public BitSet getBits() {
        return bits;
    }

    // Get number of bits
    public int getSize() {
        return bits.length();
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

    synchronized int getPieceIndexToRequest(Bitfield requestedPieces, Bitfield remotePieces) {
        // logic for which piece to choose is detailed in the proj description

        // toString() does this: {1,0,1,1} -> "1,0,1,1"

        // Prints
        Logger.getInstance().dangerouslyWrite("requestedPieces: (" + requestedPieces.getBits().length() + " bits) " + requestedPieces.asString());
        Logger.getInstance().dangerouslyWrite("remotePieces: (" + remotePieces.getBits().length() + " bits) "  + remotePieces.asString());
        Logger.getInstance().dangerouslyWrite("ourPieces: (" + this.getBits().length() + " bits) "  + this.asString());

        // get the pieces we have -> get the pieces we don't have -> get the pieces we don't have that they have
        Bitfield piecesWeCanRequest = clone(); // pieces we have
        BitSet piecesWeCanRequestBitset = piecesWeCanRequest.getBits();
        piecesWeCanRequestBitset.flip(0, piecesWeCanRequestBitset.length()); // pieces we dont have
        piecesWeCanRequestBitset.and(remotePieces.getBits()); // pieces we dont have and they have
        piecesWeCanRequestBitset.and(requestedPieces.getBits()); // pieces we dont have and they have and we havent requested yet

        String str = piecesWeCanRequest.asString();
        Logger.getInstance().dangerouslyWrite("piecesWeCanRequest (" + piecesWeCanRequestBitset.length() + " bits) " + str);

        BitSet requestedPiecesBitset = requestedPieces.getBits();

        // Choose a random piece to request if there are choices
        if (!piecesWeCanRequestBitset.isEmpty()) {
            // Request a piece that we do not have, that we haven't requested yet
            // Random Selection Strategy if there are multiple choices
            // Ex: We are Peer A, and are requesting a Piece from Peer B
            //     We will randomly select a Piece to request from Peer B
            //     of the pieces we do not have, and have not requested

            // "1,0,1,1" -> ["1","0","1" "1"]
            String[] indexes = str.substring(1, str.length()-1).split(",");
            // Get random index, trim commas off, parse into int
            int pieceIndex = Integer.parseInt(indexes[(int)(Math.random()*(indexes.length-1))].trim());

            Logger.getInstance().dangerouslyWrite("GOT PIECE TO REQUEST: " + pieceIndex);

            // since we're going to return this value, update that we will request this index
            requestedPiecesBitset.set(pieceIndex);

            // make the part requestable again in _timeoutInMillis
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            synchronized (requestedPiecesBitset) {
                                requestedPiecesBitset.clear(pieceIndex);
                            }
                        }
                    },
                    CommonConfig.getInstance().unchokingInterval * 2000
            );
            // return the index of the piece to request
            return pieceIndex;
        }
        // default
        System.out.println("WE COULD NOT FIND AN INDEX TO REQUEST!!");
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