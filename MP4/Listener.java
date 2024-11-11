import java.io.*;
import java.net.*;

class naiveclient {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Please provide the server IP and port");
            return;
        }

        // convert passed parameters to client values
        String serverIP;
        int port;
        try {
            serverIP = args[0];
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Please enter valid integers for the server IP and port number.");
            return;
        }

        // string to store server response
        int message;

        Socket clientSocket = null;
        try {
            // create client socket to connect to server
            clientSocket = new Socket(serverIP, port);

            // create input stream to read bytes from server
            InputStream inFromServer = clientSocket.getInputStream();

            System.out.print("FROM SERVER: ");

            // read the message sent (we must manually read from server)
            while ((message = inFromServer.read()) != -1) {
                // cast bytes to char, print to terminal
                System.out.print((char) message);
            }
            // close the connection
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Server refused connection.");
        }
    }
}