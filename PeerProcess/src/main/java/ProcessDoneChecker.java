

public class ProcessDoneChecker extends Thread {
    private PeerManager _peerManager;
    private FileManager _fileManager;

    // Constructs process done checker
    public ProcessDoneChecker() {
    }

    // Registers process
    public void registerFileManager(FileManager fileMgr) {
        _fileManager = fileMgr;
    }

    // Registers peer manager
    public void registerPeerManager(PeerManager peerMgr) {
        _peerManager = peerMgr;
    }

    @Override
    public void run() {
        while(!Process.shutdown) {
            if (_peerManager != null && _fileManager != null) {
                try {
                    // constantly sleeping for interval
                    Thread.sleep(Helpers.checkDoneInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // try and send bitfields out
                _fileManager.handleDone();

                // finish download
                _peerManager.download_finished();
            }
        }
    }
}
