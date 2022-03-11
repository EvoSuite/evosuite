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

import org.evosuite.Properties;
import org.evosuite.symbolic.LambdaUtils;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.expr.ref.ReferenceVariable;
import org.evosuite.symbolic.expr.reftype.LambdaSyntheticType;
import org.evosuite.symbolic.expr.reftype.LiteralClassType;
import org.evosuite.symbolic.expr.reftype.ReferenceTypeExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.ExpressionFactory;
import org.evosuite.symbolic.vm.heap.symbolicHeapSection.ArraysSection;
import org.evosuite.symbolic.vm.heap.symbolicHeapSection.SymbolicHeapArraySectionFactory;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author galeotti
 */
public final class SymbolicHeap {

    /**
     * Field Value Constants
     **/
    public static final String $INT_VALUE = "$intValue";
    public static final String $BYTE_VALUE = "$byteValue";
    public static final String $CHAR_VALUE = "$charValue";
    public static final String $LONG_VALUE = "$longValue";
    public static final String $SHORT_VALUE = "$shortValue";
    public static final String $FLOAT_VALUE = "$floatValue";
    public static final String $DOUBLE_VALUE = "$doubleValue";
    public static final String $STRING_VALUE = "$stringValue";
    public static final String $MATCHER_INPUT = "$matcherInput";
    public static final String $BOOLEAN_VALUE = "$booleanValue";
    public static final String $STRING_READER_VALUE = "$stringReaderValue";
    public static final String $BIG_INTEGER_CONTENTS = "$bigInteger_contents";
    public static final String $STRING_TOKENIZER_VALUE = "$stringTokenizerValue";
    public static final String $STRING_BUFFER_CONTENTS = "$stringBuffer_contents";
    public static final String $STRING_BUILDER_CONTENTS = "$stringBuilder_contents";

    protected static final Logger logger = LoggerFactory.getLogger(SymbolicHeap.class);

    /**
     * Counter for instances
     */
    private int newInstanceCount = 0;

    /**
     * Array's memory model
     */
    private final ArraysSection symbolicArrays;

    /**
     * Stores a mapping between identityHashCodes and NonNullReferences. Every
     * time the NonNullReference for a given Object (non String) is needed, this
     * mapping is used.
     */
    private final Map<Integer, ReferenceExpression> nonNullRefs = new HashMap<>();

    /**
     * Stores a mapping between Classes and ReferenceTypes. Every
     * time the ReferenceType for a given Object (non String) is needed, this
     * mapping is used.
     */
    private final Map<Class, ReferenceTypeExpression> symbolicReferenceTypes = new HashMap<>();

    /**
     * Stores a mapping between NonNullReferences and their symbolic values. The
     * Expression<?> contains at least one symbolic variable.
     */
    private final Map<FieldKey, Map<ReferenceExpression, Expression<?>>> symb_fields = new HashMap<>();

    /**
     * Mapping between for symbolic values stored in static fields. The
     * Expression<?> contains at least one symbolic variable.
     */
    private final Map<FieldKey, Expression<?>> symb_static_fields = new HashMap<>();

    /**
     * Constructor
     */
    public SymbolicHeap() {
        this.symbolicArrays = SymbolicHeapArraySectionFactory
                .getInstance()
                .getSymbolicHeapArraySection(Properties.SELECTED_DSE_ARRAYS_MEMORY_MODEL_VERSION);
    }

    /**
     * This constructor is for references created in instrumented code (NEW,
     * ANEW, NEWARRAY, etc).
     * <p>
     * It is the only way of creating uninitialized non-null references.
     *
     * @param objectType
     * @return
     */
    public ReferenceConstant buildNewReferenceConstant(Type objectType) {
        if (objectType.getClassName() == null)
            throw new IllegalArgumentException();

        final int newInstanceId = newInstanceCount++;
        return new ReferenceConstant(objectType, newInstanceId);
    }


