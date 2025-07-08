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

## 🧪 Enfoque TDD (Test-Driven Development)

En **DArrayLib**, cada módulo se desarrolla siguiendo un ciclo TDD estricto para garantizar fiabilidad, mantenibilidad y alta cobertura de pruebas.

1. **Fragmentación y Serialización** (`shared/`, `master/Java/data/Fragment`)

   * **Objetivo**: Aislar lógica de corte de arreglos y JSON.
   * **Pruebas**:

     * Generar un `double[]` de 100 valores aleatorios y validar que `Fragment` asigna correctamente `startIndex`, `endIndex` y copia los datos adecuados.
     * Serializar un `Fragment` a JSON y deserializarlo; comparar objeto original vs. resultante campo a campo.
   * **Éxito**: 100% de campos intactos tras ciclo ser/deser.

2. **División de Arreglo** (`master/Java/core/DArrayDouble`)

   * **Objetivo**: Fragmentar un arreglo en N partes equilibradas, gestionando residuos y errores.
   * **Pruebas**:

     * Para `double[]` de longitud 100 con N=4, comprobar que se generan cuatro fragmentos de largo 25.
     * Casos límite:

       * N > longitud: arrojar `IllegalArgumentException`.
       * Longitudes no divisibles: distribuir residuo en los primeros fragmentos y validar tamaños.
   * **Éxito**: Todos los fragmentos suman la longitud original y se lanzan excepciones apropiadas en entradas inválidas.

3. **Protocol Handler** (`master/Java/ProtocolHandler.java`)

   * **Objetivo**: Traducir entre objetos Java y mensajes JSON del protocolo.
   * **Pruebas**:

     * Convertir cada tipo de mensaje (`INIT`, `TASK`, `RESULT`, `HEARTBEAT`, `REPLICA`, `RECOVER`, `ERROR`) de objeto a cadena JSON y de regreso, validando equivalencia.
     * Pasar JSON malformado a `fromJson()` y verificar que se lanza `JsonParseException` con mensaje claro.
   * **Éxito**: Cobertura total de todos los casos de mensaje, incluidos errores de parsing.

4. **Orquestación del Maestro** (`master/Java/MasterServer.java` y `ResultManager.java`)

   * **Objetivo**: Gestionar conexiones, distribuir fragmentos y consolidar resultados.
   * **Pruebas**:

     * **Unitarias**: Simular sockets mock que envían varios objetos `RESULT`; comprobar que `ResultManager` ensambla el arreglo final en orden correcto.
     * **Integración**:

       1. Arrancar `MasterServer` en un hilo.
       2. Levantar dos instancias de worker (Java y Python) con infraestructura mínima.
       3. Enviar un arreglo fragmentado y verificar que el resultado global coincide con el cálculo secuencial.
   * **Éxito**: El maestro recupera y une fragmentos aun cuando uno de los workers responda con retraso.

5. **Procesamiento en Workers** (`workers/Python/worker_node.py`, `worker_threading.py`)

   * **Objetivo**: Aplicar operaciones matemáticas o condicionales sobre fragmentos en paralelo.
   * **Pruebas**:

     * Verificar, para un pequeño fragmento de 10 valores, que las funciones de ejemplo (e.g., `((sin+cos)^2)/(sqrt+1)`, `% 3`, logaritmo) devuelven resultados correctos.
     * Simular varias tareas encoladas y asegurarse de que el uso de `ThreadPoolExecutor` respeta la concurrencia sin perder ni duplicar datos.
   * **Éxito**: Resultados consistentes y reproducibles en ejecuciones concurrentes.

6. **Réplica y Recuperación** (`workers/Python/replica_manager.py`)

   * **Objetivo**: Mantener disponibilidad al detectar y reemplazar nodos caídos.
   * **Pruebas**:

     * Forzar la caída de un worker tras recibir solo parte de un fragmento; verificar que `replica_manager` reenvía la tarea a la réplica y obtiene el resultado correcto.
     * **Integración**: Desconectar un worker a mitad de cómputo y comprobar que el flujo maestro→réplica→resultado no interrumpe el ensamblado final.
   * **Éxito**: Tolerancia total a fallos sin pérdida de datos ni corrupción.

---

### 🔧 Herramientas y Ejecución de Pruebas

* **Java**

  * Frameworks: JUnit 5, Mockito para mocks.
  * Cobertura: JaCoCo configurado para > 90% en módulos `core`, `data`, `ProtocolHandler`.
  * Comando:

    ```bash
    # En contenedor Docker o local
    docker-compose run master mvn test
    ```

* **Python**

  * Framework: pytest + pytest-mock.
  * Cobertura: coverage.py con umbral > 90% en `workers/Python`.
  * Comando:

    ```bash
    pytest workers/Python --cov=workers/Python
    ```

---


 **cómo importar y usar** la librería DArrayLib en **Java** y en **Python**, partiendo de la estructura y empaquetado que ya tienes:

---

## 🟢 En Java

### 1. Empaquetado como JAR

