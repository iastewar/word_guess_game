/*
 * Word Guess Client
 * Ian Stewart
 *
 *
 */


import java.io.*;
import java.net.*;

public class WordGuessClient {

    public static void main(String args[]) throws Exception
    {
        if (args.length != 2)
        {
            System.out.println("Usage: TCPClient <Server IP> <Server Port>");
            System.exit(1);
        }

        // Initialize a client socket connection to the server
        Socket clientSocket = new Socket(args[0], Integer.parseInt(args[1]));

        // Initialize input and an output stream for the connection(s)
        String lineIn;
        DataOutputStream outBuffer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inBuffer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        // Initialize user input stream
        String line;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        for(;;)
        {
        	// Get user input and send to the server
        	System.out.print("\nPlease enter a message to be sent to the server:\n"
        			+ "NG-<word> - to start a new game with <word> as the word that clients must guess.\n"
        			+ "<word> - to submit a guess of <word> to the server. The game must already be started for this message to be accepted.\n"
        			+ "logout - to logout from the server.\n"
        			+ "update - to update the game (show game log)\n\n");
        	line = inFromUser.readLine();
        	line = line.toLowerCase();

        	boolean lineLetters = true;
        	String lineCheck = line;
        	if (lineCheck.startsWith("ng-"))
        		lineCheck = lineCheck.substring(3);
        	for (char c : lineCheck.toCharArray()) {
        		if (c < 'a' || c > 'z') {
        			System.out.println("\nError: words can only contain letters");
        			lineLetters = false;
            		break;
        		}
        	}
        	if (!lineLetters)
        		continue;

        	line += "\n";

        	if (line.length() > 1000) {
        		System.out.println("\nError: messages must be 1000 characters or less");
        		continue;
        	}

            // Send to the server
            outBuffer.write(line.getBytes());

            if (line.equals("logout\n"))
            		break;

            // Getting response from the server
            System.out.println();
            lineIn = inBuffer.readLine();
            if (lineIn != null)
                System.out.println(lineIn);

            while (inBuffer.ready())
            {
            	lineIn = inBuffer.readLine();
                System.out.println(lineIn);
            }

        }

        // Close the socket
        clientSocket.close();
    }
}
