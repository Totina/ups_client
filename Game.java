package ups;

import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Game {

	Stage gameWindow;
	
	GridPane cardsGrid;
	
	GridPane playerGrid;
	
	int playersInGrid = 0;
	
	// list of cards
	ArrayList<Card> listOfCards = new ArrayList<Card>();
	
	// list of players
	ArrayList<String> players = new ArrayList<String>();
	
	// number of games
	public int number_of_players = 0;
	
	// name of the client
	String clientName;
	
	// number of cards
	int numberOfCards = 0;
	
	Label winLabel;

	Label disconnectedLabel;
	
	Boolean started = false;
	
	// Buttons
	Button nextCardButton;
	Button noCardButton;
	
    
    Game(int number, String name) {
    	number_of_players = number;
    	clientName = name;
    }
    
	
	public void start() {
		gameWindow = new Stage();

		gameWindow.setTitle("Hra");
		gameWindow.setScene(getScene());
		gameWindow.setMinWidth(1000);
		gameWindow.setMinHeight(800);
		gameWindow.show();
		
	}
	
	private Scene getScene() {
		Scene scene = new Scene(getRoot());
		return scene;
	}
	
	private Parent getRoot() {
		BorderPane root = new BorderPane();
		
		root.setTop(getTopPart());
		root.setRight(getRightPart());
		root.setCenter(getCenterPart());
		root.setBottom(getBotPart());

		return root;		
	}
	
	// Creates top part - welcome
	private Node getTopPart() {
		
		VBox vb = new VBox();
		vb.setSpacing(10);
		vb.setPadding(new Insets(40));
		vb.setAlignment(Pos.CENTER);
		
		Label gamesLabel = new Label("Vítej ve hře jednadvacet, " + clientName + "!");
		gamesLabel.setStyle("-fx-font: 40 arial;");
		gamesLabel.setAlignment(Pos.CENTER);
		
		Label pLabel = new Label("Hra je založena pro " + number_of_players + " hráče");
		pLabel.setStyle("-fx-font: 30 arial;");
		pLabel.setAlignment(Pos.CENTER);		

		vb.getChildren().addAll(gamesLabel, pLabel);
		
		return vb;
	}
	
	// right part - protihraci
	private Node getRightPart() {
		playerGrid = new GridPane();
		playerGrid.setHgap(10);
		playerGrid.setVgap(20);
		playerGrid.setPadding(new Insets(30));
		//playerGrid.setAlignment(Pos.CENTER);
		
		Label label = new Label("Protihráči");
		label.setStyle("-fx-font: 28 arial;");
		playerGrid.add(label, 0, 0);
		
		return playerGrid;
	}
	
	// bottom part - back, leave, vypis
	private Node getBotPart() {
		HBox hb = new HBox();
		hb.setSpacing(10);
		hb.setPadding(new Insets(30));
		
		Button backButton = new Button("Zpět do lobby");
		backButton.setOnAction(event -> leave());
		
		winLabel = new Label("Čeká se na hráče.");
		winLabel.setStyle("-fx-font: 26 arial;");
		
		Button leaveButton = new Button("Opustit hru a odpojit se");
		leaveButton.setOnAction(event -> leaveWholeGame());
		
		hb.getChildren().addAll(backButton, leaveButton, winLabel);
				
		return hb;
	}
	
	// Center part - tvoje karty
	private Node getCenterPart() {
		
		VBox vb = new VBox();
		vb.setSpacing(10);
		vb.setPadding(new Insets(30));
		
		cardsGrid = new GridPane();
		cardsGrid.setHgap(10);
		cardsGrid.setVgap(20);
		cardsGrid.setPadding(new Insets(20, 0, 20, 0));
		
		Label cardsLabel = new Label("Tvoje karty");
		cardsLabel.setStyle("-fx-font: 26 arial;");
		
		nextCardButton = new Button("Další kartu");
		nextCardButton.setDisable(true);
		nextCardButton.setOnAction(event -> nextCard());
		
		noCardButton = new Button("Nechci další karty");
		noCardButton.setDisable(true);
		noCardButton.setOnAction(event -> noCard(nextCardButton, noCardButton));
		
		vb.getChildren().addAll(cardsLabel, cardsGrid, nextCardButton, noCardButton);
		
		return vb;
	}
	
	void nextCard() {
		ClientTCP.sendMessage(ClientConstants.GAME_PREFIX + " " + "card" + ClientConstants.END_CHAR);
	}
	
	void noCard(Button next, Button no) {
		ClientTCP.sendMessage(ClientConstants.GAME_PREFIX + " " + "no_thanks" + ClientConstants.END_CHAR);
		next.setDisable(true);
		no.setDisable(true);
	}
	
	void leave() {
		ClientTCP.sendMessage(ClientConstants.GAME_PREFIX + " " + "left_to_the_lobby" + ClientConstants.END_CHAR);
		Lobby.lobbyWindow.show();
		Lobby.warningLabel.setText("");
		ClientTCP.state = ClientState.LOBBY;
		gameWindow.close();
	}
	
	void leaveWholeGame() {
		ClientTCP.sendMessage(ClientConstants.GAME_PREFIX + " " + "left_the_game" + ClientConstants.END_CHAR);
		try {
			ClientTCP.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		gameWindow.close();
		System.exit(1);
		//Platform.exit();
	}
	
	private void addPlayer(String name) {
		
		Label label = new Label(name);
		playerGrid.add(label, 0, playersInGrid + 1);
		
		playersInGrid++;
	}
	
	void addPlayerToList(String player) {

		if(!player.equals(clientName)) {
			players.add(player);
			addPlayer(player);	
		}	
	}
	
	// server sent player left message
	void playerLeft(String name) {
		if(number_of_players < 3 && started) {
			winLabel.setText("Hráč " + name + " opustil hru. Hra je ukončena.");
			nextCardButton.setDisable(true);
			noCardButton.setDisable(true);
		}
		else {
			winLabel.setText("Hráč " + name + " opustil hru.");
		}
		
		number_of_players--;

		Label leftLabel = new Label("odešel ze hry");
		leftLabel.setTextFill(Color.RED);
		
		if(players.size() > 0) {
			int index = players.indexOf(name);
			System.out.println("index of the player who left: " + name);
			playerGrid.add(leftLabel, 1, index + 1);	
		}				
	}

	// player disconnected message received
	void playerDisconnected(String name) {
    	winLabel.setText("Hráč " + name + " se odpojil. Počkejte prosím než se připojí zpět.");

		disconnectedLabel = new Label("odpojen");
		disconnectedLabel.setTextFill(Color.ORANGE);

		if(players.size() > 0) {
			int index = players.indexOf(name);
			System.out.println("index of the player who disconnected: " + name);
			playerGrid.add(disconnectedLabel, 1, index + 1);
		}
	}

	// player reconnected message received
	void playerReconnected(String name) {
		winLabel.setText("Hráč " + name + " se vrátil do hry.");
		disconnectedLabel.setText("");
    }
	
	// add card label
	public void addCard(Card card) {
		
		if(card.pattern == 1) {
			Label label = new Label("srdcový(á) " + card.name);
			cardsGrid.add(label, 0, numberOfCards);
		}
		if(card.pattern == 2) {
			Label label = new Label("kulový(á) " + card.name);
			cardsGrid.add(label, 0, numberOfCards);
		}
		if(card.pattern == 3) {
			Label label = new Label("žaludový(á) " + card.name);
			cardsGrid.add(label, 0, numberOfCards);
		}
		if(card.pattern == 4) {
			Label label = new Label("listový(á) " + card.name);
			cardsGrid.add(label, 0, numberOfCards);
		}	
		
		numberOfCards++;		
	}
	
	// server sent win
	void win() {
		winLabel.setText("Vyhrál jsi!");	
	}
	
	// server sent lost
	void lose() {
		winLabel.setText("Prohrál jsi.");
	}
	
	void setLabel(String value) {
		winLabel.setText(value);
	}
	
	
}
