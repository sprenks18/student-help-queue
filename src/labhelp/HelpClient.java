/**
 * 
 */
package labhelp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

/**
 * The GUI client that students can use to add themselves or remove themselves
 * from the help queue.
 * 
 * @author sprenkle
 * 
 */
public class HelpClient extends JFrame {

	private static final Color WLU_BLUE = Color.decode("#003399");
	private static final int UPDATE_INTERVAL_IN_MILLISECONDS = 20000;
	private static final String LIST_COMMAND = HelpServer.LIST_COMMAND;
	private static final long serialVersionUID = 1L;
	private String username = System.getProperty("user.name");
	private String hostname = "";
	private Socket socket;
	private PrintWriter requestStream;
	private Scanner responseStream;
	private JLabel statusDisplay;
	private JTextArea waitingList;
	private JTextArea instWaitingList;
	private Timer timer;

	/**
	 * Create a new HelpClient interface
	 */
	private HelpClient() {
		setTitle("Help Client: " + username);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());

		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		createStatusDisplay();

		JPanel panel = (JPanel) getContentPane();
		panel.setLayout(new BorderLayout());
		panel.setBackground(Color.white);


		createWaitingListPanel(panel);
		createInstructorWaitingListPanel(panel);

		makeDashBoard(panel);
		
		add(statusDisplay, BorderLayout.SOUTH);


