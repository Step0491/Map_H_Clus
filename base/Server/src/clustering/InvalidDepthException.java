package clustering;

/**
 * Eccezione lanciata quando la profondità del dendrogramma è superiore al
 * numero di esempi memorizzati nel dataset.
 */
public class InvalidDepthException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Costruisce un nuovo oggetto InvalidDepthException con un messaggio
	 * predefinito.
	 */
	public InvalidDepthException() {
		super("La profondità del dendrogramma è superiore al numero di esempi memorizzati nel dataset.");
	}
}
