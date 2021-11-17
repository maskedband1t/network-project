import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
public class PeerInfo {
    private int peerId;
    private String hostname;
    private int port;
    private final AtomicBoolean _interested;
    public AtomicInteger _downloadRate; // want public accessibility here. NOTE: how much did it download over last interval
     // should we add bitset here?

    public PeerInfo() {
        peerId = -1;
        hostname = "";
        port = -1;
        _interested = new AtomicBoolean (false);
        _downloadRate = new AtomicInteger(0);
    }

    public PeerInfo(int peerId, String hostname, int port) {
        this.peerId = peerId;
        this.hostname = hostname;
        this.port = port;
        _interested = new AtomicBoolean (false);
        _downloadRate = new AtomicInteger(0);
    }

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
}