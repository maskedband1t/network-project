import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

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

        // Delete file if necessary
        if (!peerInfo.getFileComplete()) {
            File f = new File(Helpers.pathToResourcesFolder + peerId + CommonConfig.getInstance().fileName);
            if (f.exists()) {
                f.delete();
                Logger.getInstance().dangerouslyWrite("Deleted " + CommonConfig.getInstance().fileName);
            }
        }

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
            receivedPieces.getBits().set(pieceIndex);
            Logger.getInstance().dangerouslyWrite("(1.2) Adding " + pieceIndex);
            addPieceBytes(pieceIndex, piece);
        }

        // Return success
        return true;
    }

    // Adds the piece to the pieces directory, with force ability
    public synchronized boolean addPiece(int pieceIndex, byte[] piece, boolean force) {
        // True if we do not have this piece
        final boolean isNewPiece = !receivedPieces.getBits().get(pieceIndex);

        if (force || isNewPiece) {
            receivedPieces.getBits().set(pieceIndex);
            addPieceBytes(pieceIndex, piece);
        }

        // Check if we are done
        if (haveAllPieces()) {
            try{
                process.complete();
            }
            catch (IOException e){
                Helpers.println("process.complete had some issues");
            }
        }

        // Return success
        return true;
    }

    // Checks and handles completion
    public void handleDone() {
        // Check if we are done
        if (haveAllPieces()) {
            Logger.getInstance().dangerouslyWrite("(4.1) We are done!");
            try{
                process.complete();
            }
            catch(IOException e){
                Helpers.println("problem with process.complete via handleDone");
            }
        }
    }

    // Checks if we have all pieces
    private boolean haveAllPieces() {
        if (!peerInfo.is_file_complete()) {
            BitSet set = receivedPieces.getBits();
            if (set.length() < CommonConfig.getInstance().numPieces) {
                return false;
            }
            else
                return true;
            /*for (int i = 0; i < CommonConfig.getInstance().numPieces; i++) {
                if (!set.get(i)) {
                    Logger.getInstance().dangerouslyWrite("(haveAllPieces) We do not have piece " + i + " here is the bitfield: " + receivedPieces.toString());
                    return false;
                }
            }
            return true;*/
        }
        return true;
    }

    // Get a copy of receivedPieces, because we are using logical operations on the clone
    public synchronized Bitfield getReceivedPieces() {
        return (Bitfield)receivedPieces.clone();
    }

    // Get the index of the next piece to request
    public synchronized int getPieceToRequest(Bitfield remotePieces) {
        BitSet remotePiecesBitset = (BitSet)remotePieces.getBits().clone(); // remote pieces
        remotePiecesBitset.andNot(receivedPieces.getBits()); // remote pieces that we don't have
        //remotePiecesBitset.andNot(requestedPieces.getBits()); // remote pieces that we don't have and haven't requested

        if (!remotePiecesBitset.isEmpty()) {
            // Request a piece that we do not have, that we haven't requested yet
            // Random Selection Strategy if there are multiple choices
            // Ex: We are Peer A, and are requesting a Piece from Peer B
            //     We will randomly select a Piece to request from Peer B
            //     of the pieces we do not have, and have not requested

            String str = remotePiecesBitset.toString();
            Logger.getInstance().dangerouslyWrite("Request indices to choose from: " + str);
            Helpers.println("Request indices to choose from: " + str);
            Helpers.println("Their pieces: " + remotePieces.getBits().toString());
            Helpers.println("Our pieces: " + receivedPieces.getBits().toString());
            Helpers.println("Requested pieces: " + receivedPieces.getBits().toString());

            // "1,0,1,1" -> ["1","0","1" "1"]
            String[] indexes = str.substring(1, str.length()-1).split(",");
            // Get random index, trim commas off, parse into int
            int pieceIndex = Integer.parseInt(indexes[(int)(Math.random()*(indexes.length-1))].trim());

            Logger.getInstance().dangerouslyWrite("GOT PIECE TO REQUEST: " + pieceIndex);

            // since we're going to return this value, update that we will request this index
            BitSet requestedPiecesBitset = requestedPieces.getBits();
            requestedPiecesBitset.set(pieceIndex);

            // make the part requestable again in unchokingInterval * 2000 ms
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            synchronized (requestedPiecesBitset) {
                                if (!receivedPieces.getBits().get(pieceIndex)) {
                                    requestedPiecesBitset.clear(pieceIndex);
                                    Logger.getInstance().dangerouslyWrite("Reset request status for piece " + pieceIndex);
                                }
                            }
                        }
                    },
                    CommonConfig.getInstance().unchokingInterval * 1500L
            );
            // return the index of the piece to request
            Helpers.println("We choose index: " + pieceIndex);
            Logger.getInstance().dangerouslyWrite("REQUESTING " + pieceIndex);
            return pieceIndex;
        }
        // default
        Helpers.println("WE COULD NOT FIND AN INDEX TO REQUEST!!");
        return -1;
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

    // Merge the pieces into the file
    public void mergePiecesIntoFile() {
        String filePath = Helpers.pathToResourcesFolder + peerId + "/" + CommonConfig.getInstance().fileName;
        String piecesPath = Helpers.pathToResourcesFolder + peerId + "/pieces/";
        int size = CommonConfig.getInstance().numPieces - 1;

        File ofile = new File(filePath);
        FileOutputStream fos;
        FileInputStream fis;
        byte[] fileBytes;
        int bytesRead = 0;
        List<File> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new File(piecesPath + i));
        }
        try {
            fos = new FileOutputStream(ofile);
            for (File file : list) {
                fis = new FileInputStream(file);
                fileBytes = new byte[(int) file.length()];
                bytesRead = fis.read(fileBytes, 0, (int) file.length());
                assert (bytesRead == fileBytes.length);
                assert (bytesRead == (int) file.length());
                fos.write(fileBytes);
                fos.flush();
                fileBytes = null;
                fis.close();
                fis = null;
            }
            fos.close();
            fos = null;
        } catch (Exception exception) {
            exception.printStackTrace();
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
