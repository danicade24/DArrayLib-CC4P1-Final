package handler;

import data.Fragment;
import protocol.ProtocolHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class WorkerConnection {

    private final String workerId;
    private final String host;
    private final int port;

    public WorkerConnection(String workerId, String host, int port) {
        this.workerId = workerId;
        this.host     = host;
        this.port     = port;
    }

    public String getWorkerId() {
        return workerId;
    }

    /**
     * Envía un fragmento al worker y devuelve el Map con el JSON de respuesta.
     */
    public Map<String, String> sendTaskAndGetResult(Fragment fragment,
                                                    String taskId,
                                                    String operation) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(
                 new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                 true);
             BufferedReader in = new BufferedReader(
                 new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
        ) {
            // 1) Preparar mensaje TASK
            String jsonReq = ProtocolHandler.toJson(
                ProtocolHandler.createTaskMessage(
                    taskId,
                    fragment.getData(),
                    operation,
                    host + ":" + port
                )
            );
            System.out.println("Enviado a " + workerId + " -> " + jsonReq);

            // 2) Enviar
            out.println(jsonReq);

            // 3) Leer respuesta (bloquea hasta '\n')
            String line = in.readLine();
            System.out.println("Respuesta de " + workerId + " <- " + line);

            // 4) Parsear y devolver
            return ProtocolHandler.fromJson(line);

        } catch (Exception e) {
            throw new RuntimeException(
              "Error en comunicación con " + workerId + "@" + host + ":" + port, e);
        }
    }
}
