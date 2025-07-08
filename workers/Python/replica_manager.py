import threading, time, socket, json, logging

logging.basicConfig(level=logging.INFO)

class ReplicaManager:
    def __init__(self, primary_host, primary_port, backup_cmd):
        self.primary = (primary_host, primary_port)
        self.backup_cmd = backup_cmd  
        self.backup_proc = None

    def heartbeat(self):
        while True:
            try:
                with socket.create_connection(self.primary, timeout=1) as sock:
                    sock.sendall(json.dumps({"type":"heartbeat","data":[],"operation":""}).encode())
                    resp = sock.recv(1024)
                logging.debug("Primary alive")
            except Exception:
                logging.warning("Primary caída, levantando réplica...")
                if self.backup_proc is None:
                    import subprocess
                    self.backup_proc = subprocess.Popen(self.backup_cmd)
            time.sleep(5)
