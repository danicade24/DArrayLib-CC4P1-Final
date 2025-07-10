import threading
import logging
import socket
import json

logging.basicConfig(level=logging.DEBUG)

class ReplicaManager(threading.Thread):
    def __init__(self, host, port):
        super().__init__(daemon=True)
        self.host = host
        self.port = port

    def run(self):
        logging.info(f"[ReplicaManager] escuchando réplicas en {self.host}:{self.port}")
        with socket.socket() as s:
            s.bind((self.host, self.port))
            s.listen()
            while True:
                conn, addr = s.accept()
                raw = conn.recv(8192)
                logging.debug(f"[ReplicaManager] raw from {addr}: {raw!r}")
                try:
                    msg = json.loads(raw.decode())
                    logging.debug(f"[ReplicaManager] parsed: {msg}")
                    # Aquí podrías validar con un esquema propio
                except json.JSONDecodeError as e:
                    logging.error(f"[ReplicaManager] invalid JSON: {e}")
                    continue
                # Manejo de réplicas según msg['type']
                conn.sendall(json.dumps({"type": "replica_ack"}).encode())