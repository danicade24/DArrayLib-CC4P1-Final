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
import java.util.concurrent.ConcurrentHashMap;

/**
 * MasterServer: Gestiona la comunicación con los workers, distribuye fragmentos y recolecta resultados.
 * Incluye tolerancia a fallos mediante detección de latidos (heartbeat) y recuperación automática.
 */
public class MasterServer implements RecoveryCapable{

    private final int port;
    private final ResultManager resultManager;
    private final DArrayDouble dArray;
    private final List<WorkerConnection> workers;
    private final List<WorkerConnection> backupWorkers;
    private final Map<String, Fragment> workerToFragment;
    private String operation = Operation.IDENTITY;
    private double[] finalResult;
    private final WorkerHealthManager healthManager;

    /**
     * Construye un MasterServer con configuración inicial.
     * @param port Puerto de escucha para conexiones de workers.
     * @param dArray Datos a procesar.
     * @param expectedFragments Número total de fragmentos esperados.
     */
    public MasterServer(int port, DArrayDouble dArray, int expectedFragments) {
        this.port = port;
        this.dArray = dArray;
        this.resultManager = new ResultManager(expectedFragments);
        this.workers = new ArrayList<>();
        this.backupWorkers = new ArrayList<>();
        this.workerToFragment = new ConcurrentHashMap<>();
        this.healthManager = new WorkerHealthManager(this, 5000);
    }

    /**
     * Define la operación matemática a aplicar en los workers.
     * @param operation Expresión matemática como String.
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Registra los workers activos disponibles para recibir tareas.
     * @param workerConnections Lista de conexiones a workers.
     */
    public void registerWorkers(List<WorkerConnection> workerConnections) {
        workers.addAll(workerConnections);
    }

    /**
     * Registra los workers de respaldo para recuperación en caso de fallo.
     * @param backups Lista de workers de respaldo.
     */
    public void registerBackupWorkers(List<WorkerConnection> backups) {
        backupWorkers.addAll(backups);
    }

    /**
     * Inicia el servidor maestro, distribuye las tareas y escucha resultados.
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
     * Devuelve el resultado final ensamblado después de recibir todos los fragmentos.
     * @return Arreglo de doubles con el resultado global o null si no está listo.
     */
    public double[] getFinalResult() {
        return finalResult;
    }

    /**
     * Distribuye los fragmentos a los workers activos y registra la asignación.
     */
    private void distributeFragments() {
        List<Fragment> fragments = dArray.getFragments();

        for (int i = 0; i < fragments.size(); i++) {
            if (i < workers.size()) {
                WorkerConnection worker = workers.get(i);
                Fragment fragment = fragments.get(i);
                worker.sendTask(fragment, "T1", operation);
                workerToFragment.put(worker.getWorkerId(), fragment);
            } else {
                System.err.println("⚠ No hay suficientes workers registrados para enviar el fragmento " + i);
            }
        }
    }

    /**
     * Maneja la comunicación con un worker individual, procesando INIT, RESULT y HEARTBEAT.
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

            } else if ("HEARTBEAT".equals(type)) {
                String workerId = message.get("worker_id");
                healthManager.updateHeartbeat(workerId);
            }

            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Convierte una cadena de números en formato JSON a un arreglo de doubles.
     * @param arrayStr Cadena JSON con números.
     * @return Arreglo de doubles.
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
     * Obtiene el índice de inicio de un worker (actualmente sin implementación real).
     * @param workerId ID del worker.
     * @return Índice de inicio, siempre 0 por ahora.
     */
    private int findStartIndex(String workerId) {
        return 0;
    }

    /**
     * Lanza la recuperación de un fragmento asignado a un worker caído, utilizando un worker de respaldo.
     * @param failedWorkerId ID del worker fallido.
     */
    public void triggerRecoveryForWorker(String failedWorkerId) {
        Fragment fragment = workerToFragment.get(failedWorkerId);

        if (fragment == null) {
            System.err.println("⚠ No se encontró fragmento para " + failedWorkerId);
            return;
        }

        if (backupWorkers.isEmpty()) {
            System.err.println("🚨 No hay workers de reserva disponibles.");
            return;
        }

        WorkerConnection backup = backupWorkers.remove(0);
        System.out.println("🔄 Reenviando fragmento a " + backup.getWorkerId());

        backup.sendTask(fragment, "T1", operation);
        workerToFragment.put(backup.getWorkerId(), fragment);
    }

    /**
     * Método principal para iniciar el servidor y realizar pruebas manuales.
     * @param args Argumentos de línea de comandos.
     */
    public static void main(String[] args) {
        double[] data = {1.0, 2.0, 3.0, 4.0};
        DArrayDouble dArray = new DArrayDouble(data, 2);

        MasterServer server = new MasterServer(5000, dArray, 2);

        server.setOperation(Operation.SIN_PLUS_COS_SQUARE_DIV_SQRT);

        List<WorkerConnection> workers = new ArrayList<>();
        workers.add(new WorkerConnection("worker1", "localhost", 6001));
        workers.add(new WorkerConnection("worker2", "localhost", 6002));

        List<WorkerConnection> backups = new ArrayList<>();
        backups.add(new WorkerConnection("worker3", "localhost", 6003));

        server.registerWorkers(workers);
        server.registerBackupWorkers(backups);
        server.start();

        try {
            Thread.sleep(3000);
            double[] result = server.getFinalResult();

            if (result != null) {
                System.out.println("🔍 Resultado recuperado desde main:");
                for (double v : result) {
                    System.out.print(v + " ");
                }
                System.out.println();
            } else {
                System.out.println("❗ El resultado aún no está listo.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
