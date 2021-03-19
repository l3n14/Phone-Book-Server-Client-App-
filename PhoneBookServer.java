
 
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
/*
 * Leanid Paulouski 
 * 25.01.2021
 * Klasa serwera
 */

class PhoneBookServer extends JFrame implements ActionListener, Runnable {
	
	private static final long serialVersionUID = 1L;
	private static final Font font = new Font("MonoSpaced", Font.BOLD, 20);
	private static final Font font2 = new Font("MonoSpaced", Font.BOLD, 18);
	static final int SERVER_PORT = 25000;
	
	public static void main(String [] args){
		new PhoneBookServer();
	}
	
	
	
	public static JButton sendButton = new JButton("Send");
	
	private JComboBox<ClientThread> clientMenu = new JComboBox<ClientThread>();
	private JTextField messageField = new JTextField();
	private JTextArea  textArea  = new JTextArea();
	private JScrollPane scroll = new JScrollPane(textArea,
	  				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	
	PhoneBookServer(){ 
		super("Server");
	  	setSize(400,500);
	  	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	  	JPanel panel = new JPanel(null);
	  	setLayout(new BorderLayout());
	  	
	  	
	  	
	  	clientMenu.setPrototypeDisplayValue(new ClientThread("#########################"));
	  	clientMenu.setBounds(18,10,350,30);
	  	clientMenu.setFont(font);
	  	panel.add(clientMenu);
	 	
	 	
	 	messageField.addActionListener(this);
	 	messageField.setFont(font);
	 	messageField.setBounds(18, 410, 265, 40);
	  	panel.add(messageField);
	  	
	  	textArea.setLineWrap(true);
	  	textArea.setWrapStyleWord(true);
	  	textArea.setEditable(false);
	  	textArea.setFont(font);
	  	
	  	
	  	sendButton.setFont(font2);
		sendButton.setBounds(285, 410, 81, 39);
		sendButton.addActionListener(this);
		panel.add(sendButton);
	  	
		scroll.setBounds(18, 50, 350, 350);
	  	panel.add(scroll);
	  	
	  	setContentPane(panel);
	  	setVisible(true);
	  	
	  	new Thread(this).start(); 
	}
	
	synchronized public void printReceivedMessage(ClientThread client, String message){
		String text = textArea.getText();
		textArea.setText(text+client.getName() + " >>> " + message+"\n");
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}
	
	synchronized public void printSentMessage(ClientThread client, String message){
	  	String text = textArea.getText();
	  	textArea.setText(text+client.getName() + " <<< " + message+"\n");
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}
	
	synchronized public void printServerMessage(String message) {
		String text = textArea.getText();
		textArea.setText(text+"Server "+ " >>> " + message+"\n");
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}
	
	synchronized void addClient(ClientThread client){
	  	clientMenu.addItem(client);
	}  
		
	synchronized void removeClient(ClientThread client){
      	clientMenu.removeItem(client);
	}
	
	public void actionPerformed(ActionEvent event){
		String message;
		Object source = event.getSource();
	  	if (source==messageField || source==sendButton){
	  		ClientThread client = (ClientThread)clientMenu.getSelectedItem();
			if (client != null) {
				message = messageField.getText();
				printSentMessage(client, message);
				client.sendMessage(message);
				messageField.setText(null);
			}
	  	}
	  	
	}
	
	
	public void run() {
		boolean isCreated = false;
	
	
		try (ServerSocket serwer = new ServerSocket(SERVER_PORT)) {
			String host = InetAddress.getLocalHost().getHostName();
			isCreated = true;
			

			while (true) { 
				Socket socket = serwer.accept();
				if (socket != null) {
					
					new ClientThread(this, socket);
				}
			}
		} catch (IOException e) {
			System.out.println(e);
			if (!isCreated) {
				JOptionPane.showMessageDialog(null, "Server cant be created");
				System.exit(0);
			} else {
				JOptionPane.showMessageDialog(null, "Server error: cant be connected with client");
			}
		}
	}
	
} 



class ClientThread implements Runnable {
	
	private Socket socket;
	private String name;
	private PhoneBookServer Server;
	
	private ObjectOutputStream outputStream = null;
	
	ClientThread(String prototypeDisplayValue){
		name = prototypeDisplayValue;
	}
	
	ClientThread(PhoneBookServer server, Socket socket) { 
		Server = server;
	  	this.socket = socket;
	  	new Thread(this).start();  
	}
	
	public String getName(){ return name; }
	
	public String toString(){ return name; }
	
	public void sendMethod(String message) {
		Server.printServerMessage(message);
		sendMessage(message);
	}
	
	public void sendMessage(String message){
		try {
			outputStream.writeObject(message);
			if (message.equals("exit")){
				Server.removeClient(this);
				socket.close();
				socket = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	public void run(){  
		String message;
	   	try( ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
	   		 ObjectInputStream input = new ObjectInputStream(socket.getInputStream()); )
	   	{
	   		outputStream = output;
	   		name = (String)input.readObject();
	   		Server.addClient(this);
			while(true){
				message = (String)input.readObject();
				Server.printReceivedMessage(this,message);
				try {
				if (message.equals("BYE")){
					Server.removeClient(this);
					break;		
				}
				String[] txt = message.split(" ");
				if(txt[0].equals("PUT")) {
					sendMethod(PhoneBook.put(txt[1], txt[2]));
					
					
				}
				if(txt[0].equals("GET")) {
					sendMethod(PhoneBook.get(txt[1]));
					
				}
				if(txt[0].equals("REMOVE") || txt[0].equals("delete")) {
					sendMethod(PhoneBook.delete(txt[1]));
					
				}
				if(txt[0].equals("LIST")) {
					sendMethod(PhoneBook.list());
					
				}
				if(txt[0].equals("LOAD")) {
					sendMethod(PhoneBook.load(txt[1]));
					
				}
				if(txt[0].equals("SAVE")) {
					sendMethod(PhoneBook.save(txt[1]));
					
				}
				if(txt[0].equals("REPLACE")) {
					sendMethod(PhoneBook.replace(txt[1],txt[2]));
					
				}
				}catch(Exception ex) {
					sendMethod("error command");
					
				}
			}
			socket.close();
			socket = null;
	   	} catch(Exception e) {
	   		Server.removeClient(this);
	   	}
	}
	
}
