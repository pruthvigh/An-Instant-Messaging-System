/*
 * Instant Messaging Application.
 * Name: Pruthvi Raju Muthyam ID: 1001400715, Spring 17
 * 
 * 
 * MultiThreadClients class for creating connection and 
 * assigning a thread to each connection.
 * References: 
 * 1) https://www.udemy.com/java-multithreading/
 * 2) https://stackoverflow.com/questions/32389259/how-to-add-a-list-of-all-the-clients-connected-on-a-chat-application-in-java
 * 3) https://github.com/aboullaite/Multi-Client-Server-chat-application
 * 4) https://www.youtube.com/watch?v=pr02nyPqLBU
 * 5) https://goo.gl/HcRkut
 * 
 */

import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

//the server class
public class MultiThreadClients {

	 // The server socket for accepting incoming connections
	  private static ServerSocket serverSocket = null;
	  // The client socket.
	  private static Socket clientSocket = null;

	  // This chat server can accept up to maxClientsCount clients' connections.
	  private static final int maxClients = 10;
	  
	  //threads array for implementing threads
	  private static final clientThread[] threads = new clientThread[maxClients];

	
	
	public static void main(String[] args) {
		// The default port number.
	    int portNumber = 2222;
	    if (args.length < 1) {
	      System.out.println("Usage: java MultiThreadClients \n"
	          + "Now using port number=" + portNumber);
	    } else {
	      portNumber = Integer.valueOf(args[0]).intValue();
	    }

	    /*
	     * Open a server socket on the portNumber (default 2222). 
	     */
	    try {
	      serverSocket = new ServerSocket(portNumber);
	    } catch (IOException e) {
	      System.out.println(e);
	    }

	    /*
	     * Create a client socket for each connection and pass it to a new client
	     * thread.
	     */
	    while (true) {
	      try {
	        clientSocket = serverSocket.accept();
	        int i = 0;
	        for (i = 0; i < maxClients; i++) {
	          if (threads[i] == null) {
	            (threads[i] = new clientThread(clientSocket, threads)).start();
	            break;
	          }
	        }
	        if (i == maxClients) {
	          PrintStream os = new PrintStream(clientSocket.getOutputStream());
	          os.println("Server too busy. Try later.");
	          os.close();
	          clientSocket.close();
	        }
	      } catch (IOException e) {
	        System.out.println(e);
	      }
	    }
	  }  

	}


