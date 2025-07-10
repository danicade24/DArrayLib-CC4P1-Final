package handler;

import core.DArrayDouble;
import data.Fragment;

import java.util.*;
import java.util.concurrent.*;

/**
 * MasterServer: distribuye fragmentos a múltiples workers en paralelo
 * y recoge los resultados en un solo round-trip por worker.
 */
public class MasterServer {

    private final DArrayDouble dArray;
    private final List<WorkerConnection> workers;
    private final ResultManager resultManager;
    private String operation = Operation.IDENTITY;

    /**
     * Constructor principal.
     *
     * @param dArray  El array distribuido a fragmentar.
     * @param workers Lista de conexiones a workers disponibles.
     */
    public MasterServer(DArrayDouble dArray, List<WorkerConnection> workers) {
        this.dArray        = dArray;
        this.workers       = new ArrayList<>(workers);
        this.resultManager = new ResultManager(dArray.getFragments().size());
    }

    /**
     * Constructor de compatibilidad con la versión anterior.
     *
     * @param port              (despreciado) ya no se usa internamente.
     * @param dArray            El array distribuido a fragmentar.
     * @param expectedFragments (despreciado) ya no se usa, se infiere de dArray.
     */
    @Deprecated
    public MasterServer(int port, DArrayDouble dArray, int expectedFragments) {
        this(dArray, new ArrayList<>());
    }

    /**
     * Define la operación matemática que aplicarán los workers.
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Registra los workers que se emplearán para procesar (para el constructor de compatibilidad).
     */
    public void registerWorkers(List<WorkerConnection> workers) {
        this.workers.addAll(workers);
    }

    /**
     * Arranca la distribución de fragmentos en paralelo y espera todas las respuestas.
     */
    public void start() {
        System.out.println("✅ MasterServer arrancando tareas…");

        List<Fragment> fragments = dArray.getFragments();
        int nTasks = Math.min(fragments.size(), workers.size());

        ExecutorService exec = Executors.newFixedThreadPool(nTasks);
        List<Future<Map<String,String>>> futures = new ArrayList<>();

        // 1) Enviar cada fragmento en paralelo
        for (int i = 0; i < nTasks; i++) {
            final int idx = i;
            Fragment frag       = fragments.get(idx);
            WorkerConnection wc = workers.get(idx);
            String taskId       = "T" + idx;

            futures.add(exec.submit(() ->
                wc.sendTaskAndGetResult(frag, taskId, operation)
            ));
        }

        // 2) Recolectar cada respuesta
        for (int i = 0; i < futures.size(); i++) {
            try {
                Map<String,String> resp = futures.get(i).get();
                String type = resp.get("type");
                if (!"result".equals(type)) {
                    System.err.println("⚠ Worker " + i + " devolvió error: " + resp.get("message"));
                    continue;
                }

                String taskId   = resp.get("task_id");
                String resultJs = resp.get("result");
                double[] result = parseArray(resultJs);

                Fragment frag = fragments.get(i);
                resultManager.addResult(
                    taskId,
                    new Fragment(workers.get(i).getWorkerId(),
                                 frag.getStartIndex(),
                                 result)
                );
            } catch (InterruptedException|ExecutionException e) {
                System.err.println("❌ Error procesando fragmento " + i + ": " + e.getMessage());
            }
        }

        exec.shutdown();

        // 3) Ensamblar y mostrar resultado final
        double[] finalRes = resultManager.assembleResults();
        System.out.println("✅ Resultado final: " + Arrays.toString(finalRes));
    }

    /**
     * Devuelve el resultado final ensamblado o null si aún no está listo.
     */
    public double[] getFinalResult() {
        return resultManager.isComplete()
             ? resultManager.assembleResults()
             : null;
    }

    /**
     * Parsea la representación JSON de array ("[1.0,2.0]") a double[].
     */
    private double[] parseArray(String arrayStr) {
        String s = (arrayStr == null ? "" : arrayStr.trim());
        if (s.startsWith("[")) s = s.substring(1);
        if (s.endsWith("]"))   s = s.substring(0, s.length()-1);

        if (s.isEmpty()) return new double[0];

        String[] parts = s.split(",");
        double[] res = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            res[i] = Double.parseDouble(parts[i].trim());
        }
        return res;
    }

    /**
     * Ejemplo de uso en main().
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Uso: java handler.MasterServer <basePort> <nWorkers>");
            System.exit(1);
        }

        int basePort = Integer.parseInt(args[0]);  // p.ej. 6001
        int nWorkers = Integer.parseInt(args[1]);  // p.ej. 3, 5, 10, lo que quieras

        // --- Genera datos de prueba ---
        // Por ejemplo, un array de tamaño 100:
        double[] data = new double[1000];
        for (int i = 0; i < data.length; i++) data[i] = i + 1;

        // Fragmenta en nWorkers pedazos
        DArrayDouble dArray = new DArrayDouble(data, nWorkers);

        // Construye dinámicamente la lista de conexiones
        List<WorkerConnection> workers = new ArrayList<>();
        for (int i = 0; i < nWorkers; i++) {
            int port = basePort + i * 2;             // basePort, basePort+2, basePort+4, …
            workers.add(new WorkerConnection("worker" + (i+1),"localhost", port));
        }

        // Arranca el Master
        MasterServer master = new MasterServer(dArray, workers);
        master.setOperation(Operation.SIN_PLUS_COS_SQUARE_DIV_SQRT);
        master.start();

        // Opcional: espera un instante y muestra el resultado final
        Thread.sleep(1000);
        System.out.println("🔍 Resultado final: " +
            Arrays.toString(master.getFinalResult()));
    }

}
