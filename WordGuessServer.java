/*
 * Word Guess Server
 * Ian Stewart
 * 
 * 
 */

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

public class WordGuessServer {
	
	private static class Player {
		String playerSocketString;
		int playerNum;		
		
		Player(String playerSocketString, int playerNum) {
			this.playerSocketString = playerSocketString;
			this.playerNum = playerNum;			
		}		
	}
	
	
    public static int BUFFERSIZE = 1024;
    public static void main(String args[]) throws Exception 
    {
        if (args.length != 1)
        {
            System.out.println("Usage: WordGuessServer <Listening Port>");
            System.exit(1);
        }
        
        // Initialize buffers and coders for channel receive and send
  
        String line = "";
        String outputLine = "";
        String hostLine = "";
        Charset charset = Charset.forName( "us-ascii" );  
        CharsetDecoder decoder = charset.newDecoder();  
        CharsetEncoder encoder = charset.newEncoder();
        ByteBuffer inBuffer = null;
        ByteBuffer outBuffer = null;
        CharBuffer cBuffer = null;
        int bytesSent, bytesRecv;     // number of bytes sent or received
        
        int clientsConnected = 0;
        int playerNumber = 0;
        String guessWord = "";
        boolean gameStarted = false;     
        String gameStartedBy = "";
        ArrayList<Player> players = new ArrayList<Player>();
        String gameLog = "LG\nGN\n";
        
        // Initialize the selector
        Selector selector = Selector.open();

        // Create a server channel and make it non-blocking
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
       
        // Get the port number and bind the socket
        InetSocketAddress isa = new InetSocketAddress(Integer.parseInt(args[0]));
        channel.socket().bind(isa);

        // Register that the server selector is interested in connection requests
        channel.register(selector, SelectionKey.OP_ACCEPT);

        // Wait for something happen among all registered sockets
        try {
            boolean terminated = false;
            while (!terminated) 
            {
                if (selector.select(500) < 0)
                {
                    System.out.println("select() failed");
                    System.exit(1);
                }
                
                // Get set of ready sockets
                Set<SelectionKey> readyKeys = selector.selectedKeys();
                Iterator<SelectionKey> readyItor = readyKeys.iterator();

                // Walk through the ready set
                while (readyItor.hasNext()) 
                {
                    // Get key from set
                    SelectionKey key = (SelectionKey)readyItor.next();

                    // Remove current entry
                    readyItor.remove();

                    // Accept new connections, if any
                    if (key.isAcceptable())
                    {
                        
                        SocketChannel cchannel = ((ServerSocketChannel)key.channel()).accept();
                        cchannel.configureBlocking(false);
                        System.out.println("Accept connection from " + cchannel.socket().toString());
                        
                        // Register the new connection for read operation
                        cchannel.register(selector, SelectionKey.OP_READ);
                        
                        clientsConnected++;
                        playerNumber++;
                        players.add(new Player(cchannel.socket().toString(), playerNumber));
                        gameLog += "PC" + playerNumber + "\n";
                        
                    } 
                    else
                    {
                        SocketChannel cchannel = (SocketChannel)key.channel();
                        if (key.isReadable())
                        {
                            Socket socket = cchannel.socket();
                        
                            // Open input and output streams
                            inBuffer = ByteBuffer.allocateDirect(BUFFERSIZE);
                            outBuffer = ByteBuffer.allocateDirect(BUFFERSIZE);
                            cBuffer = CharBuffer.allocate(BUFFERSIZE);
                             
                            // Read from socket
                            bytesRecv = cchannel.read(inBuffer);
                            if (bytesRecv <= 0)
                            {
                                System.out.println("read() error, or connection closed");
                                key.cancel();  // deregister the socket
                                continue;
                            }
                             
                            inBuffer.flip();      // make buffer available  
                            decoder.decode(inBuffer, cBuffer, false);
                            cBuffer.flip();
                            line = cBuffer.toString();
                            
                            line = line.toLowerCase();
                            
                            System.out.println(line);                     
                                                 
                            // update request
                            
                            if (line.equals("update\n")) {
                            	outBuffer=ByteBuffer.wrap(gameLog.getBytes()); 
                                bytesSent = cchannel.write(outBuffer);
                            }
                            
                            // new game request
                            
                            else if (line.startsWith("ng-")) {
                            	if (clientsConnected < 2) {
                            		outputLine = "NP\n";
                            		outBuffer=ByteBuffer.wrap(outputLine.getBytes()); 
                                    bytesSent = cchannel.write(outBuffer);
                            	} else {                            		                          		
                            		gameStartedBy = cchannel.socket().toString();
                            		gameStarted = true;
                            		guessWord = line.substring(3);
                            		
                            		int playerNum = -1;
                            		for (Player p : players) {
                            			if (p.playerSocketString.equals(cchannel.socket().toString())) {
                            				playerNum = p.playerNum;
                            				break;
                            			}
                            		}
                            		outputLine = "GS" + playerNum + "-" + (guessWord.length()-1) +"\n";
                            		gameLog += outputLine; // add output line to game log
                            		if (gameLog.length() > 100)
                            			gameLog = gameLog.substring(0, 3) + gameLog.substring(gameLog.length()-97, gameLog.length()); // make gameLog at max 100 characters
                            		outBuffer=ByteBuffer.wrap(gameLog.getBytes()); 
                                    bytesSent = cchannel.write(outBuffer);
                                                                     
                                    
                            	}
                            }
                            
                            // logout request
                            
                            else if (line.equals("logout\n")) {
                            	clientsConnected--;
                            	int playerNum = -1;
                        		for (int i = 0; i < players.size(); i++) {
                        			if (players.get(i).playerSocketString.equals(cchannel.socket().toString())) {
                        				playerNum = players.get(i).playerNum;
                        				players.remove(i);
                        				break;
                        			}
                        		}
                            	if (clientsConnected < 2) {
                            		outputLine = "DN" + playerNum + "\n";
                            		gameLog += outputLine; // add output line to game log
                            		if (gameLog.length() > 100)
                            			gameLog = gameLog.substring(0, 3) + gameLog.substring(gameLog.length()-97, gameLog.length()); // make gameLog at max 100 characters
                                    
                                    gameStarted = false;
                                    guessWord = "";
                            	} else {
                            		outputLine = "DS" + playerNum + "\n";
                            		gameLog += outputLine; // add output line to game log
                            		if (gameLog.length() > 100)
                            			gameLog = gameLog.substring(0, 3) + gameLog.substring(gameLog.length()-97, gameLog.length()); // make gameLog at max 100 characters
                            	}
                            }
                            
                            // guess word request
                            
                            else {
                            	if (!gameStarted) {
                            		outputLine = "GN\n";
                            		outBuffer=ByteBuffer.wrap(outputLine.getBytes()); 
                                    bytesSent = cchannel.write(outBuffer);    
                                    
                            	} else if (gameStartedBy.equals(cchannel.socket().toString())) {
                            		outputLine = "OG\n";
                                	outBuffer=ByteBuffer.wrap(outputLine.getBytes()); 
                                    bytesSent = cchannel.write(outBuffer);      
                                    
                            	} else if (line.equals(guessWord)) {
                            		int playerNum = -1;
                            		for (Player p : players) {
                            			if (p.playerSocketString.equals(cchannel.socket().toString())) {
                            				playerNum = p.playerNum;
                            				break;
                            			}
                            		}
                            		outputLine = "CG" + playerNum + "-" + line.substring(0, line.length()-1) + "\n";
                            		gameLog += outputLine; // add output line to game log
                            		if (gameLog.length() > 100)
                            			gameLog = gameLog.substring(0, 3) + gameLog.substring(gameLog.length()-97, gameLog.length()); // make gameLog at max 100 characters
                            		outBuffer=ByteBuffer.wrap(gameLog.getBytes()); 
                                    bytesSent = cchannel.write(outBuffer);
                                    
                                    gameStarted = false;
                                    guessWord = "";
                                    
                            	} else {
                            		int correctLetters = 0;
                            		for (int i = 0; i < line.length()-1; i++) {                 
                            			for (int j = 0; j < guessWord.length()-1; j++) {
                            				if (line.charAt(i) == guessWord.charAt(j))
                                				correctLetters++;
                            			}                            			
                            		}
                            		
                            		int playerNum = -1;
                            		for (Player p : players) {
                            			if (p.playerSocketString.equals(cchannel.socket().toString())) {
                            				playerNum = p.playerNum;
                            				break;
                            			}
                            		}
                            		outputLine = "IG" + playerNum + "-" + line.substring(0, line.length()-1) + "-" + correctLetters + "\n";
                            		gameLog += outputLine; // add output line to game log
                            		if (gameLog.length() > 100)
                            			gameLog = gameLog.substring(0, 3) + gameLog.substring(gameLog.length()-97, gameLog.length()); // make gameLog at max 100 characters
                            		outBuffer=ByteBuffer.wrap(gameLog.getBytes()); 
                                    bytesSent = cchannel.write(outBuffer);
                            	}
                            }
                                                                                             
                        }	       
                    }
                       
                } // end of while (readyItor.hasNext()) 
            } // end of while (!terminated)
        }
        catch (IOException e) {
            System.out.println(e);
        }
 
        // close all connections
        
        Set keys = selector.keys();
        Iterator itr = keys.iterator();
        while (itr.hasNext()) 
        {
            SelectionKey key = (SelectionKey)itr.next();
            //itr.remove();
            if (key.isAcceptable())
                ((ServerSocketChannel)key.channel()).socket().close();
            else if (key.isValid())
                ((SocketChannel)key.channel()).socket().close();
        }
    }
}