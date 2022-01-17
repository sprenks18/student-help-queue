package labhelp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class HelpServer {

	private static final String REMOVE_USER_FROM_LIST = "REMOVE";
	public static final int SERVER_PORT = HelpConfiguration.SERVER_PORT;
	public static final String SERVER_HOST = HelpConfiguration.SERVER_NAME;

	public static void main(String[] args) {
		System.out.println("Starting server on " + SERVER_PORT);

		ServerSocket svr1;
		List<String> students = new ArrayList<String>();

		try {
			svr1 = new ServerSocket(SERVER_PORT);
			System.out.println("Listening for requests");

			boolean done = false;
			while (!done) {
				Socket incoming = svr1.accept();
				Scanner scanner = new Scanner(incoming.getInputStream());
				PrintWriter out = new PrintWriter(incoming.getOutputStream(),
						true);
				if (!scanner.hasNextLine()) {
					continue;
				}
				String command = scanner.nextLine();
				System.out.println("Received command: " + command);
				// check if enqueue or dequeue
				if (command == null)
					continue;
				else if (command.startsWith("ADD")) {
					String username = extractInfoFromCommand(command);
					if (!students.contains(username)) {
						students.add(username);
					}
					System.out.println(students);
					out.println(students);
				} else if (command.startsWith("LIST")) {
					System.out.println(students);
					out.println(students);
				} else if (command.startsWith(REMOVE_USER_FROM_LIST)) {
					String username = extractInfoFromCommand(command);
					Iterator<String> studentIter = students.iterator();
					while (studentIter.hasNext()) {
						if (studentIter.next().equals(username)) {
							studentIter.remove();
							break;
						}
					}
					out.println(students);
					System.out.println(students);
				} else if(command.startsWith("CLEAR")) {
					students.clear();
					out.println("Cleared waiting list");
				}
				else
					out.println("Echo:" + command.trim().toUpperCase());
				scanner.close();
			}

			svr1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static String extractInfoFromCommand(String command) {
		String info = command.substring(command.indexOf(' '));
		return info;
	}
	
	private static String extractUsernameFromCommand(final String command) {
		String[] data = command.split("\\s");
		String username = data[1];
		return username;
	}

}
