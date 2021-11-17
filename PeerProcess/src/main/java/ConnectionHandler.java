import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionHandler implements Runnable{
    private int _peerId;
    private Connection _conn;
    private FileManager _fileManager;
    private PeerManager _peerManager;
    private int _remotePeerId;
    private BlockingQueue<Message> _queue = new LinkedBlockingQueue<>();
    private boolean _connectingPeer;

    public ConnectionHandler(int id, Connection conn, FileManager fileManager, PeerManager peerManager) {
        _peerId = id;
        _conn = conn;
        _remotePeerId = -1;
        _fileManager = fileManager;
        _peerManager = peerManager;
        _connectingPeer = false;
    }

    public ConnectionHandler(int id, Connection conn, FileManager fileManager, PeerManager peerManager, int remoteId, boolean connectingPeer) {
        _peerId = id;
        _conn = conn;
        _remotePeerId = remoteId;
        _fileManager = fileManager;
        _peerManager = peerManager;
        _connectingPeer = connectingPeer;
    }

    @Override
    public void run() {
        System.out.println("Handling connection for peer " + _peerId + " with unknown peer");

        new ConnectionHelper(_queue, _conn).start();

        try {
            // if we are the connector, we send -> receive
            if (_connectingPeer) {
                _conn.sendHandshake(new HandshakeMessage(_peerId));
            }

            // receive handshake, we now identified remote peer
            HandshakeMessage rcvHandshake = _conn.receieveHandshake();
            _remotePeerId = rcvHandshake.getPeerId();

            // if we aren't the connector, we receive -> send
            if (!_connectingPeer) {
                _conn.sendHandshake(new HandshakeMessage(_peerId));
            }

            System.out.println("Received Handshake from peer " + _remotePeerId);

            // Log
            Logger.getInstance().connectedWith(_remotePeerId, _connectingPeer);

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
