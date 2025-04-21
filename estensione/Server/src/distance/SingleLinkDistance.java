package distance;

import clustering.Cluster;
import data.Data;
import data.Example;

/**
 * Calcola la distanza minima tra due cluster utilizzando la distanza minima tra
 * i punti nei due cluster.
 */
public class SingleLinkDistance implements ClusterDistance {

    /**
     * Calcola la distanza minima tra due cluster.
     *
     * @param c1 il primo cluster
     * @param c2 il secondo cluster
     * @param d  il dataset
     * @return la distanza minima tra i due cluster
     */
    @Override
    public double distance(Cluster c1, Cluster c2, Data d) {
        double min = Double.MAX_VALUE;

        // Usa gli iteratori per calcolare la distanza minima tra i punti nei due cluster
        for (int id1 : c1) {
            Example e1 = d.getExample(id1);
            for (int id2 : c2) {
                double distance = e1.distance(d.getExample(id2));
                if (distance < min) {
                    min = distance;
                }
            }
        }
        return min;
    }
}
