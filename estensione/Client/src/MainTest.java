import javax.swing.*;	
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;



import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

/**
 * Classe principale per la gestione dell'interfaccia utente del Dendrogram
 * Manager. Questa classe gestisce la comunicazione con il server e la
 * visualizzazione delle varie schermate dell'applicazione.
 */
public class MainTest {

	private ObjectOutputStream out;
	private ObjectInputStream in;
	private JFrame frame;
	private JPanel mainPanel;
	private JComboBox<String> tableComboBox;
	private JButton confirmTableButton;
	private JLabel statusLabel;
	private String selectedTable;

	/**
	 * Costruttore della classe MainTest. Inizializza la connessione con il server e
	 * l'interfaccia utente.
	 * 
	 * @param ip   Indirizzo IP del server.
	 * @param port Porta del server.
	 * @throws IOException Se si verifica un errore di I/O durante la connessione.
	 */
	public MainTest(String ip, int port) throws IOException {
		InetAddress addr = InetAddress.getByName(ip);
		System.out.println("addr = " + addr);
		Socket socket = new Socket(addr, port);
		System.out.println(socket);

		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());

		initializeUI();
	}

	/**
	 * Inizializza l'interfaccia utente con un tema scuro.
	 */
	private void initializeUI() {
		frame = new JFrame("Dendrogram Manager");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 350);
		frame.setMaximumSize(new Dimension(400, 300));
		frame.setResizable(true);
		mainPanel = new JPanel();
		frame.add(mainPanel);
		frame.setVisible(true);
		SwingUtilities.updateComponentTreeUI(frame);
		initializeComponents();
		showLoadingScreen();
	}

	/**
	 * Inizializza i componenti principali dell'interfaccia utente.
	 */
	private void initializeComponents() {
		String[] items = { "" };
		tableComboBox = new JComboBox<>(items);
		tableComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		tableComboBox.setPreferredSize(new Dimension(150, 25));
		tableComboBox.setMaximumSize(new Dimension(150, 25));
	}

	/**
	 * Mostra una schermata di caricamento mentre si attende la risposta dal server.
	 */
	private void showLoadingScreen() {
		mainPanel.removeAll();
		JDialog loadingDialog = new JDialog(frame, "Caricamento tabelle...", Dialog.ModalityType.APPLICATION_MODAL);
		JLabel loadingLabel = new JLabel("Caricamento in corso...");
		loadingLabel.setFont(new Font("Calibri", Font.BOLD, 16));
		loadingLabel.setHorizontalAlignment(JLabel.CENTER);
		loadingDialog.add(loadingLabel);
		loadingDialog.setSize(300, 100);
		loadingDialog.setLocationRelativeTo(frame);

		SwingWorker<Void, Void> worker = new SwingWorker<>() {
			@Override
			protected Void doInBackground() throws Exception {
				loadDataOnServer();
				return null;
			}

			@Override
			protected void done() {
				loadingDialog.dispose();
				try {
					initializeSelectionPanel();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		worker.execute();
		loadingDialog.setVisible(true);
	}

	/**
	 * Inizializza il pannello di selezione delle tabelle.
	 * 
	 * @throws IOException Se si verifica un errore di I/O durante
	 *                     l'inizializzazione.
	 */
	private void initializeSelectionPanel() throws IOException {
		mainPanel.removeAll();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel label1 = new JLabel("Selezione Tabella");
		label1.setFont(new Font("Calibri", Font.BOLD, 18));
		label1.setForeground(Color.BLACK);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		mainPanel.add(label1, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		mainPanel.add(tableComboBox, gbc);

		JButton terminateButton = createButton("Termina Esecuzione");
		terminateButton.addActionListener(e -> {
			try {
				// il server ora e' in attesa di un nome di tabella, gestisco le operazioni
				// necessarie a non generare errori successivamente invio nome tabella vuoto
				// e procedo a terminare l'esecuzione
				out.writeObject("");
				String response = (String) in.readObject();
				if (response.equals("OK")) {
					terminateExecution();
					statusLabel.setText("Termine esecuzione");
					System.exit(0);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		});
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		mainPanel.add(terminateButton, gbc);

		confirmTableButton = createButton("Conferma Tabella");
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		confirmTableButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (tableComboBox.getSelectedItem() != null
							&& !tableComboBox.getSelectedItem().toString().isEmpty()) {
						selectedTable = tableComboBox.getSelectedItem().toString();
						out.writeObject(selectedTable); // Invio al server il nome della tabella selezionata
						String response = (String) in.readObject(); // Ricevo la risposta dal server
						if ("OK".equals(response)) {
							showOperationSelectionPanel();
						} else {
						}
					}
				} catch (ClassNotFoundException | IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		mainPanel.add(confirmTableButton, gbc);
		statusLabel = new JLabel("");
		statusLabel.setForeground(Color.BLACK);
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		mainPanel.add(statusLabel, gbc);

		mainPanel.revalidate();
		mainPanel.repaint();
	}

	/**
	 * Carica i dati sul server richiedendo la lista delle tabelle disponibili.
	 * 
	 * @throws IOException            Se si verifica un errore di I/O durante la
	 *                                comunicazione con il server.
	 * @throws ClassNotFoundException Se la classe dell'oggetto ricevuto non è
	 *                                trovata.
	 */
	private void loadDataOnServer() throws IOException, ClassNotFoundException {
		out.writeObject(0); // Richiedo la lista delle tabelle
		String[] tableNames = (String[]) in.readObject();
		SwingUtilities.invokeLater(() -> {
			tableComboBox.removeAllItems();
			for (String tableName : tableNames) {
				tableComboBox.addItem(tableName);
			}
		});
	}

	/**
	 * Mostra il pannello di selezione delle operazioni disponibili.
	 */
	private void showOperationSelectionPanel() {
		mainPanel.removeAll();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel label = new JLabel("Seleziona un'operazione:");
		label.setFont(new Font("Calibri", Font.BOLD, 18));
		label.setForeground(Color.BLACK);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		mainPanel.add(label, gbc);

		JButton loadFileButton = new JButton("Carica Dendrogramma da File");
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		loadFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadDendrogramFromFileOnServer();
			}
		});
		mainPanel.add(loadFileButton, gbc);

		JButton mineDendrogramButton = new JButton("Apprendi Dendrogramma da Database");
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		mineDendrogramButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mineDendrogramOnServer();
			}
		});
		mainPanel.add(mineDendrogramButton, gbc);

		JButton terminateButton = new JButton("Termina Esecuzione");
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		terminateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				terminateExecution();
			}
		});
		mainPanel.add(terminateButton, gbc);

		mainPanel.revalidate();
		mainPanel.repaint();
	}

	/**
	 * Crea un pulsante con il testo specificato.
	 * 
	 * @param text Testo da visualizzare sul pulsante.
	 * @return Il pulsante creato.
	 */
	private JButton createButton(String text) {
		JButton button = new JButton(text);
		button.setPreferredSize(new Dimension(150, 25));
		button.setMaximumSize(new Dimension(150, 25));
		return button;
	}

	/**
	 * Carica un dendrogramma da un file presente sul server.
	 */
	private void loadDendrogramFromFileOnServer() {
		JDialog loadingDialog = new JDialog(frame, "Caricamento file...", Dialog.ModalityType.APPLICATION_MODAL);
		JLabel loadingLabel = new JLabel("Caricamento in corso...");
		loadingLabel.setFont(new Font("Calibri", Font.BOLD, 16));
		loadingLabel.setHorizontalAlignment(JLabel.CENTER);
		loadingDialog.add(loadingLabel);
		loadingDialog.setSize(300, 100);
		loadingDialog.setLocationRelativeTo(frame);

		SwingWorker<Object, Void> worker = new SwingWorker<>() {
			@Override
			protected Object doInBackground() throws Exception {
				out.writeObject(1);
				return in.readObject();// Ricevo dal server la lista dei file presenti
			}

			@Override
			protected void done() {
				Object response = null;
				try {
					response = get();

				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
				loadingDialog.dispose();
				if (response instanceof String[]) {
					String[] fileNames = (String[]) response;
					if (fileNames[0].equals("Non sono presenti file")) {
						// in assenza di file ritorna alla schermata principale
						initializeComponents();
						showLoadingScreen();
					} else
						showFileSelectionPanel(fileNames);
				} else {
				}
			}
		};

		worker.execute();
		loadingDialog.setVisible(true);
	}

	/**
	 * Mostra il pannello di selezione dei file disponibili sul server.
	 * 
	 * @param fileNames Array di nomi dei file disponibili.
	 */
	private void showFileSelectionPanel(String[] fileNames) {
		mainPanel.removeAll();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel label = new JLabel("Seleziona un file:");
		label.setFont(new Font("Calibri", Font.BOLD, 18));
		label.setForeground(Color.BLACK);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		mainPanel.add(label, gbc);

		JComboBox<String> fileComboBox = new JComboBox<>(fileNames);
		fileComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		fileComboBox.setPreferredSize(new Dimension(150, 25));
		fileComboBox.setMaximumSize(new Dimension(150, 25));
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		mainPanel.add(fileComboBox, gbc);

		JButton confirmButton = createButton("Conferma Selezione File");
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		confirmButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedFile = (String) fileComboBox.getSelectedItem();
				if (selectedFile != null && !selectedFile.isEmpty()) {
					loadSelectedFileFromServer(selectedFile);
				} else {

				}
			}
		});
		mainPanel.add(confirmButton, gbc);

		mainPanel.revalidate();
		mainPanel.repaint();
	}

	/**
	 * Carica il file selezionato dal server e mostra il dendrogramma.
	 * 
	 * @param fileName Nome del file da caricare.
	 */
	private void loadSelectedFileFromServer(String fileName) {
		try {
			out.writeObject(fileName);
			String response = (String) in.readObject();
			if (response.startsWith("Oggetto HierarchicalClusterMiner caricato")) {
				String dendrogram = (String) in.readObject();
				showDendrogramPanel(dendrogram, 1);
			} else {

			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();

		}
	}

	/**
	 * Mostra il pannello contenente il dendrogramma caricato.
	 * 
	 * @param dendrogram Dendrogramma da visualizzare.
	 * @param choice     Scelta dell'operazione (1 per caricamento da file, 2 per
	 *                   apprendimento da database).
	 */
	private void showDendrogramPanel(String dendrogram, int choice) {
		mainPanel.removeAll();
		mainPanel.setLayout(new BorderLayout());

		JLabel label = new JLabel("Dendrogramma caricato:");
		label.setFont(new Font("Calibri", Font.BOLD, 18));
		label.setForeground(Color.BLACK);
		mainPanel.add(label, BorderLayout.NORTH);

		JTextArea textArea = new JTextArea(dendrogram);
		textArea.setEditable(false);
		textArea.setBackground(new Color(200, 200, 200));
		textArea.setForeground(Color.BLACK);
		textArea.setFont(new Font("Calibri", Font.PLAIN, 14));
		JScrollPane scrollPane = new JScrollPane(textArea);
		mainPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton homeButton = createButton("Home");
		homeButton.addActionListener(e -> {
			initializeComponents();
			showLoadingScreen();
		});
		buttonPanel.add(homeButton);

		JButton terminateButton = createButton("Termina Esecuzione");
		terminateButton.addActionListener(e -> terminateExecution());
		buttonPanel.add(terminateButton);

		if (choice == 2) {
			buttonPanel.add(createSaveButtonPanel()); // Add the save button panel here if needed
		}
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		mainPanel.revalidate();
		mainPanel.repaint();
	}

	/**
	 * Crea il pannello per il salvataggio del dendrogramma.
	 * 
	 * @return Il pannello per il salvataggio del dendrogramma.
	 */
	private JPanel createSaveButtonPanel() {
		JPanel savePanel = new JPanel();
		savePanel.setLayout(new FlowLayout());

		JTextField fileNameField = new JTextField(15);
		fileNameField.setBackground(new Color(200,200,200));
		fileNameField.setForeground(Color.BLACK);
		fileNameField.setPreferredSize(new Dimension(120, 20));
		savePanel.add(fileNameField);

		JLabel extensionLabel = new JLabel(".dat");
		extensionLabel.setForeground(Color.BLACK);
		savePanel.add(extensionLabel);

		JButton saveButton = createButton("Salva");
		saveButton.setEnabled(false); // Initially disabled

		fileNameField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				toggleSaveButton();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				toggleSaveButton();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				toggleSaveButton();
			}

			private void toggleSaveButton() {
				String fileName = fileNameField.getText().trim();
				saveButton.setEnabled(!fileName.isEmpty());
			}
		});

		saveButton.addActionListener(e -> {
			String fileName = fileNameField.getText().trim();
			if (!StringValidator.isValidString(fileName)) {
				return;
			}
			fileName = fileName + ".dat";
			final String name = fileName;

			SwingWorker<Void, Void> saveWorker = new SwingWorker<>() {
				@Override
				protected Void doInBackground() throws Exception {
					out.writeObject(name);
					return null;
				}

				@Override
				protected void done() {
					try {
						Object response = in.readObject();
						if (response.equals("Oggetto HierarchicalClusterMiner salvato con successo."))
							showFinalScene(); // Transition to final scene
					} catch (IOException | ClassNotFoundException e) {
						e.printStackTrace();

					}
				}
			};
			saveWorker.execute();
		});

		savePanel.add(saveButton);
		return savePanel;
	}

	/**
	 * Mostra la scena finale dopo il salvataggio del dendrogramma.
	 */
	private void showFinalScene() {
		mainPanel.removeAll();
		mainPanel.setLayout(new BorderLayout());

		JLabel label = new JLabel("Dendrogramma salvato");
		label.setFont(new Font("Calibri", Font.BOLD, 18));
		label.setForeground(Color.BLACK);
		label.setBorder(new EmptyBorder(10, 100, 10, 10));
		mainPanel.add(label, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton homeButton = createButton("Home");
		homeButton.addActionListener(e -> {
			initializeComponents();
			showLoadingScreen();
		});
		buttonPanel.add(homeButton);

		JButton terminateButton = createButton("Termina Esecuzione");
		terminateButton.addActionListener(e -> terminateExecution());
		buttonPanel.add(terminateButton);

		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		mainPanel.revalidate();
		mainPanel.repaint();
	}

	/**
	 * Mostra il pannello per il salvataggio del dendrogramma.
	 */
	private void showDendrogramSavePanel() {
		JPanel savePanel = createSaveButtonPanel();

		// Add the save panel to the existing layout or a new layout
		mainPanel.add(savePanel, BorderLayout.SOUTH);

		mainPanel.revalidate();
		mainPanel.repaint();
	}

	/**
	 * Apprende un dendrogramma dal database sul server.
	 */
	private void mineDendrogramOnServer() {
		SwingWorker<Object, Void> worker = new SwingWorker<>() {
			@Override
			protected Object doInBackground() throws Exception {
				out.writeObject(2); // Invia il comando per apprendere il dendrogramma
				return in.readObject(); // Ricevi la risposta dal server
			}

			@Override
			protected void done() {
				try {
					Object response = get();
					if ("OK".equals(response)) {
						int maxDepth = (int) in.readObject();
						showDepthAndDistanceSelectionPanel(maxDepth, selectedTable);
					} else {
					}
				} catch (IOException | ClassNotFoundException | InterruptedException | ExecutionException e) {
					e.printStackTrace();

				}
			}
		};

		worker.execute();
	}

	/**
	 * Mostra il pannello per la selezione della profondità e del tipo di distanza
	 * per l'apprendimento del dendrogramma.
	 * 
	 * @param maxDepth      Profondità massima del dendrogramma.
	 * @param selectedTable Tabella selezionata.
	 */
	private void showDepthAndDistanceSelectionPanel(int maxDepth, String selectedTable) {
		mainPanel.removeAll();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.revalidate();
		mainPanel.repaint();


		// Display the selected table name
		JLabel tableLabel = new JLabel("Tabella selezionata: " + selectedTable);
		tableLabel.setFont(new Font("Calibri", Font.BOLD, 18));
		tableLabel.setFont(new Font("Calibri", Font.BOLD, 18));
		tableLabel.setForeground(Color.BLACK);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		mainPanel.add(tableLabel, gbc);

		JLabel depthLabel = new JLabel("Seleziona la profondità del dendrogramma:");
		depthLabel.setFont(new Font("Calibri", Font.BOLD, 18));
		depthLabel.setForeground(Color.BLACK);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		mainPanel.add(depthLabel, gbc);

		JComboBox<Integer> depthComboBox = new JComboBox<>();
		for (int i = 1; i <= maxDepth; i++) {
			depthComboBox.addItem(i);
		}
		depthComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		depthComboBox.setPreferredSize(new Dimension(120, 25));
		depthComboBox.setMaximumSize(new Dimension(120, 25));
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		mainPanel.add(depthComboBox, gbc);

		JLabel distanceLabel = new JLabel("Seleziona il tipo di distanza:");
		distanceLabel.setFont(new Font("Calibri", Font.BOLD, 18));
		distanceLabel.setForeground(Color.BLACK);
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		mainPanel.add(distanceLabel, gbc);

		JComboBox<String> distanceComboBox = new JComboBox<>(new String[] { "Single-link", "Average-link" });
		distanceComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		distanceComboBox.setPreferredSize(new Dimension(150, 25));
		distanceComboBox.setMaximumSize(new Dimension(150, 25));
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		mainPanel.add(distanceComboBox, gbc);

		JButton confirmButton = createButton("Conferma");
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 2;
		confirmButton.addActionListener(e -> {
			int depth = (int) depthComboBox.getSelectedItem();
			int dType = (int) distanceComboBox.getSelectedIndex() + 1;

			JDialog processingDialog = new JDialog(frame, "Elaborazione in corso...",
					Dialog.ModalityType.APPLICATION_MODAL);
			JLabel processingLabel = new JLabel("Elaborazione in corso...");
			processingLabel.setFont(new Font("Calibri", Font.BOLD, 16));
			processingLabel.setHorizontalAlignment(JLabel.CENTER);
			processingDialog.add(processingLabel);
			processingDialog.setSize(300, 100);
			processingDialog.setLocationRelativeTo(frame);

			SwingWorker<Object, Void> processingWorker = new SwingWorker<>() {
				@Override
				protected Object doInBackground() throws Exception {
					out.writeObject(depth);
					out.writeObject(dType);
					return in.readObject(); // Ricevi la risposta dal server
				}

				@Override
				protected void done() {
					processingDialog.dispose();
					try {
						Object response = get();
						if ("Clustering gerarchico completato.".equals(response)) {
							String dendrogram = (String) in.readObject();
							showDendrogramPanel(dendrogram, 2);
							showDendrogramSavePanel(); // Show the save panel after dendrogram is displayed
						} else {

						}
					} catch (IOException | ClassNotFoundException | InterruptedException | ExecutionException e) {
						e.printStackTrace();

					}
				}
			};
			processingWorker.execute();
		});
		mainPanel.add(confirmButton, gbc);

		mainPanel.revalidate();
		mainPanel.repaint();
	}

	/**
	 * Termina l'esecuzione del programma.
	 */
	private void terminateExecution() {
		try {
			out.writeObject(3); // comando per terminare il programma sul server
			statusLabel.setText("Termine esecuzione");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		String ip = args[0];
		int port = Integer.parseInt(args[1]);
		@SuppressWarnings("unused")
		MainTest main = null;
		try {
			main = new MainTest(ip, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}