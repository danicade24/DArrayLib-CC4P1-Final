import data.Fragment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultManagerTest {

    private ResultManager resultManager;

    @BeforeEach
    void setUp() {
        resultManager = new ResultManager(2); // Esperamos 2 fragmentos
    }

    @Test
    void testAddAndIsComplete() {
        Fragment frag1 = new Fragment("F0", 0, new double[]{1.0, 2.0});
        Fragment frag2 = new Fragment("F1", 2, new double[]{3.0, 4.0});

        resultManager.addResult("T1", frag1);
        assertFalse(resultManager.isComplete(), "No debe estar completo aún");

        resultManager.addResult("T1", frag2);
        assertTrue(resultManager.isComplete(), "Debe estar completo al recibir los 2 fragmentos");
    }

    @Test
    void testAssembleResultsCorrectOrder() {
        Fragment frag1 = new Fragment("F1", 2, new double[]{3.0, 4.0});
        Fragment frag0 = new Fragment("F0", 0, new double[]{1.0, 2.0});

        resultManager.addResult("T1", frag1);
        resultManager.addResult("T1", frag0);

        double[] merged = resultManager.assembleResults();

        assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0}, merged, "Los fragmentos deben ensamblarse en orden correcto");
    }

    @Test
    void testAssembleResultsWithIncompleteFragments() {
        Fragment frag = new Fragment("F0", 0, new double[]{1.0, 2.0});

        resultManager.addResult("T1", frag);

        double[] partial = resultManager.assembleResults();

        assertArrayEquals(new double[]{1.0, 2.0}, partial, "Debe ensamblar lo que se tiene hasta ahora sin fallar");
    }

    /*@Test
    void testMultipleTasksIndependence() {
        ResultManager multiTaskManager = new ResultManager(4);

        Fragment fragA1 = new Fragment("F0", 0, new double[]{1.0});
        Fragment fragA2 = new Fragment("F1", 1, new double[]{2.0});
        Fragment fragB1 = new Fragment("F0", 0, new double[]{10.0});
        Fragment fragB2 = new Fragment("F1", 1, new double[]{20.0});

        multiTaskManager.addResult("A", fragA1);
        multiTaskManager.addResult("A", fragA2);
        multiTaskManager.addResult("B", fragB1);
        multiTaskManager.addResult("B", fragB2);

        assertTrue(multiTaskManager.isComplete(), "Debe estar completo con 4 fragmentos en total");

        double[] merged = multiTaskManager.assembleResults();
        assertTrue(Arrays.stream(merged).anyMatch(d -> d == 1.0));
        assertTrue(Arrays.stream(merged).anyMatch(d -> d == 10.0));
    }*/
}