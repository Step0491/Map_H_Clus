/**
 * Classe per la validazione di stringhe secondo criteri specifici.
 */
public class StringValidator {

	/**
	 * Verifica se una stringa è valida secondo i criteri specificati.
	 *
	 * @param str La stringa da validare.
	 * @return true se la stringa è valida, false altrimenti.
	 */
	public static boolean isValidString(String str) {
		if (str == null || str.length() < 1 || str.length() > 20) {
			return false;
		}

		// Espressione regolare per matchare i caratteri specificati
		String regex = "^[a-zA-Z0-9_'òèéàùì]{1,20}$";
		return str.matches(regex);
	}
}