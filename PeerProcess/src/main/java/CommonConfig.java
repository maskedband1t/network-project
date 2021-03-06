public class CommonConfig {
    private static CommonConfig instance = null;
    int numPrefNeighbors;
    int unchokingInterval;
    int optimisticUnchokingInterval;
    String fileName;
    long fileSize;
    long pieceSize;
    int numPieces;

    // Get the singleton
    public static CommonConfig getInstance() {
        if (instance == null) {
            throw new AssertionError("CommonConfig not yet initialized. Try CommonConfig.init(int, int, int, String, long, long).");
        }
        return instance;
    }

    // Init the singleton
    public static void init(int numPrefNeighbors, int unchokingInterval, int optimisticUnchokingInterval, String fileName, long fileSize, long pieceSize) {
        if (instance != null) {
            throw new AssertionError("CommonConfig is already initialized!");
        }
        instance = new CommonConfig(numPrefNeighbors, unchokingInterval, optimisticUnchokingInterval, fileName, fileSize, pieceSize);
    }

    // Constructor used by init(...)
    public CommonConfig(int numPrefNeighbors, int unchokingInterval, int optimisticUnchokingInterval, String fileName, long fileSize, long pieceSize) {
        this.numPrefNeighbors = numPrefNeighbors;
        this.unchokingInterval = unchokingInterval;
        this.optimisticUnchokingInterval = optimisticUnchokingInterval;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.pieceSize = pieceSize;
        this.numPieces = (int)Math.ceil((double)fileSize/pieceSize);
    }

    // Debugging function to print the common configuration
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

