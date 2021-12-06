import java.io.*;
import java.util.BitSet;

public class FileManager {
    int peerId;
    PeerInfo peerInfo;
    Bitfield receivedPieces;
    Bitfield requestedPieces;
    private Process process = null;

    // Construct the FileManager
    public FileManager(PeerInfo peerInfo) {
        this.peerInfo = peerInfo;
        this.peerId = peerInfo.getId();
        this.receivedPieces = peerInfo.getBitfield();
        this.requestedPieces = new Bitfield();

        // Reset pieces directory if necessary
        File piecesDir = new File(Helpers.pathToResourcesFolder + peerId + "/pieces/");
        if (piecesDir.exists())
            Logger.getInstance().dangerouslyWrite("Deleted " + deleteFolder(piecesDir, 0) + " pieces");
        piecesDir.mkdirs();
    }

    // Delete folder
    public static int deleteFolder(File folder, int numDeleted) {
        File[] files = folder.listFiles();
        if(files != null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    numDeleted = deleteFolder(f, numDeleted+1);
                } else {
                    f.delete();
                    numDeleted += 1;
                }
            }
        }
        folder.delete();
        return numDeleted;
    }

    // Registers process
    public void registerProcess(Process proc) {
        this.process = proc;
    }

    // Actually writes piece to a piece file
    public void addPieceBytes(int pieceIndex, byte[] piece) {
        // Create the file if necessary
        File file = new File(getPathForPieceIndex(pieceIndex));
        try {
            Logger.getInstance().dangerouslyWrite("(1.2.1) Creating file for " + pieceIndex + " if necessary.");
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
            Logger.getInstance().dangerouslyWrite("(1.2.2) Wrote to file for " + pieceIndex + ".");

            // if successful, let process know we got this piece successfully
            process.receivedPiece(pieceIndex);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Adds the piece to the pieces directory
    public synchronized boolean addPiece(int pieceIndex, byte[] piece) {
        // True if we do not have this piece
        final boolean isNewPiece = !receivedPieces.getBits().get(pieceIndex);
        Logger.getInstance().dangerouslyWrite("(1.1) Is " + pieceIndex + " a a new piece?: " + isNewPiece);

        if (isNewPiece) {
            Logger.getInstance().dangerouslyWrite("(1.2) Adding " + pieceIndex);
            addPieceBytes(pieceIndex, piece);
        }

        // Check if we are done
        if (haveAllPieces()) {
            Logger.getInstance().dangerouslyWrite("(1.3) We have all pieces");
            process.complete();
        }

        // Return success
        return true;
    }

    // Adds the piece to the pieces directory, with force ability
    public synchronized boolean addPiece(int pieceIndex, byte[] piece, boolean force) {
        // True if we do not have this piece
        final boolean isNewPiece = !receivedPieces.getBits().get(pieceIndex);

        if (force || isNewPiece) {
            addPieceBytes(pieceIndex, piece);
        }

        // Check if we are done
        if (haveAllPieces()) {
            process.complete();
        }

        // Return success
        return true;
    }

    // Checks if we have all pieces
    private boolean haveAllPieces() {
        BitSet set = receivedPieces.getBits();
        for (int i = 0; i < set.size(); i++) {
            if (!set.get(i)) {
                return false;
            }
        }
        Logger.getInstance().dangerouslyWrite("ALL PIECES HAD");
        return true;
    }

    // Get a copy of receivedPieces, because we are using logical operations on the clone
    public synchronized Bitfield getReceivedPieces() {
        return (Bitfield)receivedPieces.clone();
    }

    // Get pieces that are available to request
    public BitSet getAvailablePiecesToRequest(BitSet piecesNotRequested) {
        BitSet availablePieces = getReceivedPieces().getBits();
        availablePieces.andNot(piecesNotRequested);
        return availablePieces;
    }

    // Get the index of the next piece to request
    public synchronized int getPieceToRequest(BitSet piecesNotRequested) {
        // Determine which piece to request
        int test = requestedPieces.getPieceIndexToRequest(piecesNotRequested);
        Logger.getInstance().dangerouslyWrite("REQUESTING " + test);
        return test;
    }

    public synchronized boolean hasPiece(int pieceIndex) {
        return receivedPieces.getBits().get(pieceIndex);
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
            addPiece(i, pieceSubset, true);
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
