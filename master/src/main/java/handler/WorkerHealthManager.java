package handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WorkerHealthManager: Supervisa la salud de los workers mediante heartbeats.
 */
public class WorkerHealthManager {

    private final RecoveryCapable master;
    private final int timeoutMillis;
    private final Map<String, Long> heartbeats;

    public WorkerHealthManager(RecoveryCapable master, int timeoutMillis) {
        this.master = master;
        this.timeoutMillis = timeoutMillis;
        this.heartbeats = new ConcurrentHashMap<>();

        startMonitoring();
    }

    public void updateHeartbeat(String workerId) {
        heartbeats.put(workerId, System.currentTimeMillis());
    }

    private void startMonitoring() {
        new Thread(() -> {
            while (true) {
                long now = System.currentTimeMillis();

                for (String workerId : heartbeats.keySet()) {
                    long lastSeen = heartbeats.getOrDefault(workerId, 0L);
                    if (now - lastSeen > timeoutMillis) {
                        System.err.println("❌ Nodo " + workerId + " no responde. Activando recuperación.");
                        master.triggerRecoveryForWorker(workerId);
                        heartbeats.remove(workerId);
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
}