package handler;

/**
 * Define un conjunto de operaciones matemáticas predefinidas para usar en tareas distribuidas.
 * Las operaciones son expresadas como strings que los workers deben interpretar y procesar.
 */
public class Operation {

    public static final String IDENTITY = "x";
    public static final String ADD_ONE = "x + 1";
    public static final String SUBTRACT_ONE = "x - 1";
    public static final String MULTIPLY_TWO = "x * 2";
    public static final String SQUARE = "x * x";
    public static final String SQRT = "sqrt(abs(x))";

    public static final String SIN = "sin(x)";
    public static final String COS = "cos(x)";
    public static final String TAN = "tan(x)";

    public static final String SIN_PLUS_COS = "sin(x) + cos(x)";
    public static final String SIN_SQUARE_PLUS_COS_SQUARE = "(sin(x) * sin(x)) + (cos(x) * cos(x))"; // siempre 1

    public static final String SIN_PLUS_COS_SQUARE_DIV_SQRT = "((sin(x) + cos(x))^2) / (sqrt(abs(x)) + 1)";

    public static final String EXPONENTIAL = "exp(x)";
    public static final String LOG_NATURAL = "log(x)";

    public static final String CUSTOM_PLACEHOLDER = "custom";

    /**
     * Previene la instanciación de esta clase.
     */
    private Operation() {
        throw new UnsupportedOperationException("Operation es una clase de constantes y no debe ser instanciada.");
    }
}
