# DArrayLib

![Build Status](https://img.shields.io/badge/build-passing-brightgreen) ![License](https://img.shields.io/badge/license-MIT-blue)

## 📘 Proyecto Final – CC4P1: Programación Concurrente y Distribuida

**DArrayLib** es una librería distribuida, concurrente y tolerante a fallos para procesamiento de arreglos de enteros (`int[]`) y decimales (`double[]`). Utiliza **Java** en el nodo maestro y **Python** en los nodos workers, comunicándose mediante sockets TCP nativos y aprovechando hilos en cada nodo para lograr un alto rendimiento y resiliencia.

---

## 🔍 Características Principales

* **Segmentación de Arreglos**:

  * **Clase `Fragment`**: Pieza de un arreglo con rango, contenido y metadatos.
  * **Clase `DArrayDouble`**: Divide el `double[]` original en fragmentos, serializa/deserializa y gestiona su envío.
* **Maestro Java (`master/Java`)**:

  * **Core**: Lógica de distribución de trabajo y orquestación.
  * **Data**: Modelos de datos (`Fragment`, resultados, metadatos).
  * **MasterMain.java**: Punto de entrada y configuración de puertos.
  * **MasterServer.java**: Servidor TCP que acepta conexiones de workers.
  * **ProtocolHandler.java**: Convierte mensajes JSON a objetos y viceversa.
  * **ResultManager.java**: Ensambla resultados parciales en el arreglo final.
  * **tests/**: Pruebas unitarias (JUnit) para cada componente.
* **Workers Python (`workers/Python`)**:

  * **worker\_main.py**: Inicializa el worker y mantiene la conexión con el maestro.
  * **worker\_node.py**: Procesa fragmentos (aplica operaciones matemáticas o condicionales).
  * **worker\_threading.py**: Crea y gestiona hilos para el procesamiento paralelo.
  * **replica\_manager.py**: Supervisa réplicas y recupera fragmentos tras fallo.
* **Protocolos y Configuración (`shared/`)**:

  * **communication\_protocol.md**: Especifica tipos de mensaje (INIT, TASK, RESULT, HEARTBEAT, REPLICA, RECOVER, ERROR).
  * **config.json**: Parámetros globales (puertos, número de réplicas, timeouts).
* **Casos de Ejemplo (`examples/`)**:

  * **example1\_math\_parallel/**: Datos JSON y scripts para el caso 1.
  * **example2\_conditional\_resilient/**: Recursos para el caso 2.
  * **example3\_fault\_recovery/**: Escenarios de fallo y recuperación.
* **Scripts de Utilidad (`scripts/`)**:

  * **generate\_logs.py**: Genera logs de latidos, errores y resultados.
  * **run\_cluster.sh**: Automatiza el despliegue de maestro y workers.
* **Documentación (`docs/`)**:

  * **informe\_final.pdf**: Informe técnico completo.
  * **presentacion\_final.pdf**: Slides en Beamer para la defensa.
  * **architecture.png**: Diagrama de la arquitectura.

---

## 📦 Instalación

> **Requisitos Previos:** Java 8+ y Python 3.7+ instalados.

```bash
# Clonar repositorio
git clone https://github.com/danicade24/DArrayLib-CC4P1-Final.git
cd DArrayLib

# Instalar dependencias Python
tcd workers && pip install -r requirements.txt && cd ..

# (Opcional) Ejecutar con Docker Compose
docker-compose up --build
```

---

## 🗂️ Estructura de Directorios

Estructura del proyecto (nivel 3):

```plaintext
DArrayLib/
├── docs/                                # Documentación y presentaciones
│   └── architecture.png                 # Diagrama de arquitectura
├── examples/                            # Casos de prueba predefinidos
│   ├── example1_math_parallel/          # Caso 1: procesamiento matemático paralelo
│   ├── example2_conditional_resilient/  # Caso 2: evaluación condicional con resiliencia
│   └── example3_fault_recovery/         # Caso 3: simulación de fallo y recuperación
├── master/                              # Nodo maestro Java
│   └── Java/
│       ├── core/                        # Lógica principal y servicios
│       ├── data/                        # Modelos de datos y clases auxiliares
│       ├── MasterMain.java              # Punto de entrada del maestro
│       ├── MasterServer.java            # Servidor TCP y gestión de conexiones
│       ├── ProtocolHandler.java         # Serialización y deserialización JSON
│       ├── ResultManager.java           # Consolidación de resultados
│       └── tests/                       # Pruebas unitarias con JUnit
├── scripts/                             # Scripts de utilidad
│   ├── generate_logs.py                 # Generador de logs para pruebas
│   └── run_cluster.sh                   # Script para levantar clúster completo
├── shared/                              # Protocolos y configuración global
│   ├── communication_protocol.md        # Definición de mensajes JSON
│   └── config.json                      # Parámetros de configuración (puertos, réplicas)
├── workers/                             # Nodos workers Python
│   └── Python/
│       ├── worker_main.py               # Inicialización y comunicación con maestro
│       ├── worker_node.py               # Lógica de procesamiento de fragmentos
│       ├── worker_threading.py          # Gestión de hilos para cada fragmento
│       └── replica_manager.py           # Manejo de réplicas y recuperación
└── README.md                            # Documentación principal del proyecto
```

## 🚀 Uso Rápido

1. **Iniciar maestro (Java)**:

   ```bash
   cd maestro
   mvn clean package
   java -jar target/maestro.jar --port 9000 --config ../shared/config.json
   ```

2. **Arrancar workers (Python)**:

   ```bash
   cd workers
   python worker_main.py --master-host 127.0.0.1 --master-port 9000
   ```

3. **Ejecutar ejemplos**:

   ```bash
   cd scripts
   ./run_example.sh 1    # Caso 1: Operación matemática
   ./run_example.sh 2    # Caso 2: Evaluación condicional
   ./run_example.sh 3    # Caso 3: Simulación de fallo
   ```

---

## 🏛️ Arquitectura del Sistema

```plaintext
+-------------------------+      TCP       +------------------------+
|     Maestro (Java)     | <-----------> |   Worker (Python)      |
|-------------------------|                |------------------------|
| - Divide arreglos       |                | - Recibe fragmentos    |
| - Asigna tareas         |                | - Procesa con hilos    |
| - Monitorea latidos     |                | - Envía resultados     |
| - Gestiona réplicas     |                | - Supervisa estado     |
+-------------------------+                +------------------------+
```

![Diagrama de Arquitectura](docs/architecture.png)

---

## 🧰 Protocolo de Comunicación

Detalles en `shared/communication_protocol.md`:

| Mensaje     | Descripción                          |
| ----------- | ------------------------------------ |
| `INIT`      | Inicialización de conexión           |
| `TASK`      | Envío de fragmento y operación       |
| `RESULT`    | Retorno de resultado                 |
| `HEARTBEAT` | Latido de vida                       |
| `REPLICA`   | Copia de respaldo                    |
| `RECOVER`   | Solicitud de recuperación tras fallo |
| `ERROR`     | Reporte de excepción                 |

---

## ✅ Casos de Ejemplo

1. **Procesamiento Matemático Paralelo**:

   ```java
   resultado = ((sin(x) + cos(x))^2) / (sqrt(abs(x)) + 1)
   ```

2. **Evaluación Condicional**:

   ```python
   if x % 3 == 0 or 500 <= x <= 1000:
       resultado = (x * log(x)) % 7
   ```

3. **Simulación de Fallo y Recuperación**:

   * Detecta caída por falta de `HEARTBEAT`.
   * Activa nodo de réplica y retoma cómputo.

---

## 💡 Contribuir

1. Fork del repositorio.
2. Crear feature branch: `git checkout -b feature/nueva-funcionalidad`.
3. Commit y push: `git commit -m "Añade nueva funcionalidad" && git push origin feature/nueva-funcionalidad`.
4. Pull request describiendo cambios.

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver `LICENSE` para más detalles.

---

## 👥 Autores

* **Estudiante 1** – Rol: Coordinación y Maestro Java
* **Estudiante 2** – Rol: Desarrollo de Workers Python
