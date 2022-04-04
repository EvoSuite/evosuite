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

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.expr.ref.ReferenceVariable;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.heap.SymbolicArray;
import org.evosuite.symbolic.vm.heap.SymbolicArrayImpl;
import org.evosuite.symbolic.vm.heap.SymbolicInputArray;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * Arrays implementation using lazy variables initialization for arrays that are inputs for the SUT.
 *
 * @author Ignacio Lebrero
 */
public class LazyArraysImpl implements ArraysSection {

    /**
     * Symbolic Arrays Memory model
     * <p>
     * TODO: Implement Strings and References
     */
    private final Map<ReferenceExpression, SymbolicArray> symbolicArrays = new HashMap<>();

    @Override
    public ReferenceExpression arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, ReferenceExpression symbolicValue) {
        int concreteIndex = Math.toIntExact(symbolicIndex.getConcreteValue());
        SymbolicArray symbolicArrayContents;

        symbolicArrayContents = getOrCreateSymbolicArray(symbolicArray);
        return (ReferenceExpression) getContent(symbolicArrayContents, concreteIndex, symbolicValue);
    }

    @Override
    public IntegerValue arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, IntegerValue symbolicValue) {
        int concreteIndex = Math.toIntExact(symbolicIndex.getConcreteValue());

        SymbolicArray symbolicArrayContents = getOrCreateSymbolicArray(symbolicArray);
        return (IntegerValue) getContent(symbolicArrayContents, concreteIndex, symbolicValue);
    }

    @Override
    public RealValue arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, RealValue symbolicValue) {
        int concreteIndex = Math.toIntExact(symbolicIndex.getConcreteValue());

        SymbolicArray symbolicArrayContents = getOrCreateSymbolicArray(symbolicArray);
        return (RealValue) getContent(symbolicArrayContents, concreteIndex, symbolicValue);
    }

    @Override
    public StringValue arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, StringValue symbolicValue) {
        int concreteIndex = Math.toIntExact(symbolicIndex.getConcreteValue());
        SymbolicArray symbolicArrayContents;

        symbolicArrayContents = getOrCreateSymbolicArray(symbolicArray);
        return (StringValue) getContent(symbolicArrayContents, concreteIndex, symbolicValue);
    }

    @Override
    public void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex,
                           IntegerValue symbolicValue) {
        int concreteIndex = Math.toIntExact(symbolicIndex.getConcreteValue());

        SymbolicArray symbolicArrayContents = getOrCreateSymbolicArray(symbolicArray);
        symbolicArrayContents.set(concreteIndex, symbolicValue);
    }

    @Override
    public void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex,
                           RealValue symbolicValue) {
        int concreteIndex = Math.toIntExact(symbolicIndex.getConcreteValue());

        SymbolicArray symbolicArrayContents = getOrCreateSymbolicArray(symbolicArray);
        symbolicArrayContents.set(concreteIndex, symbolicValue);
    }

    @Override
    public void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex,
                           StringValue symbolicValue) {
        int concreteIndex = Math.toIntExact(symbolicIndex.getConcreteValue());

        SymbolicArray symbolicArrayContents = getOrCreateSymbolicArray(symbolicArray);
        symbolicArrayContents.set(concreteIndex, symbolicValue);
    }

    @Override
    public void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex,
                           ReferenceExpression symbolicValue) {
        int concreteIndex = Math.toIntExact(symbolicIndex.getConcreteValue());
        SymbolicArray symbolicArrayContents;

        symbolicArrayContents = getOrCreateSymbolicArray(symbolicArray);
        symbolicArrayContents.set(concreteIndex, symbolicValue);
    }

    @Override
    public ReferenceVariable createVariableArray(Object concreteArray, int instanceId, String arrayName) {
        ReferenceVariable symbolicArrayVariableReference = new ReferenceVariable(
                Type.getType(concreteArray.getClass()),
                instanceId,
                arrayName,
                concreteArray
        );

        SymbolicArray symbolicArray = getOrCreateSymbolicArray(symbolicArrayVariableReference);
        symbolicArrays.put(symbolicArrayVariableReference, new SymbolicInputArray(symbolicArray, arrayName));

        return symbolicArrayVariableReference;
    }

    @Override
    public ReferenceConstant createConstantArray(Type arrayType, int instanceId) {
        ReferenceConstant symbolicArrayVariableReference = new ReferenceConstant(
                arrayType,
                instanceId
        );

        SymbolicArray symbolicArray = getOrCreateSymbolicArray(symbolicArrayVariableReference);
        assert (symbolicArray != null);

        return symbolicArrayVariableReference;
    }

    @Override
    public void initializeArrayReference(ReferenceExpression symbolicArrayReference) {
        symbolicArrays.put(
                symbolicArrayReference,
                getOrCreateSymbolicArray(symbolicArrayReference)
        );
    }

    /**
     * Creation section
     */

    private SymbolicArray getOrCreateSymbolicArray(ReferenceExpression symbolicArrayReference) {
        Type contentType = symbolicArrayReference.getObjectType().getElementType();
        SymbolicArray symbolicArrayContents = symbolicArrays.get(symbolicArrayReference);

        if (symbolicArrayContents == null) {
            symbolicArrayContents = new SymbolicArrayImpl(contentType);
            symbolicArrays.put(symbolicArrayReference, symbolicArrayContents);
        }

        return symbolicArrayContents;
    }

    private Expression getContent(SymbolicArray symbolicArrayContents, int concreteIndex, Expression symbolicValue) {
        Expression content = symbolicArrayContents.get(concreteIndex);

        if (content == null) {
            symbolicArrayContents.set(concreteIndex, symbolicValue);
            content = symbolicValue;
        }

        return content;
    }

}