    /**
     * Updates an instance field. The symbolic expression is stored iif it is
     * not a constant expression (i.e. it has at least one variable).
     *
     * @param className
     * @param fieldName
     * @param conc_receiver The concrete Object receiver instance
     * @param symb_receiver A symbolic NonNullReference instance
     * @param symb_value    The Expression to be stored. Null value means the previous
     *                      symbolic expression has to be erased.
     */
    public void putField(String className, String fieldName, Object conc_receiver, ReferenceExpression symb_receiver,
                         Expression<?> symb_value) {

        Map<ReferenceExpression, Expression<?>> symb_field = getOrCreateSymbolicField(className, fieldName);

        // NOTE (ilebrero): We need to store elements even if they are constant due to probable usage later on of their
        //					reference (i.e. if the reference is bounded to an object like a closure.)
        //		if (symb_value == null || !symb_value.containsSymbolicVariable()) {
        if (symb_value == null) {
            symb_field.remove(symb_receiver);
        } else {
            symb_field.put(symb_receiver, symb_value);
        }
    }

    /**
     * Special updating case scenario for Reference expresion values
     *
     * @param className
     * @param fieldName
     * @param conc_receiver The concrete Object receiver instance
     * @param symb_receiver A symbolic NonNullReference instance
     * @param symb_value    The Expression to be stored. Null value means the previous
     *                      symbolic expression has to be erased.
     */
    public void putField(String className, String fieldName, Object conc_receiver, ReferenceExpression symb_receiver,
                         ReferenceExpression symb_value) {

        Map<ReferenceExpression, Expression<?>> symb_field = getOrCreateSymbolicField(className, fieldName);

        // NOTE (ilebrero): We need to store elements even if they are constant due to probable usage later on of their
        //					reference (i.e. if the reference is bounded to an object like a closure.)
        //		if (symb_value == null || !symb_value.containsSymbolicVariable()) {
        if (symb_value == null) {
            symb_field.remove(symb_receiver);
        } else {
            symb_field.put(symb_receiver, symb_value);
        }
    }

    private Map<ReferenceExpression, Expression<?>> getOrCreateSymbolicField(String owner, String name) {
        FieldKey k = new FieldKey(owner, name);
        Map<ReferenceExpression, Expression<?>> symb_field = symb_fields.get(k);
        if (symb_field == null) {
            symb_field = new HashMap<>();
            symb_fields.put(k, symb_field);
        }

        return symb_field;
    }

    /**
     * Returns a stored symbolic expression for an int field or created one
     *
     * @param owner
     * @param name
     * @param conc_receiver
     * @param symb_receiver
     * @param conc_value
     * @return
     */
    public IntegerValue getField(String owner, String name, Object conc_receiver, ReferenceExpression symb_receiver,
                                 long conc_value) {

        Map<ReferenceExpression, Expression<?>> symb_field = getOrCreateSymbolicField(owner, name);
        IntegerValue symb_value = (IntegerValue) symb_field.get(symb_receiver);
        if (symb_value == null || symb_value.getConcreteValue() != conc_value) {
            symb_value = ExpressionFactory.buildNewIntegerConstant(conc_value);
            symb_field.remove(symb_receiver);
        }

        return symb_value;
    }

    /**
     * @param className
     * @param fieldName
     * @param conc_receiver
     * @param symb_receiver
     * @param conc_value
     * @return
     */
    public RealValue getField(String className, String fieldName, Object conc_receiver,
                              ReferenceExpression symb_receiver, double conc_value) {

        Map<ReferenceExpression, Expression<?>> symb_field = getOrCreateSymbolicField(className, fieldName);
        RealValue symb_value = (RealValue) symb_field.get(symb_receiver);
        if (symb_value == null || symb_value.getConcreteValue() != conc_value) {
            symb_value = ExpressionFactory.buildNewRealConstant(conc_value);
            symb_field.remove(symb_receiver);
        }

        return symb_value;
    }

