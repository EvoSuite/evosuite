package org.evosuite.testcase.variable.name;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.evosuite.testcase.variable.VariableReference;

/**
 * The basic implementation for a {@link VariableNameStrategy}.
 *
 * @author Afonso Oliveira
 */
public abstract class AbstractVariableNameStrategy implements VariableNameStrategy {

    protected final Map<VariableReference, String> variableNames = new ConcurrentHashMap<>();

    @Override
    public String getNameForVariable(VariableReference variable) {
        return variableNames.computeIfAbsent(variable, this::createNameForVariable);
    }

    @Override
    public Optional<VariableReference> getVariableFromName(String variableName) {
        for (Map.Entry<VariableReference, String> entry : variableNames.entrySet()) {
            if (variableName.equals(entry.getValue())) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    @Override
    public Collection<String> getVariableNames() {
        return variableNames.values();
    }

    /**
     * Create a name to uniquely identify a variable.
     *
     * @param variable The variable to be named.
     *
     * @return The name choosen for the variable.
     */
    public abstract String createNameForVariable(VariableReference variable);

}
