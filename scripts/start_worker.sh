#!/usr/bin/env bash
#
# scripts/start_workers.sh
#
# Arranca N workers Python (con réplica) y redirige sus logs a logs/worker_<port>.log
#
# Uso:
#   ./start_workers.sh <N> [base_port]
#     N          Número de workers a levantar (por defecto 3)
#     base_port  Puerto inicial (por defecto 12345)
#

export PYTHONUNBUFFERED=1
set -euo pipefail

# 1) Determinar rutas
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$SCRIPT_DIR/.."
WORKER_DIR="$REPO_ROOT/workers/Python"
LOG_DIR="$REPO_ROOT/logs"

# 2) Ir al directorio del worker y preparar logs
cd "$WORKER_DIR"
mkdir -p "$LOG_DIR"

# 3) Parámetros de entrada y valores por defecto
N="${1:-3}"
BASE_PORT="${2:-12345}"
HOST="${HOST:-0.0.0.0}"
HB_INTERVAL="${HEARTBEAT_INTERVAL:-5.0}"
HB_TIMEOUT="${HEARTBEAT_TIMEOUT:-1.0}"

declare -a PIDS=()

echo "== Arrancando $N workers en $HOST a partir de puerto $BASE_PORT =="
echo "== Logs en $LOG_DIR/worker_<port>.log =="

# 4) Bucle de arranque
for i in $(seq 0 $((N-1))); do
  port=$(( BASE_PORT + i*2 ))
  replica_port=$(( port + 1 ))
  echo "[Worker $((i+1))] puerto=$port, réplica=$replica_port"

  python3 worker_main.py \
    --host "$HOST" \
    --port "$port" \
    --replica-port "$replica_port" \
    --heartbeat-interval "$HB_INTERVAL" \
    --heartbeat-timeout "$HB_TIMEOUT" \
    > "$LOG_DIR/worker_${port}.log" 2>&1 &

  PIDS+=("$!")
done

echo "== Workers arrancados. PIDs: ${PIDS[*]} =="
printf "%s\n" "${PIDS[@]}" > "$REPO_ROOT/worker_pids.txt"

# 5) Función de limpieza
cleanup(){
  echo; echo "== Deteniendo todos los workers =="
  for pid in "${PIDS[@]}"; do
    kill "$pid" 2>/dev/null || true
  done
  rm -f "$REPO_ROOT/worker_pids.txt"
  exit 0
}
trap cleanup SIGINT SIGTERM

# 6) Esperar hasta Ctrl+C
wait
