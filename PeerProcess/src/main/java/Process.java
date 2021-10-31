import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
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
        Connection c = new Connection(info);
        peerConnections.put(info.getId(), c);

        System.out.println("Peer " + peerInfo.getId() + " connected to " + info.getId() + " at " + info.getHost() + ":" + info.getPort());
        Logger.getInstance().madeConnectionWith(info.getId());

        // TODO: create a list of currently connected peers and add this built connection to that list
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
                    PeerInfo peerInfo = PeerInfoConfig.GetPeerInfo(host, port);

                    Logger.getInstance().receivedConnectionFrom(peerInfo.getId());

                    // if handshake, make connection for this peer
                    Connection peerConn;
                    // TODO: detect handshakes
                    if (true) {//(IsHandshake()) {
                        peerConn = new Connection(peerInfo);
                        peerConnections.put(peerInfo.getId(), peerConn);
                    }
                    else {
                        // use existing connection
                        peerConn = peerConnections.get(peerInfo.getId());

                        // parse incoming info into a message
                        byte[] incomingMsgLen = new byte[4];
                        byte[] incomingMsgType = new byte[1];
                        is.readFully(incomingMsgLen);
                        is.readFully(incomingMsgType);
                        int lenAsInt = ByteBuffer.wrap(incomingMsgLen).getInt();
                        byte[] incomingMsgPayload = new byte[lenAsInt];
                        is.readFully(incomingMsgPayload);
                        Message incomingMsg = new Message(incomingMsgLen, incomingMsgType[0], incomingMsgPayload);

                        // handle msg
                        messageHandlers.get(incomingMsg).handleMsg(incomingMsg, peerConn);
                    }
                }
                catch (SocketTimeoutException e) {
                    continue;
                }
            }
            s.close();
        }
        catch (Exception e) {
        }
        shutdown = true;
    }
}
