package org.evosuite.testcase.variable.name;

import org.evosuite.Properties;

/**
 * This class encapsulates the logic of creating a new naming strategy.
 * <p>
 * With the use of method get we can create a new variable naming strategy
 * using the default configuration or by providing the desired naming strategy.
 *
 * <pre>
 *     VariableNameStrategy namingStrategy = VariableNameStrategyFactory.get();
 * </pre>
 * or
 * <pre>
 *     VariableNameStrategy namingStrategy = VariableNameStrategyFactory.get(Properties.VARIABLE_NAMING_STRATEGY);
 * </pre>
 * @author Afonso Oliveira
 *
 * @see TypeBasedVariableNameStrategy
 */
public class VariableNameStrategyFactory {

    public static VariableNameStrategy get(Properties.VariableNamingStrategy identifierNamingStrategy) {
        if (Properties.VariableNamingStrategy.TYPE_BASED.equals(identifierNamingStrategy)) {
            return new TypeBasedVariableNameStrategy();
        }
        if (Properties.VariableNamingStrategy.HEURISTICS_BASED.equals(identifierNamingStrategy)) {
            return new HeuristicsVariableNameStrategy();
        }
        else {
            throw new IllegalArgumentException(String.format("Unknown variable naming strategy: %s", identifierNamingStrategy));
        }
    }

    /**
     * Get the currently selected variable naming strategy.
     * <p>
     * The select strategy is defined in {@link Properties#VARIABLE_NAMING_STRATEGY}.
     *
     * @return The selected strategy.
     */
    public static VariableNameStrategy get() {
        return get(Properties.VARIABLE_NAMING_STRATEGY);
    }

    private VariableNameStrategyFactory() {
        // Nothing to do here
    }

    public static boolean gatherInformation(){
        if (Properties.VariableNamingStrategy.HEURISTICS_BASED.equals(Properties.VARIABLE_NAMING_STRATEGY)) {
            return true;
        }
        return false;
    }

}
