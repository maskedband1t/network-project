import java.util.BitSet;

// keeps track of pieces that were requested
public class RequestedPiecesBitSet {
    BitSet pieces;

    public RequestedPiecesBitSet(int numPieces) {
        pieces = new BitSet(numPieces);
    }

    synchronized int getPieceIndexToRequest(BitSet piecesNotRequested) {
        // logic for which piece to choose is detailed in the proj description
        piecesNotRequested.andNot(pieces);
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
            pieces.set(pieceIndex);

            // make the part requestable again in _timeoutInMillis
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            synchronized (pieces) {
                                pieces.clear(pieceIndex);
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
}
