package handler;

import data.Fragment;
import protocol.ProtocolHandler;

import java.io.PrintWriter;
import java.net.Socket;

/**
 * Representa una conexión hacia un worker remoto. Se encarga de enviar tareas (fragmentos).
 */
public class WorkerConnection {

    private final String workerId;
    private final String host;
    private final int port;

    /**
     * Crea una nueva conexión a un worker.
     *
     * @param workerId Identificador lógico del worker.
     * @param host     Dirección IP o nombre del host del worker.
     * @param port     Puerto TCP del worker.
     */
    public WorkerConnection(String workerId, String host, int port) {
        this.workerId = workerId;
        this.host = host;
        this.port = port;
    }

    /**
     * Envía un fragmento al worker con una tarea específica y una operación matemática.
     *
     * @param fragment Fragmento de datos a procesar.
     * @param taskId   Identificador único de la tarea.
     * @param operation Expresión matemática en formato String.
     */
    public void sendTask(Fragment fragment, String taskId, String operation) {
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