    /**
     * @param className
     * @param fieldName
     * @param conc_receiver
     * @param symb_receiver
     * @param conc_value
     * @return
     */
    public StringValue getField(String className, String fieldName, Object conc_receiver,
                                ReferenceExpression symb_receiver, String conc_value) {

        Map<ReferenceExpression, Expression<?>> symb_field = getOrCreateSymbolicField(className, fieldName);
        StringValue symb_value = (StringValue) symb_field.get(symb_receiver);
        if (symb_value == null || !symb_value.getConcreteValue().equals(conc_value)) {
            symb_value = ExpressionFactory.buildNewStringConstant(conc_value);
            symb_field.remove(symb_receiver);
        }

        return symb_value;
    }

    /**
     * No default concrete value means the return value could be false!
     *
     * @param className
     * @param fieldName
     * @param conc_receiver
     * @param symb_receiver
     * @return
     */
    public Expression<?> getField(String className, String fieldName, Object conc_receiver,
                                  ReferenceExpression symb_receiver) {

        Map<ReferenceExpression, Expression<?>> symb_field = getOrCreateSymbolicField(className, fieldName);
        Expression<?> symb_value = symb_field.get(symb_receiver);
        return symb_value;
    }

    public void putStaticField(String owner, String name, Expression<?> symb_value) {

        FieldKey k = new FieldKey(owner, name);
        if (symb_value == null || !symb_value.containsSymbolicVariable()) {
            symb_static_fields.remove(k);
        } else {
            symb_static_fields.put(k, symb_value);
        }

    }

    public IntegerValue getStaticField(String owner, String name, long conc_value) {

        FieldKey k = new FieldKey(owner, name);
        IntegerValue symb_value = (IntegerValue) symb_static_fields.get(k);
        if (symb_value == null || symb_value.getConcreteValue() != conc_value) {
            symb_value = ExpressionFactory.buildNewIntegerConstant(conc_value);
            symb_static_fields.remove(k);
        }

        return symb_value;

    }

    public RealValue getStaticField(String owner, String name, double conc_value) {

        FieldKey k = new FieldKey(owner, name);
        RealValue symb_value = (RealValue) symb_static_fields.get(k);
        if (symb_value == null || symb_value.getConcreteValue() != conc_value) {
            symb_value = ExpressionFactory.buildNewRealConstant(conc_value);
            symb_static_fields.remove(k);
        }

        return symb_value;
    }

    public StringValue getStaticField(String owner, String name, String conc_value) {

        FieldKey k = new FieldKey(owner, name);
        StringValue symb_value = (StringValue) symb_static_fields.get(k);
        if (symb_value == null || !symb_value.getConcreteValue().equals(conc_value)) {
            symb_value = ExpressionFactory.buildNewStringConstant(conc_value);
            symb_static_fields.remove(k);
        }

        return symb_value;
    }

    /**
     * Returns a <code>ReferenceConstant</code> if the concrete reference is
     * null. Otherwise, it looks in the list of non-null symbolic references for
     * a symbolic reference with the concrete value. If it is found, that
     * symbolic reference is returned, otherwise a new reference constant is
     * created (and added ot the list of non-null symbolic references)
     *
     * @param conc_ref
     * @return
     */
    public ReferenceExpression getReference(Object conc_ref) {
        if (conc_ref == null) {
            // null reference
            ReferenceConstant nullConstant = ExpressionFactory.buildNewNullExpression();
            return nullConstant;
        } else {
            int identityHashCode = System.identityHashCode(conc_ref);
            if (nonNullRefs.containsKey(identityHashCode)) {
                // already known object
                ReferenceExpression symb_ref = nonNullRefs.get(identityHashCode);
                return symb_ref;
            } else {
                // unknown object
                final Type type = Type.getType(conc_ref.getClass());
                ReferenceConstant ref_constant;
                if (conc_ref.getClass().isArray()) {
                    ref_constant = buildNewArrayReferenceConstant(type);
                } else {
                    ref_constant = buildNewReferenceConstant(type);
                }

                initializeReference(conc_ref, ref_constant);
                nonNullRefs.put(identityHashCode, ref_constant);
                return ref_constant;
            }
        }
    }

