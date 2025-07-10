package handler;

import core.DArrayDouble;
import data.Fragment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test para el nuevo MasterServer que usa sendTaskAndGetResult(...)
 * y ExecutorService para procesar en paralelo.
 */
class MasterServerTest {

    private MasterServer masterServer;

    @BeforeEach
    void setUp() {
        // Datos de entrada: [1.0, 2.0, 3.0, 4.0], fragmentados en 2 partes => [1,2] y [3,4]
        double[] data = {1.0, 2.0, 3.0, 4.0};
        DArrayDouble dArray = new DArrayDouble(data, 2);

        // Creamos 2 workers "falsos" que responden inmediatamente con el mismo fragmento
        WorkerConnection fakeWorker1 = new WorkerConnection("w1", "", 0) {
            @Override
            public Map<String, String> sendTaskAndGetResult(Fragment fragment,
                                                            String taskId,
                                                            String operation) {
                // Devuelve result = fragment.getData()
                return Map.of(
                    "type",    "result",
                    "task_id", taskId,
                    "result",  Arrays.toString(fragment.getData())
                );
            }
        };

        WorkerConnection fakeWorker2 = new WorkerConnection("w2", "", 0) {
            @Override
            public Map<String, String> sendTaskAndGetResult(Fragment fragment,
                                                            String taskId,
                                                            String operation) {
                return Map.of(
                    "type",    "result",
                    "task_id", taskId,
                    "result",  Arrays.toString(fragment.getData())
                );
            }
        };

        // Instanciamos el MasterServer con el nuevo constructor
        masterServer = new MasterServer(
            dArray,
            List.of(fakeWorker1, fakeWorker2)
        );
        // Definimos la operación (no tiene efecto en los fakeWorkers, pero para mantener la API)
        masterServer.setOperation("((sin(x) + cos(x))^2) / (sqrt(abs(x)) + 1)");
    }

    @Test
    void testMasterServerAssemblesFragmentsCorrectly() {
        // Ejecutamos la distribución y recolección
        masterServer.start();

        // Obtenemos el resultado final
        double[] result = masterServer.getFinalResult();
        assertNotNull(result, "El resultado no debe ser null");

        // Debería ser la concatenación exacta de [1,2] y [3,4]
        assertArrayEquals(
            new double[]{1.0, 2.0, 3.0, 4.0},
            result,
            "El resultado final debe ser [1.0, 2.0, 3.0, 4.0]"
        );
    }
}
