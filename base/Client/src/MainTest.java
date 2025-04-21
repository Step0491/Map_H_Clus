import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * La classe MainTest gestisce l'interazione con un server per caricare e
 * apprendere dendrogrammi. Fornisce un menu per scegliere tra diverse
 * operazioni: caricare un dendrogramma da file, apprendere un dendrogramma da
 * un database o terminare l'esecuzione.
 */
public class MainTest {

	private ObjectOutputStream out;
	private ObjectInputStream in;

	/**
	 * Costruisce un nuovo oggetto MainTest connettendosi al server all'indirizzo IP
	 * e porta specificati.
	 * 
	 * @param ip   L'indirizzo IP del server.
	 * @param port La porta del server.
	 * @throws IOException Se si verifica un errore di I/O durante la connessione.
	 */
	public MainTest(String ip, int port) throws IOException {
		InetAddress addr = InetAddress.getByName(ip);
		System.out.println("addr = " + addr);
		Socket socket = new Socket(addr, port);
		System.out.println(socket);

		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
	}

	/**
	 * Mostra un menu all'utente per scegliere un'operazione.
	 * 
	 * @return La scelta dell'utente come intero.
	 */
	private int menu() {
		int answer;
		System.out.println("Scegli una opzione");
		do {
			System.out.println("(1) Carica Dendrogramma da File");
			System.out.println("(2) Apprendi Dendrogramma da Database");
			System.out.println("(3) Termina Esecuzione");
			System.out.print("Risposta: ");
			answer = Keyboard.readInt();
			if (answer <= 0 || answer > 3)
				System.out.println("Errore, valore invalido");
		} while (answer <= 0 || answer > 3);
		return answer;
	}

	/**
	 * Carica i dati dal server richiedendo la lista delle tabelle disponibili e
	 * selezionando una tabella.
	 * 
	 * @throws IOException            Se si verifica un errore di I/O durante la
	 *                                comunicazione con il server.
	 * @throws ClassNotFoundException Se la classe deserializzata non è trovata.
	 */
	private void loadDataOnServer() throws IOException, ClassNotFoundException {
		boolean tableSelected = false;
		String tableName = null;
		boolean check = false;
		// Ciclo while per garantire che l'utente selezioni una tabella valida
		while (!tableSelected) {
			// Codice per richiedere e verificare la tabella selezionata
			out.writeObject(0); // Richiedo la lista delle tabelle
			String[] tableNames = (String[]) in.readObject();
			System.out.println("Tabelle disponibili nel database:");
			for (String name : tableNames) {
				System.out.println("- " + name);
			}
			do {
				System.out.println("Nome tabella:");
				tableName = Keyboard.readString();
				for (String t : tableNames) {
					if (tableName.equals(t)) {
						check = true;
					}
				}
				if (!check)
					System.out
							.println("Errore: La tabella specificata non esiste o non è valida. Per favore, riprova.");
			} while (!check);
			out.writeObject(tableName);
			String response = (String) in.readObject();
			if (response.equals("OK")) {
				tableSelected = true;
			} else {
				System.out.println("Errore: La tabella specificata non esiste o non è valida. Per favore, riprova.");
			}
		}
	}

	/**
	 * Carica un dendrogramma da un file presente sul server.
	 * 
	 * @throws IOException            Se si verifica un errore di I/O durante la
	 *                                comunicazione con il server.
	 * @throws ClassNotFoundException Se la classe deserializzata non è trovata.
	 */
	private void loadDedrogramFromFileOnServer() throws IOException, ClassNotFoundException {
		out.writeObject(1);
		String[] fileNames = (String[]) in.readObject(); // ricevo dal server la lista dei file presenti
		if (fileNames != null && fileNames.length > 0 && !fileNames[0].equals("Non sono presenti file")) {
			System.out.println("File disponibili sul server:");
			for (String fileName : fileNames) {
				System.out.println("- " + fileName);
			}
			boolean check = false;
			String fileName = null;
			do {
				System.out.println("Inserire il nome dell'archivio (comprensivo di estensione '.dat'):");
				fileName = Keyboard.readString();
				if (!fileName.endsWith(".dat"))
					fileName = fileName + ".dat";
				for (String f : fileNames) {
					if (fileName.equals(f)) {
						check = true;
					}
				}
				if (!check)
					System.out.println("Errore: Il file specificato non esiste o non è valido. Per favore, riprova.\n");
			} while (!check);
			out.writeObject(fileName);
			String response = (String) in.readObject();
			if (response.startsWith("Oggetto HierarchicalClusterMiner caricato")) {
				System.out.println((String) in.readObject());
				// stampo il dendrogramma che il server mi sta inviando
			} else {
				System.out.println("Errore: Il file specificato non esiste o non è valido. Per favore, riprova.");
			}
		} else {
			System.out.println("Errore: Non sono presenti file sul server!");
		}
	}

