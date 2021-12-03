import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Logger {
    private static Logger instance = null;
    private int peerId;
    private File logFile;

    // Get the singleton
    public static Logger getInstance() {
        if (instance == null) {
            throw new AssertionError("Logger not yet initialized. Try logger.init().");
        }
        return instance;
    }

    // Init the singleton
    public static void init(int peerId) {
        if (instance != null) {
            throw new AssertionError("Logger already initialized!");
        }

        instance = new Logger(peerId);
    }

    // Constructor used by init(...)
    private Logger(int peerId) {
        this.peerId = peerId;
        try {
            logFile = new File(Helpers.pathToResourcesFolder + "log_peer_" + peerId + ".log");
            if (logFile.exists()) {
                System.out.println("log_peer_" + peerId + " exists, deleting it!");
                logFile.delete();
            }
            logFile.createNewFile();
        }
        catch (IOException e) {
            System.err.println("An IO error occurred.");
            e.printStackTrace();
        }
    }

    // Helper function to write logs with the expected format
    private boolean writeToLog(String action) {
        try {
            // Write data to log file, prefaced with the date/time
            BufferedWriter bf = new BufferedWriter(new FileWriter(logFile, true));
            bf.write(java.time.LocalDateTime.now() + ": Peer " + peerId + " " + action);
            bf.newLine();
            bf.close();
        }
        catch (IOException e) {
            System.err.println("An IO error occurred.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Directly write to log
    public boolean dangerouslyWrite(String text) {
        try {
            BufferedWriter bf = new BufferedWriter(new FileWriter(logFile, true));
            bf.write(java.time.LocalDateTime.now() + ": " + text);
            bf.newLine();
            bf.close();
        }
        catch (IOException e) {
            System.err.println("An IO error occurred");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Log after handshake
    public boolean connectedWith(int partnerId, boolean connectingPeer) {
        if (connectingPeer) return writeToLog("makes a connection to Peer " + partnerId + ".");
        else return writeToLog("is connected from Peer " + partnerId + ".");
    }

    // Log stating this peer's preferred neighbors
    public boolean preferredNeighbors(List<Integer> ids) {
        try {
            // Generate comma-separated string of preferred neighbor ids
            String idsString = ids.toString();
            idsString = idsString.substring(1, idsString.length() - 1);
    
            return writeToLog("has the preferred neighbors " + idsString + ".");
        }
        catch (StringIndexOutOfBoundsException e) {
            System.err.println("An indexing error occurred.");
            e.printStackTrace();
            return false;
        }
    }

    // Log stating this peer's optimistically unchoked neighbor
    public boolean optimisticallyUnchokedNeighbor(int partnerId) {
        return writeToLog("has the optimistically unchoked neighbor " + partnerId + ".");
    }

    // Log after receiving Unchoke message from remote peer
    public boolean unchokedBy(int partnerId) {
        return writeToLog("is unchoked by " + partnerId + ".");
    }

    // Log after receiving Choke message from remote peer
    public boolean chokedBy(int partnerId) {
        return writeToLog("is choked by " + partnerId + ".");
    }

    // Log after receiving Have message from remote peer
    public boolean receivedHaveFrom(int partnerId, int pieceIndex) {
        return writeToLog("received the 'have' message from " + partnerId + " for the piece " + pieceIndex + ".");
    }

    // Log after receiving Interested message from remote peer
    public boolean receivedInterestedFrom(int partnerId) {
        return writeToLog("received the 'interested' message from " + partnerId + ".");
    }

    // Log after receiving Not Interested message from remote peer
    public boolean receivedNotInterestedFrom(int partnerId) {
        return writeToLog("received the 'not interested' message from " + partnerId + ".");
    }

    // Log after receiving Bitfield message from remote peer
    public boolean receivedBitfieldFrom(int partnerId) {
        return writeToLog("received the 'bitfield' message from " + partnerId + ".");
    }

    // Log after receiving Request message from remote peer
    public boolean receivedRequestFrom(int partnerId, int pieceIndex) {
        return writeToLog("received the 'request' message from " + partnerId + " for the piece " + pieceIndex + ".");
    }

    // Log after receiving Piece message from remote peer
    public boolean downloadedPiece(int partnerId, int pieceIndex, int pieceCount) {
        return writeToLog("has downloaded the piece " + pieceIndex + " from " + partnerId + ". Now the number of pieces it has is " + pieceCount + ".");
    }

    // Log after this peer has completed downloaded the file
    public boolean completedDownload() {
        return writeToLog("has downloaded the complete file.");
    }
}
