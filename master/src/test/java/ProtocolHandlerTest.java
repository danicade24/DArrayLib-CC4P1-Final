import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProtocolHandlerTest {

    @Test
    void testInitMessage() {
        Map<String, Object> msg = ProtocolHandler.createInitMessage("worker_01", 4, "python");
        String json = ProtocolHandler.toJson(msg);
        assertTrue(json.contains("\"type\":\"INIT\""));
        assertTrue(json.contains("\"worker_id\":\"worker_01\""));
        assertTrue(json.contains("\"cores\":4"));
    }

    @Test
    void testTaskMessage() {
        Map<String, Object> msg = ProtocolHandler.createTaskMessage("T123", new double[]{1.0, 2.0}, "x+1", "localhost:9000");
        String json = ProtocolHandler.toJson(msg);
        assertTrue(json.contains("\"type\":\"TASK\""));
        assertTrue(json.contains("[1.0,2.0]"));
    }

    @Test
    void testResultMessage() {
        Map<String, Object> msg = ProtocolHandler.createResultMessage("T123", "worker_01", new double[]{0.5, 0.8});
        String json = ProtocolHandler.toJson(msg);
        assertTrue(json.contains("\"type\":\"RESULT\""));
        assertTrue(json.contains("[0.5,0.8]"));
    }

    @Test
    void testHeartbeatMessage() {
        Map<String, Object> msg = ProtocolHandler.createHeartbeatMessage("worker_01");
        String json = ProtocolHandler.toJson(msg);
        assertTrue(json.contains("\"type\":\"HEARTBEAT\""));
    }

    @Test
    void testReplicaMessage() {
        Map<String, Object> msg = ProtocolHandler.createReplicaMessage("T123", new double[]{1.0, 2.0}, "replica_1");
        String json = ProtocolHandler.toJson(msg);
        assertTrue(json.contains("\"type\":\"REPLICA\""));
        assertTrue(json.contains("\"replica_id\":\"replica_1\""));
    }

    @Test
    void testRecoverMessage() {
        Map<String, Object> msg = ProtocolHandler.createRecoverMessage("worker_02", "T123");
        String json = ProtocolHandler.toJson(msg);
        assertTrue(json.contains("\"type\":\"RECOVER\""));
        assertTrue(json.contains("\"original_worker_id\":\"worker_02\""));
    }

    @Test
    void testErrorMessage() {
        Map<String, Object> msg = ProtocolHandler.createErrorMessage("Something went wrong");
        String json = ProtocolHandler.toJson(msg);
        assertTrue(json.contains("\"type\":\"ERROR\""));
        assertTrue(json.contains("\"message\":\"Something went wrong\""));
    }

    @Test
    void testDoneMessage() {
        Map<String, Object> msg = ProtocolHandler.createDoneMessage();
        String json = ProtocolHandler.toJson(msg);
        assertTrue(json.contains("\"type\":\"DONE\""));
    }

    @Test
    void testFromJsonValid() {
        String json = "{\"type\":\"RESULT\",\"worker_id\":\"worker_02\"}";
        Map<String, String> map = ProtocolHandler.fromJson(json);
        assertEquals("RESULT", map.get("type"));
        assertEquals("worker_02", map.get("worker_id"));
    }

    @Test
    void testFromJsonInvalid() {
        String json = "\"type\":\"ERROR\"";
        Exception ex = assertThrows(IllegalArgumentException.class, () -> ProtocolHandler.fromJson(json));
        assertTrue(ex.getMessage().contains("JsonParseException"));
    }
}
