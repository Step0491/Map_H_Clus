package data;

/**
 * Eccezione personalizzata per gestire la mancanza di dati nel set di dati.
 */
public class NoDataException extends Exception {
	private static final long serialVersionUID = 1L;

	public NoDataException(String message) {
		super(message);
	}
}
