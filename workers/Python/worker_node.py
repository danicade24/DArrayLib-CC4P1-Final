import socket, json, math, logging
from jsonschema import validate, ValidationError
from worker_threading import TASK_SCHEMA, process_data

def serve_once(host="0.0.0.0", port=8000):
    with socket.socket() as s:
        s.bind((host, port))
        s.listen()
        conn, addr = s.accept()
        raw = conn.recv(8192)
        try:
            msg = json.loads(raw.decode())
            validate(msg, TASK_SCHEMA)
        except Exception as e:
            conn.send(json.dumps({"type": "error", "message": str(e)}).encode())
            return
        if msg["type"] == "heartbeat":
            conn.send(json.dumps({"type": "heartbeat_ack"}).encode())
        elif msg["type"] == "task":
            result = process_data(msg["data"])
            conn.send(json.dumps({"type": "result", "result": result}).encode())