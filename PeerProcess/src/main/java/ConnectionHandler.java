import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionHandler implements Runnable{
    private int _peerId;
    private Connection _conn;
    private FileManager _fileManager;
    private PeerManager _peerManager;
    private boolean _isConnectingPeer;
    private int _remotePeerId;
    private BlockingQueue<Message> _queue = new LinkedBlockingQueue<>();

    public ConnectionHandler(int id, Connection conn, FileManager fileManager, PeerManager peerManager) {
        _peerId = id;
        _conn = conn;
        _remotePeerId = -1;
        _fileManager = fileManager;
        _peerManager = peerManager;
        _isConnectingPeer = false;
    }

    public ConnectionHandler(int id, Connection conn, FileManager fileManager, PeerManager peerManager, int remoteId, boolean srcPeer) {
        _peerId = id;
        _conn = conn;
        _remotePeerId = remoteId;
        _fileManager = fileManager;
        _peerManager = peerManager;
        _isConnectingPeer = srcPeer;
    }

    @Override
    public void run() {
        new ConnectionHelper(_queue, _conn).start();

        try {
            // send handshake
            _conn.sendHandshake(new HandshakeMessage(_peerId));

            // receive handshake, we now identified remote peer
            HandshakeMessage rcvHandshake = _conn.receieveHandshake();
            _remotePeerId = rcvHandshake.getPeerId();

            // TODO: Log successful handshake

            // start handling messages for this connection to the remote peer
            MessageHandler msgHandler = new MessageHandler(_remotePeerId, _fileManager, _peerManager);

            // handle connection
            while (true) {
                try {
                    msgHandler.handle(_conn.receive());
                }
                catch (Exception ex) {
                    System.out.println(ex);
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            _conn.close();
        }
    }
}
