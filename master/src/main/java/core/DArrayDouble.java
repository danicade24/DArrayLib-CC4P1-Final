package core;

import data.Fragment;

import java.util.LinkedList;
import java.util.List;

public class DArrayDouble {

    private double[] data;
    private int fragmentCount;
    private List<Fragment> fragments;
    private Fragment lastFragment;

    public DArrayDouble(double[] data, int fragmentCount) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("El array de datos no puede ser nulo o vacío.");
        }

        this.data = data;

        if(fragmentCount <= 0){
            throw new IllegalArgumentException("Los fragmentos no pueden ser negativos o nulos.");
        }
        this.fragmentCount = Math.min(fragmentCount, data.length);  // Garantiza que no haya fragmentos vacíos
        this.fragments = new LinkedList<>();
        divideArray();
    }

    /**
     * Divide el array original en fragmentos balanceados.
     */
    private void divideArray() {
        int n = data.length;
        int baseSize = n / fragmentCount;
        int remainder = n % fragmentCount;

        int start = 0;

        for (int i = 0; i < fragmentCount; i++) {
            int extra = (i < remainder) ? 1 : 0;
            int end = start + baseSize + extra;

            double[] fragmentData = new double[end - start];
            System.arraycopy(data, start, fragmentData, 0, end - start);

            fragments.add(new Fragment("F" + i, start,fragmentData));

            start = end;
        }
    }

    public double[] mergeFragments(List<Fragment> processedFragments) {
        double[] result = new double[data.length];
        for (Fragment frag : processedFragments) {
            System.arraycopy(frag.getData(), 0, result, frag.getStartIndex(), frag.getData().length);
        }
        return result;
    }

    public void add(double number) {
    // Crear nuevo array con un espacio adicional
    double[] newArray = new double[data.length + 1];
    System.arraycopy(data, 0, newArray, 0, data.length);
    newArray[data.length] = number;  // Agregar el nuevo número
    this.data = newArray;
}

public void add(double[] numbers) {
    if (numbers == null || numbers.length == 0) {
        return;  // Nada que agregar
    }
    double[] newArray = new double[data.length + numbers.length];
    System.arraycopy(data, 0, newArray, 0, data.length);               // Copiar array original
    System.arraycopy(numbers, 0, newArray, data.length, numbers.length); // Copiar array nuevo
    this.data = newArray;
}


    /**
     * Devuelve los fragmentos listos para enviar a los workers.
     */
    public List<Fragment> getFragments() {
        return fragments;
    }

    /**
     * Imprime todos los fragmentos (para depuración o presentación).
     */
    public void printFragments() {
        for (Fragment f : fragments) {
            System.out.println(f);
        }
    }
}
