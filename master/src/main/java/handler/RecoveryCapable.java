package handler;

/**
 * RecoveryCapable: Interfaz para servidores maestros capaces de recuperar nodos caídos.
 */
public interface RecoveryCapable {

    /**
     * Lanza la recuperación de un fragmento asignado a un worker caído.
     * @param failedWorkerId ID del worker fallido.
     */
    void triggerRecoveryForWorker(String failedWorkerId);
}
