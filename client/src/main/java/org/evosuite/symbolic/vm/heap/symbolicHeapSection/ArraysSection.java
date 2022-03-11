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
package org.evosuite.symbolic.vm.heap.symbolicHeapSection;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.expr.ref.ReferenceVariable;
import org.evosuite.symbolic.expr.str.StringValue;
import org.objectweb.asm.Type;

/**
 * General interface for the arrays memory model.
 * TODO: In the future we can do an implementation similar to the VMs, a general symbolicHeap with all the methods
 *       and an implementation of each section (e.g. AbstractSymbolicHeap -> ArraysHeap -> ReferencesHeap -> etc...
 *
 * @author Ignacio Lebrero
 */
public interface ArraysSection {


    /**
     * Creates a symbolic array representing a symbolic variable.
     *
     * @param concreteArray
     * @param instanceId
     * @param name
     * @return
     */
    ReferenceVariable createVariableArray(Object concreteArray, int instanceId, String name);

    /**
     * Creates a symbolic array representing a symbolic literal.
     *
     * @param arrayType
     * @param instanceId
     * @return
     */
    ReferenceConstant createConstantArray(Type arrayType, int instanceId);

    /**
     * Initialized an already created symbolic array
     *
     * @param symbolicArray Symbolic element of the array reference
     */
    void initializeArrayReference(ReferenceExpression symbolicArray);

    /**
     * Load operation for Real arrays
     *
     * @param symbolicArray Symbolic element of the array reference
     * @param symbolicIndex Symbolic element of the accessed index
     * @param symbolicValue Symbolic element of the accessed value
     */
    RealValue arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, RealValue symbolicValue);

    /**
     * Load operation for String arrays
     *
     * @param symbolicArray Symbolic element of the array reference
     * @param symbolicIndex Symbolic element of the accessed index
     * @param symbolicValue Symbolic element of the accessed value
     */
    StringValue arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, StringValue symbolicValue);

    /**
     * Load operation for Integer arrays
     *
     * @param symbolicArray Symbolic element of the array reference
     * @param symbolicIndex Symbolic element of the accessed index
     * @param symbolicValue Symbolic element of the accessed value
     */
    IntegerValue arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, IntegerValue symbolicValue);

    /**
     * Load operation for Reference arrays
     *
     * @param symbolicArray Symbolic element of the array reference
     * @param symbolicIndex Symbolic element of the accessed index
     * @param symbolicValue Symbolic element of the accessed value
     */
    ReferenceExpression arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, ReferenceExpression symbolicValue);


    /**
     * Store operation for Real arrays
     *
     * @param concreteArray Concrete instance of the array
     * @param symbolicArray Symbolic element of the array reference
     * @param symbolicIndex Symbolic element of the accessed index
     * @param symbolicValue Symbolic element of the accessed value
     */
    void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex, RealValue symbolicValue);

    /**
     * Store operation for String arrays
     *
     * @param concreteArray Concrete instance of the array
     * @param symbolicArray Symbolic element of the array reference
     * @param symbolicIndex Symbolic element of the accessed index
     * @param symbolicValue Symbolic element of the accessed value
     */
    void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex, StringValue symbolicValue);

    /**
     * Store operation for Integer arrays
     *
     * @param concreteArray Concrete instance of the array
     * @param symbolicArray Symbolic element of the array reference
     * @param symbolicIndex Symbolic element of the accessed index
     * @param symbolicValue Symbolic element of the accessed value
     */
    void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex, IntegerValue symbolicValue);

    /**
     * Store operation for Reference arrays
     *
     * @param concreteArray Concrete instance of the array
     * @param symbolicArray Symbolic element of the array reference
     * @param symbolicIndex Symbolic element of the accessed index
     * @param symbolicValue Symbolic element of the accessed value
     */
    void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex, ReferenceExpression symbolicValue);


}
