import data.Fragment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ResultManager: Almacena resultados parciales de los workers y los ensambla en orden correcto.
 */
public class ResultManager {

    private final ConcurrentHashMap<String, Fragment> resultMap;
    private final int expectedResults;

    public ResultManager(int expectedResults) {
        this.resultMap = new ConcurrentHashMap<>();
        this.expectedResults = expectedResults;
    }

    /**
     * Agrega un resultado parcial identificado por su taskId.
     *
     * @param taskId Identificador de la tarea.
     * @param fragment Fragmento con los resultados.
     */
    public void addResult(String taskId, Fragment fragment) {
        resultMap.put(taskId + ":" + fragment.getId(), fragment);
    }

    /**
     * Devuelve true si todos los resultados esperados han sido recibidos.
     *
     * @return boolean
     */
    public boolean isComplete() {
        return resultMap.size() >= expectedResults;
    }

    /**
     * Ensambla los resultados en un array global ordenado por el índice de inicio de cada fragmento.
     *
     * @return Array global unido.
     */
    public double[] assembleResults() {
        List<Fragment> fragments = new ArrayList<>(resultMap.values());
        fragments.sort(Comparator.comparingInt(Fragment::getStartIndex));

        int totalLength = fragments.stream().mapToInt(f -> f.getData().length).sum();
        double[] merged = new double[totalLength];

        for (Fragment frag : fragments) {
            System.arraycopy(frag.getData(), 0, merged, frag.getStartIndex(), frag.getData().length);
        }

        return merged;
    }
}
