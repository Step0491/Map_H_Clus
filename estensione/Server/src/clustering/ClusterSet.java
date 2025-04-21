package clustering;

import data.Data;
import distance.ClusterDistance;

import java.io.Serializable;

/**
 * La classe ClusterSet rappresenta un insieme di cluster.
 */
class ClusterSet implements Serializable {

	private static final long serialVersionUID = 1L;
	private Cluster[] clusters;
	private int size;

	/**
	 * Costruisce un oggetto ClusterSet con un numero specificato di cluster.
	 *
	 * @param k il numero di cluster da creare nell'insieme
	 */
	public ClusterSet(int k) {
		clusters = new Cluster[k];
		size = 0;
	}

	/**
	 * Aggiunge un cluster all'insieme.
	 *
	 * @param c il cluster da aggiungere
	 * @throws EmptyClusterException se il cluster da aggiungere è vuoto
	 */
	public void add(Cluster c) throws EmptyClusterException {
		if (c.getSize() == 0) {
			throw new EmptyClusterException();
		}
		clusters[size++] = c;
	}

	/**
	 * Restituisce il cluster all'indice specificato nell'insieme.
	 *
	 * @param i l'indice del cluster da restituire
	 * @return il cluster all'indice specificato
	 */
	public Cluster get(int i) {
		return clusters[i];
	}

	/**
	 * Restituisce una rappresentazione in stringa dell'insieme di cluster.
	 *
	 * @return una rappresentazione in stringa dell'insieme di cluster
	 */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < size; i++) {
			str.append("cluster").append(i).append(":").append(clusters[i]).append("\n");
		}
		return str.toString();
	}

	/**
	 * Restituisce una rappresentazione in stringa dell'insieme di cluster
	 * utilizzando i dati forniti.
	 *
	 * @param data l'oggetto Data che contiene i dati utilizzati per la
	 *             rappresentazione
	 * @return una rappresentazione in stringa dell'insieme di cluster utilizzando i
	 *         dati forniti
	 */
	public String toString(Data data) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < size; i++) {
			str.append("cluster").append(i).append(":").append(clusters[i].toString(data)).append("\n");
		}
		return str.toString();
	}

	/**
	 * Unisce i due cluster più vicini nell'insieme utilizzando la distanza
	 * specificata.
	 *
	 * @param distance la distanza utilizzata per calcolare la vicinanza dei cluster
	 * @param data     l'oggetto Data che contiene i dati utilizzati per il calcolo
	 *                 della distanza
	 * @return un nuovo oggetto ClusterSet contenente i cluster risultanti dalla
	 *         fusione dei due più vicini
	 */
	public ClusterSet mergeClosestClusters(ClusterDistance distance, Data data) {
	    double minDistance = Double.MAX_VALUE;
	    int closestCluster1 = -1;
	    int closestCluster2 = -1;

	    // Trova la coppia di cluster con la distanza minima
	    for (int i = 0; i < size; i++) {
	        for (int j = i + 1; j < size; j++) {
	            double currentDistance = distance.distance(clusters[i], clusters[j], data);
	            if (currentDistance < minDistance) {
	                minDistance = currentDistance;
	                closestCluster1 = i;
	                closestCluster2 = j;
	            }
	        }
	    }

	    // Unisci i due cluster della coppia trovata
	    Cluster mergedCluster = clusters[closestCluster1].mergeCluster(clusters[closestCluster2]);

	    // Crea un nuovo oggetto ClusterSet
	    ClusterSet newClusterSet = new ClusterSet(size - 1);

	    // Aggiungi tutti i cluster tranne i due cluster fusi
	    for (int i = 0; i < size; i++) {
	        try {
	            if (i == closestCluster1) {
	                // Aggiungi il nuovo cluster ottenuto dalla fusione prima degli altri cluster
	                newClusterSet.add(mergedCluster);
	            } else if (i != closestCluster2) {
	                // Clona il cluster prima di aggiungerlo al nuovo ClusterSet
	                newClusterSet.add(clusters[i].clone());
	            }
	        } catch (EmptyClusterException e) {
	            // Stampa il messaggio di errore
	            System.err.println(e.getMessage());
	        }
	    }
	    return newClusterSet;
	}

}
