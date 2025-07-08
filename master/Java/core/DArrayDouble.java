package core;

import data.Fragment;

import java.util.ArrayList;
import java.util.List;

public class DArrayDouble {

    private double[] originalArray;
    private int fragmentCount;
    private List<Fragment> fragments;

    public DArrayDouble(double[] array, int fragmentCount) {
        this.originalArray = array;
        this.fragmentCount = fragmentCount;
        this.fragments = new ArrayList<>();
        divideArray();
    }

    private void divideArray() {
        int n = originalArray.length;
        int baseSize = n / fragmentCount;
        int remainder = n % fragmentCount;

        int start = 0;

        for (int i = 0; i < fragmentCount; i++) {
            int extra = (i < remainder) ? 1 : 0;
            int end = start + baseSize + extra;

            double[] fragmentData = new double[end - start];
            System.arraycopy(originalArray, start, fragmentData, 0, end - start);

            fragments.add(new Fragment("F" + i, fragmentData));

            start = end;
        }
    }

    public List<Fragment> getFragments() {
        return fragments;
    }

    public void printFragments() {
        for (Fragment f : fragments) {
            System.out.println(f);
        }
    }
}
