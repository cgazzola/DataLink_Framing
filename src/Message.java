import java.nio.ByteBuffer;


public class Message {
	
	//These are for 'kinds' of messages
	public static final byte START = 1;
	public static final byte DATA = 2;
	public static final byte ACK = 3;
	public static final byte NAK = 4;
	public static final byte END = 5;
	
	public byte message_type;
	public byte seq_num[];
	public byte data[];
	public int data_length;
	public int seq_num_int;
	
	//receive packets buffer, and the length of it which is 1024
    public void parse(byte[] input, int length) {
    	message_type = input[0];
    	
    	seq_num = new byte[4];
    	
    	seq_num[0] = input[1];
        seq_num[1] = input[2];
        seq_num[2] = input[3];
        seq_num[3] = input[4];
        
        
        seq_num_int = ByteBuffer.wrap(seq_num).getInt();
        
        //because five bytes were added to carry encoding, the actual data message is 5 less...
        data = new byte[length - 5];
        data_length = length -5;
        
        for(int i=0; i<length-5; ++i) {
            data[i] = input[i+5];
        }
        
    }

    public static byte[] code(int snum, byte type, byte[] input, int length){
    	byte[] output = new byte[length + 5];
    	output[0] = type; //the first byte in the message will be for 'type of message'
    	
    	//for sequence number, bytes 1-4 store sequence number
    	output[1] = (byte) (snum >> 24);
        output[2] = (byte) (snum >> 16);
        output[3] = (byte) (snum >> 8);
        output[4] = (byte) (snum /*>> 0*/);
        
        //for actual message
        for(int i=0; i<length; ++i) {
            output[i+5] = input[i];
        }
        
        return output; //returns coded message
    	
    }
}