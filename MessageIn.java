package ups;

import java.util.ArrayList;


public class MessageIn {

	private String message;

	public int numberOfArguments = 0;
	
	public char prefix;
	
	public boolean isCorrectMessage = true;

	public ArrayList<String> listOfArguments;
	
	
	public MessageIn(String msg) {
		message = msg;
		if (message != null) {
			if (isCorrectLengthMessage(msg)) {
				if (isCorrectPrefix()) {
					prefix = message.charAt(0);
					separateArguments();
				} else {
					isCorrectMessage = false;
				}
			} else {
				isCorrectMessage = false;
			}
		}
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String msg) {
		message = msg;
	}

	// check if message is correct length
	private boolean isCorrectLengthMessage(String msg) {
		if (msg.length() >= ClientConstants.MIN_LENGTH_MESSAGE && msg.length() <= ClientConstants.MAX_LENGTH_MESSAGE) {
			return true;
		} else {
			System.err.println("Incorrect length of the message");
			return false;
		}

	}

	// check if message has correct prefix
	private boolean isCorrectPrefix() {
		switch (message.charAt(0)) {
		case ClientConstants.LOGIN_PREFIX:
			return true;
		case ClientConstants.LOBBY_PREFIX:
			return true;
		case ClientConstants.GAME_PREFIX:
			return true;
		case ClientConstants.PING_PREFIX:
			return true;
		case ClientConstants.ERROR_PREFIX:
				return true;
		default:
			return false;
		}

	}

	@Override
	public String toString() {
		String words = "";
		for (String string : listOfArguments) {
			words += string + ", ";
		}
		return words;
	}

	// separate arguments
	private void separateArguments() {
		String[] arguments = message.split(" ");
		if (arguments.length > 1) {
			listOfArguments = new ArrayList<String>();
		} else {
			isCorrectMessage = false;
			return;
		}

		for (int i = 1; i < arguments.length; i++) {
			if (i == arguments.length - 1) {
				if (arguments[i].contains(ClientConstants.END_CHAR)) {
					listOfArguments.add(arguments[i].substring(0, arguments[i].indexOf(ClientConstants.END_CHAR)));
				} else {
					isCorrectMessage = false;
					return;
				}
			} else {
				listOfArguments.add(arguments[i]);
			}
		}

		if (listOfArguments != null)
			numberOfArguments = listOfArguments.size();
	}
	
	
	
}