	/**
	 * Apprende un dendrogramma da un database caricando i dati precedentemente
	 * selezionati.
	 * 
	 * @throws IOException            Se si verifica un errore di I/O durante la
	 *                                comunicazione con il server.
	 * @throws ClassNotFoundException Se la classe deserializzata non è trovata.
	 */
	private void mineDedrogramOnServer() throws IOException, ClassNotFoundException {
		// la tabella e' gia' stata già selezionata all'inizio
		out.writeObject(2);
		String response = (String) in.readObject();
		if (response.equals("OK")) {
			int depth;
			int maxDepth = (int) in.readObject();
			do {
				System.out.println("Introdurre la profondità del dendrogramma:");
				depth = Keyboard.readInt();
				if (depth <= 0 || depth > maxDepth)
					System.out.println("Errore, valore invalido");
			} while (depth <= 0 || depth > maxDepth);
			out.writeObject(depth);

			int dType;
			do {
				System.out.println("Distanza: single-link (1), average-link (2):");
				dType = Keyboard.readInt();
				if (dType <= 0 || dType > 2)
					System.out.println("Errore, valore invalido");
			} while (dType <= 0 || dType > 2);
			out.writeObject(dType);

			response = (String) in.readObject();
			if (response.equals("Clustering gerarchico completato.")) {
				System.out.println((String) in.readObject()); // stampo il dendrogramma che il server mi sta inviando
				boolean saveSuccessful = false;
				while (!saveSuccessful) {
					System.out.println("Inserire il nome dell'archivio (comprensivo di estensione):");
					String fileName = Keyboard.readString();
					out.writeObject(fileName);
					String saveResponse = (String) in.readObject();
					if (saveResponse.startsWith("Oggetto HierarchicalClusterMiner salvato")) {
						System.out.println(saveResponse); // conferma salvataggio
						saveSuccessful = true;
					} else {
						System.out.println("Errore durante il salvataggio. Per favore, riprova.");
						saveSuccessful = false;
					}
				}
			} else {
				System.out.println(response); // stampo il messaggio di errore
			}
		} else {
			System.out.println("Errore: La tabella specificata non esiste o non è valida. Per favore, riprova.");
		}
	}

	/**
	 * Punto di ingresso del programma. Gestisce l'interazione con l'utente e
	 * comunica con il server.
	 * 
	 * @param args Argomenti della riga di comando (non utilizzati).
	 */
	public static void main(String[] args) {
		String ip = args[0];
		int port = Integer.parseInt(args[1]);
		MainTest main = null;
		int response = -1;
		try {
			main = new MainTest(ip, port);
			boolean continueProgram = true;
			while (continueProgram) {
				main.loadDataOnServer();
				int choice = main.menu();
				if (choice == 1) {
					main.loadDedrogramFromFileOnServer();
				} else if (choice == 2) {
					main.mineDedrogramOnServer();
				} else if (choice == 3) {
					continueProgram = false;
				}
				if (continueProgram) {
					System.out.println("Vuoi continuare a utilizzare il programma? si (1) - no (all numbers)");
					response = Keyboard.readInt();
				} else
					response = 2; // se richiedo il termine esecuzione precedentemente evito di richiederlo dopo
				if (response != 1) {
					continueProgram = false;
					main.out.writeObject(3); // comando per terminare il programma sul server
					System.out.println("Termine esecuzione");
				} else {
					continueProgram = true;

				}
			}
		} catch (IOException | ClassNotFoundException e) {
			System.out.println(e);
			return;
		}
	}
}