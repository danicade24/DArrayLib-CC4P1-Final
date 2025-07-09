package core;

import data.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * fragmentación fija con re-fragmentación obligatoria al modificar los datos.
 * Garantiza consistencia y protección de los fragmentos.
 */
public class DArrayDouble {

    private double[] data;
    private int fragmentCount;
    private List<Fragment> fragments;
    private boolean needsUpdate;

    /**
     * Crea una nueva instancia de DArrayDouble con los datos y número de fragmentos especificados.
     *
     * @param data          El array de datos original (no nulo, no vacío).
     * @param fragmentCount El número deseado de fragmentos (debe ser positivo).
     * @throws IllegalArgumentException Si los datos son inválidos o el número de fragmentos no es válido.
     */
    public DArrayDouble(double[] data, int fragmentCount) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("El array de datos no puede ser nulo o vacío.");
        }

        if (fragmentCount <= 0) {
            throw new IllegalArgumentException("El número de fragmentos debe ser positivo.");
        }

        this.data = Arrays.copyOf(data, data.length);
        this.fragmentCount = Math.min(fragmentCount, data.length);
        this.fragments = new ArrayList<>();
        this.needsUpdate = true;
        divideArray();
    }

    /**
     * Divide el array actual en fragmentos balanceados según el número de fragmentos definido.
     * Este método se llama automáticamente cuando los datos cambian.
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

            double[] fragmentData = Arrays.copyOfRange(data, start, end);
            fragments.add(new Fragment("F" + i, start, fragmentData));

            start = end;
        }

        needsUpdate = false;
    }

    /**
     * Agrega un nuevo número al array de datos y marca los fragmentos para ser recalculados.
     *
     * @param number El número a agregar al array.
     */
    public void add(double number) {
        double[] newArray = Arrays.copyOf(data, data.length + 1);
        newArray[data.length] = number;
        this.data = newArray;
        this.fragmentCount = Math.min(fragmentCount, data.length);
        this.needsUpdate = true;
    }

    /**
     * Agrega múltiples números al array de datos y marca los fragmentos para ser recalculados.
     *
     * @param numbers El array de números a agregar.
     */
    public void add(double[] numbers) {
        if (numbers == null || numbers.length == 0) {
            return;
        }

        double[] newArray = Arrays.copyOf(data, data.length + numbers.length);
        System.arraycopy(numbers, 0, newArray, data.length, numbers.length);
        this.data = newArray;
        this.fragmentCount = Math.min(fragmentCount, data.length);
        this.needsUpdate = true;
    }

    /**
     * Reconstruye el array original combinando los resultados de los fragmentos procesados.
     *
     * @param processedFragments Lista de fragmentos procesados.
     * @return Un nuevo array de datos combinado.
     */
    public double[] mergeFragments(List<Fragment> processedFragments) {
        double[] result = new double[data.length];
        for (Fragment frag : processedFragments) {
            System.arraycopy(frag.getData(), 0, result, frag.getStartIndex(), frag.getData().length);
        }
        return result;
    }

    /**
     * Devuelve los fragmentos actuales de forma inmutable.
     * Si los fragmentos necesitan actualizarse, se recalculan automáticamente.
     *
     * @return Lista inmutable de fragmentos balanceados.
     */
    public List<Fragment> getFragments() {
        if (needsUpdate) {
            divideArray();
        }
        return List.copyOf(fragments);
    }

    /**
     * Imprime los fragmentos actuales en la consola para depuración o verificación.
     */
    public void printFragments() {
        getFragments().forEach(System.out::println);
    }
}
