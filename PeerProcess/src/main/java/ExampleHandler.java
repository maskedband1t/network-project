import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

// temporary handler class for reference based off of client.java
public class ExampleHandler extends Thread{
    private String message;
    private Socket connection;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int peerId;

    public ExampleHandler(Socket connection, int peerId) {
        this.connection = connection;
        this.peerId = peerId;
    }

    public void run() {
        try {
            // get input output streams
            out = new ObjectOutputStream(connection.getOutputStream());
            out.flush();
            in = new ObjectInputStream(connection.getInputStream());
            try {
                // keep receiving/handling/returning messages until there is no more
                while (true) {
                    message = (String)in.readObject();
                    System.out.println("Received message " + message);

                    // TODO: Add logic for determining what to send back, if there is any
                    String returnMessage = "idk what goes here";
                    sendMessage(returnMessage);
                }
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // close all connections when done
            try {
                in.close();
                out.close();
                connection.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(String msg) {
        try{
            out.writeObject(msg);
            out.flush();
            System.out.println("Sent message: " + msg + " to Client " + peerId);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
