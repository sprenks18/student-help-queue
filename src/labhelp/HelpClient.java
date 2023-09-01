/**
 * 
 */
package labhelp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
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
	private static final int UPDATE_INTERVAL_IN_MILLISECONDS = 30000;
	private static final String LIST_COMMAND = HelpServer.LIST_COMMAND;
	private static final long serialVersionUID = 1L;
	private String username = System.getProperty("user.name");
	private String hostname = "";
	private Socket socket;
	private PrintWriter requestStream;
	private Scanner responseStream;
	private JTextArea waitingList;
	private JTextArea instWaitingList;
	private Timer timer;
	
	private int HEIGHT = 400;

	/**
	 * Create a new HelpClient interface
	 */
	private HelpClient() {
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());

		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		setTitle("Lab Help");
		
		JPanel panel = (JPanel) getContentPane();
		panel.setBackground(Color.white);		
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		makeDashBoard(panel);

		createWaitingListPanel(panel);
		createInstructorWaitingListPanel(panel);

		ActionListener task = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				updateLists();
			}
		};

		timer = new Timer(UPDATE_INTERVAL_IN_MILLISECONDS, task);
		timer.start();
		
		//add(listPanel, BorderLayout.CENTER);
		
		pack();
		setVisible(true);
	}

	private void createWaitingListPanel(JPanel panel) {

		JPanel wlPanel = new JPanel();
		TitledBorder title = BorderFactory.createTitledBorder("Waiting List: ");
		wlPanel.setBorder(title);
		wlPanel.setLayout(new BorderLayout());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(WLU_BLUE);

		waitingList = new JTextArea(20, 40);
		waitingList.setEditable(false);
		waitingList.setText(performCommand(HelpServer.LIST_COMMAND + HelpServer.WAITLIST));

		Font font = new Font("Arial", Font.PLAIN, 20);
		waitingList.setFont(font);
		waitingList.setForeground(WLU_BLUE);
		
		JButton questionButton = new JButton("I have a question!");
		questionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addStudentToQueue();
			}
		});
		buttonPanel.add(questionButton);

		JButton questionAnsweredButton = new JButton("Question Answered!");
		questionAnsweredButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				questionAnswered(HelpServer.WAITLIST, waitingList);
			}
		});
		buttonPanel.add(questionAnsweredButton);
		
		wlPanel.add(buttonPanel, BorderLayout.NORTH);
		wlPanel.add(waitingList);

		wlPanel.setPreferredSize(new Dimension(400, HEIGHT));

		panel.add(wlPanel);
	}

	
	private void createInstructorWaitingListPanel(JPanel panel) {

		JPanel instWLPanel = new JPanel();
		TitledBorder title = BorderFactory.createTitledBorder("Instructor Waiting List: ");
		instWLPanel.setBorder(title);
		instWLPanel.setLayout(new BorderLayout());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(WLU_BLUE);
		
		instWaitingList = new JTextArea(20, 40);
		instWaitingList.setEditable(false);
		instWaitingList.setText(performCommand(LIST_COMMAND + HelpServer.INSTRUCTOR_LIST));
		
		/*
		JScrollPane scrollV = new JScrollPane(instWaitingList);
		scrollV.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollV.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		*/
		
		JButton instructorRequestButton = new JButton("Instructor, please!");
		instructorRequestButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addStudentToInstructorQueue();
			}
		});
		
		JButton answeredButton = new JButton("Question Answered!");
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
		instWaitingList.setBackground(Color.LIGHT_GRAY);

		instWLPanel.add(instWaitingList);
		instWLPanel.setPreferredSize(new Dimension(getSize().width, 200));
		panel.add(instWLPanel);
		
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

		
		JButton updateListButton = new JButton("Update lists");
		updateListButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateLists();
			}
		});
		dashboard.add(updateListButton);
		
		panel.add(dashboard);
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

		boolean done = false;
		while (!done) {

			if (responseStream.hasNextLine()) {
				String line = responseStream.nextLine();
				// handle response
				if (line != null) {
					String response = line.trim();
					// System.out.println("server response: " + response);
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
		performCommand( HelpServer.ADD_TO_WAITLIST + username + " AT " + info);
		updateLists();
	}
	
	/**
	 * Connect to server; add the student to the queue
	 */
	private void addStudentToInstructorQueue() {
		String info = createInfo();
		performCommand(HelpServer.ADD_TO_INSTRUCTOR_QUEUE + username + " AT " + info);
		updateLists();
	}

	/**
	 * Connect to server; remove student from the list; update the view
	 */
	private void questionAnswered(String whichList, JTextArea textArea) {
		String info = createInfo();
		textArea.setText(performCommand(HelpServer.REMOVE_CMD + whichList + username + " AT " + info));
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
			hostnameInfo = (String) HelpConfiguration.hostMap.get(hostLocal);
		}
		else {
			hostnameInfo = hostLocal;
		}
		return hostnameInfo;
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
