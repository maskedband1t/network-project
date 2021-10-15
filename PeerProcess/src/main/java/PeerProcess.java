import java.io.IOException;
import java.util.Locale;

public class PeerProcess {

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

        // TODO: read/load config files
        // TODO: this info should come from config files
        PeerInfo ourInfo = new PeerInfo(peerId, "localhost", port);

        // if we are not the first peer, connect to the peer before us
        // we expect them to have peerId = peerId+1 and port = port + 1
        PeerInfo peerInfo = null;
        if (!isFirstPeer.equals("yes") && !isFirstPeer.equals("y"))  {
            // generate info of our peer from config file
            peerInfo = new PeerInfo(peerId-1, "localhost", port-1);
        }

        // TODO: set up logger

        // start the application
        // TODO: pass in a list of peerInfos instead of a single peerInfo
        P2PApp(ourInfo, peerInfo);
    }
}
