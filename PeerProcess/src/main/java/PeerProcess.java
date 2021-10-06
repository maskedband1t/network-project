import java.io.IOException;

public class PeerProcess {

    public static void P2PApp(PeerInfo ourInfo, PeerInfo peerInfo) {
        // initialize the process for this process
        Process peer = new Process(ourInfo);

        // build connections to its peers
        peer.buildPeer(peerInfo);

        (new Thread() {
            public void run() { peer.run();
            }}).start();
    }

    public static void main(String[] args) throws IOException
    {
        // TODO: read and load configuration files
        PeerInfo ourInfo = new PeerInfo(1, "localhost", 4000);

        // TODO: should set up ALL peers before us
        PeerInfo peerInfo = new PeerInfo(2, "localhost", 4001);

        // TODO: set up logger

        // start the application
        // TODO: start up app with ALL peers before us
        P2PApp(ourInfo, peerInfo);
    }
}
