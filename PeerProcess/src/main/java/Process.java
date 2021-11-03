import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Hashtable;
import java.util.List;

public class Process {
    /* MESSAGE TYPES */
    public static final Integer CHOKE = 0;
    public static final Integer UNCHOKE = 1;
    public static final Integer INTERESTED = 2;
    public static final Integer NOTINTERESTED = 3;
    public static final Integer HAVE = 4;
    public static final Integer BITFIELD = 5;
    public static final Integer REQUEST = 6;
    public static final Integer PIECE = 7;
    public static final Integer HANDSHAKE = 8;

    // handlers
    Hashtable<Integer, IHandler> messageHandlers = new Hashtable<Integer,IHandler>();
    Hashtable<Integer, Connection> peerConnections = new Hashtable<Integer, Connection>();

    // other variables
    private PeerInfo peerInfo;
    private boolean shutdown;

    // TODO: need to have a bitfield for file information

    // TODO: need to have a list of peers currently connected to

    // TODO: need to save a directory of all peers from config file

    public Process(PeerInfo peerInfo) {
        this.peerInfo = peerInfo;
        this.shutdown = false;

        // adding handlers for each message type
        messageHandlers.put(CHOKE, new Handlers.ChokeHandler());
        messageHandlers.put(UNCHOKE, new Handlers.UnchokeHandler());
        messageHandlers.put(INTERESTED, new Handlers.InterestedHandler());
        messageHandlers.put(NOTINTERESTED, new Handlers.UninterestedHandler());
        messageHandlers.put(HAVE, new Handlers.HaveHandler());
        messageHandlers.put(BITFIELD, new Handlers.BitfieldHandler());
        messageHandlers.put(REQUEST, new Handlers.RequestHandler());
        messageHandlers.put(PIECE, new Handlers.PieceHandler());

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

            // create connection
            System.out.println("Created connection");
            Connection c = new Connection(peer.getId(), peer.getPort()); // dev purposes, localhost host assumed
            peerConnections.put(peer.getId(), c);

            System.out.println("Peer " + peerInfo.getId() + " connected to " + peer.getId() + " at " + peer.getHost() + ":" + peer.getPort());
            Logger.getInstance().madeConnectionWith(peer.getId());

            // send handshake
            c.sendHandshake(handshakeMsg);
            System.out.println("Sent handshake");
        }
    }

    public void run() {
        try {
            // listens for other peers to connect to us
            ServerSocket s = new ServerSocket(peerInfo.getPort());

            System.out.println("Peer " + peerInfo.getId() + " is listening on " + peerInfo.getHost() + ":" + peerInfo.getPort());

            while (!shutdown) {
                try {
                    // every time a peer connects to us, we handle their connection with Handler
                    Socket c = s.accept();
                    c.setSoTimeout(0);
                    DataInputStream is = new DataInputStream(c.getInputStream());

                    // we can collect ip and port and map to peer id
                    String host = c.getInetAddress().getHostName();
                    int port = c.getPort();
                    PeerInfo peerInfo = PeerInfoConfig.getInstance().GetPeerInfo(host, port);

                    Logger.getInstance().receivedConnectionFrom(peerInfo.getId());

                    // if handshake, make connection for this peer
                    Connection peerConn;
                    // read first 18 bytes, check if handshake
                    byte[] firstFourBytes = new byte[4];
                    is.readFully(firstFourBytes);
                    if (Base64.getEncoder().encodeToString(firstFourBytes).equals("P2PF")) {
                        byte[] restOfHandshake = new byte[28];
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        outputStream.write(firstFourBytes);
                        outputStream.write(restOfHandshake);
                        HandshakeMessage msg = new HandshakeMessage(outputStream.toByteArray());
                        System.out.println("Received handshake from peer id: " + msg.PeerId);
                        if (msg.PeerId != peerInfo.getId()) {
                            System.out.println("Received handshake with Peer Id:" + msg.PeerId + " from someone with id: " + peerInfo.getId());
                            return;
                        }
                        // we return handshake and build the connection
                        buildPeer(peerInfo);
                    }
                    else {
                        // use existing connection
                        peerConn = peerConnections.get(peerInfo.getId());

                        // parse incoming info into a message
                        byte[] incomingMsgType = new byte[1];
                        is.readFully(firstFourBytes);
                        is.readFully(incomingMsgType);
                        int lenAsInt = ByteBuffer.wrap(firstFourBytes).getInt();
                        byte[] incomingMsgPayload = new byte[lenAsInt];
                        is.readFully(incomingMsgPayload);
                        Message incomingMsg = new Message(firstFourBytes, incomingMsgType[0], incomingMsgPayload);

                        // handle msg
                        messageHandlers.get(incomingMsg).handleMsg(incomingMsg, peerConn);
                    }
                }
                catch (SocketTimeoutException e) {
                    System.out.println(e);
                    shutdown = true;
                }
            }
            s.close();
        }
        catch (Exception e) {
            System.out.println(e);
        }
        shutdown = true;
    }
}