    /**
     * Builds a new reference variable using a var_name and a concrete object
     * The concrete object can be null.
     *
     * @param concreteObject
     * @param var_name
     * @return
     */
    public ReferenceVariable buildNewReferenceVariable(Object concreteObject, String var_name) {
        final Type referenceType;
        if (concreteObject == null) {
            referenceType = Type.getType(Object.class);
        } else {
            referenceType = Type.getType(concreteObject.getClass());
        }
        final int newInstanceId = newInstanceCount++;
        final ReferenceVariable r = new ReferenceVariable(referenceType, newInstanceId, var_name, concreteObject);
        return r;
    }

    /**
     * Initializes a reference using a concrete object
     *
     * @param concreteReference
     * @param symbolicReference
     */
    public void initializeReference(Object concreteReference, ReferenceExpression symbolicReference) {
        if (concreteReference != null) {
            if (!symbolicReference.isInitialized()) {
                symbolicReference.initializeReference(concreteReference);

                if (concreteReference.getClass().isArray()) {
                    symbolicArrays.initializeArrayReference(symbolicReference);
                }
            }

            // Fix: Reference variables are initialized when created, so they were never set on the heap reference map.
            int identityHashCode = symbolicReference.getConcIdentityHashCode();
            if (!nonNullRefs.containsKey(identityHashCode)) {
                nonNullRefs.put(identityHashCode, symbolicReference);
            }
        }
    }

    /******* Arrays Implementation *******/

    /**
     * This constructor is for references created in array related instrumented code (NEWARRAY, ANEWARRAY, MULTINEWARRAY).
     * <p>
     * It is the only way of creating uninitialized non-null arrays.
     *
     * @param arrayType
     * @return
     */
    public ReferenceConstant buildNewArrayReferenceConstant(Type arrayType) {
        if (arrayType.getClassName() == null)
            throw new IllegalArgumentException();

        final int newInstanceId = newInstanceCount++;
        return symbolicArrays.createConstantArray(arrayType, newInstanceId);
    }

    /**
     * Builds a new array reference variable using an array type, and the name the concrete array can be null.
     *
     * @param concreteArray
     * @param arrayVarName
     * @return
     */
    public ReferenceVariable buildNewArrayReferenceVariable(Object concreteArray, String arrayVarName) {
        final int newInstanceId = newInstanceCount++;
        return symbolicArrays.createVariableArray(concreteArray, newInstanceId, arrayVarName);
    }

    /********* Load Operations *********/

