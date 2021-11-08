import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Handlers {
    public static boolean choked = true;

    public static class ChokeHandler implements IHandler {
        public void handleMsg(Message msg, Connection peerConn) {
            // TODO: Implement Handler for Choke Messages

            // NO packet payload

            // Psuedocode
            // Unchoked = false
            // We should no longer be sending RequestMessages to the peer
            choked = true;
            Logger.getInstance().chokedBy(peerConn.GetInfo().getId());

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

            choked = false;
            Logger.getInstance().unchokedBy(peerConn.GetInfo().getId());

            while (!choked) { // need to choose an actual conditional - where to keep track of choked?
                int index = 0; // int index = Bitfield.getRandomUnsetIndex();
                byte[] length = ByteBuffer.allocate(4).putInt(4).array();
                byte[] pieceIndex = ByteBuffer.allocate(4).putInt(index).array();
                Message requestMsg = new Message(length, Process.REQUEST.byteValue(), pieceIndex);
                try {
                    peerConn.send(requestMsg);
                }
                catch (IOException e) {
                    System.err.println("IO Error on request.");
                    e.printStackTrace();
                }
            }
            // handle not received

            throw new NotImplementedException();
        }
    }

    public static class InterestedHandler implements IHandler {
        public void handleMsg(Message msg, Connection peerConn) {
            // This doesn't do anything?
            Logger.getInstance().receivedInterestedFrom(peerConn.GetInfo().getId());
        }
    }

    public static class UninterestedHandler implements IHandler {
        public void handleMsg(Message msg, Connection peerConn) {
            // This doesn't do anything?
            Logger.getInstance().receivedNotInterestedFrom(peerConn.GetInfo().getId());
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

            // TODO: log correct pieceindex
            Logger.getInstance().receivedHaveFrom(peerConn.GetInfo().getId(), -1);//, pieceIndex)

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

            // Logger.getInstance().downloadedPiece(peerConn.GetInfo().getId(), pieceIndex, pieceCount);
            // if complete file:
            //     Logger.getInstance().completedDownload();

            throw new NotImplementedException();
        }
    }
}
