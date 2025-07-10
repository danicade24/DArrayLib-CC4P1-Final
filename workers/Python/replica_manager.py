import threading
import logging
import socket
import json
import subprocess
import time

logger = logging.getLogger()

class ReplicaManager(threading.Thread):
    """
    Hilo que arranca un worker de réplica y envía heartbeats al primario.
    """
    def __init__(self, primary_host, primary_port,
                 backup_cmd, heartbeat_interval, heartbeat_timeout):
        super().__init__(daemon=True)
        self.primary_host     = primary_host
        self.primary_port     = primary_port
        self.backup_cmd       = backup_cmd
        self.heartbeat_interval = heartbeat_interval
        self.heartbeat_timeout  = heartbeat_timeout
        self._running = True

    def run(self):
        # Arranca el proceso de réplica
        logger.info(f"ReplicaManager arranca réplica: {' '.join(self.backup_cmd)}")
        self.backup_proc = subprocess.Popen(self.backup_cmd)

        # Ciclo de heartbeats
        while self._running:
            try:
                with socket.create_connection((self.primary_host, self.primary_port),
                                              timeout=self.heartbeat_timeout) as s:
                    msg = {"type": "heartbeat", "data": [], "operation": ""}
                    s.sendall((json.dumps(msg) + "\n").encode())
                    logger.debug("Heartbeat enviado al primario")
                    # opcional: leer ACK si quieres validar
            except Exception as e:
                logger.error(f"Heartbeat falló: {e}")
            time.sleep(self.heartbeat_interval)

    def stop(self):
        self._running = False
        if hasattr(self, "backup_proc"):
            self.backup_proc.terminate()
