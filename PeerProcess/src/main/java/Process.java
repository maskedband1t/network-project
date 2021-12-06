import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class Process implements Runnable {
    // list of connections per peers
    // we do not map to id because we do not know their id at this scope level
    List<ConnectionHandler> _connHandlers = new ArrayList<ConnectionHandler>();

    // our info && bitfield && manager objects for files and peers
    private PeerInfo peerInfo;
    private FileManager fileManager;
    private PeerManager peerManager;

    // whether we should shutdown the program
    public static boolean shutdown;

    // whether neighbors are done
    private AtomicBoolean _peers_file_complete;

    // Constructs the Process
    public Process(PeerInfo peerInfo) {
        this.peerInfo = peerInfo;
        this.shutdown = false;
        this.fileManager = new FileManager(peerInfo);
        this.peerManager = new PeerManager(peerInfo.getId());
        this._peers_file_complete = new AtomicBoolean(false);
    }

    // Split the file into pieces
    public void splitFile() {
        fileManager.splitFileIntoPieces();
    }

    // Initialize PeerManager
    public void initPeerManager() {
        // Run the peer manager on a thread
        if (this.peerManager != null) {
            this.peerManager.registerProcess(this);
            (new Thread() {
                public void run() {
                    peerManager.run();
                }
            }).start();
        }
    }

    // Initialize FileManager
    public void initFileManager() {
        if (this.fileManager != null)
            this.fileManager.registerProcess(this);
    }

    // Builds Connection to peer
    public void buildPeer(PeerInfo info) throws IOException {
        System.out.println("Attempting to connect to peer id: " + info.getId());
        Connection c = new Connection(info);
        System.out.println("ADDING CONNECTION HANDLER [BUILDING NEW CONNECTION]");
        addConnectionHandler(new ConnectionHandler(peerInfo, c, fileManager, peerManager, info, true));
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
    private synchronized boolean addConnectionHandler(ConnectionHandler connHdlr) {
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

    // choke peers
    public synchronized void choke_peers(Set<Integer> peers) throws IOException{
        for (ConnectionHandler ch : _connHandlers)
            if (peers.contains(ch.getRemotePeerId())) {
                //System.out.println("Choking: " + ch.getRemotePeerId());
                ch.send(new Message(Helpers.CHOKE, new byte[]{}));
            }
    }

    // unchoke peers
    public synchronized void unchoke_peers(Set<Integer> peers) throws IOException {
        for (ConnectionHandler ch : _connHandlers) {
            if (peers.contains(ch.getRemotePeerId())) {
                //System.out.println("Unchoking: " + ch.getRemotePeerId());
                ch.send(new Message(Helpers.UNCHOKE, new byte[]{}));
            }
        }
    }

    // Handle when a piece arrives
    public void receivedPiece(int pieceIndex) throws IOException {
        for (ConnectionHandler ch : _connHandlers) {
            Logger.getInstance().dangerouslyWrite("(1.2.3) Letting " + ch.getRemotePeerId() + " know that we have piece " + pieceIndex);
            ch.send(new Message(Helpers.HAVE, Helpers.intToBytes(pieceIndex, 4)));
            if (!peerManager.isPeerInteresting(ch.getRemotePeerId(), fileManager.getReceivedPieces())) {
                ch.send(new Message(Helpers.NOTINTERESTED, new byte[]{}));
            }
        }
    }

    public void neighborsComplete() {
        _peers_file_complete.set(true);
        if (peerInfo.getFileComplete() && _peers_file_complete.get()) {
            Logger.getInstance().completedDownload();
            shutdown = true;
            System.exit(0);
        }
    }

    // Handle when the file is complete
    public synchronized void complete() {
        Logger.getInstance().completedDownload();
        peerInfo.set_file_complete(true);
        if (peerInfo.getFileComplete() && _peers_file_complete.get()) {
            Logger.getInstance().completedDownload();
            shutdown = true;
            System.exit(0);
        }
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

                    // Add connection - the handler will handle this on a separate thread
                    PeerSocket peerSocket = new PeerSocket(c);

                    // We use a default peer info since we haven't identified who they are yet
                    Connection conn = new Connection(new PeerInfo(), peerSocket);
                    System.out.println("ADDING CONNECTION HANDLER [DETECTED NEW CONNECTION]");
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