		ActionListener task = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				updateLists();
			}
		};

		timer = new Timer(UPDATE_INTERVAL_IN_MILLISECONDS, task);
		timer.start();
		
		pack();
		setVisible(true);
	}

	private void createWaitingListPanel(JPanel panel) {

		JPanel wlPanel = new JPanel();
		TitledBorder title = BorderFactory.createTitledBorder("Waiting List: ");
		wlPanel.setBorder(title);

		waitingList = new JTextArea(20, 40);
		waitingList.setEditable(false);
		waitingList.setText(performCommand(LIST_COMMAND));

		Font font = new Font("Arial", Font.PLAIN, 20);
		waitingList.setFont(font);
		waitingList.setForeground(WLU_BLUE);

		wlPanel.add(waitingList);

		panel.add(wlPanel);
	}

	
	private void createInstructorWaitingListPanel(JPanel panel) {

		JPanel instWLPanel = new JPanel();
		TitledBorder title = BorderFactory.createTitledBorder("Instructor Waiting List: ");
		instWLPanel.setBorder(title);
		instWLPanel.setLayout(new BorderLayout());
		
		JPanel buttonPanel = new JPanel();
		

		instWaitingList = new JTextArea(20, 40);
		instWaitingList.setEditable(false);
		instWaitingList.setText(performCommand(LIST_COMMAND));
		
		JButton instructorRequestButton = new JButton("Instructor, please!");
		instructorRequestButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addStudentToInstructorQueue();
			}
		});
		
		JButton answeredButton = new JButton("Question answered");
		answeredButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				questionAnswered(HelpServer.INSTRUCTOR_LIST, instWaitingList);
			}
		});
		
		buttonPanel.add(instructorRequestButton);
		buttonPanel.add(answeredButton);
		
		instWLPanel.add(buttonPanel, BorderLayout.NORTH);


		Font font = new Font("Arial", Font.PLAIN, 20);
		instWaitingList.setFont(font);
		instWaitingList.setForeground(WLU_BLUE);

		instWLPanel.add(instWaitingList);
		instWLPanel.setSize(50, instWLPanel.getPreferredSize().height);

		panel.add(instWLPanel, BorderLayout.EAST);
		
		System.out.println(instWLPanel.getSize());
		System.out.println(instWLPanel.getWidth());

	}
	
	private void createStatusDisplay() {
		statusDisplay = new JLabel();
		statusDisplay.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusDisplay.setAlignmentX(CENTER_ALIGNMENT);
		statusDisplay.setText("Connecting to server " + HelpConfiguration.SERVER_NAME);
	}

	/**
	 * Makes the initial connection to the server
	 */
	private void connectToServer() {
		try {
			socket = new Socket(HelpServer.SERVER_HOST, HelpServer.SERVER_PORT);
			requestStream = new PrintWriter(socket.getOutputStream(), true);
			responseStream = new Scanner(socket.getInputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.err.println("Don't know about host " + HelpServer.SERVER_HOST);
			System.exit(ABORT);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Couldn't get I/O for the connection to " + HelpServer.SERVER_HOST);
			System.exit(ABORT);
		}
	}

	@Override
	public void dispose() {
		try {
			questionAnswered(HelpServer.WAITLIST, waitingList);
			questionAnswered(HelpServer.INSTRUCTOR_LIST, instWaitingList);
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.dispose();
		System.exit(0);
	}

	/**
	 * Create the dashboard of buttons on the given panel
	 * 
	 * @param panel
	 */
	private void makeDashBoard(JPanel panel) {
		JPanel dashboard = new JPanel();
		dashboard.setBackground(WLU_BLUE);

		JButton questionButton = new JButton("I have a question!");
		questionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addStudentToQueue();
			}
		});
		dashboard.add(questionButton);

		JButton updateListButton = new JButton("Update lists");
		updateListButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateLists();
			}
		});
		dashboard.add(updateListButton);

		JButton questionAnsweredButton = new JButton("Question Answered!");
		questionAnsweredButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				questionAnswered(HelpServer.WAITLIST, waitingList);
			}
		});
		dashboard.add(questionAnsweredButton);
		
		panel.add(dashboard, BorderLayout.NORTH);
	}

	/**
	 * 
	 * @param command
	 * @return
	 */
	private String performCommand(String command) {
		connectToServer();
		// System.out.println("Attempting " + command);
		requestStream.println(command);
		statusDisplay.setText("Processing...");

		boolean done = false;
		while (!done) {

			if (responseStream.hasNextLine()) {
				String line = responseStream.nextLine();
				// handle response
				if (line != null) {
					String response = line.trim();
					// System.out.println("server response: " + response);
					statusDisplay.setText("");
					response = formatResponse(response);
					return response;
				}
			}
		}
		return null;
	}

	/**
	 * Format the response from the server
	 * 
	 * @param response
	 * @return
	 */
	private String formatResponse(String response) {
		String formattedString = response.substring(1, response.length() - 1);
		formattedString = formattedString.trim();
		if (formattedString.equals("")) {
			return formattedString;
		}
		StringBuilder builder = new StringBuilder();
		String[] studentList = formattedString.split(",  ");

		for (int i = 0; i < studentList.length; i++) {
			builder.append(i + 1);
			builder.append(". ");
			builder.append(studentList[i]);
			builder.append("\n");
		}
		return builder.toString();
	}

	/**
	 * Connect to server; get where they are on the list; update the view
	 */
	private void updateLists() {
		waitingList.setText(performCommand(LIST_COMMAND + HelpServer.WAITLIST ));
		instWaitingList.setText(performCommand(LIST_COMMAND + HelpServer.INSTRUCTOR_LIST ));
	}

	/**
	 * Connect to server; add the student to the queue
	 */
	private void addStudentToQueue() {
		String info = createInfo();
		waitingList.setText(performCommand("ADD " + username + " AT " + info));
		statusDisplay.setText("Added " + username + " to queue.");
	}
	
	/**
	 * Connect to server; add the student to the queue
	 */
	private void addStudentToInstructorQueue() {
		String info = createInfo();
		instWaitingList.setText(performCommand(HelpServer.ADD_TO_INSTRUCTOR_QUEUE + " " + username + " AT " + info));
		statusDisplay.setText("Added " + username + " to instructor queue.");
	}

	/**
	 * Connect to server; remove student from the list; update the view
	 */
	private void questionAnswered(String whichList, JTextArea textArea) {
		String info = createInfo();
		textArea.setText(performCommand(HelpServer.REMOVE_CMD + "_" + whichList + " " + username + " AT " + info));
		statusDisplay.setText("Removed " + username + " from queue.");
		updateLists();
	}

	/**
	 * Connect to server; remove all students from the list
	 */
	private void clearWaitingList() {
		performCommand(HelpServer.CLEAR_CMD);
	}

	/**
	 * Creates info about the host machine
	 * @return the host machine information
	 */
	private String createInfo() {
		String hostnameInfo = "";
		String hostLocal = hostname;
		if (hostname.contains(".")) { // fully qualified hostname
			hostLocal = hostname.substring(0, hostname.indexOf('.'));
		}
		if (HelpConfiguration.hostMap.containsKey(hostLocal)) {
			hostnameInfo = " - " + (String) HelpConfiguration.hostMap.get(hostLocal);
		}
		return hostname + hostnameInfo;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HelpClient client = new HelpClient();
		if (args.length > 0) {
			if (args[0].equals("clear") || args[0].equals("reset")) {
				System.out.println("Requesting wait list cleared");
				client.clearWaitingList();
				client.dispose();
			}
		}

	}

}
