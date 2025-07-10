package data;

import java.util.Arrays;

/**
 * FragmentInt: Representa un fragmento de un arreglo de enteros para procesamiento distribuido.
 */
public class FragmentInt {

    private final String id;
    private final int startIndex;
    private final int[] data;

    /**
     * Construye un FragmentInt con identificador, índice de inicio y datos.
     * @param id Identificador único del fragmento.
     * @param startIndex Índice inicial dentro del arreglo original.
     * @param data Subarreglo de enteros.
     */
    public FragmentInt(String id, int startIndex, int[] data) {
        this.id = id;
        this.startIndex = startIndex;
        this.data = data;
    }

    /**
     * Obtiene el identificador del fragmento.
     * @return ID del fragmento.
     */
    public String getId() {
        return id;
    }

    /**
     * Obtiene el índice de inicio del fragmento.
     * @return Índice inicial.
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * Obtiene los datos del fragmento.
     * @return Arreglo de enteros.
     */
    public int[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "FragmentInt{" +
                "id='" + id + '\'' +
                ", startIndex=" + startIndex +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
