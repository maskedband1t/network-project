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
        // TODO: takes in param peerId from args
        PeerInfo ourInfo = new PeerInfo(1, "localhost", 4000);

        // TODO: read and load configuration files

        // TODO: set up logger

        // generate info of our peer from config file
        PeerInfo peerInfo = new PeerInfo(2, "localhost", 4001);

        // start the application
        // TODO: pass in a list of peerInfos instead of a single peerInfo
        P2PApp(ourInfo, peerInfo);
    }
}
