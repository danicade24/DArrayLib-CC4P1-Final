package handler;

import core.DArrayInt;
import data.FragmentInt;
import protocol.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * MasterServerInt: Versión para arrays de enteros con resiliencia y gestión de workers.
 */
public class MasterServerInt implements RecoveryCapable {

    private final int port;
    private final ResultManagerInt resultManager;
    private final DArrayInt dArray;
    private final List<WorkerConnectionInt> workers;
    private final List<WorkerConnectionInt> backupWorkers;
    private final Map<String, FragmentInt> workerToFragment;
    private String operation = "x";
    private int[] finalResult;
    private final WorkerHealthManager healthManager;

    public MasterServerInt(int port, DArrayInt dArray, int expectedFragments) {
        this.port = port;
        this.dArray = dArray;
        this.resultManager = new ResultManagerInt(expectedFragments);
        this.workers = new ArrayList<>();
        this.backupWorkers = new ArrayList<>();
        this.workerToFragment = new ConcurrentHashMap<>();
        this.healthManager = new WorkerHealthManager(this, 5000);
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void registerWorkers(List<WorkerConnectionInt> workerConnections) {
        workers.addAll(workerConnections);
    }

    public void registerBackupWorkers(List<WorkerConnectionInt> backups) {
        backupWorkers.addAll(backups);
    }

    public void start() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("✅ MasterServerInt escuchando en puerto " + port);

                distributeFragments();

                while (!resultManager.isComplete()) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> handleWorker(clientSocket)).start();
                }

                this.finalResult = resultManager.assembleResults();

                System.out.println("✅ Resultado final: " + Arrays.toString(finalResult));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public int[] getFinalResult() {
        return finalResult;
    }

    private void distributeFragments() {
        List<FragmentInt> fragments = dArray.getFragments();

        for (int i = 0; i < fragments.size(); i++) {
            if (i < workers.size()) {
                WorkerConnectionInt worker = workers.get(i);
                FragmentInt fragment = fragments.get(i);
                worker.sendTask(fragment, "T1", operation);
                workerToFragment.put(worker.getWorkerId(), fragment);
            } else {
                System.err.println("⚠ No hay suficientes workers para el fragmento " + i);
            }
        }
    }

    private void handleWorker(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
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

                int[] parsedResult = parseArray(resultArray);
                FragmentInt fragment = new FragmentInt(workerId, 0, parsedResult);
                resultManager.addResult(taskId, fragment);

            } else if ("HEARTBEAT".equals(type)) {
                String workerId = message.get("worker_id");
                healthManager.updateHeartbeat(workerId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int[] parseArray(String arrayStr) {
        arrayStr = arrayStr.replace("[", "").replace("]", "");
        String[] parts = arrayStr.split(",");
        int[] result = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i].trim());
        }

        return result;
    }

    @Override
    public void triggerRecoveryForWorker(String failedWorkerId) {
        FragmentInt fragment = workerToFragment.get(failedWorkerId);

        if (fragment == null) {
            System.err.println("⚠ No se encontró fragmento para " + failedWorkerId);
            return;
        }

        if (backupWorkers.isEmpty()) {
            System.err.println("🚨 No hay workers de respaldo disponibles.");
            return;
        }

        WorkerConnectionInt backup = backupWorkers.remove(0);
        System.out.println("🔄 Reenviando fragmento a " + backup.getWorkerId());

        backup.sendTask(fragment, "T1", operation);
        workerToFragment.put(backup.getWorkerId(), fragment);
    }
}