import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class Client {
	public static void main(String args[]) throws IOException{
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		DatagramSocket clientSocket2 = new DatagramSocket(2015);
		InetAddress IPaddress = InetAddress.getByName("localhost"); 
		byte[] sendData;
	    byte[] receiveData = new byte[18];
	    DatagramPacket sendPacket;
	    
	   
	    //to parse message
	    Message message = new Message();
	    String fileData;
	    PrintWriter writer = null;
	    String ackString = "ACK";
			 
	    sendData = new byte[8];
		System.out.println("Type <start> to start program.");
		String sentence = in.readLine();  
		    
		sendData = sentence.getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, IPaddress, 2014);

		clientSocket.send(sendPacket);
		    
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
		while(true){
			clientSocket2.receive(receivePacket);
			
			message.parse(receivePacket.getData(), receivePacket.getLength());
			
			System.out.println("Frame kind: " + String.valueOf(message.message_type));
            System.out.println("Sequence Num: " + String.valueOf(message.seq_num_int));
            int seq_num = message.seq_num_int;
            fileData = new String(message.data,0, message.data_length);
            System.out.println(fileData);
            
            //record sequence number of received frames
            if(message.message_type == Message.DATA) {
            // open a file once and start saving onto it.
            if(writer == null)
                writer = new PrintWriter("./rcv/logfile", "UTF-8");
            // writing onto file.
            System.out.println("Writing Sequence Number to file.");
            writer.print("Sequence Number Received: " + String.valueOf(message.seq_num_int)+'\n');
            

            sendData = Message.code(seq_num, Message.ACK, ackString.getBytes(), ackString.length() );
            DatagramPacket sendPacket2 = new DatagramPacket(sendData, sendData.length, IPaddress, 2015);
            System.out.println("\nSending ACKNOWLEDGEMENT\n");
            clientSocket.send(sendPacket);
            
            
            
            }else if(message.message_type == Message.END) {
                writer.close();
                writer = null;
            }	
		}
	}
}
