import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FileManager {
    private static FileManager instance = null;

    public static FileManager getInstance() {
        if (instance == null) {
            throw new AssertionError("FileManager not yet initialized. Try FileManager.init().");
        }
        return instance;
    }

    public static void init() {
        if (instance != null) {
            throw new AssertionError("FileManager is already initialized!");
        }
        instance = new FileManager();
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
}
