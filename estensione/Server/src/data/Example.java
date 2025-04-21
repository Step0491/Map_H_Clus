package data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * La classe Example rappresenta un vettore di valori reali.
 */
public class Example implements Iterable<Double> {

    private List<Double> example; // vettore di valori reali

    /**
     * Costruisce un nuovo oggetto Example con un vettore vuoto.
     */
    public Example() {
        example = new LinkedList<>();
    }

    /**
     * Aggiunge un valore alla fine del vettore.
     *
     * @param v il valore da aggiungere
     */
    public void add(Double v) {
        example.add(v);
    }

    /**
     * Restituisce il valore alla posizione specificata nel vettore.
     *
     * @param index l'indice del valore da restituire
     * @return il valore alla posizione specificata nel vettore
     */
    public Double get(int index) {
        return example.get(index);
    }

    /**
     * Calcola e restituisce la distanza euclidea tra il vettore dell'istanza
     * corrente e quello di un altro oggetto Example.
     *
     * @param other l'altro oggetto Example
     * @return la distanza euclidea tra i due vettori
     */
    public double distance(Example other) {
        double sumOfSquares = 0.0;
        if (example.size() != other.example.size()) {
            throw new IllegalArgumentException("I vettori hanno dimensioni diverse.");
        }
        for (int i = 0; i < example.size(); i++) {
            double diff = example.get(i) - other.example.get(i);
            sumOfSquares += diff * diff;
        }
        return sumOfSquares; // Corretto per restituire la radice quadrata della somma dei quadrati
    }

    /**
     * Restituisce un iteratore per il vettore di valori reali.
     *
     * @return un iteratore per il vettore di valori reali
     */
    @Override
    public Iterator<Double> iterator() {
        return example.iterator();
    }

    /**
     * Restituisce una rappresentazione in stringa dell'oggetto Example.
     *
     * @return una stringa che rappresenta l'oggetto Example
     */
    @Override
    public String toString() {
        return "Example [example=" + example + "]";
    }
}
