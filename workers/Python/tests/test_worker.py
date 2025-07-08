# tests/test_worker.py

import sys, os
# Inserta la carpeta 'Python' (la carpeta padre de tests) en sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

import socket
import json
import threading
import time
import pytest

from worker_threading import ThreadedTCPServer, ThreadedTCPRequestHandler

HOST, PORT = "127.0.0.1", 54321

@pytest.fixture(scope="module", autouse=True)
def server():
    server = ThreadedTCPServer((HOST, PORT), ThreadedTCPRequestHandler)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    time.sleep(0.1)
    yield
    server.shutdown()
    server.server_close()

def send_and_receive(message: dict):
    with socket.create_connection((HOST, PORT), timeout=1) as sock:
        sock.sendall(json.dumps(message).encode('utf-8'))
        resp = sock.recv(8192)
    return json.loads(resp.decode('utf-8'))

def test_heartbeat_ack():
    msg = {"type": "heartbeat", "data": [], "operation": ""}
    resp = send_and_receive(msg)
    assert resp == {"type": "heartbeat_ack"}

def test_valid_task_returns_results():
    data = [0.0, 1.0, -1.0, 3.1415]
    msg = {"type": "task", "data": data, "operation": "math_formula"}
    resp = send_and_receive(msg)
    assert resp["type"] == "result"
    assert isinstance(resp["result"], list)
    assert len(resp["result"]) == len(data)
    assert all(isinstance(v, (int, float)) for v in resp["result"])

def test_empty_data():
    msg = {"type": "task", "data": [], "operation": "math_formula"}
    resp = send_and_receive(msg)
    assert resp["type"] == "result"
    assert resp["result"] == []

def test_invalid_json_format():
    with socket.create_connection((HOST, PORT), timeout=1) as sock:
        sock.sendall(b"hola no soy JSON")
        resp = sock.recv(8192)
    obj = json.loads(resp.decode('utf-8'))
    assert obj["type"] == "error"

def test_invalid_schema_missing_fields():
    bad = {"type": "task", "operation": "math_formula"}  # falta 'data'
    resp = send_and_receive(bad)
    assert resp["type"] == "error"
    assert "data" in resp["message"]

def test_mixed_extreme_values():
    data = [0, 1e6, -1e6]
    msg = {"type": "task", "data": data, "operation": ""}
    resp = send_and_receive(msg)
    assert resp["type"] == "result"
    assert all(v is not None for v in resp["result"])
