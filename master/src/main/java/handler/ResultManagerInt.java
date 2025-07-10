package handler;

import data.FragmentInt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ResultManagerInt: Almacena resultados parciales de los workers (int) y los ensambla en orden correcto.
 */
public class ResultManagerInt {

    private final ConcurrentHashMap<String, FragmentInt> resultMap;
    private final int expectedResults;

    /**
     * Crea un nuevo ResultManagerInt.
     * @param expectedResults Número total de resultados esperados.
     */
    public ResultManagerInt(int expectedResults) {
        this.resultMap = new ConcurrentHashMap<>();
        this.expectedResults = expectedResults;
    }

    /**
     * Agrega un resultado parcial identificado por su taskId.
     * @param taskId ID de la tarea.
     * @param fragment Fragmento con los resultados.
     */
    public void addResult(String taskId, FragmentInt fragment) {
        resultMap.put(taskId + ":" + fragment.getId(), fragment);
    }

    /**
     * Verifica si se han recibido todos los resultados esperados.
     * @return true si está completo.
     */
    public boolean isComplete() {
        return resultMap.size() >= expectedResults;
    }

    /**
     * Ensambla los resultados en un array global ordenado por índice.
     * @return Array global unido.
     */
    public int[] assembleResults() {
        List<FragmentInt> fragments = new ArrayList<>(resultMap.values());
        fragments.sort(Comparator.comparingInt(FragmentInt::getStartIndex));

        int totalLength = fragments.stream().mapToInt(f -> f.getData().length).sum();
        int[] merged = new int[totalLength];

        for (FragmentInt frag : fragments) {
            System.arraycopy(frag.getData(), 0, merged, frag.getStartIndex(), frag.getData().length);
        }

        return merged;
    }
}
