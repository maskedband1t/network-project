import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
public class PeerInfo {
    private int peerId;
    private String hostname;
    private int port;
    private AtomicBoolean _interested;
    private AtomicInteger _downloadRate;
    private Bitfield _bitfield;
    private AtomicBoolean _file_complete = new AtomicBoolean(false);;

    // Construct a default PeerInfo
    public PeerInfo() {
        this.peerId = -1;
        this.hostname = "";
        this.port = -1;
        this._interested = new AtomicBoolean (false);
        this._downloadRate = new AtomicInteger(0);
        this._bitfield = new Bitfield(false);
    }

    // Construct PeerInfo with all of it's values populated
    public PeerInfo(int peerId, String hostname, int port, boolean file_complete) {
        this.peerId = peerId;
        this.hostname = hostname;
        this.port = port;
        this._interested = new AtomicBoolean (false);
        this._downloadRate = new AtomicInteger(0);
        this._file_complete.set(file_complete);
        this._bitfield = new Bitfield(file_complete);
    }

    // Get _file_complete
    public boolean is_file_complete() { return this._file_complete.get(); }

    // Get _downloadRate
    public int get_download_rate() { return this._downloadRate.get(); }

    // Get _downloadRate as an Atomic
    public AtomicInteger get_download_rate_atomic() { return this._downloadRate; }

    // Get the peer id
    public int getId() {
        return peerId;
    }

    // Get the host name
    public String getHost() {
        return hostname;
    }

    // Get the port
    public int getPort() {
        return port;
    }

    // Get the bitfield
    public Bitfield getBitfield() { return _bitfield; }

    // Get _file_complete
    public boolean getFileComplete() {
        if (_file_complete != null)
            return _file_complete.get();
        return false;
    }

    // Get _interested
    public boolean isInterested() {
        return _interested.get();
    }

    // Set _file_complete
    public void set_file_complete(boolean is_complete) { this._file_complete.set(is_complete); }

    // Set _downloadRate
    public void set_download_rate(int rate) { this._downloadRate.set(rate); }

    // Set _bitfield
    public void setBitfield(Bitfield b){ _bitfield = b; }

    // Set _interested
    public void setIfInterested(boolean isInterested) {
        System.out.println("Setting interested to " + isInterested + " for " + peerId);
        _interested.set(isInterested);
    }

    // Set the peer id
    public void setId(int peerId) {
        this.peerId = peerId;
    }

    // Set the host name
    public void setHost(String hostname) {
        this.hostname = hostname;
    }

    // Set the port
    public void setPort(int port) {
        this.port = port;
    }

    // Init the bitfield object
    public void initBitfield() {
        if (this._file_complete != null)
            _bitfield = new Bitfield(this._file_complete.get());
        else
            _bitfield = new Bitfield(false);
    }

    // Get list of peerIds
    public static List<Integer> toIdList(Collection<PeerInfo> peerInfoList) {
       List<Integer> ids = new ArrayList<Integer>();
       for(PeerInfo peer : peerInfoList){
           ids.add(peer.getId());
       }
       return ids;
    }

    // Debugging purposes, print this peer's info
    public void debugPrint() {
        System.out.println(getId() + " " + getHost() + " " + getPort() + " " + getFileComplete());
    }
}