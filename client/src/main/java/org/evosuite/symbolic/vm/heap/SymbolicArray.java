/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.vm.heap;

import org.evosuite.symbolic.expr.Expression;
import org.objectweb.asm.Type;

/**
 * Interface for the internal heap representation of arrays.
 *
 * @author Ignacio Lebrero
 */
public interface SymbolicArray {

    /**
     * Returns the array content type
     *
     * @return a {@link org.objectweb.asm.Type} object
     */
    Type getContentType();

    /**
     * Returns an expression representing the symbolic value stored at the ith position
     *
     * @param index
     * @return a {@link org.evosuite.symbolic.expr.Expression} object
     */
    Expression get(Integer index);

    /**
     * Symbolically updates the ith element of the array
     *
     * @param index
     * @param expression
     */
    void set(Integer index, Expression expression);

}
