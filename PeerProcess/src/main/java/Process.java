import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class Process implements Runnable {

    // handlers
    private final List<Connection> _connHandlers = new ArrayList<Connection>();
    Hashtable<Integer, Connection> peerConnections = new Hashtable<Integer, Connection>();
    Hashtable<Integer, Bitfield> peerBitfields = new Hashtable<Integer, Bitfield>();

    // other variables
    private PeerInfo peerInfo;
    private boolean shutdown;
    private Bitfield bitfield;

    private FileManager fileManager;
    private PeerManager peerManager;

    // TODO: need to have a list of peers currently connected to

    // TODO: need to save a directory of all peers from config file

    public Process(PeerInfo peerInfo) {
        this.peerInfo = peerInfo;
        this.shutdown = false;
        this.bitfield = new Bitfield(CommonConfig.getInstance().fileSize, CommonConfig.getInstance().pieceSize);

        // TODO: Need to add more to fileMgr and peerMgr constructors
        this.fileManager = new FileManager();
        this.peerManager = new PeerManager();
    }

    public List<Message> sendToPeer(String peerid, String msgtype,
                                    String msgdata) {
        // TODO: send to an existing peer
        throw new NotImplementedException();
    }

    public List<Message> connectAndSend(PeerInfo peerInfo, String msgtype,
                                            String msgdata) {
        // TODO: connect to peer and send them a message
        throw new NotImplementedException();
    }

    public void buildPeer(PeerInfo info) throws IOException {
        // format handshake msg
        String inputString = "P2PFILESHARINGPROJ";
        byte[] peerId = ByteBuffer.allocate(4).putInt(peerInfo.getId()).array();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(inputString.getBytes());
        outputStream.write(new byte[]{0,0,0,0,0,0,0,0,0,0});
        outputStream.write(peerId);
        byte[] handshakeMsg = outputStream.toByteArray();

        // create connection
        Connection c = new Connection(info);
        peerConnections.put(info.getId(), c);
        peerBitfields.put(info.getId(), new Bitfield(CommonConfig.getInstance().fileSize, CommonConfig.getInstance().pieceSize));

        System.out.println("Peer " + peerInfo.getId() + " connected to " + info.getId() + " at " + info.getHost() + ":" + info.getPort());
        Logger.getInstance().madeConnectionWith(info.getId());

        // send handshake
        c.sendHandshake(handshakeMsg);
    }

    public void buildPeers() throws IOException {
        List<PeerInfo> ourPeers = PeerInfoConfig.getInstance().GetPeersToConnectToFor(peerInfo.getId());
        for (PeerInfo peer : ourPeers) {
            System.out.println(peerInfo.getId() + " will connect to " + peer.getId());
        }
        for (PeerInfo peer : ourPeers) {
            System.out.println("Attempting to connect to peer id: " + peer.getId());
            // format handshake msg
            String inputString = "P2PFILESHARINGPROJ";
            byte[] peerId = ByteBuffer.allocate(4).putInt(peerInfo.getId()).array();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(inputString.getBytes());
            outputStream.write(new byte[]{0,0,0,0,0,0,0,0,0,0});
            outputStream.write(peerId);
            byte[] handshakeMsg = outputStream.toByteArray();

            System.out.println("Handshake message created for " + peerInfo.getId() + " -> " + peer.getId() + ": " + handshakeMsg.toString());

            // create connection
            System.out.println("Created connection for localhost:" + peer.getPort());
            Connection c = new Connection(peer.getId(), peer.getPort()); // dev purposes, localhost host assumed
            peerConnections.put(peer.getId(), c);

            System.out.println("Peer " + peerInfo.getId() + " connected to " + peer.getId() + " at " + peer.getHost() + ":" + peer.getPort());
            Logger.getInstance().madeConnectionWith(peer.getId());

            // send handshake
            c.sendHandshake(handshakeMsg);
            System.out.println("Sent handshake");
        }
    }

    private boolean addConnection(Connection conn) {
        if (!_connHandlers.contains(conn)) {
            _connHandlers.add(conn);
            new Thread(conn).start();
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
                    addConnection(new Connection(peerInfo.getId(), peerSocket, fileManager, peerManager));
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
            System.out.println("Shuttding Down");
        }
    }
}
