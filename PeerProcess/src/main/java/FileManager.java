import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;

public class FileManager {
    public FileManager() {

    }

    public byte[] GetPiece(byte[] pieceIndex) {
        int index = ByteBuffer.wrap(pieceIndex).getInt();
        File dir = new File("./peer_" + pieceIndex + "/files/parts/thefile");
        // make dir if not made
        dir.mkdirs();
        File file = new File(dir.getAbsolutePath() + "/" + index);
        return getByteArrayFromFile(file);
    }

    private byte[] getByteArrayFromFile(File file){
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] fileBytes = new byte[(int) file.length()];
            int bytesRead = fis.read(fileBytes, 0, (int) file.length());
            fis.close();
            return fileBytes;
        } catch (FileNotFoundException e) {
            //LogHelper.getLogger().warning(e);
        } catch (IOException e) {
            //LogHelper.getLogger().warning(e);
        }
        finally {
            // make sure to close if not done yet
            if (fis != null) {
                try {
                    fis.close();
                }
                catch (IOException ex) {}
            }
        }
        return null;
    }

    public BitSet getReceivedPieces() {
        return null;
    }

    public byte[] getPiece(int pieceIndexFromByteArray) {
        return null;
    }

    public void addPiece(int pieceIdx, byte[] pieceContent) {
    }

    public int getPieceToRequest(BitSet receivedPieces) {
        return -1;
    }
}
