import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class Process implements Runnable {
    // list of connections per peers
    // we do not map to id because we do not know their id at this scope level
    List<ConnectionHandler> _connHandlers = new ArrayList<ConnectionHandler>();

    // our info && bitfield && manager objects for files and peers
    private PeerInfo peerInfo;
    private FileManager fileManager;
    public PeerManager peerManager;
    private ProcessDoneChecker _processDoneChecker;

    // whether we should shutdown the program
    public static boolean shutdown;

    // whether neighbors are done
    private AtomicBoolean _peers_file_complete;

    // Constructs the Process
    public Process(PeerInfo peerInfo) {
        this.peerInfo = peerInfo;
        this.shutdown = false;
        this._peers_file_complete = new AtomicBoolean(false);
        this.fileManager = new FileManager(peerInfo);
        this.peerManager = new PeerManager(peerInfo.getId());
        _processDoneChecker = new ProcessDoneChecker();
    }

    // Split the file into pieces
    public void splitFile() {
        fileManager.splitFileIntoPieces();
    }

    // Initialize PeerManager
    public void initPeerManager() {
        // Run the peer manager on a thread
        if (this.peerManager != null) {
            this.peerManager.registerProcess(this);
            (new Thread() {
                public void run() {
                    peerManager.run();
                }
            }).start();
        }
    }

    // Initialize FileManager
    public void initFileManager() {
        if (this.fileManager != null)
            this.fileManager.registerProcess(this);
    }

    // Builds Connection to peer
    public void buildPeer(PeerInfo info) {
        Connection c = null;
        try {
            Helpers.println("Attempting to connect to peer id: " + info.getId());
            c = new Connection(info);
            Helpers.println("ADDING CONNECTION HANDLER [BUILDING NEW CONNECTION]");
            ConnectionHandler ch = new ConnectionHandler(peerInfo, c, fileManager, peerManager, info, true);
            addConnectionHandler(ch);
        }
        catch (Exception e) {
            if (c != null)
                c.close();
        }
    }

    // Builds Connections to all peers
    public void buildPeers() throws IOException {
        // Gets all peers to connect to
        List<PeerInfo> ourPeers = PeerInfoConfig.getInstance().GetPeersToConnectToFor(peerInfo.getId());

        // Debugging print statement
        Helpers.println(peerInfo.getId() + " will connect to ");
        for (PeerInfo peer : ourPeers) {
            Helpers.print(peer.getId() + ",");
        }

        // Build connection to each peer
        for (PeerInfo peer : ourPeers) {
            buildPeer(peer);
        }
    }

    // Adds a ConnectionHandler for a remote peer
    private synchronized boolean addConnectionHandler(ConnectionHandler connHdlr) {
        if (!_connHandlers.contains(connHdlr)) {
            _connHandlers.add(connHdlr);
            //Logger.getInstance().dangerouslyWrite(peerInfo.getId() + " added connection handler for " + connHdlr.getPeerId());
            new Thread(connHdlr).start(); // start handling connection on another thread
            try {
                wait(10);
            } catch (InterruptedException e) {
                e.printStackTrace();;
            }
        }
        return true;
    }

    // choke peers
    public synchronized void choke_peers(Set<Integer> peers) throws IOException{
        for (ConnectionHandler ch : _connHandlers)
            if (peers.contains(ch.getRemotePeerId())) {
                //Helpers.println("Choking: " + ch.getRemotePeerId());
                ch.send(new Message(Helpers.CHOKE, new byte[]{}));
            }
    }

    // unchoke peers
    public synchronized void unchoke_peers(Set<Integer> peers) throws IOException {
        for (ConnectionHandler ch : _connHandlers) {
            if (peers.contains(ch.getRemotePeerId())) {
                //Helpers.println("Unchoking: " + ch.getRemotePeerId());
                ch.send(new Message(Helpers.UNCHOKE, new byte[]{}));
            }
        }
    }

    // Handle when a piece arrives
    public synchronized void receivedPiece(int pieceIndex) throws IOException {
        for (ConnectionHandler ch : _connHandlers) {
            //Logger.getInstance().dangerouslyWrite("(1.2.3) Letting " + ch.getRemotePeerId() + " know that we have piece " + pieceIndex);
            ch.send(new Message(Helpers.HAVE, Helpers.intToBytes(pieceIndex, 4)));
            if (!peerManager.isPeerInteresting(ch.getRemotePeerId(), fileManager.getReceivedPieces())) {
                ch.send(new Message(Helpers.NOTINTERESTED, new byte[]{}));
            }
        }
    }

    // Handle when everyone else completes the file
    public synchronized void neighborsComplete() {
        _peers_file_complete.set(true);
        // we are done && everyone else is done
        if (peerInfo.getFileComplete() || peerInfo.getBitfield().getBits().cardinality() == CommonConfig.getInstance().numPieces) {
            //Logger.getInstance().dangerouslyWrite("Current state of peers: ");
            //for(PeerInfo p: PeerInfoConfig.getInstance().peerInfos)
            //    Logger.getInstance().dangerouslyWrite(p.getId() + ": " + p.getFileComplete() + " (" + p.getBitfield().getBits().cardinality() + ")");
            //Logger.getInstance().dangerouslyWrite("(HandleHave or HandleBitfield) Everyone else is done AND we are done.");
            successfullyExit();
        }
    }

    // Handle when the file is complete
    public synchronized void complete() throws IOException {
        peerInfo.set_file_complete(true);

        // Send our completed bitfield to everyone else
        Bitfield field = peerInfo.getBitfield();
        if (!field.empty() && !peerInfo.getStartedWithFile()) {
            byte[] arr = field.getBits().toByteArray();
            for (ConnectionHandler ch : _connHandlers) {
                //Logger.getInstance().dangerouslyWrite("Sending over our bitfield to " + ch.getRemotePeerId());
                Message bitfieldMsg = new Message(Helpers.BITFIELD, arr);
                ch.sendDirectly(bitfieldMsg);
            }
        }
        //else
            //Logger.getInstance().dangerouslyWrite("Our bitfield is empty! Will not send it over.");

        // Handle shutdown
        if (_peers_file_complete.get()) { // we are done && everyone else is done
            //Logger.getInstance().dangerouslyWrite("(4.1.1) We are done AND everyone else is done.");
            successfullyExit();
        }
        else { // Debug print why we didn't shut down
            //Logger.getInstance().dangerouslyWrite("(4.1.1) Not everyone is done. Here are the pieces missing: ");
            // print for us
            BitSet pBits = (BitSet)peerInfo.getBitfield().getBits().clone();
            pBits.flip(0, CommonConfig.getInstance().numPieces);
            //Logger.getInstance().dangerouslyWrite(peerInfo.getId() + " bitfield missing: " + pBits.toString());

            // print for peers
            for (PeerInfo p : peerManager._peers) {
                pBits = (BitSet)p.getBitfield().getBits().clone();
                pBits.flip(0, CommonConfig.getInstance().numPieces);
                //Logger.getInstance().dangerouslyWrite(p.getId() + " bitfield missing: " + pBits.toString());
            }
        }
    }

    // Writes completion to summary log
    private synchronized void writeToSummaryLog() {
        // write to summary log
        try {
            File logFile = new File(Helpers.pathToResourcesFolder + "summary.log");
            //logFile.createNewFile();

            BufferedWriter bf = new BufferedWriter(new FileWriter(logFile, true));
            bf.write(java.time.LocalDateTime.now() + ": Peer " + peerInfo.getId()  + " has all the pieces now!");
            bf.newLine();
            bf.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Handles completion of process
    private synchronized void successfullyExit() {
        writeToSummaryLog();
        Logger.getInstance().completedDownload();
        fileManager.mergePiecesIntoFile();
        shutdown = true;
        System.out.println("Successfully exiting...");
        System.exit(0);
    }

    // The entry point for this thread
    @Override
    public void run() {
        System.out.println("Running...");

        // Start the ProcessDoneChecker on its own thread
        _processDoneChecker.registerFileManager(fileManager);
        _processDoneChecker.registerPeerManager(peerManager);
        _processDoneChecker.start();

        try {
            // Create a Server Socket listener
            ServerSocket s = new ServerSocket(peerInfo.getPort());

            // Debugging print statement
            Helpers.println("Peer " + peerInfo.getId() + " is listening on " + peerInfo.getHost() + ":" + peerInfo.getPort());

            // While we are not in shutdown mode (not everyone has the file completed)
            while (!shutdown) {
                try {
                    // Every time a peer connects to us, we handle their connection with Handler
                    Socket c = s.accept();

                    // Add connection - the handler will handle this on a separate thread
                    PeerSocket peerSocket = new PeerSocket(c);

                    // We use a default peer info since we haven't identified who they are yet
                    Connection conn = new Connection(new PeerInfo(), peerSocket);
                    Helpers.println("ADDING CONNECTION HANDLER [DETECTED NEW CONNECTION]");
                    addConnectionHandler(new ConnectionHandler(peerInfo, conn, fileManager, peerManager));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            s.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            Helpers.println("Shutting Down");
        }
    }
}
