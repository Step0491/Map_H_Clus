package clustering;

import data.Data;
import java.io.Serializable;

/**
 * La classe Dendrogram rappresenta un dendrogramma, una struttura gerarchica
 * che visualizza la fusione graduale di cluster.
 */
class Dendrogram implements Serializable {

    private static final long serialVersionUID = 1L;
    private ClusterSet[] tree;

    /**
     * Costruisce un nuovo oggetto Dendrogram con la profondità specificata.
     *
     * @param depth la profondità del dendrogramma
     */
    public Dendrogram(int depth) {
        tree = new ClusterSet[depth];
    }

    /**
     * Imposta l'insieme di cluster per un livello specificato nel dendrogramma.
     *
     * @param c     l'insieme di cluster da impostare
     * @param level il livello nel dendrogramma
     */
    public void setClusterSet(ClusterSet c, int level) {
        tree[level] = c;
    }

    /**
     * Restituisce l'insieme di cluster per il livello specificato nel dendrogramma.
     *
     * @param level il livello nel dendrogramma
     * @return l'insieme di cluster per il livello specificato
     */
    public ClusterSet getClusterSet(int level) {
        return tree[level];
    }

    /**
     * Restituisce la profondità del dendrogramma.
     *
     * @return la profondità del dendrogramma
     */
    public int getDepth() {
        return tree.length;
    }

    /**
     * Restituisce una rappresentazione in stringa del dendrogramma.
     *
     * @return una rappresentazione in stringa del dendrogramma
     */
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < tree.length; i++) {
            str.append("level").append(i).append(":\n").append(tree[i]).append("\n");
        }
        return str.toString();
    }

    /**
     * Restituisce una rappresentazione in stringa del dendrogramma utilizzando i
     * dati forniti.
     *
     * @param data l'oggetto Data che contiene i dati utilizzati per la
     *             rappresentazione
     * @return una rappresentazione in stringa del dendrogramma utilizzando i dati
     *         forniti
     */
    public String toString(Data data) throws InvalidDepthException {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < tree.length; i++) {
            str.append("level").append(i).append(":\n").append(tree[i].toString(data)).append("\n");
        }
        return str.toString();
    }
}
