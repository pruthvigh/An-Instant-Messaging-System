/*
 * Instant Messaging Application.
 * Name: Pruthvi Raju Muthyam ID: 1001400715, Spring 17
 * 
 * 
 * ChatCLient class for setting up client GUI and chat access
 * References: 
 * 1) https://www.udemy.com/java-multithreading/
 * 2) https://stackoverflow.com/questions/32389259/how-to-add-a-list-of-all-the-clients-connected-on-a-chat-application-in-java
 * 3) https://github.com/aboullaite/Multi-Client-Server-chat-application
 * 4) https://www.youtube.com/watch?v=pr02nyPqLBU
 * 5) https://goo.gl/HcRkut
 * 
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;



public class ChatClient {

	/** Chat client access */
    static class ChatAccess extends Observable {
        private Socket socket;
        private OutputStream outputStream;

        @Override
        public void notifyObservers(Object arg) {
            super.setChanged();
            super.notifyObservers(arg);
        }

        /** Create socket, and receiving thread
         * for receiving messages to display on the chat access area.
         *  
         */
        public void InitSocket(String server, int port) throws IOException {
            socket = new Socket(server, port);
            outputStream = socket.getOutputStream();

            Thread receivingThread = new Thread() {
                @Override
                public void run() {
                    try {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));
                        String inputLine;
                        while ((inputLine = reader.readLine()) != null)
                            notifyObservers(inputLine);
                    } catch (IOException ex) {
                        notifyObservers(ex);
                    }
                }
            };
            receivingThread.start();
        }

        private static final String CRLF = "\r\n"; // newline

        /** Send a line of text 
         * and appending to the textarea.
         * */
        public void send(String text) {
            try {
                outputStream.write((text + CRLF).getBytes());
                outputStream.flush();
            } catch (IOException ex) {
                notifyObservers(ex);
            }
        }

        /** Close the socket */
        public void close() {
            try {
                socket.close();
            } catch (IOException ex) {
                notifyObservers(ex);
            }
        }
    }

    /** Chat client UI */
    static class ChatFrame extends JFrame implements Observer {

        private JTextArea textArea;
        private JTextField inputTextField;
        private JButton sendButton;
        private ChatAccess chatAccess;

        public ChatFrame(ChatAccess chatAccess) {
            this.chatAccess = chatAccess;
            chatAccess.addObserver(this);
            buildGUI();
        }

        /** Builds the user interface */
        private void buildGUI() {
            textArea = new JTextArea(20, 50);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            add(new JScrollPane(textArea), BorderLayout.CENTER);

            Box box = Box.createHorizontalBox();
            add(box, BorderLayout.SOUTH);
            inputTextField = new JTextField();
            sendButton = new JButton("Send");
            box.add(inputTextField);
            box.add(sendButton);

            // Action for the inputTextField and the SendButton
            ActionListener sendListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String str = inputTextField.getText();
                    if (str != null && str.trim().length() > 0)
                        chatAccess.send(str);
                    inputTextField.selectAll();
                    inputTextField.requestFocus();
                    inputTextField.setText("");
                }
            };
            inputTextField.addActionListener(sendListener);
            sendButton.addActionListener(sendListener);

            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    chatAccess.close();
                }
            });
        }

        /** Updates the UI depending on the Object argument
         * Thread updating parts of the GUI rather than updating whole of it.
         *  
         *  */
        public void update(Observable o, Object arg) {
            final Object finalArg = arg;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    textArea.append(finalArg.toString());
                    textArea.append("\n");
                }
            });
        }
    }
	
	public static void main(String[] args) {
		String server = args[0];
        int port =2222;
        ChatAccess access = new ChatAccess();

        JFrame chatFrame = new ChatFrame(access);
        chatFrame.setTitle("MyChatApp - connected to " + server + ":" + port);
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.pack();
        chatFrame.setLocationRelativeTo(null);
        chatFrame.setResizable(false);
        chatFrame.setVisible(true);

        try {
            access.InitSocket(server,port);
        } catch (IOException ex) {
            System.out.println("Cannot connect to " + server + ":" + port);
            ex.printStackTrace();
            System.exit(0);
        }
	}

}
