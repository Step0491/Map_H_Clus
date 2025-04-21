package clustering;

/**
 * Eccezione lanciata quando si tenta di eseguire un'operazione su un cluster
 * vuoto.
 */
class EmptyClusterException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Crea un'istanza di EmptyClusterException con un messaggio di errore
	 * predefinito.
	 */
	public EmptyClusterException() {
		super("Il cluster è vuoto e l'operazione richiesta non può essere eseguita.");
	}
}
