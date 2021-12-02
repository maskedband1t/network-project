import java.io.*;
import java.util.BitSet;

public class FileManager {
    int peerId;
    Bitfield receivedPieces;
    Bitfield requestedPieces;

    // Construct the FileManager
    public FileManager(int peerId) {
        this.peerId = peerId;
        this.receivedPieces = new Bitfield();
        this.requestedPieces = new Bitfield();

        // Create pieces directory if necessary
        File piecesDir = new File(Helpers.pathToResourcesFolder + peerId + "/pieces/");
        piecesDir.mkdirs();
    }

    // Adds the piece to the pieces directory
    public boolean addPiece(int pieceIndex, byte[] piece) {
        // Create the file if necessary
        File file = new File(getPathForPieceIndex(pieceIndex));
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Write the piece contents to the file
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            fos.write(piece);
            fos.flush();
            fos.close();
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }

        // Return success
        return true;
    }

    // Get a copy of receivedPieces, because we are using logical operations on the clone
    public BitSet getReceivedPieces() {
        return (BitSet)receivedPieces.getBits().clone();
    }

    // Get pieces that are available to request
    public BitSet getAvailablePiecesToRequest(BitSet piecesNotRequested) {
        BitSet availablePieces = getReceivedPieces();
        availablePieces.andNot(piecesNotRequested);
        return availablePieces;
    }

    // Get the index of the next piece to request
    public int getPieceToRequest(BitSet piecesNotRequested) {
        // Determine which piece to request
        return requestedPieces.getPieceIndexToRequest(piecesNotRequested);
    }

    // Get the byte array of the piece at an index
    public byte[] getPiece(int pieceIndex) {
        String path = getPathForPieceIndex(pieceIndex);
        return getFile(path);
    }

    // Split the file into pieces
    public void splitFileIntoPieces() {
        // Get the file
        String wholeFilePath = Helpers.pathToResourcesFolder + peerId + "/" + CommonConfig.getInstance().fileName;
        byte[] wholeFile = getFile(wholeFilePath);

        int size = (int)CommonConfig.getInstance().pieceSize;
        int fileSize = (int)CommonConfig.getInstance().fileSize;

        // Write all pieces
        for (int i = 0; i < CommonConfig.getInstance().numPieces; i++) {
            int start = size * i;
            int end = Math.min(start + size, fileSize);
            byte[] pieceSubset = Helpers.getByteSubset(wholeFile, start, end);
            addPiece(i, pieceSubset);
        }
    }

    // Helper function
    // Get the byte array of a file at a path
    private byte[] getFile(String path){
        // Get the file at the given path
        File file = new File(path);

        // Validate it exists
        try {
            if (!file.exists())
                throw new IOException("File doesn't exist");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Read the contents of the file
        int fileLength = (int)file.length();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] bytes = new byte[fileLength];
            fis.read(bytes, 0, fileLength);
            fis.close();
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Close the file stream
        finally {
            if (fis != null) {
                try {
                    fis.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    // Helper function
    // Get the string path for a piece index
    private String getPathForPieceIndex(int pieceIndex) {
        return Helpers.pathToResourcesFolder + peerId + "/pieces/" + pieceIndex;
    }
}
