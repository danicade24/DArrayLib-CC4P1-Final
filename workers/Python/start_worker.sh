#!/usr/bin/env bash
#
# Uso: ./start_workers.sh <N> [base_port]
#   N          = número de workers a levantar
#   base_port  = puerto inicial (default: 12345)
#
set -euo pipefail

N="${1:-3}"                  # cuántos workers lanzar
BASE_PORT="${2:-12345}"      # puerto del primer worker
HOST="${HOST:-0.0.0.0}"      # host donde bindear
HB_INTERVAL="${HEARTBEAT_INTERVAL:-5.0}"
HB_TIMEOUT="${HEARTBEAT_TIMEOUT:-1.0}"

# Array para guardar PIDs
declare -a PIDS=()

echo "Arrancando $N workers en $HOST a partir de puerto $BASE_PORT..."

for i in $(seq 0 $((N-1))); do
  port=$(( BASE_PORT + i*2 ))
  replica_port=$(( port + 1 ))
  echo "[Worker $((i+1))] puerto=$port réplica=$replica_port"
  python3 worker_main.py \
    --host "$HOST" \
    --port "$port" \
    --replica-port "$replica_port" \
    --heartbeat-interval "$HB_INTERVAL" \
    --heartbeat-timeout "$HB_TIMEOUT" \
    > "worker_${port}.log" 2>&1 &
  PIDS+=($!)
done

echo "Workers arrancados. PIDs: ${PIDS[*]}"
# Atajo para borrar fichero de PID viejo
printf "%s\n" "${PIDS[@]}" > worker_pids.txt

# Función para parar todo con Ctrl+C
cleanup(){
  echo; echo "Deteniendo todos los workers..."
  for pid in "${PIDS[@]}"; do
    kill "$pid" 2>/dev/null || true
  done
  rm -f worker_pids.txt
  exit 0
}
trap cleanup SIGINT SIGTERM

# Espera a que terminen (o a Ctrl+C)
wait
