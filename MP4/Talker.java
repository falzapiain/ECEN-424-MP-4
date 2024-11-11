import java.io.*;
import java.net.*;
import java.util.Scanner;

class serverInstance extends Thread {
    // conccurent client tracker
    private static int concurrentClients = 1;

    // private variables
    private String message;
    private int writeCount;
    private Socket connectionSocket;

    // overwridden run method to execute individual tasks concurrently
    // this run function will send messages to a client that connects
    public void run() {
        try {
                incrementClient();
                // create output stream from passed in connection socket
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

                // write the message to the stream
                for (int i = 0; i < writeCount-1; i++) {
                    outToClient.writeBytes(message);
                    // wait one second between transmission
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("Error when waiting in thread!");
                    }
                }

                // finish writing and close connection
                outToClient.writeBytes(message + "\n");
                connectionSocket.close();  

        } catch (IOException e) {
            System.out.println("Error when writing to client!");
            return;
        } finally {
            decrementClient();
        }
    }
    
    // method to increment client count
    private static synchronized void incrementClient() {
        concurrentClients++;
    }

    // method to decrement client count
    private static synchronized void decrementClient() {
        concurrentClients--;
    }

    // method to retrieve current client count
    public static synchronized int getConccurentCount() {
        //System.out.println("There are " + concurrentClients + " clients");
        return concurrentClients;
    }

    // method to get message to send to client
    public void setMessage(String m1) {
        this.message = m1;
    }

    // method to get the number of times to write the message to client
    public void setWriteAmount(int n) {
        this.writeCount = n;
    }

    // method to get welcome socket
    public void setConnectionSocket(Socket connection) {
        this.connectionSocket = connection;
    }

}

class server {
    // the server takes in the port and number of clients, to do this in VSCode, pass them in launch.json in .vscode directory
    // this should also work if run from the terminal, but I am not on a UNIX device
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Please provide the Server Port followed by the Number of Clients.");
            return;
        }

        // convert passed parameters to server values
        int serverPort; 
        int maxClients;
        try {
            serverPort = Integer.parseInt(args[0]);
            maxClients = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Please enter valid integers for the server port and max number of clients.");
            return;
        }
        
        
        // create scanner object to read values from terminal, prompt user for a string and integer, store them
        Scanner userInterface = new Scanner(System.in);
        System.out.print("Please enter a string: ");
        String inputString;

        try {
            inputString = userInterface.nextLine();
        } catch (NumberFormatException e) {
            System.out.print("Please enter a valid input only.");
            userInterface.close();
            return;
        }

        System.out.print("Please enter a positive integer: ");
        int inputInt;
        try {
            inputInt = Integer.parseInt(userInterface.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid integer only.");
            userInterface.close();
            return;
        }

        // ensure positive value
        if (inputInt < 0) {
            System.out.println("Please enter a valid positive integer only.");
            userInterface.close();
            return;
        }

        userInterface.close();
        // create universal welcome socket for all clients with identical ports
        ServerSocket welcomeSocket;
        try {
            welcomeSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            System.out.println("Unable to create welcome socket!");
            return;
        }
        
        /*
        // for every client that attempts to connect, give them a thread that writes to them
        while (serverInstance.getConccurentCount() < maxClients) {
            try {
                // wait for a client to connect
                Socket connectionSocket = welcomeSocket.accept();

                // create their thread and set values
                serverInstance currentThread = new serverInstance();
                currentThread.setMessage(inputString);
                currentThread.setWriteAmount(inputInt);
                currentThread.setConnectionSocket(connectionSocket);

                // start the thread
                currentThread.start();

            } catch (IOException e) {
                System.out.println("Error when establishing client connection.");
            }
        }

        // maximum number of clients reached, close the welcome socket
        try {
            welcomeSocket.close();
        } catch (IOException e) {
            System.out.println("Error when closing connection");
        }
        */



        // for every client that attempts to connect, give them a thread that writes to them
        while (true) {
            try {
                // check conccurent clients
                if (serverInstance.getConccurentCount() < maxClients) {
                    // wait for a client to connect
                    Socket connectionSocket = welcomeSocket.accept();

                    // create their thread and set values
                    serverInstance currentThread = new serverInstance();
                    currentThread.setMessage(inputString);
                    currentThread.setWriteAmount(inputInt);
                    currentThread.setConnectionSocket(connectionSocket);

                    // start the thread
                    currentThread.start();
                // there are too many clients!
                } else {
                    System.out.println("Maximum clients reached, please wait...");
                    // wait five seconds before checking again
                    try {
                        Thread.sleep(5000); 
                    } catch (InterruptedException e) {
                        System.out.println("Error when waiting for server refresh");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error when establishing client connection.");
            }
        }
    } 
}