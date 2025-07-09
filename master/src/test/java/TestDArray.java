import core.DArrayDouble;

public class TestDArray {
    public static void main(String[] args) {
        double[] data = new double[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = i + 1.0;
        }

        DArrayDouble dArray = new DArrayDouble(data, 4);
        dArray.printFragments();
    }
}
