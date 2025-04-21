package distance;

import clustering.Cluster;
import data.Data;
import data.Example;

/**
 * Calcola la distanza media tra due cluster utilizzando la media delle distanze
 * euclidee tra tutti i punti nei due cluster.
 */
public class AverageLinkDistance implements ClusterDistance {

    /**
     * Calcola la distanza media tra due cluster.
     *
     * @param c1 il primo cluster
     * @param c2 il secondo cluster
     * @param d  il dataset
     * @return la distanza media tra i due cluster
     */
    @Override
    public double distance(Cluster c1, Cluster c2, Data d) {
        double totalDistance = 0.0;
        int count = 0;

        // Usa gli iteratori per calcolare la somma delle distanze tra tutti i punti nei due cluster
        for (int id1 : c1) {
            for (int id2 : c2) {
                Example ex1 = d.getExample(id1);
                Example ex2 = d.getExample(id2);
                totalDistance += ex1.distance(ex2);
                count++;
            }
        }

        // Calcola la media delle distanze
        if (count == 0)
            return Double.POSITIVE_INFINITY; // Evita divisione per zero
        else
            return totalDistance / count;
    }
}
