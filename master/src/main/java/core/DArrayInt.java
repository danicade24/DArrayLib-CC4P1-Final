package core;

import data.FragmentInt;

import java.util.ArrayList;
import java.util.List;

/**
 * DArrayInt: Maneja la segmentación y gestión de arreglos de enteros para procesamiento distribuido.
 */
public class DArrayInt {

    private int[] data;
    private int fragmentCount;
    private List<FragmentInt> fragments;

    /**
     * Crea un DArrayInt con segmentación automática.
     * @param data Arreglo original de enteros.
     * @param fragmentCount Número de fragmentos.
     */
    public DArrayInt(int[] data, int fragmentCount) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("El array de datos no puede ser nulo o vacío.");
        }

        this.data = data;

        if (fragmentCount <= 0) {
            throw new IllegalArgumentException("Los fragmentos no pueden ser negativos o nulos.");
        }
        this.fragmentCount = Math.min(fragmentCount, data.length);
        this.fragments = new ArrayList<>();
        divideArray();
    }

    /**
     * Divide el arreglo en fragmentos balanceados.
     */
    private void divideArray() {
        fragments.clear();

        int n = data.length;
        int baseSize = n / fragmentCount;
        int remainder = n % fragmentCount;

        int start = 0;

        for (int i = 0; i < fragmentCount; i++) {
            int extra = (i < remainder) ? 1 : 0;
            int end = start + baseSize + extra;

            int[] fragmentData = new int[end - start];
            System.arraycopy(data, start, fragmentData, 0, end - start);

            fragments.add(new FragmentInt("F" + i, start, fragmentData));

            start = end;
        }
    }

    /**
     * Devuelve la lista de fragmentos generados.
     * @return Lista de fragmentos.
     */
    public List<FragmentInt> getFragments() {
        return List.copyOf(fragments);
    }

    /**
     * Ensambla los resultados de los fragmentos procesados.
     * @param processedFragments Lista de fragmentos procesados.
     * @return Arreglo combinado con los resultados.
     */
    public int[] mergeFragments(List<FragmentInt> processedFragments) {
        int[] result = new int[data.length];
        for (FragmentInt frag : processedFragments) {
            System.arraycopy(frag.getData(), 0, result, frag.getStartIndex(), frag.getData().length);
        }
        return result;
    }

    /**
     * Imprime los fragmentos para depuración.
     */
    public void printFragments() {
        for (FragmentInt f : fragments) {
            System.out.println(f);
        }
    }
}
