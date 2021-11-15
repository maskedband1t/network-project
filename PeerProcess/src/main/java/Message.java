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

    public Message(byte type, byte[] data) {
        this.length = (data == null ? 0 : data.length) + 1;
        this.type = type;
        this.payload = (byte[])data.clone();
    }

    public int getLength() { return length; }

    public byte getType() { return type; }

    public byte[] getPayload() {
        return (byte[])payload.clone();
    }
}