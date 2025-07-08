import argparse, threading, logging
from worker_threading import ThreadedTCPServer, ThreadedTCPRequestHandler
from replica_manager import ReplicaManager

def main(host, port, replica_port):
    logging.basicConfig(level=logging.INFO)
    # Arranca el servidor concurrente
    server = ThreadedTCPServer((host, port), ThreadedTCPRequestHandler)
    t = threading.Thread(target=server.serve_forever, daemon=True)
    t.start()
    logging.info(f"Worker principal en {host}:{port}")

    # Arranca el replica manager que vigila este worker
    replica = ReplicaManager(host, port, ["python","worker_node.py","--host",host,"--port",str(replica_port)])
    hb = threading.Thread(target=replica.heartbeat, daemon=True)
    hb.start()

    t.join()

if __name__=="__main__":
    p = argparse.ArgumentParser()
    p.add_argument("--host", default="0.0.0.0")
    p.add_argument("--port", type=int, default=12345)
    p.add_argument("--replica-port", type=int, default=12346)
    args = p.parse_args()
    main(args.host, args.port, args.replica_port)
