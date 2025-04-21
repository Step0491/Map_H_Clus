package database;

/**
 * Eccezione personalizzata per la gestione degli errori di connessione al database.
 */
public class DatabaseConnectionException extends Exception {
	private static final long serialVersionUID = 1L;

	public DatabaseConnectionException(String message) {
        super(message);
    }
}
