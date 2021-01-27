package ups;

import java.io.*;
import java.net.*;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;


public class ClientTCP {

	// instance
	private static ClientTCP ClientTCPInstance = null;
	
	// client socket
	static Socket socket;
	
	// name of the client
	public static String name = "";
	
	// state of the client
	public static ClientState state = null;
	
	// is client connected
	static boolean connected = false;
	
	private MessageInThread reader;
	
	// writer
	static PrintWriter writer;

	//
	Button button;

	//label
	Label label;
	
	
		// private construktor
		private ClientTCP(String ipAddress, int port, String name, Label label, Button button) {
			state = ClientState.LOGIN;
			// create a socket
			try {
				socket = new Socket(ipAddress, port);
			} catch (IOException e) {	
				System.out.println("Error connecting to the server.");
				label.setText("Nepovedlo se připojit k serveru.");
				return;
			} catch (IllegalArgumentException e) {
				System.out.println("Error connecting to the server.");
				label.setText("Nepovedlo se připojit k serveru.");
				return;
			} catch (NullPointerException e) {
				System.out.println("Error connecting to the server.");
				label.setText("Nepovedlo se připojit k serveru.");
				return;
			}

			try {
				// reader
				reader = new MessageInThread(socket);
				
				//writer
				writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
				
				System.out.println("Connected to " + socket.getInetAddress() + ":" + socket.getPort());
				label.setText("");
				button.setDisable(true);
				connected = true;
			} catch (Exception e) {	
				System.err.println("[Error] " + e.getMessage() + "\n" + "Unable to connect to " + ipAddress + ":" + port);
				label.setText("Nepovedlo se připojit k serveru.");
			}
			
			// send message "I name"
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					sendMessage(ClientConstants.LOGIN_PREFIX + " " + name + ClientConstants.END_CHAR);
									
				}
			});
			
		}
		
		public static void init(String ipAddress, int port, String name, Label label, Button button) {
			ClientTCPInstance = new ClientTCP(ipAddress, port, name, label, button);
		}
		
	    // static method to create instance of Singleton class 
	    public static ClientTCP getInstance() { 
	        return ClientTCPInstance; 
	    } 
		
		
		
		public static void sendMessage(String msg) {
			putMessage(new MessageIn(msg));
		}
		
		public static void putMessage(MessageIn msg) {
			System.out.println("Sending: " + msg.getMessage());
			try {
				writer.print(msg.getMessage());
				writer.flush();
			} catch (Exception e) {
				System.err.println("\nWrite error");
			}
		}
		

	 
}
