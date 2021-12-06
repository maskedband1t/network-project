import java.io.IOException;
import java.util.TimerTask;

public class RequestHandler extends TimerTask {
    private final Message _msg;
    private final FileManager _fileMgr;
    private final PeerManager _peerMgr;
    private final Connection _conn;
    private final int _remotePeerId;

    RequestHandler (Message message, FileManager fileMgr, PeerManager peerMgr, Connection conn, int remotePeerId) {
        _msg = message;
        _fileMgr = fileMgr;
        _peerMgr = peerMgr;
        _conn = conn;
        _remotePeerId = remotePeerId;
    }

    @Override
    public void run() {
        int pieceIdx = Helpers.getPieceIndexFromByteArray(_msg.getPayload());

        if (!_fileMgr.hasPiece(pieceIdx)) {
            try {
                _conn.send(_msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
