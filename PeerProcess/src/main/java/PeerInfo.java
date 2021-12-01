import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
public class PeerInfo {
    private int peerId;
    private String hostname;
    private int port;
    private final AtomicBoolean _interested;
    private AtomicInteger _downloadRate;
     // should we add bitfield here?
    private Bitfield _bitfield;
    private AtomicBoolean _file_complete;

    public PeerInfo() {
        peerId = -1;
        hostname = "";
        port = -1;
        _interested = new AtomicBoolean (false);
        _downloadRate = new AtomicInteger(0);
        this._bitfield = null;
        _file_complete = new AtomicBoolean(false);
    }

    public PeerInfo(int peerId, String hostname, int port, boolean file_complete) {
        this.peerId = peerId;
        this.hostname = hostname;
        this.port = port;
        _interested = new AtomicBoolean (false);
        _downloadRate = new AtomicInteger(0);
        
        this._file_complete.set(file_complete);
        this._bitfield = new Bitfield(this._file_complete.get());
    }

    public boolean is_file_complete() { return this._file_complete.get(); }
    public void set_file_complete(boolean is_complete) { this._file_complete.set(is_complete); }

    public int get_download_rate() { return this._downloadRate.get(); }
    public AtomicInteger get_download_rate_atomic() { return this._downloadRate; }
    public void set_download_rate(int rate) { this._downloadRate.set(rate); }

    public void initBitfield() { _bitfield = new Bitfield(this.is_file_complete.get());}

    public Bitfield getBitfield() { return _bitfield; }

    public void setBitfield(Bitfield b){_bitfield = b;} // setter

    public boolean isInterested() {
        return _interested.get();
    }
    public void setIfInterested(boolean isInterested) {
        if(isInterested){_interested.set (true);}
        else{_interested.set(false);}
    }

    public int getId() {
        return peerId;
    }

    public String getHost() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public void setId(int peerId) {
        this.peerId = peerId;
    }

    public void setHost(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }
    // get list of pure peerIds
    public static List<Integer> toIdList(List<PeerInfo> peerInfoList){
       List<PeerInfo> ids = new ArrayList<>();
       for(PeerInfo peer : peerInfoList){
           ids.add(peer.getId());
       }
    }

    public void debugPrint() {
        System.out.println(getId() + " " + getHost() + " " + getPort() + " " + getHasFile());
    }
}