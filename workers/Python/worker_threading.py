import socketserver, json, math, logging
from jsonschema import validate, ValidationError

logging.basicConfig(level=logging.INFO, format='[%(asctime)s] %(levelname)s: %(message)s')

TASK_SCHEMA = {
    "type": "object",
    "properties": {
        "type": {
            "type": "string",
            "enum": ["task", "heartbeat"]   # <-- DOS strings separados, NO uno solo con coma
        },
        "data": {
            "type": "array",
            "items": {"type": "number"}
        },
        "operation": {"type": "string"}
    },
    "required": ["type", "data", "operation"]
}


def process_data(data):
    return [((math.sin(x) + math.cos(x)) ** 2) / (math.sqrt(abs(x)) + 1) for x in data]

class ThreadedTCPRequestHandler(socketserver.BaseRequestHandler):
    def handle(self):
        raw = self.request.recv(8192).strip()
        client = f"{self.client_address[0]}:{self.client_address[1]}"
        logging.info(f"[Threaded] Connection from {client}: {raw}")
        try:
            msg = json.loads(raw.decode())
            validate(msg, TASK_SCHEMA)
        except (json.JSONDecodeError, ValidationError) as e:
            self.request.sendall(json.dumps({"type": "error", "message": str(e)}).encode())
            return
        
        if msg["type"] == "heartbeat":
            self.request.sendall(json.dumps({"type": "heartbeat_ack"}).encode())
        else:
            result = process_data(msg["data"])
            self.request.sendall(json.dumps({"type": "result", "result": result}).encode())
            
class ThreadedTCPServer(socketserver.ThreadingMixIn, socketserver.TCPServer):
    daemon_threads = True
    allow_reuse_address = True