    /**
     * Load operation for Real arrays
     *
     * @param symbolicArray Symbolic element og the real array reference
     * @param symbolicIndex Symbolic element of the accessed index
     * @param symbolicValue Symbolic element of the accessed value
     * @return a {@link RealValue} symoblic element.
     */
    public RealValue arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, RealValue symbolicValue) {
        return symbolicArrays.arrayLoad(symbolicArray, symbolicIndex, symbolicValue);
    }

    /**
     * Load operation for String arrays
     *
     * @param symbolicArray Symbolic element og the string array reference
     * @param symbolicIndex Symbolic element of the accessed index
     * @param symbolicValue Symbolic element of the accessed value
     * @return a {@link StringValue} symoblic element.
     */
    public StringValue arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, StringValue symbolicValue) {
        return symbolicArrays.arrayLoad(symbolicArray, symbolicIndex, symbolicValue);
    }

    /**
     * Load operation for Integer arrays
     *
     * @param symbolicArray Symbolic element og the integer array reference
     * @param symbolicIndex Symbolic element of the accessed index
     * @param symbolicValue Symbolic element of the accessed value
     * @return a {@link IntegerValue} symoblic element.
     */
    public IntegerValue arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, IntegerValue symbolicValue) {
        return symbolicArrays.arrayLoad(symbolicArray, symbolicIndex, symbolicValue);
    }

    /**
     * Load operation for Reference arrays
     *
     * @param symbolicArray Symbolic element og the reference array reference
     * @param symbolicIndex Symbolic element of the accessed index
     * @param symbolicValue Symbolic element of the accessed value
     * @return a {@link ReferenceExpression} symoblic element.
     */
    public ReferenceExpression arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, ReferenceExpression symbolicValue) {
        return symbolicArrays.arrayLoad(symbolicArray, symbolicIndex, symbolicValue);
    }

    /********* Store Operations *********/

    /**
     * Store operation for Real arrays
     *
     * @param symbolicArray Symbolic element og the reference array reference
     * @param symbolicIndex Symbolic element of the accessed index
     * @param symbolicValue Symbolic element of the accessed value
     */
    public void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex, RealValue symbolicValue) {
        symbolicArrays.arrayStore(concreteArray, symbolicArray, symbolicIndex, symbolicValue);
    }

    /**
     * Store operation for String arrays
     *
     * @param symbolicArray Symbolic element og the reference array reference
     * @param symbolicIndex Symbolic element of the accessed index
     * @param symbolicValue Symbolic element of the accessed value
     */
    public void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex, StringValue symbolicValue) {
        symbolicArrays.arrayStore(concreteArray, symbolicArray, symbolicIndex, symbolicValue);
    }

    /**
     * Store operation for Integer arrays
     *
     * @param symbolicArray Symbolic element og the reference array reference
     * @param symbolicIndex Symbolic element of the accessed index
     * @param symbolicValue Symbolic element of the accessed value
     */
    public void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex, IntegerValue symbolicValue) {
        symbolicArrays.arrayStore(concreteArray, symbolicArray, symbolicIndex, symbolicValue);
    }

    /**
     * Store operation for Reference arrays
     *
     * @param symbolicArray Symbolic element og the reference array reference
     * @param symbolicIndex Symbolic element of the accessed index
     * @param symbolicValue Symbolic element of the accessed value
     */
    public void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex, ReferenceExpression symbolicValue) {
        symbolicArrays.arrayStore(concreteArray, symbolicArray, symbolicIndex, symbolicValue);
    }


    /******** Types Implementation *******/

    public ReferenceTypeExpression buildNewLambdaConstant(Class<?> lambdaAnonymousClass, boolean ownerIsIgnored) {
        if (lambdaAnonymousClass == null)
            throw new IllegalArgumentException("Lambda Anonymous Class cannot be null.");

        ReferenceTypeExpression lambdaExpression;
        lambdaExpression = symbolicReferenceTypes.get(lambdaAnonymousClass);

        if (lambdaExpression == null) {
            lambdaExpression = new LambdaSyntheticType(lambdaAnonymousClass, ownerIsIgnored);
            symbolicReferenceTypes.put(lambdaAnonymousClass, lambdaExpression);
        }

        return lambdaExpression;
    }

    /**
     * Retrieves the symbolic expression related to this class.
     *
     * @param type
     * @return
     */
    public ReferenceTypeExpression getReferenceType(Class type) {
        if (type == null) return ExpressionFactory.buildNewNullReferenceType();

        ReferenceTypeExpression typeExpression;
        typeExpression = symbolicReferenceTypes.get(type);

        if (typeExpression == null) {
            if (LambdaUtils.isLambda(type)) {
                //If we haven't seen this lambda before then it's from non-instrumented sources
                typeExpression = new LambdaSyntheticType(type, true);
            } else {
                typeExpression = new LiteralClassType(type);
            }
        }

        return typeExpression;
    }
}
