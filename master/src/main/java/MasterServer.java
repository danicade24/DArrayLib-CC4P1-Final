import core.DArrayDouble;
import data.Fragment;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

/**
 * MasterServer: Gestiona la comunicación con los workers, distribuye fragmentos y recolecta resultados.
 */
public class MasterServer {

    private final int port;
    private final ResultManager resultManager;
    private final DArrayDouble dArray;

    /**
     * Crea un MasterServer en el puerto especificado.
     *
     * @param port              Puerto en el que el servidor escuchará.
     * @param dArray            Array de datos a procesar.
     * @param expectedFragments Número total de fragmentos esperados.
     */
    public MasterServer(int port, DArrayDouble dArray, int expectedFragments) {
        this.port = port;
        this.dArray = dArray;
        this.resultManager = new ResultManager(expectedFragments);
    }

    /**
     * Inicia el servidor maestro en un nuevo hilo y espera conexiones de los workers.
     */
    public void start() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("✅ MasterServer escuchando en puerto " + port);

                while (!resultManager.isComplete()) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> handleWorker(clientSocket)).start();
                }

                double[] finalResult = resultManager.assembleResults();
                System.out.println("✅ Resultado final: ");
                for (double v : finalResult) {
                    System.out.print(v + " ");
                }
                System.out.println();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Maneja la comunicación con un worker específico, interpretando mensajes y recolectando resultados.
     *
     * @param clientSocket Socket de conexión con el worker.
     */
    private void handleWorker(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream();
            PrintWriter writer = new PrintWriter(out, true);
        ) {
            String line = in.readLine();
            Map<String, String> message = ProtocolHandler.fromJson(line);

            String type = message.get("type");

            if ("INIT".equals(type)) {
                writer.println(ProtocolHandler.toJson(ProtocolHandler.createDoneMessage()));

            } else if ("RESULT".equals(type)) {
                String taskId = message.get("task_id");
                String workerId = message.get("worker_id");
                String resultArray = message.get("result");

                double[] parsedResult = parseArray(resultArray);
                int startIndex = findStartIndex(workerId); // Implementación placeholder

                Fragment fragment = new Fragment(workerId, startIndex, parsedResult);
                resultManager.addResult(taskId, fragment);
            }

            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Parsea una cadena JSON de array a un array de doubles.
     *
     * @param arrayStr Cadena con formato [x, y, z]
     * @return Array de doubles.
     */
    private double[] parseArray(String arrayStr) {
        arrayStr = arrayStr.replace("[", "").replace("]", "");
        String[] parts = arrayStr.split(",");
        double[] result = new double[parts.length];

        for (int i = 0; i < parts.length; i++) {
            result[i] = Double.parseDouble(parts[i].trim());
        }

        return result;
    }

    /**
     * Obtiene el índice de inicio para un worker.
     * Actualmente devuelve siempre 0 (placeholder).
     *
     * @param workerId Identificador del worker.
     * @return Índice de inicio (provisional).
     */
    private int findStartIndex(String workerId) {
        return 0;
    }

    /**
     * Método principal para iniciar el servidor manualmente.
     */
    public static void main(String[] args) {
        double[] data = {1.0, 2.0, 3.0, 4.0};
        DArrayDouble dArray = new DArrayDouble(data, 2);

        MasterServer server = new MasterServer(5000, dArray, 2);
        server.start();
    }
}
