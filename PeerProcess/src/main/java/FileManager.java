import java.io.*;
import java.nio.ByteBuffer;
import java.util.BitSet;

public class FileManager {
    int peerId;
    BitSet receivedPieces;
    RequestedPiecesBitSet requestedPieces;
    int numPieces;

    public FileManager(int peerId) {
        this.peerId = peerId;
        this.numPieces = (int)Math.ceil(CommonConfig.getInstance().fileSize/CommonConfig.getInstance().pieceSize);
        this.receivedPieces = new BitSet(numPieces);
        this.requestedPieces = new RequestedPiecesBitSet(numPieces);

        // create pieces dir if necessary
        File piecesDir = new File(Helpers.pathToResourcesFolder + peerId + "/pieces/");
        piecesDir.mkdirs();
    }

    public boolean addPiece(int pieceIndex, byte[] piece) {
        // output piece as a file piece
        File file = new File(getPathForPieceIndex(pieceIndex));
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    public byte[] getPiece(int pieceIndex) {
        String path = getPathForPieceIndex(pieceIndex);
        return getFile(path);
    }

    public byte[] getFile(String path){
        File file = new File(path);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private String getPathForPieceIndex(int pieceIndex) {
        return Helpers.pathToResourcesFolder + peerId + "/pieces/" + pieceIndex;
    }

    public void splitFileIntoPieces() {
        // get the file
        String wholeFilePath = Helpers.pathToResourcesFolder + peerId + "/" + CommonConfig.getInstance().fileName;
        byte[] wholeFile = getFile(wholeFilePath);
        byte[] currPieceBytes;
        int size = (int)CommonConfig.getInstance().pieceSize;
        for (int i = 0; i < numPieces; i++) {
            int start = size * i;
            int end = start + size;
            byte[] pieceSubset = Helpers.getByteSubset(wholeFile, start, end);
            addPiece(i, pieceSubset);
        }
    }
}
