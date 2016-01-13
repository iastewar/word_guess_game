/*
 * A simple TCP client that sends messages to a server and display the message
   from the server. 
 * For use in CPSC 441 lectures
 * Instructor: Prof. Mea Wang
 * 
 * Modified by Ian Stewart to be used with WordGuessServer.java
 * 10045484
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
        	
        	if (line.length() > 100) {
        		System.out.println("\nError: messages must be 100 characters or less");
        		continue;
        	}
        	
            // Send to the server
            outBuffer.write(line.getBytes());
            
            if (line.equals("logout\n"))
            		break;
            
            // Getting response from the server
            System.out.println();
            lineIn = inBuffer.readLine();
            if (lineIn != null) {
            	 //System.out.println(lineIn);
            	if (lineIn.startsWith("LG")) {
            		System.out.println("Game Log:");
            	} else if (lineIn.startsWith("PC")) {
            		lineIn = lineIn.substring(2, lineIn.length());
            		System.out.println("Player " + lineIn + " connected");
            	} else if (lineIn.startsWith("DS")) {
            		lineIn = lineIn.substring(2, lineIn.length());
            		System.out.println("Player " + lineIn + " disconnected");
            	} else if (lineIn.startsWith("DN")) {
            		lineIn = lineIn.substring(2, lineIn.length());
            		System.out.println("Player " + lineIn + " disconnected, game can not be started");
            	} else if (lineIn.startsWith("NP")) {
            		System.out.println("Need at least 2 players to start game");
            	} else if (lineIn.startsWith("GS")) {
            		lineIn = lineIn.substring(2, lineIn.length());
            		String[] values = lineIn.split("-");
            		System.out.println("Player " + values[0] + ": Game has started with a word of length " + values[1]);
            	} else if (lineIn.startsWith("GN")) {
            		System.out.println("Game has not started yet");
            	} else if (lineIn.startsWith("OG")) {
            		System.out.println("You are not allowed to guess your own word!");
            	} else if (lineIn.startsWith("CG")) {
            		lineIn = lineIn.substring(2, lineIn.length());
            		String[] values = lineIn.split("-");
            		System.out.println("Player " + values[0] + ": Correct guess: " + values[1] + ", game over");
            	} else if (lineIn.startsWith("IG")) {
            		lineIn = lineIn.substring(2, lineIn.length());
            		String[] values = lineIn.split("-");
            		System.out.println("Player " + values[0] + ": Incorrect guess: " + values[1] + ", with " + values[2] + " letters correct");
            	}
               
            }
            while (inBuffer.ready())
            {	
            	lineIn = inBuffer.readLine();
                //System.out.println(lineIn);
            	if (lineIn.startsWith("LG")) {
            		System.out.println("Game Log:");
            	} else if (lineIn.startsWith("PC")) {
            		lineIn = lineIn.substring(2, lineIn.length());
            		System.out.println("Player " + lineIn + " connected");
            	} else if (lineIn.startsWith("DS")) {
            		lineIn = lineIn.substring(2, lineIn.length());
            		System.out.println("Player " + lineIn + " disconnected");
            	} else if (lineIn.startsWith("DN")) {
            		lineIn = lineIn.substring(2, lineIn.length());
            		System.out.println("Player " + lineIn + " disconnected, game can not be started");
            	} else if (lineIn.startsWith("NP")) {
            		System.out.println("Need at least 2 players to start game");
            	} else if (lineIn.startsWith("GS")) {
            		lineIn = lineIn.substring(2, lineIn.length());
            		String[] values = lineIn.split("-");
            		System.out.println("Player " + values[0] + ": Game has started with a word of length " + values[1]);
            	} else if (lineIn.startsWith("GN")) {
            		System.out.println("Game has not started yet");
            	} else if (lineIn.startsWith("OG")) {
            		System.out.println("You are not allowed to guess your own word!");
            	} else if (lineIn.startsWith("CG")) {
            		lineIn = lineIn.substring(2, lineIn.length());
            		String[] values = lineIn.split("-");
            		System.out.println("Player " + values[0] + ": Correct guess: " + values[1] + ", game over");
            	} else if (lineIn.startsWith("IG")) {
            		lineIn = lineIn.substring(2, lineIn.length());
            		String[] values = lineIn.split("-");
            		System.out.println("Player " + values[0] + ": Incorrect guess: " + values[1] + ", with " + values[2] + " letters correct");
            	}
            }
            System.out.println();
            
        }
        
        // Close the socket
        clientSocket.close();           
    } 
} 



/*
[LG\n]											"Game Log:\n"
[PC<playerNumber>\n]							"Player " + playerNumber + " connected\n"
[DS<playerNumber>\n]							"Player " + playerNumber + " disconnected\n"
[DN<playerNumber>\n]							"Player " + playerNumber + " disconnected, game can not be started\n"
[NP\n]											"Need at least 2 players to start game\n"
[GS<playerNumber>-<wordLength>\n]				"Player " + playerNumber + ": Game has started with a word of length " + (guessWord.length()-1) +"\n"
[GN\n]											"Game has not started yet\n"
[OG\n]											"You are not allowed to guess your own word!\n"
[CG<playerNumber>-<word>\n]						"Player " + playerNumber + ": Correct guess: " + line.substring(0, line.length()-1) + ", game over\n"
[IG<playerNumber>-<word>-<correctLetters>\n]	"Player " + playerNumber + ": Incorrect guess: " + line.substring(0, line.length()-1) + ", with " + correctLetters + " letters correct\n"
*/
















