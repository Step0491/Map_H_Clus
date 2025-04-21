package database;

/** Eccezione personalizzata per attributi non numerici */
public class MissingNumberException extends Exception {
	private static final long serialVersionUID = 1L;

	public MissingNumberException(String message) {
		super(message);
	}
}
