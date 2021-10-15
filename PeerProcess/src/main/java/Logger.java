import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Logger {
    private int peerId;
    private File logFile;
    
    public Logger(int peerId) {
        this.peerId = peerId;
        try {
            logFile = new File("log_peer_" + peerId + ".log");
            logFile.createNewFile();
        }
        catch (IOException e) {
            System.out.println("An IO error occurred.");
            e.printStackTrace();
        }
    }

    private boolean writeToLog(String action) {
        try {
            // Write data to log file, prefaced with the date/time
            BufferedWriter bf = new BufferedWriter(new FileWriter(logFile, true));
            bf.write(java.time.LocalDateTime.now() + ": Peer " + peerId + action);
            bf.newLine();
            bf.close();
        }
        catch (IOException e) {
            System.out.println("An IO error occurred.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean madeConnectionWith(int partnerId) {
        return writeToLog(" makes a connection to Peer " + partnerId + ".");
    }

    public boolean receivedConnectionFrom(int partnerId) {
        return writeToLog(" is connected from Peer " + partnerId + ".");
    }

    public boolean preferredNeighbors(List<Integer> ids) {
        try {
            // Generate comma-separated string of preferred neighbor ids
            String idsString = ids.toString();
            idsString = idsString.substring(1, idsString.length() - 1);
    
            return writeToLog(" has the preferred neighbors " + idsString + ".");
        }
        catch (StringIndexOutOfBoundsException e) {
            System.out.println("An indexing error occurred.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean optimisticallyUnchokedNeighbor(int partnerId) {
        return writeToLog(" has the optimistically unchoked neighbor " + partnerId + ".");
    }

    public boolean unchokedBy(int partnerId) {
        return writeToLog(" is unchoked by " + partnerId + ".");
    }

    public boolean chokedBy(int partnerId) {
        return writeToLog(" is choked by " + partnerId + ".");
    }

    public boolean receivedInterestedFrom(int partnerId) {
        return writeToLog(" received the 'interested' message from " + partnerId + ".");
    }

    public boolean receivedNotInterestedFrom(int partnerId) {
        return writeToLog(" received the 'not interested' message from " + partnerId + ".");
    }

    public boolean downloadedPiece(int partnerId, int pieceIndex, int pieceCount) {
        return writeToLog(" has downloaded the piece " + pieceIndex + " from " + partnerId + ". Now the number of pieces it has is " + pieceCount + ".");
    }

    public boolean completedDownload() {
        return writeToLog(" has downloaded the complete file.");
    }
}