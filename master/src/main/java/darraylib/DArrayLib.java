package darraylib;

import core.DArrayDouble;
import handler.MasterServer;

/**
 * Punto de entrada principal de la librería DArrayLib.
 * Proporciona métodos estáticos para crear y orquestar el sistema Maestro-Worker.
 */
public class DArrayLib {

    /**
     * Crea una nueva instancia de DArrayDouble para fragmentar un arreglo de doubles.
     *
     * @param data          El arreglo de datos a fragmentar.
     * @param fragmentCount Número de fragmentos deseados.
     * @return Instancia de DArrayDouble.
     */
    public static DArrayDouble createArray(double[] data, int fragmentCount) {
        return new DArrayDouble(data, fragmentCount);
    }

    /**
     * Crea e inicia un servidor maestro listo para recibir resultados de workers.
     *
     * @param port      Puerto en el que escuchará el servidor maestro.
     * @param dArray    Instancia de DArrayDouble que contiene los datos a procesar.
     * @return Instancia de MasterServer.
     */
    public static MasterServer createMasterServer(int port, DArrayDouble dArray) {
        return new MasterServer(port, dArray, dArray.getFragments().size());
    }
}
