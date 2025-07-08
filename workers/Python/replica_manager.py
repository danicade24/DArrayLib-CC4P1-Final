import threading
import time
import socket
import json
import logging
import subprocess
import signal

logging.basicConfig(
    level=logging.INFO,
    format='[%(asctime)s] %(levelname)s %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)

class ReplicaManager:
    def __init__(self, primary_host, primary_port, backup_cmd,
                 heartbeat_interval=5.0, heartbeat_timeout=1.0):
        """
        primary_host, primary_port: dirección del worker primario.
        backup_cmd: lista de comando para levantar la réplica.
        heartbeat_interval: cada cuántos segundos enviar latido.
        heartbeat_timeout: cuánto esperar respuesta en segundos.
        """
        self.primary = (primary_host, primary_port)
        self.backup_cmd = backup_cmd
        self.heartbeat_interval = heartbeat_interval
        self.heartbeat_timeout = heartbeat_timeout

        self.backup_proc = None
        self._stop = threading.Event()

    def start(self):
        """Arranca el hilo de monitoring."""
        self._thread = threading.Thread(target=self._monitor, daemon=True)
        self._thread.start()

    def stop(self):
        """Señala que pare el monitoring y cierra la réplica si existe."""
        self._stop.set()
        if self.backup_proc:
            logging.info("Deteniendo réplica secundaria")
            self.backup_proc.send_signal(signal.SIGTERM)
            try:
                self.backup_proc.wait(timeout=2.0)
            except subprocess.TimeoutExpired:
                logging.warning("Replica no respondió a SIGTERM, forzando kill")
                self.backup_proc.kill()
        self._thread.join()

    def _monitor(self):
        """Bucle de heartbeat y gestión de réplica."""
        while not self._stop.is_set():
            alive = self._send_heartbeat()
            if not alive:
                if self.backup_proc is None:
                    logging.warning("Primario caído, arrancando réplica...")
                    self.backup_proc = subprocess.Popen(self.backup_cmd)
                else:
                    logging.debug("Réplica ya en ejecución")
            else:
                # Primario vivo
                if self.backup_proc:
                    logging.info("Primario recuperado, deteniendo réplica...")
                    self.backup_proc.send_signal(signal.SIGTERM)
                    try:
                        self.backup_proc.wait(timeout=2.0)
                    except subprocess.TimeoutExpired:
                        logging.warning("Forzando kill de la réplica")
                        self.backup_proc.kill()
                    self.backup_proc = None
            time.sleep(self.heartbeat_interval)

    def _send_heartbeat(self):
        """Envía un latido y espera ack. Devuelve True si recibió ack."""
        try:
            with socket.create_connection(self.primary, timeout=self.heartbeat_timeout) as sock:
                lb = {"type": "heartbeat", "data": [], "operation": ""}
                sock.sendall(json.dumps(lb).encode())
                resp = sock.recv(1024)
                msg = json.loads(resp.decode())
                return msg.get("type") == "heartbeat_ack"
        except Exception as e:
            logging.debug(f"Heartbeat fallido: {e}")
            return False