Al compilar el proyecto maestro con Maven (o Gradle), se genera un artefacto `darraylib-master.jar` que contiene:

* Las clases públicas:

  * `com.tuempresa.darraylib.DArrayDouble`
  * `com.tuempresa.darraylib.Fragment`
  * `com.tuempresa.darraylib.Operation` (interfaz funcional)
  * Cualquier clase helper que quieras exponer (p. ej. `MasterClient`)

### 2. Declarar la dependencia

Si usas **Maven**, en tu `pom.xml` del proyecto consumidor agrega:

```xml
<dependency>
  <groupId>com.tuempresa.darraylib</groupId>
  <artifactId>darraylib-master</artifactId>
  <version>1.0.0</version>
  <scope>compile</scope>
</dependency>
```

Si usas **Gradle**, en tu `build.gradle`:

```groovy
dependencies {
    implementation "com.tuempresa.darraylib:darraylib-master:1.0.0"
}
```

### 3. Importar y usar en código

Ahora en cualquier clase Java sólo necesitas:

```java
import com.tuempresa.darraylib.DArrayDouble;
import com.tuempresa.darraylib.Operation;

public class App {
    public static void main(String[] args) {
        // 1. Preparamos un arreglo de ejemplo
        double[] datos = { 0.0, 1.0, 2.0, 3.0, /* … */ };

        // 2. Creamos la instancia indicando N fragmentos
        DArrayDouble darr = new DArrayDouble(datos, 4);

        // 3. Definimos la operación a aplicar
        Operation<Double, Double> op = x ->
            Math.pow(Math.sin(x) + Math.cos(x), 2) / (Math.sqrt(Math.abs(x)) + 1);

        // 4. Procesamos en paralelo y distribuido
        double[] resultado = darr.mapParallel(op);

        // ¡Listo!
        System.out.println("Resultado[0] = " + resultado[0]);
    }
}
```

Bajo esa llamada, DArrayLib:

1. Inicia internamente el “Maestro” (si no está ya corriendo).
2. Fragmenta `datos` en 4 partes.
3. Distribuye tareas a los workers (Java o Python).
4. Ensambla el arreglo final y te lo devuelve.

---

## 🟢 En Python

### 1. Empaquetado como paquete pip

En el repositorio Python tienes dos paquetes:

* `darraylib_master` (cliente / maestro)
* `darraylib_worker` (código de worker)

Asegúrate de tener en la raíz un `setup.py` (o `pyproject.toml`) que declare ambos:

```python
# setup.py (resumen)
from setuptools import setup, find_packages

setup(
    name="darraylib",
    version="1.0.0",
    packages=find_packages(include=["darraylib_master*", "darraylib_worker*"]),
    install_requires=[
        # aquí tus dependencias, p.ej. "numpy"
    ],
    entry_points={
        "console_scripts": [
            "darray_worker = darraylib_worker.worker_main:main"
        ]
    }
)
```

### 2. Instalación vía pip

Desde la carpeta raíz del proyecto:

```bash
pip install .
```

Esto hará disponible:

* El módulo `darraylib_master`
* El módulo `darraylib_worker`
* El comando terminal `darray_worker` para arrancar un worker

### 3. Importar y usar en código

#### a) Arrancar uno o varios workers

Opcionalmente, en un terminal:

```bash
darray_worker --master-host 127.0.0.1 --master-port 9000
```

O bien directamente desde Python:

```python
from darraylib_worker.worker_main import start_worker

start_worker(master_host="127.0.0.1", master_port=9000)
```

#### b) Desde el cliente Python

```python
from darraylib_master import DArrayDouble

# 1. Generamos datos de ejemplo
datos = [i * 0.5 for i in range(1000)]

# 2. Creamos el objeto DArrayDouble
darr = DArrayDouble(datos, num_fragments=4)

# 3. Definimos la función a aplicar
def mi_operacion(x):
    return ( (math.sin(x) + math.cos(x))**2 ) / (math.sqrt(abs(x)) + 1)

# 4. Ejecutamos
resultado = darr.map_parallel(mi_operacion)

# 5. Usamos el resultado
print("Primer valor:", resultado[0])
```

Internamente, `map_parallel` se encarga de:

1. Fragmentar la lista `datos`.
2. Enviar cada fragmento al maestro (que corre en Java o en Python).
3. Esperar y ensamblar el resultado completo.

---

## 🔑 Clave: API de Alto Nivel

En **ningún momento** el desarrollador que consume la librería necesita:

* Crear `ServerSocket` o `socket.socket`.
* Gestionar hilos (`Thread`, `ThreadPoolExecutor`).
* Serializar o deserializar JSON.
* Manejar reconexiones o heartbeats.

Todo eso está **bajo el capó**, y queda oculto tras métodos concisos:

* **Java**: `new DArrayDouble(...).mapParallel(...)`
* **Python**: `DArrayDouble(...).map_parallel(...)`

Así, basta con “importar la librería” y usar su API pública para aprovechar paralelo, distribución y tolerancia a fallos en tus proyectos.

-
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
