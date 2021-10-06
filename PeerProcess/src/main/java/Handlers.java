import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Handlers {
    public static class ChokeHandler implements IHandler {
        public void handleMsg(Message msg, PeerInfo peerInfo) {
            // TODO: Implement Handler for Choke Messages
            throw new NotImplementedException();
        }
    }

    public static class UnchokeHandler implements IHandler {
        public void handleMsg(Message msg, PeerInfo peerInfo) {
            // TODO: Implement Handler for Unchoke Messages
            throw new NotImplementedException();
        }
    }

    public static class InterestedHandler implements IHandler {
        public void handleMsg(Message msg, PeerInfo peerInfo) {
            // TODO: Implement Handler for Interested Messages
            throw new NotImplementedException();
        }
    }

    public static class UninterestedHandler implements IHandler {
        public void handleMsg(Message msg, PeerInfo peerInfo) {
            // TODO: Implement Handler for Uninterested Messages
            throw new NotImplementedException();
        }
    }

    public static class HaveHandler implements IHandler {
        public void handleMsg(Message msg, PeerInfo peerInfo) {
            // TODO: Implement Handler for Have Messages
            throw new NotImplementedException();
        }
    }

    public static class BitfieldHandler implements IHandler {
        public void handleMsg(Message msg, PeerInfo peerInfo) {
            // TODO: Implement Handler for Bitfield Messages
            throw new NotImplementedException();
        }
    }

    public static class RequestHandler implements IHandler {
        public void handleMsg(Message msg, PeerInfo peerInfo) {
            // TODO: Implement Handler for Request Messages
            throw new NotImplementedException();
        }
    }

    public static class PieceHandler implements IHandler {
        public void handleMsg(Message msg, PeerInfo peerInfo) {
            // TODO: Implement Handler for Piece Messages
            throw new NotImplementedException();
        }
    }
}
