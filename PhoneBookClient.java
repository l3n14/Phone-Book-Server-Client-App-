import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

/*
 * Leanid Paulouski 
 * 25.01.2021
 * Klasa klienta
 */

public class PhoneBookClient extends JFrame implements ActionListener, Runnable {

	private static final long serialVersionUID = 1L;
	private static final Font font = new Font("MonoSpaced", Font.BOLD, 20);
	private static final Font font2 = new Font("MonoSpaced", Font.BOLD, 18);
	static final int SERVER_PORT = 25000;
	private String name;
	private String serverHost;
	private Socket socket;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;

	public static void main(String[] args) throws InterruptedException {

		String name;
		String host;

		host = JOptionPane.showInputDialog("Enter server adress");
		name = JOptionPane.showInputDialog("Enter yours name");
		if (name != null && !name.equals("")) {
			new PhoneBookClient(name, host);
		}

	}

	public static JPanel mainPanel = new JPanel(null);
	public static JButton sendButton = new JButton("Send");
	public static JTextArea chatArea = new JTextArea();
	public static JScrollPane textPane = new JScrollPane(chatArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	public static JTextField messageField = new JTextField();


	void createUI() {
		setSize(400, 500);
		setDefaultCloseOperation(3);
		setLayout(new BorderLayout());
		setResizable(false);
		setTitle(name);

		mainPanel.setBounds(0, 0, 400, 500);



		textPane.setFont(font);
		textPane.setBounds(18, 10, 350, 390);
		mainPanel.add(textPane);

		messageField.setFont(font);
		messageField.setBounds(18, 410, 265, 40);
		messageField.addActionListener(this);
		mainPanel.add(messageField);

		chatArea.setFont(font);
		chatArea.setEditable(false);

		sendButton.setFont(font2);
		sendButton.setBounds(285, 410, 81, 39);
		sendButton.addActionListener(this);
		mainPanel.add(sendButton);

		setContentPane(mainPanel);
		setVisible(true);
	}

	PhoneBookClient(String name, String host) {
		super(name);
		this.name = name;
		this.serverHost = host;
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				try {
					outputStream.close();
					inputStream.close();
					socket.close();
				} catch (IOException e) {
					System.out.println(e);
				}
			}

			@Override
			public void windowClosed(WindowEvent event) {
				windowClosing(event);
			}
		});
		createUI();
		setVisible(true);
		new Thread(this).start();
	}

	synchronized public void printReceivedMessage(String message) {
		String text = chatArea.getText();
		chatArea.setText(text + ">>> " + message+"\n");
		chatArea.setCaretPosition(chatArea.getDocument().getLength());
	}

	synchronized public void printSentMessage(String message) {
		String text = chatArea.getText();
		chatArea.setText(text+"<<< " + message+"\n");
		chatArea.setCaretPosition(chatArea.getDocument().getLength());
	}

	public void actionPerformed(ActionEvent event) {
		String message;
		Object source = event.getSource();
		if (source == messageField || source==sendButton) {
			try {
				message = messageField.getText();
				outputStream.writeObject(message);
				printSentMessage(message);
				if (message.equals("exit")) {
					inputStream.close();
					outputStream.close();
					socket.close();
					setVisible(false);
					dispose();
					return;
				}
				messageField.setText(null);
			} catch (IOException e) {
				System.out.println("Wyjatek klienta " + e);
			}
		}
		
	}

	public void run() {
		if (serverHost.equals("")) {
			serverHost = "localhost";
		}
		try {
			socket = new Socket(serverHost, SERVER_PORT);
			inputStream = new ObjectInputStream(socket.getInputStream());
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			outputStream.writeObject(name);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Connection could not be created");
			setVisible(false);
			dispose(); 
			return;
		}
		try {
			while (true) {
				String message = (String) inputStream.readObject();
				printReceivedMessage(message);
				if (message.equals("exit")) {
					inputStream.close();
					outputStream.close();
					socket.close();
					setVisible(false);
					dispose();
					break;
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Connection was interrupted!");
			setVisible(false);
			dispose();
		}
	}

}
