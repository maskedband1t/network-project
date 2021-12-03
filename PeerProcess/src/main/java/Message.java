/**
* A message in our protocol consists of:
*    - 4-byte  message  length  field
*    - 1-byte  message  type  field
*    - a message payload with variable size.
**/
public class Message {
    private int length;
    private byte type;
    private byte[] payload;

    // Construct a message with a type and payload
    public Message(byte type, byte[] data) {
        this.length = (data == null ? 0 : data.length) + 1;
        this.type = type;
        if (data != null)
            this.payload = data.clone();
    }

    // Get the length of the payload
    public int getLength() { return length; }

    // Get the type of the message
    public byte getType() { return type; }

    // Get the payload
    public byte[] getPayload() {
        return payload.clone();
    }
}