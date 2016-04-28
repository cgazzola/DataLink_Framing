import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;


public class Server {
	
	//PRINT INFO ON RECIEVED PACKETS
    public static void printDatagramInfo(DatagramPacket receivePacket) {
        //get Client IP
        InetAddress IPAddress = receivePacket.getAddress();
        System.out.println("IP Address: " + IPAddress);

        //get Client port
        int port = receivePacket.getPort();
        System.out.println("Port #: " + port);
 
    }
	
	
	
	public static void main(String args[]) throws IOException{
		
		int seq_num = 0;
		int rc;
		byte[] receiveData;
		byte[] sendData;
		Message message = new Message();
		String eof = "End Of File";
		
		Random random = new Random();
		//long size;
		DatagramSocket serverSocket = new DatagramSocket(2014);

		
		receiveData = new byte[15];
		DatagramPacket frame = new DatagramPacket(receiveData, receiveData.length);
		serverSocket.receive(frame);
		InetAddress IPaddress = frame.getAddress();
		System.out.println("INET " + IPaddress);
			
		byte[] data;
			
		int port = frame.getPort();
		System.out.println("port " + port);
	
		String sentence = new String(frame.getData(), 0, frame.getLength());
		String start =  new String("start");
			
		System.out.println("RECEIVED FROM CLIENT: " + sentence);
	
		if(sentence.equals(start)){
			System.out.println("Fetching file...\n");
			File file = new File("./files/africa.txt");
			FileInputStream in = new FileInputStream(file);
			System.out.println("Length of file = " + file.length());
				
			byte[] fileBuff = new byte[8];
			rc = in.read(fileBuff);
			
			
			sendData = Message.code(seq_num, Message.DATA, fileBuff, rc);
			while(rc != -1){
				int rand = random.nextInt(10);
				if(rand < 4){
					System.out.println("~~~~ERROR PACKET NOT SENT ~~~~~");
					continue;
				}
				else{
					//send the packet
					seq_num += 1;
					System.out.println("Sending packet: " + seq_num);
						
					String messageString = new String(fileBuff);
					System.out.println("Message to be sent: " + messageString);
						
					DatagramPacket sendPacket = new DatagramPacket(sendData, rc+5, IPaddress, 2015);
					serverSocket.send(sendPacket);
					
					serverSocket.receive(frame);
					message.parse(frame.getData(), frame.getLength());
					if(message.message_type == Message.ACK){
						System.out.println("Got ACK!!!!\n\n");
					}
					else{
						System.out.println("Didn't get Ack\n\n");
					}
					
					fileBuff = new byte[8];
					rc = in.read(fileBuff);
					if(rc > 0)
                        sendData = Message.code(seq_num, Message.DATA, fileBuff, rc);
				}	
			}	
			sendData = Message.code(seq_num, Message.END, eof.getBytes(), eof.length());
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPaddress, 2015);
            System.out.println("Sending empty packet");
            serverSocket.send(sendPacket);
            System.out.println("File Sending Complete.");
            in.close();
		}
	}
}
