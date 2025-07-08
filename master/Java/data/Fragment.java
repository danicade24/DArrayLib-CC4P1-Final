package data;

import java.util.Arrays;

public class Fragment {
    private String id;
    private double[] data;

    public Fragment(String id, double[] data) {
        this.id = id;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public double[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Fragment{" +
                "id='" + id + '\'' +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
