import argparse
import threading
import logging
import signal
import sys
from worker_threading import ThreadedTCPServer, ThreadedTCPRequestHandler
from replica_manager import ReplicaManager

# Configuración única de logging para toda la app
logging.basicConfig(
    level=logging.DEBUG,
    format="%(asctime)s %(levelname)s %(message)s",
    datefmt="[%Y-%m-%d %H:%M:%S]"
)
logger = logging.getLogger()

def main(host, port, replica_port, heartbeat_interval, heartbeat_timeout):
    # Arranca el servidor concurrente
    server = ThreadedTCPServer((host, port), ThreadedTCPRequestHandler)
    server.allow_reuse_address = True
    server_thread = threading.Thread(target=server.serve_forever, daemon=True)
    server_thread.start()
    logger.info(f"Worker principal escuchando en {host}:{port}")

    # Arranca el ReplicaManager (gestiona la réplica síncrona)
    mgr = ReplicaManager(
        primary_host=host,
        primary_port=port,
        backup_cmd=["python3", "worker_node.py",
                    "--host", host, "--port", str(replica_port)],
        heartbeat_interval=heartbeat_interval,
        heartbeat_timeout=heartbeat_timeout
    )
    mgr.start()
    logger.info("ReplicaManager arrancado")

    # Shutdown limpio al recibir SIGINT/SIGTERM
    def shutdown(signum, frame):
        logger.info("Señal de terminación recibida, cerrando worker...")
        server.shutdown()
        mgr.stop()
        sys.exit(0)

    signal.signal(signal.SIGINT, shutdown)
    signal.signal(signal.SIGTERM, shutdown)

    # Mantener vivo el hilo principal
    server_thread.join()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Worker Python concurrente con ReplicaManager"
    )
    parser.add_argument("--host", default="0.0.0.0")
    parser.add_argument("--port", type=int, default=12345)
    parser.add_argument("--replica-port", type=int, default=12346)
    parser.add_argument("--heartbeat-interval", type=float, default=5.0)
    parser.add_argument("--heartbeat-timeout", type=float, default=1.0)
    args = parser.parse_args()

    main(
        host=args.host,
        port=args.port,
        replica_port=args.replica_port,
        heartbeat_interval=args.heartbeat_interval,
        heartbeat_timeout=args.heartbeat_timeout
    )
