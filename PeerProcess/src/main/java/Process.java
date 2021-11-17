import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class Process implements Runnable {
    // list of connections per peers
    // we do not map to id because we do not know their id at this scope level
    List<ConnectionHandler> _connHandlers = new ArrayList<ConnectionHandler>();
    // list of bitfields per peer
    Hashtable<Integer, Bitfield> peerBitfields = new Hashtable<Integer, Bitfield>();

    // our info && bitfield && manager objects for files and peers
    private PeerInfo peerInfo;
    private Bitfield bitfield;
    private FileManager fileManager;
    private PeerManager peerManager;

    // whether we should shutdown the program
    private boolean shutdown;

    public Process(PeerInfo peerInfo) {
        this.peerInfo = peerInfo;
        this.shutdown = false;
        this.bitfield = new Bitfield(CommonConfig.getInstance().fileSize, CommonConfig.getInstance().pieceSize);

        // TODO: Need to add more to fileMgr and peerMgr constructors
        this.fileManager = new FileManager(peerInfo.getId());
        this.peerManager = new PeerManager();
    }

    // TODO: do we need this func?
    public List<Message> sendToPeer(String peerid, String msgtype,
                                    String msgdata) {
        // TODO: send to an existing peer
        return new ArrayList<Message>();
    }

    // TODO: do we need this func?
    public List<Message> connectAndSend(PeerInfo peerInfo, String msgtype,
                                            String msgdata) {
        // TODO: connect to peer and send them a message
        return new ArrayList<Message>();
    }

    public void buildPeer(PeerInfo info) throws IOException {
        System.out.println("Attempting to connect to peer id: " + info.getId());
        Socket s = new Socket(info.getHost(), info.getPort());
        Connection c = new Connection(info);
        addConnectionHandler(new ConnectionHandler(peerInfo.getId(), c, fileManager, peerManager, info.getId(), true));
    }

    public void buildPeers() throws IOException {
        List<PeerInfo> ourPeers = PeerInfoConfig.getInstance().GetPeersToConnectToFor(peerInfo.getId());
        for (PeerInfo peer : ourPeers) {
            System.out.println(peerInfo.getId() + " will connect to " + peer.getId());
        }

        // init connection handler for each peer
        for (PeerInfo peer : ourPeers) {
            buildPeer(peer);
        }
    }

    private boolean addConnectionHandler(ConnectionHandler connHdlr) {
        if (!_connHandlers.contains(connHdlr)) {
            _connHandlers.add(connHdlr);
            new Thread(connHdlr).start();
            try {
                wait(10);
            } catch (InterruptedException e) {
            }
        }
        return true;
    }

    public void run() {
        try {
            // listens for other peers to connect to us
            ServerSocket s = new ServerSocket(peerInfo.getPort());

            System.out.println("Peer " + peerInfo.getId() + " is listening on " + peerInfo.getHost() + ":" + peerInfo.getPort());

            while (!shutdown) {
                try {
                    // Every time a peer connects to us, we handle their connection with Handler
                    Socket c = s.accept();
                    c.setSoTimeout(0);

                    // Log
                    Logger.getInstance().receivedConnectionFrom(peerInfo.getId());

                    // Add connection
                    PeerSocket peerSocket = new PeerSocket(c);
                    Connection conn = new Connection(new PeerInfo(), peerSocket);
                    addConnectionHandler(new ConnectionHandler(peerInfo.getId(), conn, fileManager, peerManager));
                }
                catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            System.out.println("Shutting Down");
        }
    }
}
