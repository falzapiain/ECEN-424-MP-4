import java.io.*;
import java.net.*;
import java.util.Random;

class Listener {

    public static void main(String[] args) throws Exception {
       if (args.length < 3) {
            System.out.println("Please provide the server IP and port");
            return;
       }

        // convert passed parameters to client values
        String talkerIP;
        int portTalker;
        int portListener;
        try {
            talkerIP = args[0];
            portTalker = Integer.parseInt(args[1]);
            portListener = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.out.println("Please enter valid integers for the server IP and port number.");
            return;
        }    	
    	
       DatagramSocket listenerSocket = null;

    	try {
            listenerSocket = new DatagramSocket(portListener);
            InetAddress talkerAddress = InetAddress.getByName(talkerIP);
            // Listen for messages from the Talker
            byte[] receieveData = new byte[1024];        	
        	DatagramPacket receivePacket = new DatagramPacket(receieveData, receieveData.length);
            listenerSocket.receive(receivePacket);
            
            Random random = new Random();

            // ACK each message
            int expectedSeqNum = 0;
            int prevMsgNum = 0;
            int numMsg = 0;
            int frame = 0;
            int msgNum = 0;
            String msgTalk = "";
            String msgFull = "";
            while (expectedSeqNum <= numMsg) {
                String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
            	// Find length of message
            	if (expectedSeqNum == 0) {
            		frame = receivedMessage.indexOf(":");
                    msgNum = Integer.parseInt(receivedMessage.substring(0,frame)); // exclude frame
                    numMsg = Integer.parseInt(receivedMessage.substring(frame + 1));
                    
                    System.out.println("Expecting " + numMsg + " messages.");
            	} else {
                    receivePacket = new DatagramPacket(new byte[1024], 1024);
                    listenerSocket.receive(receivePacket);
                    // put the symbol or character that separates the message number from the rest of the message
                    frame = receivedMessage.indexOf(":");
                    msgNum = Integer.parseInt(receivedMessage.substring(0,frame)); // exclude frame
                    msgTalk = receivedMessage.substring(frame + 1); // exclude frame             
                
                    System.out.println("Received message " + msgNum + ": " + msgTalk);            		
            	}
                


                // Random ACK
                if (random.nextBoolean()) {
                    String ackMessage = String.valueOf(msgNum + 1);  // ACK the next expected message
                    byte[] sendData = ackMessage.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, talkerAddress, portTalker);
                    listenerSocket.send(sendPacket);
                    System.out.println("ACK: " + msgNum);
                    if (prevMsgNum != 0) {
                    	msgFull = msgFull + msgTalk;
                	}
                    prevMsgNum = msgNum;
                    expectedSeqNum++;
                    
                    if(expectedSeqNum == numMsg) {
//                        System.out.println(msgFull);
                        listenerSocket.close();
                    }

                } else {
                    System.out.println("Dropped ACK " + msgNum);
                }

            }
            listenerSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    	
        // close the connection                	
    	if (listenerSocket != null && !listenerSocket.isClosed()) {
        	listenerSocket.close();
        }

    }
}