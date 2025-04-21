import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Classe MultiServer: gestisce le connessioni multiple dei client e monitorizza
 * lo stato del server.
 */
public class MultiServer {
    // Contatore dei client attivi
    private static AtomicInteger activeClients = new AtomicInteger(0);
    private static boolean running = true;
    private static boolean firstClientConnected = false;
    private static ServerSocket serverSocket;

    /**
     * Metodo principale del server. Avvia il server e gestisce le connessioni dei client.
     * 
     * @param args Argomenti del programma, dove il primo argomento rappresenta la porta
     *             su cui il server ascolterà.
     */
    public static void main(String[] args) {
        // Aggiungi uno shutdown hook per gestire la chiusura del server in modo sicuro
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

                // Avvia il thread di monitoraggio solo dopo la connessione del primo client
                if (!firstClientConnected) {
                    firstClientConnected = true;
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
     * Avvia un thread di monitoraggio per controllare quando non ci sono più client attivi.
     */
    private static void startMonitoringThread() {
        Thread shutdownThread = new Thread(() -> {
            while (running) {
                // Ferma il server quando non ci sono più client connessi
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
     * Ferma il server chiudendo il ServerSocket.
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
