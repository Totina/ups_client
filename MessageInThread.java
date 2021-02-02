package ups;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;


import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;


public class MessageInThread extends Thread {
		
	// Buffered reader reference
	BufferedReader reader;

	// Is the reader running
	public static boolean isRunning;
	
	// reference to the game
	static Game game;

	static // name of the client
	String name;

	// number of games and players
	static int number = 1;
	static int players = 2;
	static int game_id = 0;
	static int state = 0;
	static int value = 1;
	static int pattern = 1;

	
	// Constructor
	public MessageInThread(Socket socket) {
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		start();
	}
	
	// Function with the while loop, reading incoming messages
	@Override
	public void run() {
		isRunning = true;
		
		while (isRunning) {
			try {
				System.out.println("Reading...");
				
				String line = reader.readLine();
				if(line == null && ClientTCP.connected) {
					System.out.println("Error: Received null message");
					errorServerNotResponding();
					break;
				}
				System.out.println(line);
				
				// create and process message
				MessageIn message = new MessageIn(line);
				processMessage(message);
				
			} catch (Exception e) {
				System.out.println("Error: Reading Exception");
				errorServerNotResponding();
				break;
			}
				
		}
		System.out.println("End listening...");
	}
	
	private void errorServerNotResponding() {
		disconnectClient("Disconnected from the server");
	}

	public static synchronized void Stop() {
		isRunning = false;
		System.out.println("Stopping the thread.");
	}
	
