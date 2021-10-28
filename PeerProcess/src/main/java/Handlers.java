import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Handlers {
    public static class ChokeHandler implements IHandler {
        public void handleMsg(Message msg, Connection peerConn) {
            // TODO: Implement Handler for Choke Messages

            // NO packet payload

            // Psuedocode
            // Unchoked = false
            // We should no longer be sending RequestMessages to the peer

             Logger.getInstance().chokedBy(peerInfo.getId());

            throw new NotImplementedException();
        }
    }


    public static class UnchokeHandler implements IHandler {
        public void handleMsg(Message msg, Connection peerConn) {
            // TODO: Implement Handler for Unchoke Messages

            // NO packet payload

            // Psuedocode
            // While(Unchoked):
            //     PieceToRequest = Random Selection Strategy to select a piece that the peer has that we do not and have not requested
            //     SendRequestMessageTo(peerInfo, forPieceWithHeader=PieceToRequest.Header)
                    /**
                     * Message msg = new Message();
                     * peerConn.send(msg);
                     * **/
            //     // Handle the edge case in which we request the piece but never recieve it bc the peer chokes us
            //     If NotReceiveForRequest():
            //         break

             Logger.getInstance().unchokedBy(peerInfo.getId());

            throw new NotImplementedException();
        }
    }

    public static class InterestedHandler implements IHandler {
        public void handleMsg(Message msg, Connection peerConn) {
            // TODO: Implement Handler for Interested Messages

            // NO packet payload

            // Psuedocode
            // I don't think this does anything? Interested/Not interested messages are moreso used by the peer that sent it

             Logger.getInstance().receivedInterestedFrom(peerInfo.getId());

            throw new NotImplementedException();
        }
    }

    public static class UninterestedHandler implements IHandler {
        public void handleMsg(Message msg, Connection peerConn) {
            // TODO: Implement Handler for Uninterested Messages

            // NO packet payload

            // Psuedocode
            // I don't think this does anything? Interested/Not interested messages are moreso used by the peer that sent it

            Logger.getInstance().receivedNotInterestedFrom(peerInfo.getId());

            throw new NotImplementedException();
        }
    }

    public static class HaveHandler implements IHandler {
        public void handleMsg(Message msg, Connection peerConn) {
            // TODO: Implement Handler for Have Messages

            // HAS packet payload: 4 byte piece index field
            // Ex (I think): Each piece has a 4-byte "header"/index field, the peer sends a message to us that they have
            // this specific piece matching the header info

            // Psuedocode
            // Update the bitfield for the peer that sent this message
            // Note: We keep a bitfield per peer to track what pieces they have

            // If bitfield contains PieceThatWeDontHave:
                    //SendInterestedMessageTo(peerInfo)
                    /**
                     * Message msg = new Message(new byte[]{0,0,0,0}, 2, new byte[]);
                     * peerConn.send(msg);
                     * **/
            // Else:
                    //SendNotInterestedMessageTo(peerInfo)
                    /**
                     * Message msg = new Message(new byte[]{0,0,0,0}, 3, new byte[]);
                     * peerConn.send(msg);
                     * **/


            Logger.getInstance().receivedHaveFrom(peerInfo.getId(), pieceIndex)

            throw new NotImplementedException();
        }
    }

    public static class BitfieldHandler implements IHandler {
        public void handleMsg(Message msg, Connection peerConn) {
            // TODO: Implement Handler for Bitfield Messages

            // HAS packet payload: bitfield structure, which tracks the pieces of the file the peer has
            // Ex: If there are 32 pieces of the file, and the peer has all of them, it will send a payload of 32 1 bits

            // Psuedocode
            // Initialize the bitfield for the peer that sent this message
            // Note: We only handle this bitfield message once per peer

            // If bitfield contains PieceThatWeDontHave:
                    //SendInterestedMessageTo(peerInfo)
                    /**
                     * Message msg = new Message(new byte[]{0,0,0,0}, 2, new byte[]);
                     * peerConn.send(msg);
                     * **/
            // Else:
                    //SendNotInterestedMessageTo(peerInfo)
                    /**
                     * Message msg = new Message(new byte[]{0,0,0,0}, 3, new byte[]);
                     * peerConn.send(msg);
                     * **/

            throw new NotImplementedException();
        }
    }

    public static class RequestHandler implements IHandler {
        public void handleMsg(Message msg, Connection peerConn) {
            // TODO: Implement Handler for Request Messages

            // HAS packet payload: 4 byte piece index field
            // Ex: The peer has requested for us to send the piece corresponding to the 4 byte piece index field in the payload

            // Psuedocode
            // SendPieceMessageTo(peerInfo, pieceWithHeader=payload)
            /**
             * Message msg = new Message(new byte[]{length of payload}, 7, new byte[piece index field + piece content]);
             * peerConn.send(msg);
             */

            throw new NotImplementedException();
        }
    }

    public static class PieceHandler implements IHandler {
        public void handleMsg(Message msg, Connection peerConn) {
            // TODO: Implement Handler for Piece Messages

            // Psuedocode
            // HAS packet payload: 4 byte piece index field AND piece content
            // Ex: The peer sent the whole piece including its index field

            // Foreach neighbor in neighbors:
            //     If neighbor.bitfield does not have any pieces we want:
            //         SendNotInterestedMessageTo(neighbor.peerInfo)
                        /**
                         * Message msg = new Message(new byte[]{0,0,0,0}, 3, new byte[]);
                         * peerConn.send(msg);
                         * **/

            throw new NotImplementedException();
        }
    }
}
