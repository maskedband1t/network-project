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
        System.out.println("ConnectionHelper has access to connection for" + _conn.GetInfo().getId());
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
                            CommonConfig.getInstance().optimisticUnchokingInterval * 2
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
                System.out.println("Dequeued a send message with type " + Helpers.GetMessageType(msg.getType()) + ", the queue now has the following messages: ");
                for(Object m : _queue.toArray()) {
                    System.out.println("> " + ((Message)m).getType());
                }

                // Validate not null
                if (msg == null) {
                    System.out.println("Connection helper: msg is null :(");
                    continue;
                }

                // We only want to accept msg if we know their id
                if (_conn.GetInfo().getId() != -1) {
                    //System.out.println("We have a valid connection to " + _conn.GetInfo().getId());
                    if (msg.getType() == Helpers.CHOKE && !_remoteChoked){
                        System.out.println("Choking and Sending message over connection");
                        Logger.getInstance().dangerouslyWrite("Choking and Sending message over connection");
                        _remoteChoked = true;
                        // Send the actual msg
                        sendCheckRequest(msg);
                    }
                    else if (msg.getType() == Helpers.UNCHOKE && _remoteChoked) {
                        System.out.println("Unchoking and Sending message over connection");
                        Logger.getInstance().dangerouslyWrite("Unchoking and Sending message over connection");
                        _remoteChoked = false;
                        // Send the actual msg
                        sendCheckRequest(msg);
                    }
                    // Choke/Unchoke doesn't go through
                    else if (msg.getType() == Helpers.CHOKE || msg.getType() == Helpers.UNCHOKE) {
                        System.out.println("NOT sending message over connection");
                    }
                    else {
                        System.out.println("Sending message over connection");
                        sendCheckRequest(msg);
                    }
                }
                else
                    System.out.println("Cannot send messages yet - we have not handshaked");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
