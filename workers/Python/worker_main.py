import argparse
import logging
import socketserver
from worker_threading import ThreadedTCPRequestHandler
from replica_manager import ReplicaManager

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default="0.0.0.0")
    parser.add_argument("--port", type=int, default=8000)
    parser.add_argument("--replica-port", type=int, default=8001)
    args = parser.parse_args()

    logging.basicConfig(level=logging.DEBUG)
    logging.info(f"[WorkerMain] iniciando servidor en {args.host}:{args.port}")

    # Arranca el manejo de réplicas en background:
    replica_mgr = ReplicaManager(args.host, args.replica_port)
    replica_mgr.start()

    # Servidor concurrente en port principal
    server = socketserver.ThreadingTCPServer(
        (args.host, args.port), ThreadedTCPRequestHandler)
    server.allow_reuse_address = True
    server.serve_forever()