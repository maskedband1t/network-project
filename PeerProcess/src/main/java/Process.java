import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
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

    // other variables
    private PeerInfo peerInfo;
    private boolean shutdown;

    // TODO: need to have a bitfield for file information

    // TODO: need to have a list of peers currently connected to

    public Process(PeerInfo peerInfo) {
        this.peerInfo = peerInfo;
        this.shutdown = false;
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

    public void buildPeer(PeerInfo peerInfo) {
        // TODO: connect to the peer (this is for initial connections only)
        throw new NotImplementedException();
    }

    public void run() {
        try {
            // listens for other peers to connect to us
            ServerSocket s = new ServerSocket(peerInfo.getPort());

            while (!shutdown) {
                try {
                    // every time a peer connects to us, we handle their connection with Handler
                    Socket c = s.accept();
                    c.setSoTimeout(0);

                    // TODO: implement multiple handlers for handling different types of incoming client messages
                    new Handler(c, peerInfo.getId()).start();
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
