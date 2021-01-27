package ups;


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ClientMain extends Application{
	
	// ip
	TextField ipField;
	// port
	TextField portField;
	// name field
	TextField nameField;

	Button connectButton;
	Label label;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage stage) throws Exception {		
		stage.setTitle("Připojení k serveru");		
		stage.setScene(getScene());
		stage.setMinWidth(500);
		stage.setMinHeight(300);

		stage.show();
	}
	
	private Scene getScene() {
		Scene scene = new Scene(getRoot());
		return scene;
	}
	
	// creates root element of the scene - BorderPane
	private Parent getRoot() {
		BorderPane root = new BorderPane();
		
		//adding child elements to the BorderPane		
		root.setCenter(getButtons());
		
		return root;		
	}
	
	
	// Creates panel with buttons
	private Node getButtons() {

		// grid
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(30));
				
		// ip
		Label ipLabel = new Label("IP adresa: ");
		grid.add(ipLabel, 0, 0);	
		
		ipField = new TextField();
		ipField.setPromptText("Vložte ip adresu (př. 127.0.0.1)");
		ipField.setText("127.0.0.1");
		ipField.setPrefColumnCount(25);
		grid.add(ipField, 1, 0);
		
		// port
		Label portLabel = new Label("Port: ");
		grid.add(portLabel, 0, 1);	
		
		portField = new TextField();
		portField.setPromptText("Vložte číslo portu");
		portField.setText("5000");
		portField.setPrefColumnCount(25);
		grid.add(portField, 1, 1);
		
		// name
		Label nameLabel = new Label("Jméno: ");
		grid.add(nameLabel, 0, 2);
		
		nameField = new TextField();
		nameField.setPromptText("Vložte svoje jméno (2 - 20 znaků)");
		nameField.setText("Jmeno");
		nameField.setPrefColumnCount(25);
		grid.add(nameField, 1, 2);
		
		// login
		connectButton = new Button("Připojit");
		grid.add(connectButton, 0, 3);
		
		// connect label
		label = new Label("");
		label.setTextFill(Color.RED);
		grid.add(label, 0, 4, 2, 1);
		
		connectButton.setOnAction(event -> connect());

		return grid;
	}
	
	
	private void connect() {

		if (nameField.getText().isEmpty()) {
			label.setText("Pole jméno nemůže být prázdné.");
		}
		else {
			String ip = ipField.getText();
			int port = Integer.parseInt(portField.getText());
			String name = nameField.getText();

			ClientTCP client = ClientTCP.getInstance();
			client.init(ip, port, name, label, connectButton);
		}
	}

	@Override
	public void stop(){
		System.exit(1);
	}
	

	
}

