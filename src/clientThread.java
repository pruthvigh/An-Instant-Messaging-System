/*
 * Instant Messaging Application.
 * Name: Pruthvi Raju Muthyam ID: 1001400715, Spring 17
 * 
 *  **** MULTITHREADS IMPLEMENTED HERE, EACH THREAD FOR ONE CLIENT ****
 *  
 *  
 * CLientThread class for thread management for each connection, setting up and closing 
 * streams, sockets and connection.
 * References: 
 * 1) https://www.udemy.com/java-multithreading/
 * 2) https://stackoverflow.com/questions/32389259/how-to-add-a-list-of-all-the-clients-connected-on-a-chat-application-in-java
 * 3) https://github.com/aboullaite/Multi-Client-Server-chat-application
 * 4) https://www.youtube.com/watch?v=pr02nyPqLBU
 * 5) https://goo.gl/HcRkut
 * 
 */


import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

public class clientThread extends Thread{
	private String clientName = null;
	  private DataInputStream inputStream = null;
	  private PrintStream outputStream = null;
	  private Socket clientSocket = null;
	  private final clientThread[] threads;
	  private int maxClients;

	  //Hashset for storing names of clients so that duplication of names are avoided.
	  
	  private static HashSet<String> clientNames  = new HashSet<String>();
	  
	  
	  //Hashmap for stroing names and their output streams.
	private static HashMap<String,PrintStream> outputStreams = new HashMap<String, PrintStream>() ;
	  
	  //constructor initializing with threads array and connection socket with server
	  public clientThread(Socket clientSocket, clientThread[] threads) {
	    this.clientSocket = clientSocket;
	    this.threads = threads;
	    maxClients = threads.length;
	  }

	  public void run() {
	    int maxClientsCount = this.maxClients;
	    clientThread[] threads = this.threads;

	    try {
	      /*
	       * Create input and output streams for this client.
	       */
	      inputStream = new DataInputStream(clientSocket.getInputStream());
	      outputStream = new PrintStream(clientSocket.getOutputStream());
	      String name;
	      /*
	       * getting display name and checking for bad names by looking up for the name 
	       * in the HashSet for client names. If the name is unique, it is accepted 
	       * and is stored into the hash set, else warning message is displayed.
	       */
	      
	      while (true) {
	        outputStream.println("Enter your name.");
	        name = inputStream.readLine().trim();
	        if (!clientNames.contains(name)){
	        	clientNames.add(name);
	        	outputStreams.put(name, outputStream); 
	          break;
	        } else {
	          outputStream.println("Name already used. Choose another one.");
	        }
	      }
	      
	      /* Welcome the new the client.
	       * Allocating a thread for this client and setting up the name variable
	       * with the client's name.
	       *  Broadcasting to all the client about the new client added.
	       */
	      outputStream.println("Welcome " + name
	          + " to our chat room.\nTo leave enter /quit in a new line.");
	      synchronized (this) {
	        for (int i = 0; i < maxClientsCount; i++) {
	          if (threads[i] != null && threads[i] == this) {
	            clientName = name;
	            break;
	          }
	        } 
	          
	        for (int i = 0; i < maxClientsCount; i++) {
	          if (threads[i] != null && threads[i] != this) {
	            threads[i].outputStream.println("*** A new user " + name
	                + " entered the chat room !!! ***");
	          }
	        }
	        }
	      
	      /*
	       * taking input from the user to whom to connect to from the available online 
	       * clients from.
	       */
	      
	      String whomToConnect;
	      
	      
	      while (true) {
	    	  outputStream.println("select the name whom you want to connect.");
		        whomToConnect = inputStream.readLine().trim();
		        if (clientNames.contains(whomToConnect)){
		        	
		          break;
		        } else {
		          outputStream.println( whomToConnect + 
		       " is not available right now, try with a different person");
		        }
		      }
	      
	      outputStream.println("you are now connected to " + whomToConnect);
	      
	      
	      
	      
	      /* Start the conversation.
	       * If client inputs "/quit" connection ends and the associated streams and 
	       * sockets are closed.
	       *  Else, the message is broadcasted to the appropriate client.
	       *  
	       *  */
	      while (true) {
	        String line = inputStream.readLine();
	        //String header = "HTTP.";
	        //String message = line + header;
	        
	        if (line.startsWith("/quit")) {
	          break;
	        }
	              synchronized (this) {
	                for (int i = 0; i < maxClientsCount; i++) {
	                  if (threads[i] != null && threads[i] != this
	                      && threads[i].clientName != null
	                      && threads[i].clientName.equals(whomToConnect)) {
	                    threads[i].outputStream.println("<" + name + "> " + line);
	                    /*
	                     * Echo this message to let the client know the private
	                     * message was sent.
	                     */
	                    this.outputStream.println(">" + name + "> " + line);
	                    break;
	                  }
	                }
	              }
	      }
	      
	      /*
	       * Broadcasting to the client about leaving of the another client.
	       * 
	       * 
	       */
	      
	      synchronized (this) {
	        for (int i = 0; i < maxClientsCount; i++) {
	          if (threads[i] != null && threads[i] != this
	              && threads[i].clientName != null) {
	            threads[i].outputStream.println("*** The user " + name
	                + " is leaving the chat room !!! ***");
	          }
	        }
	      }
	      outputStream.println("*** Bye " + name + " ***");

	      /*
	       * Clean up. Set the current thread variable to null so that a new client
	       * could be accepted by the server.
	       */
	      synchronized (this) {
	        for (int i = 0; i < maxClientsCount; i++) {
	          if (threads[i] == this) {
	            threads[i] = null;
	          }
	        }
	      }
	      /*
	       * Close the output stream, close the input stream, close the socket.
	       */
	      inputStream.close();
	      outputStream.close();
	      clientSocket.close();
	    } catch (IOException e) {
	    }
	  }
}
