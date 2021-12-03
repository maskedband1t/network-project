import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Process implements Runnable {
    // list of connections per peers
    // we do not map to id because we do not know their id at this scope level
    List<ConnectionHandler> _connHandlers = new ArrayList<ConnectionHandler>();

    // our info && bitfield && manager objects for files and peers
    private PeerInfo peerInfo;
    private FileManager fileManager;
    private PeerManager peerManager;

    // whether we should shutdown the program
    private boolean shutdown;

    // Constructs the Process
    public Process(PeerInfo peerInfo) {
        this.peerInfo = peerInfo;
        this.shutdown = false;
        this.fileManager = new FileManager(peerInfo.getId());
        this.peerManager = new PeerManager(peerInfo.getId());
    }

    // Split the file into pieces
    public void splitFile() {
        fileManager.splitFileIntoPieces();
    }

    // Builds Connection to peer
    public void buildPeer(PeerInfo info) throws IOException {
        System.out.println("Attempting to connect to peer id: " + info.getId());
        Socket s = new Socket(info.getHost(), info.getPort());
        Connection c = new Connection(info);
        addConnectionHandler(new ConnectionHandler(peerInfo, c, fileManager, peerManager, info.getId(), true));
    }

    // Builds Connections to all peers
    public void buildPeers() throws IOException {
        // Gets all peers to connect to
        List<PeerInfo> ourPeers = PeerInfoConfig.getInstance().GetPeersToConnectToFor(peerInfo.getId());

        // Debugging print statement
        System.out.println(peerInfo.getId() + " will connect to ");
        for (PeerInfo peer : ourPeers) {
            System.out.print(peer.getId() + ",");
        }

        // Build connection to each peer
        for (PeerInfo peer : ourPeers) {
            buildPeer(peer);
        }
    }

    // Adds a ConnectionHandler for a remote peer
    synchronized private boolean addConnectionHandler(ConnectionHandler connHdlr) {
        if (!_connHandlers.contains(connHdlr)) {
            _connHandlers.add(connHdlr);
            new Thread(connHdlr).start(); // start handling connection on another thread
            try {
                wait(10);
            } catch (InterruptedException e) {
                e.printStackTrace();;
            }
        }
        return true;
    }

    // The entry point for this thread
    @Override
    public void run() {
        try {
            // Create a Server Socket listener
            ServerSocket s = new ServerSocket(peerInfo.getPort());

            // Debugging print statement
            System.out.println("Peer " + peerInfo.getId() + " is listening on " + peerInfo.getHost() + ":" + peerInfo.getPort());

            // While we are not in shutdown mode (not everyone has the file completed)
            while (!shutdown) {
                try {
                    // Every time a peer connects to us, we handle their connection with Handler
                    Socket c = s.accept();
                    c.setSoTimeout(0);

                    // Add connection - the handler will handle this on a separate thread
                    PeerSocket peerSocket = new PeerSocket(c);

                    // We use a default peer info since we haven't identified who they are yet
                    Connection conn = new Connection(new PeerInfo(), peerSocket);
                    addConnectionHandler(new ConnectionHandler(peerInfo, conn, fileManager, peerManager));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("Shutting Down");
        }
    }
}
