import java.io.*;
import java.nio.ByteBuffer;
import java.util.BitSet;

public class FileManager {
    int peerId;
    BitSet receivedPieces;
    RequestedPiecesBitSet requestedPieces;

    public FileManager(int peerId) {
        this.peerId = peerId;
        int numPieces = (int)Math.ceil(CommonConfig.getInstance().fileSize/CommonConfig.getInstance().pieceSize);
        this.receivedPieces = new BitSet(numPieces);
        this.requestedPieces = new RequestedPiecesBitSet(numPieces);
    }

    public boolean addPiece(int pieceIndex, byte[] piece) {
        // output piece as a file piece
        File file = getFileForPieceIndex(pieceIndex);
        // make dir if not made
        file.mkdirs();

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            fos.write(piece);
            fos.flush();
            fos.close();
        } catch(Exception e) {
            System.out.println(e);
            return false;
        }

        // success
        return true;
    }

    // We are going to return a copy of receivedPieces, because we are using logical operations on the clone
    public BitSet getReceivedPieces() {
        return (BitSet)receivedPieces.clone();
    }

    public byte[] getPiece(int pieceIndex) {
        File file = getFileForPieceIndex(pieceIndex);
        int fileLength = (int)file.length();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] bytes = new byte[fileLength];
            fis.read(bytes, 0, fileLength);
            fis.close();
            return bytes;
        } catch (Exception e) {
            System.out.println(e);
        }
        // make sure close stream
        finally {
            if (fis != null) {
                try {
                    fis.close();
                }
                catch (IOException ex) {}
            }
        }
        return null;
    }

    public BitSet getAvailablePiecesToRequest(BitSet piecesNotRequested) {
        BitSet availablePieces = getReceivedPieces();
        availablePieces.andNot(piecesNotRequested);
        return availablePieces;
    }

    public int getPieceToRequest(BitSet piecesNotRequested) {
        BitSet availablePieces = getAvailablePiecesToRequest(piecesNotRequested);

        // determine which piece to request based off of which ones we haven't requested yet,
        // and which ones are available from the remote peer
        return requestedPieces.getPieceIndexToRequest(piecesNotRequested);
    }

    private String getPathForPieceIndex(int pieceIndex) {
        return "./peer_" + peerId + "/pieces/" + pieceIndex;
    }

    private File getFileForPieceIndex(int pieceIndex) {
        return new File(getPathForPieceIndex(pieceIndex));
    }
}
