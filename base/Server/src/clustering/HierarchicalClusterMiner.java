package clustering;

import data.Data;
import distance.ClusterDistance;

import java.io.*;

/**
 * La classe HierarchicalClusterMiner implementa un algoritmo per l'analisi
 * gerarchica dei cluster.
 */
public class HierarchicalClusterMiner implements Serializable {

	private static final long serialVersionUID = 1L; // Ensure compatibility during serialization

	private Dendrogram dendrogram;

	/**
	 * Costruisce un nuovo oggetto HierarchicalClusterMiner con la profondità
	 * specificata e i dati forniti.
	 *
	 * @param depth la profondità del dendrogramma
	 * @param data  l'oggetto Data che contiene i dati utilizzati per l'analisi
	 */
	public HierarchicalClusterMiner(int depth, Data data) {
		try {
			if (depth > data.getNumberOfExamples())
				throw new InvalidDepthException();
			dendrogram = new Dendrogram(depth);
		} catch (InvalidDepthException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Esegue l'analisi dei cluster utilizzando i dati forniti e la distanza
	 * specificata.
	 *
	 * @param data     l'oggetto Data che contiene i dati utilizzati per l'analisi
	 * @param distance la distanza utilizzata per calcolare la vicinanza dei cluster
	 */
	public void mine(Data data, ClusterDistance distance) {
		// Livello base del dendrogramma
		ClusterSet baseLevel = new ClusterSet(data.getNumberOfExamples());
		for (int i = 0; i < data.getNumberOfExamples(); i++) {
			Cluster singleCluster = new Cluster();
			singleCluster.addData(i);
			try {
				baseLevel.add(singleCluster);
			} catch (EmptyClusterException e) {
				// Stampa il messaggio di errore
				System.err.println(e.getMessage());
			}
		}
		dendrogram.setClusterSet(baseLevel, 0);

		// Costruzione dei livelli successivi del dendrogramma
		for (int level = 1; level < dendrogram.getDepth(); level++) {
			ClusterSet prevLevelClusters = dendrogram.getClusterSet(level - 1);
			ClusterSet newLevelClusters = prevLevelClusters.mergeClosestClusters(distance, data);
			dendrogram.setClusterSet(newLevelClusters, level);
		}
	}

	/**
	 * Restituisce una rappresentazione in stringa del dendrogramma generato
	 * dall'analisi dei cluster.
	 *
	 * @return una rappresentazione in stringa del dendrogramma
	 */
	public String toString() {
		return dendrogram.toString();
	}

	/**
	 * Restituisce una rappresentazione in stringa del dendrogramma utilizzando i
	 * dati forniti.
	 *
	 * @param data l'oggetto Data che contiene i dati utilizzati per la
	 *             rappresentazione
	 * @return una rappresentazione in stringa del dendrogramma utilizzando i dati
	 *         forniti
	 * @throws InvalidDepthException 
	 */
	public String toString(Data data) throws InvalidDepthException {
		return dendrogram.toString(data);
	}

	/**
	 * Salva lo stato corrente di HierarchicalClusterMiner su un file.
	 *
	 * @param fileName il nome del file in cui salvare lo stato
	 * @throws FileNotFoundException se il file non può essere creato o aperto
	 * @throws IOException           se si verifica un errore durante la scrittura
	 *                               del file
	 */
	public void salva(String fileName) throws FileNotFoundException, IOException {
		if (!fileName.endsWith(".dat")) {
			fileName += ".dat";
		}
		
		// Crea la directory SerializedFiles se non esiste
		File directory = new File("SerializedFiles");
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Unable to create directory: " + "SerializedFiles");
            }
        }
		
        File file = new File(directory, fileName);
        
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
			oos.writeObject(this);
		}
	}

	/**
	 * Carica un'istanza di HierarchicalClusterMiner da un file.
	 *
	 * @param fileName il nome del file da cui caricare l'istanza
	 * @return l'istanza caricata di HierarchicalClusterMiner
	 * @throws FileNotFoundException  se il file non può essere trovato
	 * @throws IOException            se si verifica un errore durante la lettura
	 *                                del file
	 * @throws ClassNotFoundException se la classe del file serializzato non può
	 *                                essere trovata
	 */
	public static HierarchicalClusterMiner loadHierarchicalClusterMiner(String fileName)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		if (!fileName.endsWith(".dat")) {
			fileName += ".dat";
		}
		
		File file = new File("SerializedFiles", fileName);
		
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
			return (HierarchicalClusterMiner) ois.readObject();
		}
	}
}