import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Vector;

public class PeerProcess {
    // Debugging purposes, print out the PeerInfoConfig and CommonConfig
    public static void debugPrintConfigs() {
        PeerInfoConfig.getInstance().debugPrint();
        CommonConfig.getInstance().debugPrint();
    }

    // Parse and store the PeerInfoConfig
    // IMPORTANT: we expect the configurations to be in the relative path: ../../../resources/main/
    public static void getPeerInfoConfig() {
        String st;
        Vector<PeerInfo> peerInfoVector = new Vector<PeerInfo>();

        try {
            BufferedReader in = new BufferedReader(new FileReader(Helpers.pathToResourcesFolder + "PeerInfo.cfg"));
            // For each line, split into tokens and store as a PeerInfo object
            while((st = in.readLine()) != null) {
                String[] tokens = st.split("\\s+");

                if (tokens.length != 4)
                    throw new InputMismatchException("The given PeerInfo.cfg does not have the expected format <id> <hostname> <port> <hasFile>");

                peerInfoVector.addElement(new PeerInfo(
                        Integer.parseInt(tokens[0]),
                        tokens[1],
                        Integer.parseInt(tokens[2]),
                        tokens[3].equals("1")
                ));
            }
            in.close();

            // Create the PeerInfoConfig
            PeerInfoConfig.init(peerInfoVector);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Parse and store the COmmonConfig
    // IMPORTANT: we expect the configurations to be in the relative path: ../../../resources/main/
    public static void getCommonConfig() {
        String st;
        int i = 1;
        Vector<String> values = new Vector<String>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(Helpers.pathToResourcesFolder + "Common.cfg"));
            // For each line, store the value
            while((st = in.readLine()) != null) {
                String[] tokens = st.split("\\s+");
                values.add(tokens[1]);
            }
            in.close();

            // Create the config
            CommonConfig.init(Integer.parseInt(values.elementAt(0)),
                    Integer.parseInt(values.elementAt(1)),
                    Integer.parseInt(values.elementAt(2)),
                    values.elementAt(3),
                    Integer.parseInt(values.elementAt(4)),
                    Integer.parseInt(values.elementAt(5))
            );
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Starts the Process for this peer
    public static void P2PApp(PeerInfo ourInfo) throws IOException {
        // Initialize the process for this process
        Process peer = new Process(ourInfo);

        // If we have the file, split it up into pieces
        if (PeerInfoConfig.getInstance().HasFile(ourInfo.getId()))
            peer.splitFile();

        // Run the peer manager
        peer.initPeerManager();

        // Build connections to its peers if it has any peers
        peer.buildPeers();

        // Run the peer on a thread
        (new Thread() {
            public void run() { peer.run();
            }}).start();
    }

    // Entry point for the entire project, one per peer
    public static void main(String[] args) throws IOException
    {
        // Must of format: java peerProcess <peerId>
        if (args.length != 1) {
            System.out.println("Insufficient arguments: java peerProcess <peerId>");
            return;
        }

        // Parse inputs
        int peerId = Integer.parseInt(args[0]);

        // Read/load config files
        // Note: Common config must be loaded first, because we require a parameter in it for peer info loading
        getCommonConfig();
        getPeerInfoConfig();

        // Debugging purposes, print out the configuration files
        debugPrintConfigs();

        // Get this peer's PeerInfo
        PeerInfo ourInfo = PeerInfoConfig.getInstance().GetPeerInfo(peerId);

        // Update bitfield for this peer
        ourInfo.initBitfield();

        // Initialize logger
        Logger.init(peerId);

        // Start the application
        P2PApp(ourInfo);
    }
}
