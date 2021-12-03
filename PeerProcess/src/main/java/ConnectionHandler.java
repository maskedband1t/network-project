import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionHandler implements Runnable{
    private PeerInfo _info;
    private Connection _conn;
    private FileManager _fileManager;
    private PeerManager _peerManager;
    private PeerInfo _remotePeerInfo;
    private int _remotePeerId;
    private BlockingQueue<Message> _queue = new LinkedBlockingQueue<>();
    private boolean _connectingPeer;

    // Constructs a ConnectionHandler for an anonymous remote peer
    public ConnectionHandler(PeerInfo in, Connection conn, FileManager fileManager, PeerManager peerManager) {
        _info = in;
        _conn = conn;
        _remotePeerInfo = new PeerInfo();
        _remotePeerId = _remotePeerInfo.getId();
        _fileManager = fileManager;
        _peerManager = peerManager;
        _connectingPeer = false;
    }

    // Constructs a ConnectionHandler for a known remote peer
    public ConnectionHandler(PeerInfo in, Connection conn, FileManager fileManager, PeerManager peerManager, PeerInfo remoteInfo, boolean connectingPeer) {
        _info = in;
        _conn = conn;
        _remotePeerInfo = remoteInfo;
        _remotePeerId = _remotePeerInfo.getId();
        _fileManager = fileManager;
        _peerManager = peerManager;
        _connectingPeer = connectingPeer;
    }

    // Get remote peer id
    public int getRemotePeerId() { return _remotePeerId; }

    // Get peer id
    public int getPeerId() { return _info.getId(); }

    // Send message
    public void send(Message msg) throws IOException {
        _conn.send(msg);
    }

    @Override
    public void run() {
        System.out.println("Handling connection for peer " + _info.getId() + " with unknown peer");

        // Acts as the first layer of our ConnectionHandler, handling choke information
        new ConnectionHelper(_queue, _conn).start();

        try {
            // If we are the connector, we send -> receive
            if (_connectingPeer)
                _conn.sendHandshake(new HandshakeMessage(_info.getId()));

            // Receive handshake, we now identified remote peer
            HandshakeMessage rcvHandshake = _conn.receiveHandshake();
            _remotePeerId = rcvHandshake.getPeerId();

            // Based off of their id, fill connection's peerinfo properly
            _conn.updatePeerInfo(PeerInfoConfig.getInstance().GetPeerInfo(_remotePeerId));

            // If we aren't the connector, we receive -> send
            if (!_connectingPeer)
                _conn.sendHandshake(new HandshakeMessage(_info.getId()));

            // After handshake send bitfield message if we have any pieces
            System.out.println("Checking if we have any set bits in our bitfield...");
            Bitfield field = _info.getBitfield();
            if (!field.empty()) {
                // TODO: Debugging purposes, can remove
                System.out.println("We do! Sending out bitfield too: ");
                field.debugPrint();

                // Send the bitfield message
                Message msg = new Message(Helpers.BITFIELD, field.getBits().toByteArray());
                _conn.send(msg);
            }

            System.out.println("Received Handshake from peer " + _remotePeerId);

            // Log
            Logger.getInstance().connectedWith(_remotePeerId, _connectingPeer);

            // Start handling messages for this connection to the remote peer
            MessageHandler msgHandler = new MessageHandler(_remotePeerId, _fileManager, _peerManager);

            // Handle the connection, this is the server portion of our peer
            while (!Process.shutdown) {
                try {
                    msgHandler.handle(_conn.receive());
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
            // Close the connection before exiting the run() function
            _conn.close();
        }
    }
}
