import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Vector;

public class PeerProcess {
    public static void debugPrintConfigs() {
        PeerInfoConfig.getInstance().debugPrint();
        CommonConfig.getInstance().debugPrint();
    }

    public static void getPeerInfoConfig() {
        String st;
        Vector<PeerTrackerInfo> peerInfoVector = new Vector<PeerTrackerInfo>();

        try {
            BufferedReader in = new BufferedReader(new FileReader(Helpers.pathToResourcesFolder + "PeerInfo.cfg"));
            while((st = in.readLine()) != null) {

                String[] tokens = st.split("\\s+");

                peerInfoVector.addElement(new PeerTrackerInfo(
                        Integer.parseInt(tokens[0]),
                        tokens[1],
                        Integer.parseInt(tokens[2]),
                        tokens[3].equals("1")
                    )
                );
            }
            in.close();

            // create the config
            PeerInfoConfig.init(peerInfoVector);
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public static void getCommonConfig() {
        String st;
        int i = 1;
        Vector<String> values = new Vector<String>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(Helpers.pathToResourcesFolder + "Common.cfg"));
            while((st = in.readLine()) != null) {
                String[] tokens = st.split("\\s+");
                values.add(tokens[1]);
            }
            in.close();

            // create the config
            CommonConfig.init(Integer.parseInt(values.elementAt(0)),
                    Integer.parseInt(values.elementAt(1)),
                    Integer.parseInt(values.elementAt(2)),
                    values.elementAt(3),
                    Integer.parseInt(values.elementAt(4)),
                    Integer.parseInt(values.elementAt(5))
            );

        }
        catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public static void P2PApp(PeerInfo ourInfo) throws IOException {
        // initialize the process for this process
        Process peer = new Process(ourInfo);

        // if we have the file, split it up into pieces
        if (PeerInfoConfig.getInstance().HasFile(ourInfo.getId()))
            peer.splitFile();

        // build connections to its peers if it has any peers
        peer.buildPeers();

        (new Thread() {
            public void run() { peer.run();
            }}).start();
    }

    // java peerProcess <peerId>
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1) {
            System.out.println("Insufficient arguments: java peerProcess <peerId>");
            return;
        }

        // parse inputs
        int peerId = Integer.parseInt(args[0]);

        // read/load config files
        getPeerInfoConfig();
        getCommonConfig();
        debugPrintConfigs();

        // get our peer info
        PeerTrackerInfo ourTrackerInfo = PeerInfoConfig.getInstance().GetPeerTrackerInfo(peerId);
        PeerInfo ourInfo = ourTrackerInfo.peerInfo;

        // initialize logger
        Logger.init(peerId);

        // start the application
        P2PApp(ourInfo);
    }
}
