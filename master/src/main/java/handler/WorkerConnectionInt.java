package handler;

import data.FragmentInt;
import protocol.ProtocolHandler;

import java.io.PrintWriter;
import java.net.Socket;

/**
 * WorkerConnectionInt: Representa una conexión hacia un worker remoto para datos int.
 * Se encarga de enviar tareas (fragmentos) de enteros.
 */
public class WorkerConnectionInt {

    private final String workerId;
    private final String host;
    private final int port;

    /**
     * Crea una nueva conexión a un worker.
     *
     * @param workerId Identificador lógico del worker.
     * @param host Dirección IP o nombre del host.
     * @param port Puerto TCP del worker.
     */
    public WorkerConnectionInt(String workerId, String host, int port) {
        this.workerId = workerId;
        this.host = host;
        this.port = port;
    }

    /**
     * Devuelve el identificador del worker.
     * @return ID del worker.
     */
    public String getWorkerId() {
        return workerId;
    }

    /**
     * Envía un fragmento de enteros al worker junto con la tarea y operación.
     *
     * @param fragment Fragmento de datos int a procesar.
     * @param taskId ID único de la tarea.
     * @param operation Operación matemática a realizar.
     */
    public void sendTask(FragmentInt fragment, String taskId, String operation) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String json = ProtocolHandler.toJson(
                ProtocolHandler.createTaskMessage(
                    taskId,
                    fragment.getData(),
                    operation,
                    host + ":" + port
                )
            );

            out.println(json);
            System.out.println("Enviado a " + workerId + " -> " + json);

        } catch (Exception e) {
            System.err.println("Error al enviar tarea a " + workerId + ": " + e.getMessage());
        }
    }
}
