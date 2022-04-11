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
import org.evosuite.symbolic.expr.ref.array.ArrayConstant;
import org.evosuite.symbolic.expr.ref.array.ArrayValue;
import org.evosuite.symbolic.expr.ref.array.ArrayVariable;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.ExpressionFactory;
import org.evosuite.utils.TypeUtil;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * Arrays implementation using a composition of select / store operations.
 *
 * @author Ignacio Lebrero
 */
public class SelectStoreImpl implements ArraysSection {

    /**
     * Symbolic Arrays Memory model
     * <p>
     * TODO: Implement Strings and References
     */
    private final Map<ReferenceExpression, ArrayValue.RealArrayValue> realArrays = new HashMap<>();
    private final Map<ReferenceExpression, ArrayValue.StringArrayValue> stringArrays = new HashMap<>();
    private final Map<ReferenceExpression, ArrayValue.IntegerArrayValue> integerArrays = new HashMap<>();
    private final Map<ReferenceExpression, ArrayValue.ReferenceArrayValue> referenceArrays = new HashMap<>();


    @Override
    public IntegerValue arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, IntegerValue symbolicValue) {
        ArrayValue.IntegerArrayValue arrayExpression = integerArrays.get(symbolicArray);
        return ExpressionFactory.buildArraySelectExpression(arrayExpression, symbolicIndex, symbolicValue);
    }

    /**
     * TODO: Implement me!
     */
    @Override
    public ReferenceExpression arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, ReferenceExpression symbolicValue) {
        ArrayValue.ReferenceArrayValue arrayExpression = referenceArrays.get(symbolicArray);
        return null;
    }

    @Override
    public RealValue arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, RealValue symbolicValue) {
        ArrayValue.RealArrayValue arrayExpression = realArrays.get(symbolicArray);
        return ExpressionFactory.buildArraySelectExpression(arrayExpression, symbolicIndex, symbolicValue);
    }

    @Override
    public StringValue arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, StringValue symbolicValue) {
        ArrayValue.StringArrayValue arrayExpression = stringArrays.get(symbolicArray);
        return ExpressionFactory.buildArraySelectExpression(arrayExpression, symbolicIndex, symbolicValue);
    }

    @Override
    public void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex,
                           IntegerValue symbolicValue) {
        ArrayValue.IntegerArrayValue symbolic_array_instance = integerArrays.get(symbolicArray);
        ArrayValue.IntegerArrayValue new_symbolic_array_instance = ExpressionFactory.buildArrayStoreExpression(
                symbolic_array_instance,
                symbolicIndex,
                symbolicValue,
                concreteArray
        );

        integerArrays.put(symbolicArray, new_symbolic_array_instance);
    }

    @Override
    public void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex,
                           RealValue symbolicValue) {

        ArrayValue.RealArrayValue symbolic_array_instance = realArrays.get(symbolicArray);
        ArrayValue.RealArrayValue new_symbolic_array_instance = ExpressionFactory.buildArrayStoreExpression(
                symbolic_array_instance,
                symbolicIndex,
                symbolicValue,
                concreteArray
        );

        realArrays.put(symbolicArray, new_symbolic_array_instance);
    }

    /**
     * TODO: Implement me!
     */
    @Override
    public void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex,
                           ReferenceExpression symbolicValue) {
//		ArrayValue.ReferenceArrayValue symbolic_array_instance = referenceArrays.get(symbolicArray);
//		ArrayValue.ReferenceArrayValue new_symbolic_array_instance = ExpressionFactory.buildArrayStoreExpression(
//			symbolic_array_instance,
//      symbolicIndex,
//      symbolicValue,
//      concreteArray
//		);
//
//		referenceArrays.put(symbolicArray, new_symbolic_array_instance);
    }


    @Override
    public void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex,
                           StringValue symbolicValue) {
        ArrayValue.StringArrayValue symbolic_array_instance = stringArrays.get(symbolicArray);
        ArrayValue.StringArrayValue new_symbolic_array_instance = ExpressionFactory.buildArrayStoreExpression(
                symbolic_array_instance,
                symbolicIndex,
                symbolicValue,
                concreteArray
        );

        stringArrays.put(symbolicArray, new_symbolic_array_instance);
    }

    @Override
    public ArrayVariable createVariableArray(Object concreteArray, int instanceId, String arrayName) {
        ArrayVariable arrayVariable = (ArrayVariable) ExpressionFactory.buildArrayVariableExpression(instanceId, arrayName, concreteArray);

        if (concreteArray != null) initializeArrayReference(arrayVariable);

        return arrayVariable;
    }

    @Override
   public ArrayConstant createConstantArray(Type arrayType, int instanceId) {
        ArrayConstant arrayConstant = (ArrayConstant) ExpressionFactory.buildArrayConstantExpression(arrayType, instanceId);

        return arrayConstant;
    }

    @Override
    public void initializeArrayReference(ReferenceExpression symbolicArray) {
        Type arrayType = symbolicArray.getObjectType().getElementType();

        if (TypeUtil.isIntegerValue(arrayType)) {
            initIntegerArray(symbolicArray);
        } else if (TypeUtil.isRealValue(arrayType)) {
            initRealArray(symbolicArray);
        } else if (TypeUtil.isStringValue(arrayType)) {
            initStringArray(symbolicArray);
        } else {
            initReferenceArray(symbolicArray);
        }
    }

    private void initReferenceArray(ReferenceExpression symbolicArray) {
        referenceArrays.put(
                symbolicArray,
                (ArrayValue.ReferenceArrayValue) symbolicArray
        );
    }

    private void initStringArray(ReferenceExpression symbolicArray) {
        stringArrays.put(
                symbolicArray,
                (ArrayValue.StringArrayValue) symbolicArray
        );
    }

    private void initRealArray(ReferenceExpression symbolicArray) {
        realArrays.put(
                symbolicArray,
                (ArrayValue.RealArrayValue) symbolicArray
        );
    }

    private void initIntegerArray(ReferenceExpression symbolicArray) {
        integerArrays.put(
                symbolicArray,
                (ArrayValue.IntegerArrayValue) symbolicArray
        );
    }
}
