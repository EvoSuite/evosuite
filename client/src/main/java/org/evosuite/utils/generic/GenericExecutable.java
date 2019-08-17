package org.evosuite.utils.generic;

import org.evosuite.testcase.variable.VariableReference;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;

/**
 * A shared superclass for the common functionality of {@link GenericMethod} and
 * {@link GenericConstructor}, similar in spirit to {@link java.lang.reflect.Executable Executable}
 * from the Java Reflection API.
 *
 * @param <T>
 * @param <U>
 */
public abstract class GenericExecutable<T extends GenericExecutable<?, ?>,
        U extends Executable> extends GenericAccessibleObject<GenericExecutable<T, U>> {

    /**
     * @param owner the class where this accessible object is located in
     */
    GenericExecutable(final GenericClass owner) {
        super(owner);
    }

    public abstract Type[] getRawParameterTypes();

    public abstract Type getReturnType();

    abstract Type[] getExactParameterTypes(final U m, final Type type);

    public abstract Type[] getParameterTypes();

    public abstract Parameter[] getParameters();

    /**
     * Tells whether there the owning class contains an overloaded executable with the given list
     * of parameters.
     *
     * @param parameters the parameter list of the overloaded executable
     * @return {@code} true if there is an overloaded executable, {@code false} otherwise
     */
    public abstract boolean isOverloaded(List<VariableReference> parameters);

    /**
     * Returns the fully qualified name concatenated with the descriptor of the underlying
     * executable.
     *
     * @return the fully qualified name concatenated with the descriptor
     */
    public abstract String getNameWithDescriptor();

    /**
     * Returns the descriptor for the underlying executable.
     *
     * @return the descriptor
     */
    public abstract String getDescriptor();
}
