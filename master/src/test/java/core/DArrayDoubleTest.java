package core;

import data.Fragment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DArrayDoubleTest {

    private DArrayDouble dArray;

    @BeforeEach
    void setUp() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        dArray = new DArrayDouble(data, 2);
    }

    @Test
    void testInitialFragmentation() {
        List<Fragment> fragments = dArray.getFragments();
        assertEquals(2, fragments.size(), "Debe haber 2 fragmentos.");
        assertEquals(3, fragments.get(0).getData().length);
        assertEquals(2, fragments.get(1).getData().length);
    }

    @Test
    void testAddSingleValueAndFragmentUpdate() {
        dArray.add(6.0);
        List<Fragment> fragments = dArray.getFragments();

        // Ahora hay 6 elementos en total → con 2 fragmentos
        assertEquals(2, fragments.size());
        int totalElements = fragments.stream().mapToInt(f -> f.getData().length).sum();
        assertEquals(6, totalElements, "Debe haber 6 elementos en total después de add.");
    }

    @Test
    void testAddMultipleValuesAndFragmentUpdate() {
        dArray.add(new double[]{6.0, 7.0});
        List<Fragment> fragments = dArray.getFragments();

        assertEquals(2, fragments.size());
        int totalElements = fragments.stream().mapToInt(f -> f.getData().length).sum();
        assertEquals(7, totalElements, "Debe haber 7 elementos después de add múltiple.");
    }

    @Test
    void testMergeFragments() {
        List<Fragment> fragments = dArray.getFragments();
        double[] merged = dArray.mergeFragments(fragments);

        assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0, 5.0}, merged, "El array reconstruido debe coincidir.");
    }

    @Test
    void testGetFragmentsIsImmutable() {
        List<Fragment> fragments = dArray.getFragments();
        assertThrows(UnsupportedOperationException.class, () -> fragments.clear(),
                "La lista de fragmentos debe ser inmutable.");
    }

    @Test
    void testAddEmptyArrayDoesNotChangeFragments() {
        List<Fragment> before = dArray.getFragments();
        dArray.add(new double[]{});  // No debería cambiar nada
        List<Fragment> after = dArray.getFragments();
        assertEquals(before.size(), after.size(), "El número de fragmentos no debe cambiar.");
    }
}
