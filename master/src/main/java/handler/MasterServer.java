package handler;

import core.DArrayDouble;
import data.Fragment;
import protocol.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MasterServer: Gestiona la comunicación con los workers, distribuye fragmentos y recolecta resultados.
 */
public class MasterServer {

    private final int port;
    private final ResultManager resultManager;
    private final DArrayDouble dArray;
    private final List<WorkerConnection> workers;
    private String operation = Operation.IDENTITY; // Operación por defecto
    private double[] finalResult; // Resultado final ensamblado

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
        this.workers = new ArrayList<>();
    }

    /**
     * Define la operación matemática que los workers deben aplicar.
     *
     * @param operation Expresión matemática en formato String.
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Registra una lista de workers disponibles para distribuir las tareas.
     *
     * @param workerConnections Lista de conexiones a workers.
     */
    public void registerWorkers(List<WorkerConnection> workerConnections) {
        workers.addAll(workerConnections);
    }

    /**
     * Inicia el servidor maestro en un nuevo hilo, distribuye las tareas y espera resultados.
     */
    public void start() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("✅ MasterServer escuchando en puerto " + port);

                distributeFragments();

                while (!resultManager.isComplete()) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> handleWorker(clientSocket)).start();
                }

                this.finalResult = resultManager.assembleResults();

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
     * Devuelve el resultado final ensamblado o null si aún no está listo.
     *
     * @return Array de resultados o null.
     */
    public double[] getFinalResult() {
        return finalResult;
    }

    /**
     * Envía los fragmentos a los workers registrados junto con la operación especificada.
     */
    private void distributeFragments() {
        List<Fragment> fragments = dArray.getFragments();

        for (int i = 0; i < fragments.size(); i++) {
            if (i < workers.size()) {
                WorkerConnection worker = workers.get(i);
                worker.sendTask(fragments.get(i), "T1", operation);
            } else {
                System.err.println("⚠ No hay suficientes workers registrados para enviar el fragmento " + i);
            }
        }
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
                int startIndex = findStartIndex(workerId);

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
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        double[] data = {1.0, 2.0, 3.0, 4.0};
        DArrayDouble dArray = new DArrayDouble(data, 2);

        MasterServer server = new MasterServer(5000, dArray, 2);

        server.setOperation(Operation.SIN_PLUS_COS_SQUARE_DIV_SQRT);

        List<WorkerConnection> workers = new ArrayList<>();
        workers.add(new WorkerConnection("worker1", "localhost", 6001));
        workers.add(new WorkerConnection("worker2", "localhost", 6002));

        server.registerWorkers(workers);
        server.start();

        try {
            Thread.sleep(3000); // Esperar a que lleguen los resultados
            double[] result = server.getFinalResult();

            if (result != null) {
                System.out.println("🔍 Resultado recuperado desde main:");
                for (double v : result) {
                    System.out.print(v + " ");
                }
                System.out.println();
            } else {
                System.out.println("El resultado aún no está listo.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
} 
