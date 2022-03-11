/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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
public abstract class GenericExecutable<T extends GenericExecutable<T, U>, U extends Executable>
        extends GenericAccessibleObject<T> {

    /**
     * @param owner the class where this accessible object is located in
     */
    GenericExecutable(final GenericClass<?> owner) {
        super(owner);
    }

    public abstract Type[] getRawParameterTypes();

    public abstract Type getReturnType();

    abstract Type[] getExactParameterTypes(final U m, final Type type);

    public abstract Type[] getParameterTypes();

    public abstract Parameter[] getParameters();

    @Override
    public final boolean isField() {
        return false;
    }

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
