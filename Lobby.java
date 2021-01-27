package ups;


import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Lobby {

	static Stage lobbyWindow;
	
	static GridPane grid;
	
	static Label warningLabel;
	
	// instance
	private static Lobby LobbyInstance = null;
	
	// number of games
	public static int number_of_games = 0;
	
	// list labelu
	static ArrayList<Label> labels = new ArrayList<Label>();
	
	
	// get instance
    public static Lobby getInstance() { 
        return LobbyInstance; 
    } 
    
    private Lobby(int number) {
    	number_of_games = number;
    }
    
	public static void init(int number_of_games) {
		LobbyInstance = new Lobby(number_of_games);
	}
	
	public static void start() {
		lobbyWindow = new Stage();

		lobbyWindow.setTitle("Lobby");
		lobbyWindow.setScene(getScene());
		lobbyWindow.setMinWidth(1000);
		lobbyWindow.setMinHeight(500);
		lobbyWindow.show();
		
	}

	private static Scene getScene() {
		Scene scene = new Scene(getRoot());
		return scene;
	}
	
	private static Parent getRoot() {
		BorderPane root = new BorderPane();
		
		root.setCenter(getCenterPart());
		root.setBottom(getBotPart());

		return root;		
	}
	
	// Creates center part
	private static Node getCenterPart() {
		grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(20);
		grid.setPadding(new Insets(30));

		Label gamesLabel = new Label("Seznam herních místností ");
		grid.add(gamesLabel, 0, 0, 3, 1);
		
		for (int i = 0; i < number_of_games; i++) {
			addGame(i);
		}
		
		
		return grid;
	}
	
	// Creates bot part
	private static Node getBotPart() {

		HBox hb = new HBox();
		hb.setSpacing(10);
		hb.setPadding(new Insets(30));
		
		Button leaveButton = new Button("Opustit lobby a odpojit se.");
		leaveButton.setOnAction(event -> leaveWholeGame());
		
		warningLabel = new Label("");
		warningLabel.setStyle("-fx-font: 20 arial;");
		warningLabel.setTextFill(Color.RED);
		
		hb.getChildren().addAll(leaveButton, warningLabel);
		
		return hb;
	}
	
	private static void addGame(int id) {
		
		Label label = new Label("Game " + id);
		grid.add(label, 0, id + 1);
		
		Button button = new Button("Vstoupit");
		grid.add(button, 1, id + 1);
				
		button.setOnMouseClicked((MouseEvent event1) -> {			
			ClientTCP.sendMessage(ClientConstants.LOBBY_PREFIX + " " + "enter"
					+ " " + (id) + ClientConstants.END_CHAR);
		});
		
		Button infoButton = new Button("Info o hře");
		grid.add(infoButton, 2, id + 1);
		
		infoButton.setOnAction(event -> gimmeInfo(id));
		
		Label labelInfo = new Label("");
		labels.add(labelInfo);
		grid.add(labelInfo, 3, id + 1);
		
		
	}
	
	// leave the game
	static void leaveWholeGame() {
		ClientTCP.sendMessage(ClientConstants.LOBBY_PREFIX + " " + "left_the_game" + ClientConstants.END_CHAR);
		// message
		try {
			ClientTCP.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		lobbyWindow.close();
		System.exit(1);
		//Platform.exit();
		
	}
	
	// ask for info about the room
	static void gimmeInfo(int id) {
		ClientTCP.sendMessage(ClientConstants.LOBBY_PREFIX + " " + "game"
				+ " " + (id) + ClientConstants.END_CHAR);
	
	}
	
	// info about game from servr
	static void infoAboutGame(int id, int state, int players) {
			
		String stateString = "";
		String enter = "NE";
		
		if(state == 0) {
			stateString = "Prázdná";
			labels.get(id).setTextFill(Color.GREEN);
			enter = "ANO";
		}
		else if(state == 1) {
			stateString = "Čeká na hráče";
			labels.get(id).setTextFill(Color.GREEN);
			enter = "ANO";
		}
		else if(state == 2) {
			stateString = "Plná";
			labels.get(id).setTextFill(Color.RED);
		}
		else if(state == 3) {
			stateString = "Ve hře";
			labels.get(id).setTextFill(Color.RED);
		}
		else if(state == 4) {
			stateString = "Ukončená";
			labels.get(id).setTextFill(Color.RED);
		}
		
		
		labels.get(id).setText("Stav hry: " + stateString + " | Počet hráčů ve hře: " + players + " | Můžeš vstoupit: " + enter);
		
	}
	
	static void cantEnter() {
		warningLabel.setText("Nemůžeš vstoupit do místnosti. Je buď plná nebo probíhá hra.");
	}

}
