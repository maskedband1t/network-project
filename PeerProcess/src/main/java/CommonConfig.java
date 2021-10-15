import java.util.List;

public class CommonConfig {
    int numPrefNeighbors;
    int unchokingInterval;
    int optimisticUnchokingInterval;
    String fileName;
    long fileSize;
    long pieceSize;

    public CommonConfig(int numPrefNeighbors, int unchokingInterval, int optimisticUnchokingInterval, String fileName, long fileSize, long pieceSize) {
        this.numPrefNeighbors = numPrefNeighbors;
        this.unchokingInterval = unchokingInterval;
        this.optimisticUnchokingInterval = optimisticUnchokingInterval;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.pieceSize = pieceSize;
    }

    public void debugPrint() {
        System.out.println("Common Config");
        System.out.println("----------------");
        System.out.println("NumberOfPreferredNeighbors " + numPrefNeighbors);
        System.out.println("UnchokingInterval " + unchokingInterval);
        System.out.println("OptimisticUnchokingInterval " + optimisticUnchokingInterval);
        System.out.println("FileName " + fileName);
        System.out.println("FileSize " + fileSize);
        System.out.println("PieceSize " + pieceSize);
    }
}

