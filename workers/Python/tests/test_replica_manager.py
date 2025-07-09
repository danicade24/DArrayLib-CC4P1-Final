# workers/Python/tests/test_replica_manager.py

import socket
import threading
import time
import json
import pytest
import os
from replica_manager import ReplicaManager

HOST, PORT_PRIMARY = "127.0.0.1", 60000

# Servidor dummy que responde solo a heartbeats,
# siempre escuchando pero únicamente ack cuando run_event está activo.
def run_dummy_primary(port, run_event, stop_event):
    srv = socket.socket()
    srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    srv.bind((HOST, port))
    srv.listen()
    srv.settimeout(0.2)
    while not stop_event.is_set():
        try:
            conn, _ = srv.accept()
            data = conn.recv(1024)
            try:
                msg = json.loads(data.decode())
                if msg.get("type") == "heartbeat" and run_event.is_set():
                    conn.sendall(json.dumps({"type": "heartbeat_ack"}).encode())
            except json.JSONDecodeError:
                pass
            conn.close()
        except socket.timeout:
            continue
    srv.close()

@pytest.fixture
def dummy_server():
    """
    run_event: controla si el dummy responde latidos.
    stop_event: detiene el hilo completamente al final.
    """
    run_event = threading.Event()
    stop_event = threading.Event()
    run_event.set()
    t = threading.Thread(
        target=run_dummy_primary,
        args=(PORT_PRIMARY, run_event, stop_event),
        daemon=True
    )
    t.start()
    yield run_event
    stop_event.set()
    t.join(timeout=1)

def test_failover_and_recovery(tmp_path, dummy_server):
    # Script de réplica que duerme hasta recibir SIGTERM
    replica_py = tmp_path / "replica.py"
    replica_py.write_text("""\
import time, signal, sys

def handle_sigterm(signum, frame):
    sys.exit(0)

signal.signal(signal.SIGTERM, handle_sigterm)

while True:
    time.sleep(0.1)
""")

    cmd = ["python3", str(replica_py)]

    mgr = ReplicaManager(
        HOST,
        PORT_PRIMARY,
        cmd,
        heartbeat_interval=0.2,
        heartbeat_timeout=0.1
    )
    mgr.start()

    # 1) Con primario vivo, no arranca réplica
    time.sleep(0.5)
    assert mgr.backup_proc is None

    # 2) Simula caída del primario (no responde latidos)
    dummy_server.clear()
    time.sleep(0.5)
    assert mgr.backup_proc is not None

    # 3) Restaura el primario y da un margen
    dummy_server.set()
    time.sleep(0.5)

    # 4) Parar el manager (envía SIGTERM y espera cierre de réplica)
    mgr.stop()

    # 5) Verificar que la réplica haya terminado
    proc = mgr.backup_proc
    if proc is not None:
        assert proc.poll() is not None, "La réplica no murió tras mgr.stop()"
