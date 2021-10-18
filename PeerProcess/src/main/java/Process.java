import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Hashtable;
import java.util.List;

public class Process {
    /* MESSAGE TYPES */
    public static final int CHOKE = 0;
    public static final int UNCHOKE = 1;
    public static final int INTERESTED = 2;
    public static final int NOTINTERESTED = 3;
    public static final int HAVE = 4;
    public static final int BITFIELD = 5;
    public static final int REQUEST = 6;
    public static final int PIECE = 7;

    // handlers
    Hashtable <Integer, IHandler> messageHandlers = new Hashtable<Integer,IHandler>();

    // other variables
    private PeerInfo peerInfo;
    private boolean shutdown;
    private Logger logger;

    // TODO: need to have a bitfield for file information

    // TODO: need to have a list of peers currently connected to

    // TODO: need to save a directory of all peers from config file

    public Process(PeerInfo peerInfo) {
        this.peerInfo = peerInfo;
        this.shutdown = false;

        // instantiate logger
        this.logger = new Logger(peerInfo.getId());

        // adding handlers for each message type
        messageHandlers.put(0, new Handlers.ChokeHandler());
        messageHandlers.put(1, new Handlers.UnchokeHandler());
        messageHandlers.put(2, new Handlers.InterestedHandler());
        messageHandlers.put(3, new Handlers.UninterestedHandler());
        messageHandlers.put(4, new Handlers.HaveHandler());
        messageHandlers.put(5, new Handlers.BitfieldHandler());
        messageHandlers.put(6, new Handlers.RequestHandler());
        messageHandlers.put(0, new Handlers.PieceHandler());

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

        System.out.println("Peer " + peerInfo.getId() + " connected to " + info.getId() + " at " + info.getHost() + ":" + info.getPort());
        logger.madeConnectionWith(info.getId());

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
                    
                    // TODO: logger.receivedConnectionFrom(peerId)

                    // TODO: implement multiple handlers for handling different types of incoming client messages
                    new ExampleHandler(c, peerInfo.getId()).start();

                    // TODO: get peerId (and pieceIndex if needed) or move each within handler and provide logger
                    // if 'unchoke' message: logger.unchokedBy(peerId)
                    // if 'choke' message: logger.chokedBy(peerId)
                    // if 'have' message: logger.receivedHaveFrom(peerId, pieceIndex)
                    // if 'interested' message: logger.receivedInterestedFrom(peerId)
                    // if 'not interested' message: logger.receivedNotInterestedFrom(peerId)
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
