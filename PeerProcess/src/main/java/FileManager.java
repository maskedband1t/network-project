import java.io.*;
import java.util.BitSet;

public class FileManager {
    int peerId;
    Bitfield receivedPieces;
    Bitfield requestedPieces;
    private Process process = null;

    // Construct the FileManager
    public FileManager(int peerId) {
        this.peerId = peerId;
        this.receivedPieces = new Bitfield();
        this.requestedPieces = new Bitfield();

        // Create pieces directory if necessary
        File piecesDir = new File(Helpers.pathToResourcesFolder + peerId + "/pieces/");
        piecesDir.mkdirs();
    }

    // Registers process
    public synchronized void registerProcess(Process proc) {
        this.process = proc;
    }

    // Adds the piece to the pieces directory
    public synchronized boolean addPiece(int pieceIndex, byte[] piece) {
        // True if we do not have this piece
        final boolean isNewPiece = !receivedPieces.getBits().get(pieceIndex);

        if (isNewPiece) {
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

                // if successful, let process know we got this piece successfully
                process.receivedPiece(pieceIndex);
            } catch(Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        // Check if we are done
        if (haveAllPieces()) {
            process.complete();
        }

        // Return success
        return true;
    }

    // Checks if we have all pieces
    private synchronized boolean haveAllPieces() {
        BitSet set = receivedPieces.getBits();
        for (int i = 0; i < set.size(); i++) {
            if (!set.get(i)) {
                return false;
            }
        }
        return true;
    }

    // Get a copy of receivedPieces, because we are using logical operations on the clone
    public synchronized Bitfield getReceivedPieces() {
        return (Bitfield)receivedPieces.clone();
    }

    // Get pieces that are available to request
    public synchronized BitSet getAvailablePiecesToRequest(BitSet piecesNotRequested) {
        BitSet availablePieces = getReceivedPieces().getBits();
        availablePieces.andNot(piecesNotRequested);
        return availablePieces;
    }

    // Get the index of the next piece to request
    public synchronized int getPieceToRequest(BitSet piecesNotRequested) {
        // Determine which piece to request
        return requestedPieces.getPieceIndexToRequest(piecesNotRequested);
    }

    // Get the byte array of the piece at an index
    public synchronized byte[] getPiece(int pieceIndex) {
        String path = getPathForPieceIndex(pieceIndex);
        return getFile(path);
    }

    // Split the file into pieces
    public synchronized void splitFileIntoPieces() {
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
    private synchronized byte[] getFile(String path){
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
