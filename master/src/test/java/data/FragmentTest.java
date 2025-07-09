package data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FragmentTest {

    private Fragment fragment;
    private double[] sampleData;

    @BeforeEach
    void setUp() {
        sampleData = new double[]{1.1, 2.2, 3.3, 4.4};
        fragment = new Fragment("F0", 0, sampleData);
    }

    @Test
    void testFragmentCreation() {
        assertNotNull(fragment);
        assertEquals("F0", fragment.getId());
        assertEquals(0, fragment.getStartIndex());
        assertArrayEquals(sampleData, fragment.getData());
    }

    @Test
    void testGetId() {
        assertEquals("F0", fragment.getId());
    }

    @Test
    void testGetStartIndex() {
        assertEquals(0, fragment.getStartIndex());
    }

    @Test
    void testGetData() {
        double[] expected = {1.1, 2.2, 3.3, 4.4};
        assertArrayEquals(expected, fragment.getData());
    }

    @Test
    void testToString() {
        String expected = "Fragment{id='F0', startIndex=0, data=[1.1, 2.2, 3.3, 4.4]}";
        assertEquals(expected, fragment.toString());
    }

    @Test
    void testEmptyData() {
        Fragment emptyFragment = new Fragment("F1", 5, new double[0]);
        assertNotNull(emptyFragment.getData());
        assertEquals(0, emptyFragment.getData().length);
        assertEquals(5, emptyFragment.getStartIndex());
        assertEquals("F1", emptyFragment.getId());
    }
}
