## 📡 Protocolo de Comunicación Maestro ↔️ Worker

Todos los mensajes se enviarán por **sockets TCP** y estarán en formato **JSON**, para facilitar la interoperabilidad entre Java y Python.

---

### 🧾 Mensajes básicos del protocolo

| Tipo de mensaje | Dirección       | Descripción                                            |
| --------------- | --------------- | ------------------------------------------------------ |
| `INIT`          | Worker → Master | Primer saludo al conectar                              |
| `REGISTER_OK`   | Master → Worker | Confirmación de registro                               |
| `TASK`          | Master → Worker | Instrucciones de procesamiento con fragmento de array  |
| `RESULT`        | Worker → Master | Resultado parcial                                      |
| `ERROR`         | Worker → Master | Fallo interno del worker                               |
| `HEARTBEAT`     | Worker → Master | Señal de vida periódica                                |
| `REPLICA`       | Master → Worker | Envío de datos réplica                                 |
| `RECOVER`       | Master → Worker | Instrucción de asumir una réplica y continuar la tarea |
| `DONE`          | Master → Worker | Fin de la operación                                    |

---

### 📦 Estructura de cada mensaje (en JSON)

#### `INIT` (worker → master)

```json
{
  "type": "INIT",
  "worker_id": "worker_01",
  "capabilities": {
    "cores": 4,
    "language": "python"
  }
}
```

#### `TASK` (master → worker)

```json
{
  "type": "TASK",
  "task_id": "T123",
  "fragment": [1.0, 2.5, 3.1, 4.8],
  "operation": "((sin(x) + cos(x))^2) / (sqrt(abs(x)) + 1)",
  "send_result_to": "127.0.0.1:9000"
}
```

#### `RESULT` (worker → master)

```json
{
  "type": "RESULT",
  "task_id": "T123",
  "worker_id": "worker_01",
  "result": [0.75, 1.13, 0.98, 0.64]
}
```

#### `HEARTBEAT` (worker → master)

```json
{
  "type": "HEARTBEAT",
  "worker_id": "worker_01"
}
```

#### `REPLICA` (master → worker)

```json
{
  "type": "REPLICA",
  "task_id": "T123",
  "fragment": [1.0, 2.5, 3.1, 4.8],
  "replica_id": "replica_for_worker_02"
}
```

#### `RECOVER` (master → replica)

```json
{
  "type": "RECOVER",
  "original_worker_id": "worker_02",
  "task_id": "T123"
}
```

---

## 📁 ¿Dónde guardar este protocolo?

En tu proyecto:

```
DArrayLib/shared/communication_protocol.md
```

---
