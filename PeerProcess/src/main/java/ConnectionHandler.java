import java.io.IOException;
import java.util.List;
import java.util.UUID;
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
    private UUID _uuid;

    // Constructs a ConnectionHandler for an anonymous remote peer
    public ConnectionHandler(PeerInfo in, Connection conn, FileManager fileManager, PeerManager peerManager) {
        _info = in;
        _conn = conn;
        _remotePeerInfo = new PeerInfo();
        _remotePeerId = _remotePeerInfo.getId();
        _fileManager = fileManager;
        _peerManager = peerManager;
        _connectingPeer = false;
        _uuid = java.util.UUID.randomUUID();
        Helpers.println("Constructed Connection Handler [" + _uuid + "]for " + _info.getId() + " to remote peer " + _remotePeerInfo.getId());
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
        _uuid = java.util.UUID.randomUUID();
        Helpers.println("Constructed Connection Handler [" + _uuid + "]for " + _info.getId() + " to remote peer " + _remotePeerInfo.getId());}

    // Get remote peer id
    public int getRemotePeerId() { return _remotePeerId; }

    // Get peer id
    public int getPeerId() { return _info.getId(); }

    // Send message
    public void send(Message msg) throws IOException {
        //_conn.send(msg);
        _queue.add(msg);
    }

    // Sends multiple messages
    public void queueMessages(List<Message> msgs) {
        for (Message m : msgs)
            _queue.add(m);
    }

    @Override
    public void run() {
        // Acts as the first layer of our ConnectionHandler, handling choke information
        ConnectionHelper helper = new ConnectionHelper(_queue, _conn, _fileManager, _peerManager, _remotePeerId);
        helper.registerHandler(this);
        helper.start();

        try {
            // If we are the connector, we send -> receive
            //if (_connectingPeer)
            _conn.sendHandshake(new HandshakeMessage(_info.getId()));

            // Receive handshake, we now identified remote peer
            HandshakeMessage rcvHandshake = _conn.receiveHandshake();
            _remotePeerId = rcvHandshake.getPeerId();

            // Based off of their id, fill connection's peerinfo properly
            PeerInfo remoteInfo = PeerInfoConfig.getInstance().GetPeerInfo(_remotePeerId);
            _conn.updatePeerInfo(remoteInfo);
            _remotePeerInfo = remoteInfo;
            Helpers.println("Updated Connection Handler [" + _uuid + "]for " + _info.getId() + " to remote peer " + _remotePeerInfo.getId());

            // If we aren't the connector, we receive -> send
            //if (!_connectingPeer)
            //    _conn.sendHandshake(new HandshakeMessage(_info.getId()));

            // After handshake send bitfield message if we have any pieces
            Helpers.println("Checking if we have any set bits in our bitfield...");
            Bitfield field = _info.getBitfield();
            if (!field.empty()) {
                // TODO: Debugging purposes, can remove
                Helpers.println("We do! Sending out bitfield too");
                field.debugPrint();

                Logger.getInstance().dangerouslyWrite("Sending our bitfield over to " + _remotePeerId + ": " + field.asString());

                // Send the bitfield message
                Message msg = new Message(Helpers.BITFIELD, field.getBits().toByteArray());
                _conn.send(msg);
            }

            Helpers.println("Received Handshake from peer " + _remotePeerId);

            // Log
            Logger.getInstance().connectedWith(_remotePeerId, _connectingPeer);

            // Start handling messages for this connection to the remote peer
            MessageHandler msgHandler = new MessageHandler(_remotePeerId, _info, _fileManager, _peerManager);

            // Handle the connection, this is the server portion of our peer
            while (!Process.shutdown) {
                try {
                    Message msgReceived = _conn.receive();
                    if (msgReceived != null) {
                        Helpers.println("Received a message of type " + Helpers.GetMessageType(msgReceived.getType()));
                        Message msgToReturn = msgHandler.handle(msgReceived);
                        if (msgToReturn != null) {
                            _conn.send(msgToReturn);
                            Helpers.println("We are returning a message with type " + Helpers.GetMessageType(msgToReturn.getType()));
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    //System.exit(0);
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
