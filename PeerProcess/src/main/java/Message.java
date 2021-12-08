/**
* A message in our protocol consists of:
*    - 4-byte  message  length  field
*    - 1-byte  message  type  field
*    - a message payload with variable size.
**/
public class Message {
    private int length = 0;
    private byte type = 0;
    private byte[] payload = new byte[]{};

    // Construct a message with a type and payload
    public Message(byte type, byte[] data) {
        this.length = (data == null || data.equals(new byte[]{}) ? 0 : data.length) + 1; // 1 for type byte
        this.type = type;
        if (data != null)
            this.payload = data.clone();
        //Helpers.println("Created " + Helpers.GetMessageType(type) + " message with length: " + this.length + " and payload " + data);
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