package protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * ProtocolHandler: Maneja la creación y parsing de mensajes JSON para workers con soporte para double[] e int[].
 */
public class ProtocolHandler {

    /**
     * Convierte un mapa en una cadena JSON simple.
     *
     * @param map Mapa de datos.
     * @return Cadena JSON.
     */
    public static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
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
            } else if (value instanceof int[]) {
                sb.append(arrayToJson((int[]) value));
            } else if (value instanceof Map) {
                sb.append(toJson((Map<String, Object>) value));
            } else {
                sb.append("null");
            }

            if (++count < map.size()) sb.append(",");
        }

        sb.append("}");
        return sb.toString();
    }

    private static String arrayToJson(double[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String arrayToJson(int[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) sb.append(",");
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

    public static Map<String, Object> createTaskMessage(String taskId, double[] fragment, String operation, String sendResultTo) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "TASK");
        message.put("task_id", taskId);
        message.put("fragment", fragment);
        message.put("operation", operation);
        message.put("send_result_to", sendResultTo);
        return message;
    }

    public static Map<String, Object> createTaskMessage(String taskId, int[] fragment, String operation, String sendResultTo) {
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

    public static Map<String, Object> createResultMessage(String taskId, String workerId, int[] result) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "RESULT");
        message.put("task_id", taskId);
        message.put("worker_id", workerId);
        message.put("result", result);
        return message;
    }

    public static Map<String, Object> createDoneMessage() {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "DONE");
        return message;
    }
}
