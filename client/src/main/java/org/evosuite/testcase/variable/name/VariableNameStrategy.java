package org.evosuite.testcase.variable.name;

import org.evosuite.testcase.variable.VariableReference;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * The variable name strategy defines the logic behind how a name is generated.
 *
 * @author Afonso Oliveira
 *
 * @see TypeBasedVariableNameStrategy
 */
public interface VariableNameStrategy {

    /**
     * Get the name that is used to identify a {@link VariableReference}.
     *
     * @param variable The variable for which the name should be generated.
     *
     * @return The name to be used when referencing the variable.
     */
    String getNameForVariable(VariableReference variable);

    /**
     * Get the {@link VariableReference} from the variable name.
     *
     * @param variableName The name used to identify the variable.
     *
     * @return The variable reference.
     */
    Optional<VariableReference> getVariableFromName(String variableName);

    /**
     * Get the collection of names used for the variables.
     *
     * @return The collection of variable names.
     */
    Collection<String> getVariableNames();

    /**
     * Allows to add information on dictionaries for variable naming
     */
    void addVariableInformation(Map<String, Map<VariableReference, String>> information);


}
