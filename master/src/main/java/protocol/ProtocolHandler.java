package protocol;
import java.util.HashMap;
import java.util.Map;

/**
 * Protocolo maestro: Traduce entre objetos Java y mensajes JSON manualmente.
 */
public class ProtocolHandler {

    public static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int count = 0;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();

            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else if (value instanceof Number) {
                sb.append(value);
            } else if (value instanceof double[]) {
                sb.append(arrayToJson((double[]) value));
            } else if (value instanceof Map) {
                sb.append(toJson((Map<String, Object>) value));
            } else {
                sb.append("null");
            }

            if (++count < map.size()) {
                sb.append(",");
            }
        }

        sb.append("}");
        return sb.toString();
    }

    private static String arrayToJson(double[] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(",");
            }
        }

        sb.append("]");
        return sb.toString();
    }

    public static Map<String, String> fromJson(String json) {
        Map<String, String> map = new HashMap<>();

        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new IllegalArgumentException("JsonParseException: Formato inválido de objeto JSON.");
        }

        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return map;

        String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length != 2) {
                throw new IllegalArgumentException("JsonParseException: Clave-valor mal formado.");
            }
            String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
            String value = keyValue[1].trim().replaceAll("^\"|\"$", "");

            map.put(key, value);
        }

        return map;
    }

    public static Map<String, Object> createInitMessage(String workerId, int cores, String language) {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("cores", cores);
        capabilities.put("language", language);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "INIT");
        message.put("worker_id", workerId);
        message.put("capabilities", capabilities);

        return message;
    }

    public static Map<String, Object> createTaskMessage(String taskId, double[] fragment, String operation, String sendResultTo) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "TASK");
        message.put("task_id", taskId);
        message.put("fragment", fragment);
        message.put("operation", operation);
        message.put("send_result_to", sendResultTo);

        return message;
    }

    public static Map<String, Object> createResultMessage(String taskId, String workerId, double[] result) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "RESULT");
        message.put("task_id", taskId);
        message.put("worker_id", workerId);
        message.put("result", result);

        return message;
    }

    public static Map<String, Object> createHeartbeatMessage(String workerId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "HEARTBEAT");
        message.put("worker_id", workerId);

        return message;
    }

    public static Map<String, Object> createReplicaMessage(String taskId, double[] fragment, String replicaId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "REPLICA");
        message.put("task_id", taskId);
        message.put("fragment", fragment);
        message.put("replica_id", replicaId);

        return message;
    }

    public static Map<String, Object> createRecoverMessage(String originalWorkerId, String taskId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "RECOVER");
        message.put("original_worker_id", originalWorkerId);
        message.put("task_id", taskId);

        return message;
    }

    public static Map<String, Object> createErrorMessage(String errorDescription) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "ERROR");
        message.put("message", errorDescription);

        return message;
    }

    public static Map<String, Object> createDoneMessage() {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "DONE");
        return message;
    }
}
