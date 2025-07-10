import socketserver
import json
import logging
from jsonschema import validate, ValidationError
from worker_threading import TASK_SCHEMA, process_data

logging.basicConfig(level=logging.DEBUG)

class ThreadedTCPRequestHandler(socketserver.BaseRequestHandler):
    def handle(self):
        raw = self.request.recv(8192).strip()
        client = f"{self.client_address[0]}:{self.client_address[1]}"
        logging.debug(f"[ThreadedHandler] raw bytes from {client}: {raw!r}")

        try:
            msg = json.loads(raw.decode())
            logging.debug(f"[ThreadedHandler] parsed JSON: {msg}")
            validate(msg, TASK_SCHEMA)
        except (json.JSONDecodeError, ValidationError) as e:
            logging.error(f"[ThreadedHandler] invalid message: {e}")
            self.request.sendall(json.dumps({
                "type": "error",
                "message": str(e)
            }).encode())
            return

        if msg["type"] == "heartbeat":
            logging.debug("[ThreadedHandler] handling HEARTBEAT")
            self.request.sendall(json.dumps({"type": "heartbeat_ack"}).encode())

        elif msg["type"] == "task":
            logging.debug(f"[ThreadedHandler] handling TASK with data: {msg['data']}")
            result = process_data(msg["data"])
            logging.debug(f"[ThreadedHandler] result: {result}")
            self.request.sendall(json.dumps({"type": "result", "result": result}).encode())