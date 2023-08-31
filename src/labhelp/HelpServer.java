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

	public static final String CLEAR_CMD = "CLEAR";

	public static final String ADD_CMD = "ADD";
	public static final String ADD_TO_WAITLIST = ADD_CMD + "_WL";
	public static final String ADD_TO_INSTRUCTOR_QUEUE = ADD_CMD + "_INST";

	public static final String REMOVE_CMD = "REMOVE";
	public static final String REMOVE_USER_FROM_WAITLIST = REMOVE_CMD + "_WL";
	public static final String REMOVE_USER_FROM_INST_QUEUE = REMOVE_CMD + "_INST";
	
	public static final String LIST_COMMAND = "LIST";

	public static final int SERVER_PORT = HelpConfiguration.SERVER_PORT;
	public static final String SERVER_HOST = HelpConfiguration.SERVER_NAME;

	public static void main(String[] args) {
		System.out.println("Starting server on " + SERVER_PORT);

		ServerSocket svr1;
		List<String> studentsWaiting = new ArrayList<String>();
		List<String> studentsWaitingForInstructor = new ArrayList<>();

		try {
			svr1 = new ServerSocket(SERVER_PORT);
			System.out.println("Listening for requests");

			boolean done = false;
			while (!done) {
				Socket incoming = svr1.accept();
				Scanner scanner = new Scanner(incoming.getInputStream());
				PrintWriter out = new PrintWriter(incoming.getOutputStream(), true);
				if (!scanner.hasNextLine()) {
					continue;
				}
				String command = scanner.nextLine();
				System.out.println("Received command: " + command);
				// check if enqueue or dequeue
				if (command == null)
					continue;
				else if (command.startsWith(ADD_CMD)) {
					List<String> whichList = command.startsWith(ADD_TO_WAITLIST)?studentsWaiting:studentsWaitingForInstructor;
					String username = extractInfoFromCommand(command);
					if (!whichList.contains(username)) {
						whichList.add(username);
					}
					System.out.println(whichList);
					out.println(whichList);
				} else if (command.startsWith("LIST")) {
					System.out.println(studentsWaiting);
					out.println(studentsWaiting);
				} else if (command.startsWith(REMOVE_CMD)) {
										
					List<String> whichList = command.startsWith(REMOVE_USER_FROM_WAITLIST)?studentsWaiting:studentsWaitingForInstructor;
					
					String username = extractInfoFromCommand(command);
					Iterator<String> studentIter = whichList.iterator();
					while (studentIter.hasNext()) {
						if (studentIter.next().equals(username)) {
							studentIter.remove();
							break;
						}
					}
					out.println(whichList);
					System.out.println(whichList);
				} else if (command.startsWith(CLEAR_CMD)) {
					studentsWaiting.clear();
					studentsWaitingForInstructor.clear();
					System.out.println("Cleared waiting lists");
					out.println(studentsWaiting);
					out.println(studentsWaitingForInstructor);
				} else
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
