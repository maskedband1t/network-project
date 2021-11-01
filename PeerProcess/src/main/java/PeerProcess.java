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
            BufferedReader in = new BufferedReader(new FileReader("./../../../resources/main/PeerInfo.cfg"));
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
            BufferedReader in = new BufferedReader(new FileReader("./../../../resources/main/Common.cfg"));
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

    public static void P2PApp(PeerInfo ourInfo, PeerInfo peerInfo) throws IOException {
        // initialize the process for this process
        Process peer = new Process(ourInfo);

        // build connections to its peers if it has any peers
        if (peerInfo != null)
            peer.buildPeer(peerInfo);

        (new Thread() {
            public void run() { peer.run();
            }}).start();
    }

    // TODO: IMPORTANT: at the moment when you run peerProcess, it fails to connect because it tries to build
    //  a socket connection to localhost:4001, but that socket is not up yet
    //  The FIRST peer should only listen, THEN the second peer can connect to the first
    // java peerProcess <peerId> <port> <isFirstPeer>
    public static void main(String[] args) throws IOException
    {
        if (args.length != 3) {
            System.out.println("Insufficient arguments: java peerProcess <peerId> <port> <isFirstPeer>");
            System.out.println("isFirstPeer is true if == 'y' or 'yes'");
            return;
        }

        // parse inputs
        int peerId = Integer.parseInt(args[0]);
        int port = Integer.parseInt(args[1]);
        String isFirstPeer = args[2].toLowerCase(Locale.ROOT);

        // read/load config files
        getPeerInfoConfig();
        getCommonConfig();
        debugPrintConfigs();

        // TODO: use config files

        // TODO: this info should come from config files
        PeerInfo ourInfo = new PeerInfo(peerId, "localhost", port);

        // if we are not the first peer, connect to the peer before us
        // we expect them to have peerId = peerId+1 and port = port + 1
        PeerInfo peerInfo = null;
        if (!isFirstPeer.equals("yes") && !isFirstPeer.equals("y"))  {
            // generate info of our peer from config file
            peerInfo = new PeerInfo(peerId-1, "localhost", port-1);
        }

        // initialize logger
        Logger.init(peerId);

        // start the application
        // TODO: pass in a list of peerInfos instead of a single peerInfo
        P2PApp(ourInfo, peerInfo);
    }
}
