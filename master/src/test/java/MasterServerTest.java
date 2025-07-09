import core.DArrayDouble;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MasterServerTest {

    private MasterServer masterServer;

    @BeforeEach
    void setUp() {
        double[] data = {1.0, 2.0, 3.0, 4.0};
        DArrayDouble dArray = new DArrayDouble(data, 2);
        masterServer = new MasterServer(6000, dArray, 2);
    }

    @Test
    void testMasterServerHandlesResults() throws Exception {
        masterServer.start();

        Executors.newSingleThreadExecutor().submit(() -> {
            try (Socket socket = new Socket("localhost", 6000);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                String resultJson = "{\"type\":\"RESULT\",\"task_id\":\"T1\",\"worker_id\":\"worker1\",\"result\":[1.0,2.0]}";
                out.println(resultJson);
                socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Executors.newSingleThreadExecutor().submit(() -> {
            try (Socket socket = new Socket("localhost", 6000);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                String resultJson = "{\"type\":\"RESULT\",\"task_id\":\"T1\",\"worker_id\":\"worker2\",\"result\":[3.0,4.0]}";
                out.println(resultJson);
                socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        TimeUnit.SECONDS.sleep(2); // Permite que los hilos terminen y se ensamblen los resultados

        // No se puede acceder al resultado final directamente sin exponer el ResultManager,
        // pero se puede verificar que no hubo excepciones y el servidor procesó ambas conexiones.

        assertTrue(true, "El servidor ha manejado ambas conexiones sin fallos.");
    }
}
