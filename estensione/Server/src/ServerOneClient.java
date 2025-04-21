import clustering.HierarchicalClusterMiner;
import clustering.InvalidDepthException;
import data.Data;
import data.NoDataException;
import database.DatabaseConnectionException;
import database.DbAccess;
import distance.AverageLinkDistance;
import distance.ClusterDistance;
import distance.SingleLinkDistance;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;

/**
 * Classe ServerOneClient: gestisce la comunicazione con un singolo client e le operazioni
 * richieste dal client.
 */
public class ServerOneClient implements Runnable {
    private Socket clientSocket;

    /**
     * Costruttore che inizializza il client socket.
     * 
     * @param socket Il socket del client connesso.
     */
    public ServerOneClient(Socket socket) {
        this.clientSocket = socket;
    }

    /**
     * Metodo run che gestisce la connessione del client e le operazioni richieste.
     */
    @Override
    public void run() {
        DbAccess dbAccess = new DbAccess();
        try {
            // Tentativo di connessione al database
            dbAccess.initConnection();
        } catch (DatabaseConnectionException e) {
            System.out.println("Errore di connessione al database: " + e.getMessage());
            return; // Termina l'esecuzione del thread
        }

        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            boolean continueProgram = true;
            String tableName = null;
            while (continueProgram) {
                try {
                    Object command = in.readObject();
                    System.out.println("Comando ricevuto: " + command);

                    if (command instanceof Integer) {
                        int action = (Integer) command;

                        if (action == 0) {
                            // Gestisce la richiesta di caricamento dei dati dal database
                            boolean correctName = false;
                            while (!correctName) {
                                try {
                                    String[] tableNames = dbAccess.getTableNames();
                                    out.writeObject(tableNames); // Invio la lista delle tabelle al client
                                    tableName = (String) in.readObject();
                                    out.writeObject("OK");
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                } finally {
                                    correctName = true;
                                }
                            }
                        } else if (action == 1) {
                            // Invio la lista dei file disponibili nella directory corrente al client
                            File currentDir = new File("SerializedFiles");
                            String[] fileNames = null;
                            if (currentDir != null) {
                                fileNames = currentDir.list((dir, name) -> name.endsWith(".dat"));
                                if (fileNames.length > 0 && fileNames != null) {
                                    out.writeObject(fileNames);
                                    String filePath = (String) in.readObject();
                                    if (!filePath.isEmpty()) {
                                        File file = new File("SerializedFiles", filePath);
                                        if (file.exists() && file.isFile()) {
                                            try {
                                                HierarchicalClusterMiner clustering = HierarchicalClusterMiner
                                                        .loadHierarchicalClusterMiner(filePath);
                                                out.writeObject(
                                                        "Oggetto HierarchicalClusterMiner caricato con successo.");
                                                out.writeObject(clustering.toString());
                                            } catch (IOException | ClassNotFoundException e) {
                                                out.writeObject("Errore durante il caricamento dell'oggetto: "
                                                        + e.getMessage());
                                            }
                                        } else {
                                            out.writeObject(
                                                    "Errore: Il file specificato non esiste o non è un file valido.");
                                        }
                                    }
                                } else {
                                    out.writeObject(new String[]{"Non sono presenti file"});
                                }
                            }
                        } else if (action == 2) {
                            // Apprende HierarchicalClusterMiner da DataBase
                            boolean dataLoaded = false;
                            Data data = null;
                            while (!dataLoaded) {
                                try {
                                    data = new Data(tableName);
                                    dataLoaded = true;
                                    out.writeObject("OK");
                                } catch (NoDataException e) {
                                    out.writeObject("Errore: " + e.getMessage());
                                }
                            }
                            if (data != null) {
                                try {
                                    out.writeObject(data.getNumberOfExamples());
                                    int depth = (Integer) in.readObject();
                                    int choice = (Integer) in.readObject();
                                    if (depth <= 0 || depth > data.getNumberOfExamples()) {
                                        out.writeObject("Valore non accettabile! Deve essere tra 1 e "
                                                + data.getNumberOfExamples());
                                        continue;
                                    }
                                    ClusterDistance distance = null;
                                    if (choice == 1) {
                                        distance = new SingleLinkDistance();
                                    } else if (choice == 2) {
                                        distance = new AverageLinkDistance();
                                    } else {
                                        out.writeObject("Valore non accettabile! Deve essere 1 o 2.");
                                        continue;
                                    }
                                    HierarchicalClusterMiner clustering = null;
                                    clustering = new HierarchicalClusterMiner(depth, data);
                                    clustering.mine(data, distance);
                                    out.writeObject("Clustering gerarchico completato.");
                                    out.writeObject(clustering.toString(data));

                                    boolean saveSuccessful = false;
                                    while (!saveSuccessful) {
                                        String savePath = (String) in.readObject();
                                        try {
                                            clustering.salva(savePath);
                                            out.writeObject("Oggetto HierarchicalClusterMiner salvato con successo.");
                                            saveSuccessful = true;
                                        } catch (IOException e) {
                                            out.writeObject(
                                                    "Errore durante il salvataggio dell'oggetto: " + e.getMessage());
                                            saveSuccessful = false;
                                        }
                                    }
                                } catch (InvalidDepthException | ClassNotFoundException e) {
                                    out.writeObject("Errore: " + e.getMessage());
                                }
                            }
                        } else if (action == 3) {
                            System.out.println("Client ha terminato la sua esecuzione.");
                            continueProgram = false; // Termina il loop principale
                        }
                    }
                } catch (EOFException e) {
                    System.out.println("Connessione chiusa dal client.");
                    continueProgram = false;
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    continueProgram = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Decrementa il contatore dei client attivi
            MultiServer.decrementClientCount();
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                dbAccess.closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
