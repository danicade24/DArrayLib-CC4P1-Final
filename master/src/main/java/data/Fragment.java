package data;

import java.util.Arrays;

/**
 * Representa un fragmento de datos enviado a un worker para procesamiento distribuido.
 */

public class Fragment {
    private String id;          // ID único del fragmento
    private int startIndex;     // Índice de inicio en el array original
    private double[] data;      // Subarray de datos

    public Fragment(String id, int startIndex, double[] data) {
        this.id = id;
        this.startIndex = startIndex;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public double[] getData() {
        return data;
    }

    public int size(){
        return data.length;
    }

    @Override
    public String toString() {
        return "Fragment{" +
                "id='" + id + '\'' +
                ", startIndex=" + startIndex +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
