package data;

/**
 * Eccezione che viene lanciata quando si tenta di calcolare la distanza tra due
 * esempi di dimensioni diverse.
 */
class InvalidSizeException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Crea un'istanza di InvalidSizeException con un messaggio di errore
	 * predefinito.
	 */
	public InvalidSizeException() {
		super("Impossibile calcolare la distanza tra due esempi di dimensioni diverse.");
	}
}
