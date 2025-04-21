package distance;

import clustering.Cluster;
import data.Data;

/**
 * Definisce un'interfaccia per il calcolo della distanza tra due cluster.
 */
public interface ClusterDistance {

    /**
	 * Calcola la distanza tra due cluster.
	 *
	 * @param c1 il primo cluster
	 * @param c2 il secondo cluster
	 * @param d  il dataset
	 * @return la distanza tra i due cluster
	 */
	double distance(Cluster c1, Cluster c2, Data d);
}
