package clustering;

import data.Data;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.io.Serializable;

/**
 * La classe Cluster rappresenta un cluster di dati, dove i dati sono
 * identificati dai loro indici.
 */
public class Cluster implements Iterable<Integer>, Cloneable, Serializable {

	private static final long serialVersionUID = 1L;
	private Set<Integer> clusteredData;

	public Cluster() {
		clusteredData = new TreeSet<>();
	}

	/**
	 * Aggiunge l'indice di un dato al cluster, evitando duplicati.
	 *
	 * @param id l'indice del dato da aggiungere al cluster
	 */
	void addData(int id) {
		clusteredData.add(id);
	}

	/**
	 * Restituisce la dimensione del cluster.
	 *
	 * @return la dimensione del cluster
	 */
	public int getSize() {
		return clusteredData.size();
	}

	/**
	 * Crea un nuovo cluster che è la fusione dei due cluster pre-esistenti.
	 *
	 * @param c il cluster da unire al cluster corrente
	 * @return un nuovo cluster che è la fusione dei due cluster pre-esistenti
	 */
	public Cluster mergeCluster(Cluster c) {
		Cluster newC = this.clone();
		for (int id : c.clusteredData) {
			newC.addData(id);
		}
		return newC;
	}

	/**
	 * Restituisce una rappresentazione in stringa del cluster.
	 *
	 * @return una rappresentazione in stringa del cluster
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		Iterator<Integer> iterator = clusteredData.iterator();
		while (iterator.hasNext()) {
			str.append(iterator.next());
			if (iterator.hasNext()) {
				str.append(",");
			}
		}
		return str.toString();
	}

	/**
	 * Restituisce una rappresentazione in stringa del cluster utilizzando i dati
	 * forniti.
	 *
	 * @param data l'oggetto Data che contiene i dati utilizzati per la
	 *             rappresentazione
	 * @return una rappresentazione in string
	 * @return una rappresentazione in stringa del cluster utilizzando i dati
	 *         forniti
	 */
	public String toString(Data data) {
		StringBuilder str = new StringBuilder();
		for (int id : clusteredData) {
			str.append("<").append(data.getExample(id)).append(">");
		}
		return str.toString();
	}

	/**
	 * Restituisce un iteratore per gli elementi del cluster.
	 *
	 * @return un iteratore per gli elementi del cluster
	 */
	@Override
	public Iterator<Integer> iterator() {
		return clusteredData.iterator();
	}

	/**
	 * Crea e restituisce una copia del cluster corrente.
	 *
	 * @return una copia del cluster corrente
	 */
	@Override
	public Cluster clone() {
		try {
			Cluster copy = (Cluster) super.clone();
			copy.clusteredData = new TreeSet<>(this.clusteredData);
			return copy;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(); // Can't happen
		}
	}
}
