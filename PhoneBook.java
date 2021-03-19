import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
/*
 * Leanid Paulouski 
 * 25.01.2021
 * Klasa książki telefonicznej
 */
public class PhoneBook implements Serializable {

	private static ConcurrentHashMap<String, String> book = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, String> currentBook = new ConcurrentHashMap<>();
	private static final long serialVersionUID = 1L;

	public PhoneBook(ConcurrentHashMap<String, String> booker) {
		this.currentBook = booker;
	}

	public static String save(String fileName) {

		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
			oos.writeObject(new PhoneBook(book));
		} catch (Exception ex) {
			return "Error " + ex.getMessage();
		}
		return "OK";
	}

	public static String load(String nazwa_pliku) {
		FileInputStream file_stream = null;
		ObjectInputStream object_stream = null;
		PhoneBook phone_book = null;

		try {
			file_stream = new FileInputStream(nazwa_pliku);
			object_stream = new ObjectInputStream(file_stream);

			phone_book = (PhoneBook) object_stream.readObject();
			book = phone_book.currentBook;

			object_stream.close();
			file_stream.close();
		}

		catch (IOException exce) {
			return "Error "+exce.getMessage();
		}

		catch (ClassNotFoundException ex) {
			return "Error "+ex.getMessage();
		}
		return "OK";

	}

	/*
	 * public static String load(String fileName) throws Exception { try
	 * (ObjectInputStream ois = new ObjectInputStream(new
	 * FileInputStream(fileName))) { if (!book.isEmpty()) { book.clear(); }
	 * PhoneBook currentBook =new PhoneBook (ois.readObject()); } catch (Exception
	 * ex) {
	 * 
	 * return "Error "+ex.getMessage();
	 * 
	 * } return "OK"; }
	 */

	public static String list() {
		StringBuilder sb = new StringBuilder();
		sb.append(" : ");
		for (ConcurrentHashMap.Entry<String, String> item : book.entrySet()) {
			sb.append(item.getKey() + ", ");
		}
		if (sb.toString().equals("")) {
			return "Error: no contacts!";
		}
		return "Ok " + sb.toString();
	}

	public static String delete(String name) {
		if (book.containsKey(name) == false) {
			return "Error: contact not found!";
		}
		book.remove(name);
		return "OK";
	}

	public static String get(String name) {
		if (book.containsKey(name) == false) {
			return "Error: contact not found!";
		}
		try {
			return "Name:" + name + "  Number: " + book.get(name);
		} catch (Exception e) {
			return "Error: contact not found!";
		}
	}

	public static String put(String name, String number) {
		book.put(name, number);
		return "OK";
	}

	public static String replace(String name, String number) {
		book.remove(name);
		book.put(name, number);
		return "OK";
	}
}
