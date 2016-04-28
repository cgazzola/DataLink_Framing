import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
		
		byte[] receiveData2 = new byte[15];
		byte[] sendData2;
		
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
				
				seq_num += 1;
				System.out.println("Sending Packet: " + seq_num);
				
				int rand = random.nextInt(10);
				if(rand < 4){
					serverSocket.setSoTimeout(1000); //sets timeout to 1 second
					try{ //simulation of lost packet...no ack
						serverSocket.receive(frame);
						continue;
					}
					catch(SocketTimeoutException e){
						System.out.println("ERROR: DID NOT RECEIVE ACK, RESENDING PACKET");
						System.out.println("Sending packet " + seq_num + " again.\n");
						
						String messageString = new String(fileBuff);
						//System.out.println("Message to be sent: " + messageString);
							
						DatagramPacket sendPacket = new DatagramPacket(sendData, rc+5, IPaddress, 2015);
						//DatagramPacket ack_frame = new DatagramPacket(receiveData2, receiveData2.length);
						//serverSocket.receive(frame);
//						if(message.message_type == Message.ACK){
//							System.out.println("Got ACK!!!!\n\n");
//						}else{
//							System.out.println("Didn't get Ack\n\n");
//						}
						
						
					}
					
				}
				else{
					
					serverSocket.setSoTimeout(1000); //set timeout
					//send the packet
					
					//System.out.println("Sending packet: " + seq_num);
						
					String messageString = new String(fileBuff);
					//System.out.println("Message to be sent: " + messageString + "\n\n\n");
						
					DatagramPacket sendPacket = new DatagramPacket(sendData, rc+5, IPaddress, 2015);
					serverSocket.send(sendPacket);
					try{
						//DatagramPacket ack_frame = new DatagramPacket(receiveData2, receiveData2.length);
						serverSocket.receive(frame);
						message.parse(frame.getData(), frame.getLength());
						if(message.message_type == Message.ACK){
							System.out.println("Got ACK!!!!\n\n");
						}else{
							System.out.println("Didn't get Ack\n\n");
						}
					}catch(SocketTimeoutException e){
						System.out.println("ERROR: didn't catch any packet");
					}
					
					
					fileBuff = new byte[8];
					rc = in.read(fileBuff);
					if(rc > 0)
                        sendData = Message.code(seq_num, Message.DATA, fileBuff, rc);
				}	
			}	
			
			//send end of file, empty packet message to client.
			
			sendData = Message.code(seq_num, Message.END, eof.getBytes(), eof.length());
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPaddress, 2015);
            System.out.println("Sending empty packet");
            serverSocket.send(sendPacket);
            System.out.println("File Sending Complete.");
            in.close();
		}
	}
}