	public static void disconnectClient(String text) {
		//ClientTCP.sendMessage("E" + "error_occurred" + text + ClientConstants.END_CHAR);
		System.out.println("Disconnecting");
		System.out.println(text);
		Stop();
		try {
			ClientTCP.socket.close();
			System.out.println("Socket closing");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.exit(0);

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// open main window
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText(text);
				alert.showAndWait();

				if(game != null && game.gameWindow != null) {
					game.gameWindow.close();
				}
				if(Lobby.lobbyWindow != null) {
					Lobby.lobbyWindow.close();
				}
				System.exit(1);
				
			}
		});
	}
	
	// Processing incoming messages from the server.
	public static void processMessage(MessageIn message) {
		
		// print incoming message
		System.out.println(
				ClientTCP.name + " [State: "+ ClientTCP.state +" MESSAGE RECEIVED: '" + message.getMessage() + "', valid = " + message.isCorrectMessage + "]");
		
		// message incorrect
		if (!message.isCorrectMessage) {
			disconnectClient("Message_incorrect.");
		}
		
		// switch
		switch (message.prefix) {
		/*********************************************** LOGIN **********************************************/
		case ClientConstants.LOGIN_PREFIX:
			// login successful
			if (message.numberOfArguments == 2 && message.listOfArguments.get(0).equals("logged_in") && ClientTCP.state == ClientState.LOGIN) {
				// get accepted name
				ClientTCP.getInstance().name = message.listOfArguments.get(1);
				name = message.listOfArguments.get(1);
			}
			else {
				System.out.println("Error logging in.");
				disconnectClient("Error logging in.");
			}
			break;
			/*********************************************** LOBBY **********************************************/		
		case ClientConstants.LOBBY_PREFIX:
			//
			if (message.numberOfArguments == 2 && message.listOfArguments.get(0).equals("number_of_games")) {
				try {
					number = Integer.parseInt(message.listOfArguments.get(1));
				}catch(Exception e) {
					disconnectClient("Incorrect number of games.");
				}
				ClientTCP.state = ClientState.LOBBY;

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						
						Lobby lobby = Lobby.getInstance();
						Lobby.init(number);
						Lobby.start();

					
					}
				});
			
			}
			else if (message.numberOfArguments == 3 && message.listOfArguments.get(0).equals("entered") && ClientTCP.state == ClientState.LOBBY) {
				try {
					int game_id = Integer.parseInt(message.listOfArguments.get(1));
					players = Integer.parseInt(message.listOfArguments.get(2));
				}catch(Exception e) {
					disconnectClient("Incorrect game ID.");
				}

				
				ClientTCP.state = ClientState.IN_GAME;
				
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						
						game = new Game(players, name);
						game.start();
						Lobby.lobbyWindow.hide();

					}
				});
				
			}
			else if(message.numberOfArguments == 4 && message.listOfArguments.get(0).equals("info") && ClientTCP.state == ClientState.LOBBY) {
				try {
					game_id = Integer.parseInt(message.listOfArguments.get(1));
					state = Integer.parseInt(message.listOfArguments.get(2));
					players = Integer.parseInt(message.listOfArguments.get(3));
				}catch(Exception e) {
					disconnectClient("Incorrect game ID.");
				}
				
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						
						Lobby.infoAboutGame(game_id, state, players);

					}
				});
							
			}
			else if(message.numberOfArguments == 2 && message.listOfArguments.get(0).equals("cant_enter") && ClientTCP.state == ClientState.LOBBY) {
				
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						Lobby.cantEnter();
					}
				});				
				
				
			}
			else {
				System.out.println("Error: lobby, wrong message.");
				disconnectClient("Error lobby, incorrect message.");
			}
			
			
			break;
			/*********************************************** GAME **********************************************/	
		case ClientConstants.GAME_PREFIX:
			
			if(message.numberOfArguments == 1 && message.listOfArguments.get(0).equals("game_started") && ClientTCP.state == ClientState.IN_GAME) {
				
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						game.setLabel("Hra začala. Karty byly rozdány.");
						game.started = true;
						game.nextCardButton.setDisable(false);
						game.noCardButton.setDisable(false);
					}
				});
				
			}
			else if(message.numberOfArguments == 2 && message.listOfArguments.get(0).equals("player") && ClientTCP.state == ClientState.IN_GAME) {
				String player = message.listOfArguments.get(1);
				
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						game.addPlayerToList(player);
					}
				});
				
			}
			else if (message.numberOfArguments == 4 && message.listOfArguments.get(0).equals("card") && ClientTCP.state == ClientState.IN_GAME) {
				String name = message.listOfArguments.get(1);
				try {
					value = Integer.parseInt(message.listOfArguments.get(2));
					pattern = Integer.parseInt(message.listOfArguments.get(3));
				}catch(Exception e) {
					disconnectClient("Incorrect game ID.");
				}

				
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						
						Card card = new Card(name, value, pattern);
						game.addCard(card);
						game.listOfCards.add(card);

					}
				});
				
				
			}
			else if (message.numberOfArguments == 1 && message.listOfArguments.get(0).equals("win") && ClientTCP.state == ClientState.IN_GAME) {
				
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						game.win();
					}
				});
								
			}
			else if (message.numberOfArguments == 1 && message.listOfArguments.get(0).equals("lose") && ClientTCP.state == ClientState.IN_GAME) {
				
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						game.lose();
					}
				});
								
			}
			else if (message.numberOfArguments == 2 && message.listOfArguments.get(0).equals("player_left") && ClientTCP.state == ClientState.IN_GAME) {
				String name = message.listOfArguments.get(1);
				
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
					
						game.playerLeft(name);
						
					}
				});
								
			}
			else if (message.numberOfArguments == 2 && message.listOfArguments.get(0).equals("player_disconnected") && ClientTCP.state == ClientState.IN_GAME) {
				String name = message.listOfArguments.get(1);

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						game.playerDisconnected(name);
					}
				});

			}
			else if (message.numberOfArguments == 2 && message.listOfArguments.get(0).equals("player_reconnected") && ClientTCP.state == ClientState.IN_GAME) {
				String name = message.listOfArguments.get(1);

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						game.playerReconnected(name);
					}
				});

			}
			else if (message.numberOfArguments == 1 && message.listOfArguments.get(0).equals("full_hand_of_cards") && ClientTCP.state == ClientState.IN_GAME) {

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						game.full_hand();
					}
				});

			}
			else {
				System.out.println("Incorrect message");
				disconnectClient("Incorrect message");
			}
			break;		
			
			/*********************************************** PING **********************************************/	
		case ClientConstants.PING_PREFIX:
			
			if (message.numberOfArguments == 1 && message.listOfArguments.get(0).equals("ping")) {
				System.out.println("Server pinged");
				
				ClientTCP.sendMessage(ClientConstants.PING_PREFIX + " " + "pong" + ClientConstants.END_CHAR);
				
			}
			
			break;

			/*********************************************** ERROR **********************************************/

			case ClientConstants.ERROR_PREFIX:

				if (message.numberOfArguments == 1 && message.listOfArguments.get(0).equals("error_logging_in")) {
					disconnectClient("Error logging in. Incorrect name.");

				}

				break;
		}
		
	}
	
	

}
