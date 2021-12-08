import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionHelper extends Thread {
    private boolean _remoteChoked = true;
    private BlockingQueue<Message> _queue = new LinkedBlockingQueue<>();
    private final FileManager _fileMgr;
    private final PeerManager _peerMgr;
    private final Connection _conn;
    private final int _remotePeerId;

    private ConnectionHandler ch;

    // Construct the ConnectionHelper
    public ConnectionHelper(BlockingQueue<Message> q, Connection c, FileManager fileMgr, PeerManager peerMgr, int remoteId) {
        _queue = q;
        _conn = c;
        _fileMgr = fileMgr;
        _peerMgr = peerMgr;
        _remotePeerId = remoteId;
        Helpers.println("ConnectionHelper has access to connection for" + _conn.GetInfo().getId());
    }

    // Register the ConnectionHandler (parent)
    public void registerHandler(ConnectionHandler hand) {
        ch = hand;
    }

    // Ensures that all requests go through to remote peers
    private synchronized void sendCheckRequest(Message msg) throws IOException {
        if (msg != null) {
            _conn.send(msg);
            switch (msg.getType()) {
                case Helpers.REQUEST: {
                    new java.util.Timer().schedule(
                            new RequestHandler(msg, _fileMgr, _peerMgr, _conn, _remotePeerId),
                            CommonConfig.getInstance().optimisticUnchokingInterval * 2000
                    );
                }
            }
        }
    }

    @Override
    public void run() {
        while (!Process.shutdown) {
            try {
                // Handle the messages in queue
                Message msg = _queue.take();

                // Debugging print statement
                // For now, don't print for CHOKE and UNCHOKE msgs
                if (msg.getType() != 1 && msg.getType() != 0) {
                    Helpers.println("Dequeued a send message with type " + Helpers.GetMessageType(msg.getType()) + ", the queue now has the following messages: ");
                    for (Object m : _queue.toArray()) {
                        Helpers.print(((Message) m).getType() + ",");
                    }
                    Helpers.println("");
                }

                // Validate not null
                if (msg == null) {
                    Helpers.println("Connection helper: msg is null :(");
                    continue;
                }

                // We only want to accept msg if we know their id
                if (_conn.GetInfo().getId() != -1) {
                    //Helpers.println("We have a valid connection to " + _conn.GetInfo().getId());
                    if (msg.getType() == Helpers.CHOKE && !_remoteChoked){
                        Helpers.println("Choking and Sending message over connection");
                        //Logger.getInstance().dangerouslyWrite("Choking and Sending message over connection");
                        _remoteChoked = true;
                        // Send the actual msg
                        sendCheckRequest(msg);
                    }
                    else if (msg.getType() == Helpers.UNCHOKE && _remoteChoked) {
                        Helpers.println("Unchoking and Sending message over connection");
                        //Logger.getInstance().dangerouslyWrite("Unchoking and Sending message over connection");
                        _remoteChoked = false;
                        // Send the actual msg
                        sendCheckRequest(msg);
                    }
                    // Choke/Unchoke doesn't go through
                    else if (msg.getType() == Helpers.CHOKE || msg.getType() == Helpers.UNCHOKE) {
                        Helpers.println("NOT sending message over connection");
                    }
                    else {
                        Helpers.println("Sending message over connection");
                        sendCheckRequest(msg);
                    }
                }
                else {
                    _queue.add(msg);
                    Helpers.println("Cannot send messages yet - we have not handshaked");
                    //Logger.getInstance().dangerouslyWrite("ConnectionHelper remote id is -1, will readd msg to queue");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
