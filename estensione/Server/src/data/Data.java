package data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import database.*;

/**
 * La classe Data rappresenta un dataset di esempi.
 */
public class Data {

    private List<Example> data; // lista di oggetti "Example" che rappresenta il dataset
    private int numberOfExamples; // numero di esempi nel dataset

    /**
     * Costruisce un nuovo oggetto Data leggendo gli esempi dalla tabella specificata.
     * 
     * @param tableName il nome della tabella da cui leggere gli esempi
     * @throws NoDataException se non ci sono dati nella tabella
     */
    public Data(String tableName) throws NoDataException {
        data = new ArrayList<>(); // inizializza la lista di Example

        DbAccess dbAccess = new DbAccess();
        try {
            dbAccess.initConnection();
            TableData tableData = new TableData(dbAccess);
            List<Example> examples = tableData.getDistinctTransazioni(tableName);
            if (examples.isEmpty()) {
                throw new NoDataException("La tabella non contiene dati.");
            }
            data.addAll(examples);
            numberOfExamples = data.size();
            dbAccess.closeConnection();
        } catch (SQLException | EmptySetException | MissingNumberException | DatabaseConnectionException e) {
            throw new NoDataException("Errore durante la lettura dei dati: " + e.getMessage());
        }
    }

    /**
     * Restituisce il numero di esempi memorizzati nel dataset.
     *
     * @return il numero di esempi nel dataset
     */
    public int getNumberOfExamples() {
        return numberOfExamples;
    }

    /**
     * Restituisce l'esempio memorizzato in una posizione specifica del dataset.
     *
     * @param exampleIndex l'indice dell'esempio nel dataset
     * @return l'esempio memorizzato in data[exampleIndex]
     */
    public Example getExample(int exampleIndex) {
        return data.get(exampleIndex);
    }

    /**
     * Calcola e restituisce una matrice delle distanze euclidee tra tutti gli
     * esempi nel dataset.
     *
     * @return una matrice delle distanze euclidee tra gli esempi nel dataset
     */
    public double[][] distance() {
        int n = getNumberOfExamples();
        double[][] distanceMatrix = new double[n][n];

        // Calcola le distanze tra tutti gli esempi nel dataset
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double dist = getExample(i).distance(getExample(j));
                distanceMatrix[i][j] = dist;
            }
        }

        return distanceMatrix;
    }

    /**
     * Restituisce una rappresentazione in stringa del dataset.
     *
     * @return una rappresentazione in stringa del dataset
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < data.size(); index++) {
            sb.append(index).append(": ").append(data.get(index)).append("\n");
        }
        return sb.toString();
    }

    /**
     * Imposta gli esempi nel dataset.
     *
     * @param examples la lista di esempi da impostare
     */
    public void setExamples(List<Example> examples) {
        this.data = examples;
        this.numberOfExamples = examples.size();
    }
}
