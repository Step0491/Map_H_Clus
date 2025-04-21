import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Questa classe rappresenta un server multi-client che gestisce le connessioni dei client
 * e monitora lo stato del server.
 */
public class MultiServer {
    private static AtomicInteger activeClients = new AtomicInteger(0);
    private static boolean running = true;
    private static boolean firstClientConnected = false;
    private static ServerSocket serverSocket;

    /**
     * Metodo principale che avvia il server e gestisce le connessioni dei client.
     * @param args Argomenti della riga di comando. Il primo argomento deve essere la porta su cui il server ascolta.
     */
    public static void main(String[] args) {
        // Aggiungi uno shutdown hook per chiudere il server in modo ordinato alla chiusura del programma
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook triggered. Chiusura del server in corso...");
            stopServer();
        }));
        
        int port = Integer.parseInt(args[0]); // Porta su cui il server ascolterà
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server in ascolto sulla porta " + port);

            // Aspetta il primo client prima di avviare il thread di monitoraggio
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connesso: " + clientSocket.getInetAddress());

                // Incrementa il contatore dei client attivi
                activeClients.incrementAndGet();

                // Imposta il flag quando il primo client si connette
                if (!firstClientConnected) {
                    firstClientConnected = true;
                    // Avvia il thread di monitoraggio solo dopo la connessione del primo client
                    startMonitoringThread();
                }

                // Crea un nuovo thread per gestire il client
                Thread clientThread = new Thread(new ServerOneClient(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            if (running) { // Ignora l'eccezione se il server viene chiuso intenzionalmente
                e.printStackTrace();
            }
        } finally {
            stopServer();
            System.out.println("Server terminato in assenza di client connessi.");
        }
    }

    /**
     * Avvia un thread di monitoraggio per controllare se tutti i client si sono disconnessi.
     * Il server si fermerà se il primo client si è disconnesso e non ci sono più client attivi.
     */
    private static void startMonitoringThread() {
        Thread shutdownThread = new Thread(() -> {
            while (running) {
                if (firstClientConnected && activeClients.get() == 0) {
                    stopServer();
                }
                try {
                    Thread.sleep(1000); // Controlla ogni secondo
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        shutdownThread.start();
    }

    /**
     * Decrementa il contatore dei client attivi.
     */
    public static void decrementClientCount() {
        activeClients.decrementAndGet();
    }

    /**
     * Ferma il server chiudendo il socket del server e impostando lo stato di esecuzione su falso.
     */
    private static void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server socket closed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
