import socketserver
import json
import math
import logging
from jsonschema import validate, ValidationError

# Schema que valida el task_id, type, data y operation
TASK_SCHEMA = {
    "type": "object",
    "properties": {
        "type":      {"type": "string", "enum": ["task", "heartbeat"]},
        "task_id":   {"type": "string"},
        "data":      {"type": "array",  "items": {"type": "number"}},
        "operation": {"type": "string"}
    },
    "required": ["type", "data", "operation"]  # task_id es opcional para heartbeat
}

def process_data(data):
    """Aplica la fórmula ((sin(x)+cos(x))^2)/(sqrt(abs(x))+1) a cada elemento."""
    out = []
    for x in data:
        num = (math.sin(x) + math.cos(x)) ** 2
        den = math.sqrt(abs(x)) + 1
        out.append(num/den)
    return out

# Logger configurado en worker_main.py, aquí sólo obtenemos el logger
logger = logging.getLogger()

class ThreadedTCPRequestHandler(socketserver.BaseRequestHandler):
    def handle(self):
        # Conexión recibida
        logger.info(f"[Handler] conexión de {self.client_address}")

        # Leer bytes (hasta 8192) y strip
        raw = self.request.recv(8192).strip()
        logger.debug(f"[Handler] raw recibidos: {raw!r}")

        # Parsear JSON y validar schema
        try:
            msg = json.loads(raw.decode())
            logger.debug(f"[Handler] parsed JSON: {msg}")
            validate(msg, TASK_SCHEMA)
        except (json.JSONDecodeError, ValidationError) as e:
            logger.error(f"[Handler] mensaje inválido: {e}")
            resp = {"type": "error", "message": str(e)}
            self.request.sendall((json.dumps(resp) + "\n").encode())
            return

        # Responder según tipo
        if msg["type"] == "heartbeat":
            resp = {"type": "heartbeat_ack"}
            logger.debug(f"[Handler] enviando HEARTBEAT_ACK")
        else:  # "task"
            task_id = msg.get("task_id", "")
            data    = msg["data"]
            logger.debug(f"[Handler] procesando TASK id={task_id} data={data}")
            result = process_data(data)
            resp = {"type": "result", "task_id": task_id, "result": result}
            logger.debug(f"[Handler] enviando RESPONSE: {resp}")

        # Enviar con newline para que Java lo lea con readLine()
        self.request.sendall((json.dumps(resp) + "\n").encode())

class ThreadedTCPServer(socketserver.ThreadingMixIn, socketserver.TCPServer):
    daemon_threads = True
    allow_reuse_address = True
