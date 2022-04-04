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
package org.evosuite.symbolic.vm;

import org.evosuite.dse.AbstractVM;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.constraint.IntegerConstraint;
import org.evosuite.symbolic.expr.fp.RealConstant;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.instrument.ConcolicInstrumentingClassLoader;
import org.evosuite.symbolic.vm.heap.SymbolicHeap;
import org.evosuite.symbolic.vm.string.Types;
import org.evosuite.utils.TypeUtil;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import static org.evosuite.dse.util.Assertions.notNull;

/**
 * Static area (static fields) and heap (instance fields)
 * <p>
 * FIXME: reset static state before each execution.
 *
 * @author csallner@uta.edu (Christoph Csallner)
 */
public final class HeapVM extends AbstractVM {

    private static final Logger logger = LoggerFactory.getLogger(HeapVM.class);

    public static final String ARRAY_LENGTH = "length";

    private final SymbolicEnvironment env;

    private final ConcolicInstrumentingClassLoader classLoader;

    private final PathConditionCollector pc;

    public HeapVM(SymbolicEnvironment env, PathConditionCollector pc,
                  ConcolicInstrumentingClassLoader classLoader) {
        this.env = env;
        this.pc = pc;
        this.classLoader = classLoader;
    }

    /* Fields */

    /**
     * Resolve (static or instance) field
     *
     * <p>
     * JVM Specification, Section 5.4.3.2: Field Resolution:
     * http://java.sun.com/
     * docs/books/jvms/second_edition/html/ConstantPool.doc.html#71685
     * <p>
     * TODO: Resolve field once and for all, then cache it.
     */
    public static Field resolveField(Class<?> claz, String name) {
        notNull(claz, name);

        Field[] fields = claz.getDeclaredFields();
        for (Field field : fields)
            if (field.getName().equals(name)) // owner declares the "name" field
                return field;

        Class<?>[] suprs = claz.getInterfaces();
        for (Class<?> supr : suprs) {
            Field res = resolveField(supr, name);
            if (res != null) // super interface declares it
                return res;
        }

        Class<?> supr = claz.getSuperclass();
        if (supr != null) // super class declares it
            return resolveField(supr, name);

        return null;
    }

    /**
     * GetStatic mypackage/MyClass fieldName FieldType
     *
     * @param owner     name of a class or interface.
     * @param fieldName name of the field to be read. The owner class or interface
     *                  itself my have declared this field. If owner is a class, then
     *                  this field may also be declared by a - super-class of the
     *                  owner class, or by a - interface implemented by (a super-class
     *                  of) the owner class.
     *                  <p>
     *                  http://java.sun.com/docs/books/jvms/second_edition/html/
     *                  Instructions2.doc5.html#getstatic
     */
    @Override
    public void GETSTATIC(String owner, String fieldName, String desc) {

        /**
         * Prepare Class
         */
        Class<?> claz = env.ensurePrepared(owner); // type name given in
        // bytecode

        Field concrete_field = resolveField(claz, fieldName); // field may be
        // declared by
        // interface
        Class<?> declaringClass = concrete_field.getDeclaringClass();

        if (declaringClass.isInterface()) {
            /*
             * Unlikely that we ever get here. Java compiler probably computes
             * value of this (final) field and replaces any
             * "getstatic MyInterface myField" by "sipush fieldValue" or such.
             * Even if we get here, there should be no need to prepare this
             * field, as there has to be an explicit initialization, hence a
             * <clinit>().
             */
            logger.debug("Do we have to prepare the static fields of an interface?");
            env.ensurePrepared(declaringClass);
        }

        boolean isAccessible = concrete_field.isAccessible();
        if (!isAccessible) {
            concrete_field.setAccessible(true);
        }

        /**
         * First, Get symbolic expression. If no symbolic expression exists, use
         * concrete value. Then, update operand stack according to type
         */
        Type type = Type.getType(desc);

        try {

            if (type.equals(Type.INT_TYPE)) {

                int value = concrete_field.getInt(null);
                IntegerValue intExpr = env.heap.getStaticField(
                        owner, fieldName, value);
                env.topFrame().operandStack.pushBv32(intExpr);

            } else if (type.equals(Type.CHAR_TYPE)) {

                char value = concrete_field.getChar(null);
                IntegerValue intExpr = env.heap.getStaticField(
                        owner, fieldName, value);
                env.topFrame().operandStack.pushBv32(intExpr);

            } else if (type.equals(Type.SHORT_TYPE)) {

                short value = concrete_field.getShort(null);
                IntegerValue intExpr = env.heap.getStaticField(
                        owner, fieldName, value);
                env.topFrame().operandStack.pushBv32(intExpr);

            } else if (type.equals(Type.BOOLEAN_TYPE)) {

                boolean booleanValue = concrete_field.getBoolean(null);
                int value = booleanValue ? 1 : 0;
                IntegerValue intExpr = env.heap.getStaticField(
                        owner, fieldName, value);
                env.topFrame().operandStack.pushBv32(intExpr);

            } else if (type.equals(Type.BYTE_TYPE)) {

                byte value = concrete_field.getByte(null);
                IntegerValue intExpr = env.heap.getStaticField(
                        owner, fieldName, value);
                env.topFrame().operandStack.pushBv32(intExpr);

            } else if (type.equals(Type.LONG_TYPE)) {

                long value = concrete_field.getLong(null);
                IntegerValue intExpr = env.heap.getStaticField(
                        owner, fieldName, value);
                env.topFrame().operandStack.pushBv64(intExpr);

            } else if (type.equals(Type.FLOAT_TYPE)) {

                float value = concrete_field.getFloat(null);
                RealValue fp32 = env.heap.getStaticField(owner,
                        fieldName, value);
                env.topFrame().operandStack.pushFp32(fp32);

            } else if (type.equals(Type.DOUBLE_TYPE)) {

                double value = concrete_field.getDouble(null);
                RealValue fp64 = env.heap.getStaticField(owner,
                        fieldName, value);
                env.topFrame().operandStack.pushFp64(fp64);

            } else {

                Object value = concrete_field.get(null);
                ReferenceExpression ref = env.heap.getReference(value);
                env.topFrame().operandStack.pushRef(ref);
            }

            if (!isAccessible) {
                concrete_field.setAccessible(false);
            }

        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc11.html#putstatic
     */
    @Override
    public void PUTSTATIC(String owner, String name, String desc) {

        /**
         * Prepare classes
         */
        Class<?> claz = env.ensurePrepared(owner); // type name given in
        // bytecode
        Field field = resolveField(claz, name);
        /* See GetStatic */
        Class<?> declaringClass = field.getDeclaringClass();
        if (declaringClass.isInterface()) {
            logger.debug("Do we have to prepare the static fields of an interface?");
            env.ensurePrepared(declaringClass);
        }

        /**
         * Update symbolic state (if needed)
         */
        Operand value_operand = env.topFrame().operandStack.popOperand();
        Expression<?> symb_value = OperandUtils.retrieveOperandExpression(value_operand);

        // NonNullReference are not stored in the symbolic heap fields
        if (symb_value instanceof ReferenceOperand) return;

        env.heap.putStaticField(owner, name, symb_value);
    }

    /**
     * Allocate space on the heap and push a reference ref to it onto the stack.
     * <p>
     * For each instance field declared by class className, we add a tuple (ref,
     * default value) to the field's map.
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc10.html#new
     */
    @Override
    public void NEW(String className) {
        /**
         * Since this callback is invoked before the actual object creation, we
         * do nothing.
         *
         * We do not need to discard any elements from the operand stack since
         * it is given empty.
         *
         * PRE-Stack: empty
         *
         * POST-Stack: objectref (delayed)
         */
        Class<?> clazz = classLoader.getClassForName(className);
        Type objectType = Type.getType(clazz);
        ReferenceConstant newObject = this.env.heap.buildNewReferenceConstant(objectType);
        env.topFrame().operandStack.pushRef(newObject);
    }

    /**
     * Retrieve the value of an instance field
     *
     * <p>
     * Before actually retrieving the value, the JVM will check if the instance
     * is null. If the receiver instance is null, the JVM will throw a null
     * pointer exception.
     *
     * @see http
     * ://java.sun.com/docs/books/jvms/second_edition/html/Instructions2
     * .doc5.html#getfield
     */
    @Override
    public void GETFIELD(Object conc_receiver, String className,
                         String fieldName, String desc) {
        // consume symbolic operand
        ReferenceExpression receiver_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_receiver, receiver_ref);

        Field field = resolveField(classLoader.getClassForName(className),
                fieldName);
        env.ensurePrepared(field.getDeclaringClass());

        boolean isAccessible = field.isAccessible();
        if (!isAccessible) {
            field.setAccessible(true);
        }

        /*
         * Schedule reference field type to be asserted -- before null check, as
         * null check will create a new node in path constraint
         */
        /* null-check */
        if (nullReferenceViolation(receiver_ref, conc_receiver)) {
            return;
        }

        ReferenceExpression symb_receiver = receiver_ref;

        Type type = Type.getType(desc);

        try {

            if (type.equals(Type.INT_TYPE)) {

                int value = field.getInt(conc_receiver);
                IntegerValue intExpr = env.heap.getField(
                        className, fieldName, conc_receiver, symb_receiver,
                        value);
                env.topFrame().operandStack.pushBv32(intExpr);

            } else if (type.equals(Type.LONG_TYPE)) {

                long value = field.getLong(conc_receiver);
                IntegerValue intExpr = env.heap.getField(
                        className, fieldName, conc_receiver, symb_receiver,
                        value);
                env.topFrame().operandStack.pushBv64(intExpr);

            } else if (type.equals(Type.FLOAT_TYPE)) {

                float value = field.getFloat(conc_receiver);
                RealValue fp32 = env.heap
                        .getField(className, fieldName, conc_receiver,
                                symb_receiver, value);
                env.topFrame().operandStack.pushFp32(fp32);

            } else if (type.equals(Type.DOUBLE_TYPE)) {

                double value = field.getDouble(conc_receiver);
                RealValue fp64 = env.heap.getField(className,
                        fieldName, conc_receiver, symb_receiver, value);
                env.topFrame().operandStack.pushFp64(fp64);

            } else if (type.equals(Type.CHAR_TYPE)) {

                char value = field.getChar(conc_receiver);
                IntegerValue intExpr = env.heap.getField(
                        className, fieldName, conc_receiver, symb_receiver,
                        value);
                env.topFrame().operandStack.pushBv32(intExpr);

            } else if (type.equals(Type.SHORT_TYPE)) {

                short value = field.getShort(conc_receiver);
                IntegerValue intExpr = env.heap.getField(
                        className, fieldName, conc_receiver, symb_receiver,
                        value);
                env.topFrame().operandStack.pushBv32(intExpr);

            } else if (type.equals(Type.BOOLEAN_TYPE)) {

                boolean booleanValue = field.getBoolean(conc_receiver);
                int value = booleanValue ? 1 : 0;
                IntegerValue intExpr = env.heap.getField(
                        className, fieldName, conc_receiver, symb_receiver,
                        value);
                env.topFrame().operandStack.pushBv32(intExpr);

            } else if (type.equals(Type.BYTE_TYPE)) {

                byte value = field.getByte(conc_receiver);
                IntegerValue intExpr = env.heap.getField(
                        className, fieldName, conc_receiver, symb_receiver,
                        value);
                env.topFrame().operandStack.pushBv32(intExpr);

            } else {

                Object value = field.get(conc_receiver);
                ReferenceExpression ref = env.heap.getReference(value);
                env.topFrame().operandStack.pushRef(ref);
            }

            if (!isAccessible) {
                field.setAccessible(false);
            }

        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Store a value in an instance field.
     * <p>
     * Before actually retrieving the value, the JVM will check if the instance
     * is null. If the receiver instance is null, the JVM will throw a null
     * pointer exception.
     */
    @Override
    public void PUTFIELD(Object conc_receiver, String className,
                         String fieldName, String desc) {
        /**
         * Pop symbolic heap
         */
        Operand value_operand = env.topFrame().operandStack.popOperand();
        ReferenceExpression receiver_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_receiver, receiver_ref);

        /**
         * Prepare classes
         */
        Field field = resolveField(classLoader.getClassForName(className),
                fieldName);
        env.ensurePrepared(field.getDeclaringClass());

        /* null-check */
        if (nullReferenceViolation(receiver_ref, conc_receiver)) {
            return;
        }

        ReferenceExpression symb_receiver = receiver_ref;

        /**
         * Compute new symbolic state
         */
        Expression<?> symb_value = null;
        if (value_operand instanceof IntegerOperand) {
            IntegerOperand intOp = (IntegerOperand) value_operand;
            symb_value = intOp.getIntegerExpression();
        } else if (value_operand instanceof RealOperand) {
            RealOperand realOp = (RealOperand) value_operand;
            symb_value = realOp.getRealExpression();
        } else if (value_operand instanceof ReferenceOperand) {

            // NonNullReference are not stored in the symbolic heap fields
            return;

        }
        env.heap.putField(className, fieldName, conc_receiver, symb_receiver,
                symb_value);
    }

    /* Arrays */

    /**
     * Create a (one-dimensional) array of primitive component type, e.g., new
     * int[3]
     * <p>
     * Allocate space on the heap and push a reference ref to it onto the stack.
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc10.html#newarray
     */
    @Override
    public void NEWARRAY(int conc_array_length, Class<?> componentType, String className, String methodName) {
        /**
         * Since this callback is invoked before the actual array creation, we
         * can only add negative index constraints.
         * newarray
         * PRE: int (length)
         *
         * POST: arrayref (delayed)
         */
        // discard symbolic arguments
        IntegerValue symb_array_length = env.topFrame().operandStack.popBv32();

        /* negative index */
        if (negativeArrayLengthViolation(conc_array_length, symb_array_length, className, methodName))
            return;

        // create array class
        int[] lenghts = new int[]{0};
        Class<?> array_class = Array.newInstance(componentType, lenghts)
                .getClass();

        Type arrayType = Type.getType(array_class);

        ReferenceConstant symb_array_ref = env.heap.buildNewArrayReferenceConstant(arrayType);

        env.heap.putField("", ARRAY_LENGTH, null, symb_array_ref,
                symb_array_length);

        env.topFrame().operandStack.pushRef(symb_array_ref);
    }

    /**
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc
     * .html#anewarray
     */
    @Override
    public void ANEWARRAY(int conc_array_length, String componentTypeName, String className, String methodName) {
        /**
         * Since this callback is invoked before the actual array creation, we
         * can only add negative index constraints.
         *
         * PRE: int (length)
         *
         * POST: arrayref (delayed)
         */

        // discard symbolic arguments
        IntegerValue symb_array_length = env.topFrame().operandStack.popBv32();

        /* negative index */
        if (negativeArrayLengthViolation(conc_array_length, symb_array_length, className, methodName))
            return;

        // create array class
        Type componentType = Type.getObjectType(componentTypeName.replace('/', '.'));
        Class<?> componentClass = classLoader.getClassForType(componentType);
        int[] lenghts = new int[]{0};
        Class<?> array_class = Array.newInstance(componentClass, lenghts)
                .getClass();

        Type arrayType = Type.getType(array_class);
        ReferenceConstant symb_array_ref = env.heap.buildNewArrayReferenceConstant(arrayType);

        env.heap.putField("", ARRAY_LENGTH, null, symb_array_ref,
                symb_array_length);

        env.topFrame().operandStack.pushRef(symb_array_ref);
    }

    /**
     * MULTIANEWARRAY
     *
     * <pre>
     * boolean[] b1 = new boolean[1]; // NEWARRAY T_BOOLEAN
     * Boolean[] B1 = new Boolean[1]; // ANEWARRAY java/lang/Boolean
     * boolean[][] b2 = new boolean[1][2]; // MULTIANEWARRAY [[Z 2
     * Boolean[][] B2 = new Boolean[1][2]; // MULTIANEWARRAY [[Ljava/lang/Boolean; 2
     * </pre>
     */
    @Override
    public void MULTIANEWARRAY(String arrayTypeDesc, int nrDimensions, String className, String methodName) {
        /**
         * Since this callback is invoked before the actual array creation, we
         * can only add negative index constraints.
         *
         * PRE: int (dimensions) | ... | int (size2) | int (size1)
         *
         * POST: arrayref (delayed)
         */

        // push negartive length constraints
        for (int i = 0; i < nrDimensions; i++) {
            IntegerValue symb_length = env.topFrame().operandStack.popBv32();
            int conc_length = symb_length.getConcreteValue()
                    .intValue();
            if (negativeArrayLengthViolation(conc_length, symb_length, className, methodName)) {
                return;
            }
        }

        Type multiArrayType = Type.getType(arrayTypeDesc);
        // push delayed object
        // @FIXME
        ReferenceConstant newMultiArray = env.heap.buildNewArrayReferenceConstant(multiArrayType);
        env.topFrame().operandStack.pushRef(newMultiArray);
    }

    @Override
    public void ARRAYLENGTH(Object conc_array) {
        /* get symbolic arguments */
        ReferenceExpression array_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_array, array_ref);

        /* null-check */
        if (nullReferenceViolation(array_ref, conc_array)) {
            return;
        }

        int conc_array_length = Array.getLength(conc_array);
        ReferenceExpression symb_array_ref = array_ref;

        IntegerValue symb_array_length = env.heap.getField("",
                ARRAY_LENGTH, conc_array, symb_array_ref, conc_array_length);
        env.topFrame().operandStack.pushBv32(symb_array_length);
    }

    /**
     * Load an int value from an array and push it on the stack
     * <p>
     * ..., arrayref, index ==> ..., value
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#iaload
     */
    @Override
    public void IALOAD(Object conc_array, int conc_index, String className, String methodName) {
        // pop symbolic arguments
        IntegerValue symb_index = env.topFrame().operandStack.popBv32();
        ReferenceExpression array_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_array, array_ref);

        /* null-check */
        if (nullReferenceViolation(array_ref, conc_array)) {
            return;
        }

        /* negative index */
        if (negativeIndexViolation(conc_index, symb_index, className, methodName)) {
            return;
        }

        /* out of bound index */
        ReferenceExpression symb_array_reference = array_ref;
        int conc_array_length = Array.getLength(conc_array);
        IntegerValue symb_array_length = env.heap.getField("", ARRAY_LENGTH,
                conc_array, symb_array_reference, conc_array_length);

        if (indexTooBigViolation(conc_index, symb_index, conc_array_length,
                symb_array_length, className, methodName))
            return;

        int bv32 = Array.getInt(conc_array, conc_index);
        IntegerValue c = env.heap.arrayLoad(symb_array_reference, symb_index, new IntegerConstant(bv32));
        env.topFrame().operandStack.pushBv32(c);
    }

    @Override
    public void LALOAD(Object conc_array, int conc_index, String className, String methodName) {
        // pop symbolic arguments
        IntegerValue symb_index = env.topFrame().operandStack.popBv32();
        ReferenceExpression array_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_array, array_ref);

        /* null-check */
        if (nullReferenceViolation(array_ref, conc_array)) {
            return;
        }

        /* negative index */
        if (negativeIndexViolation(conc_index, symb_index, className, methodName)) {
            return;
        }

        /* out of bound index */
        ReferenceExpression symb_array_reference = array_ref;
        int conc_array_length = Array.getLength(conc_array);
        IntegerValue symb_array_length = env.heap.getField("", ARRAY_LENGTH,
                conc_array, symb_array_reference, conc_array_length);

        if (indexTooBigViolation(conc_index, symb_index, conc_array_length,
                symb_array_length, className, methodName))
            return;

        long bv64 = Array.getLong(conc_array, conc_index);
        IntegerValue c = env.heap.arrayLoad(symb_array_reference, symb_index,
                new IntegerConstant(bv64));
        env.topFrame().operandStack.pushBv64(c);

    }

    @Override
    public void FALOAD(Object conc_array, int conc_index, String className, String methodName) {
        // pop symbolic arguments
        IntegerValue symb_index = env.topFrame().operandStack.popBv32();
        ReferenceExpression array_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_array, array_ref);

        /* null-check */
        if (nullReferenceViolation(array_ref, conc_array)) {
            return;
        }

        /* negative index */
        if (negativeIndexViolation(conc_index, symb_index, className, methodName)) {
            return;
        }

        /* out of bound index */
        ReferenceExpression symb_array_reference = array_ref;
        int conc_array_length = Array.getLength(conc_array);
        IntegerValue symb_array_length = env.heap.getField("", ARRAY_LENGTH,
                conc_array, symb_array_reference, conc_array_length);

        if (indexTooBigViolation(conc_index, symb_index, conc_array_length,
                symb_array_length, className, methodName))
            return;

        float fp32 = Array.getFloat(conc_array, conc_index);
        RealValue c = env.heap
                .arrayLoad(symb_array_reference, symb_index, new RealConstant(fp32));
        env.topFrame().operandStack.pushFp32(c);

    }

    /**
     * Load double from array
     * <p>
     * ..., arrayref, index ==> ..., value
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc3.html#daload
     */
    @Override
    public void DALOAD(Object conc_array, int conc_index, String className, String methodName) {
        // pop symbolic arguments
        IntegerValue symb_index = env.topFrame().operandStack.popBv32();
        ReferenceExpression array_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_array, array_ref);

        /* null-check */
        if (nullReferenceViolation(array_ref, conc_array)) {
            return;
        }

        /* negative index */
        if (negativeIndexViolation(conc_index, symb_index, className, methodName)) {
            return;
        }
        /* out of bound index */
        ReferenceExpression symb_array_reference = array_ref;
        int conc_array_length = Array.getLength(conc_array);
        IntegerValue symb_array_length = env.heap.getField("", ARRAY_LENGTH,
                conc_array, symb_array_reference, conc_array_length);

        if (indexTooBigViolation(conc_index, symb_index, conc_array_length,
                symb_array_length, className, methodName))
            return;

        double fp64 = Array.getDouble(conc_array, conc_index);
        RealValue c = env.heap
                .arrayLoad(symb_array_reference, symb_index, new RealConstant(fp64));
        env.topFrame().operandStack.pushFp64(c);
    }

    @Override
    public void AALOAD(Object conc_array, int conc_index, String className, String methodName) {
        // pop symbolic arguments
        IntegerValue symb_index = env.topFrame().operandStack.popBv32();
        ReferenceExpression array_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_array, array_ref);

        /* null-check */
        if (nullReferenceViolation(array_ref, conc_array)) {
            return;
        }

        /* negative index */
        if (negativeIndexViolation(conc_index, symb_index, className, methodName)) {
            return;
        }

        /* out of bound index */
        ReferenceExpression symb_array_reference = array_ref;
        int conc_array_length = Array.getLength(conc_array);
        IntegerValue symb_array_length = env.heap.getField("", ARRAY_LENGTH,
                conc_array, symb_array_reference, conc_array_length);

        if (indexTooBigViolation(conc_index, symb_index, conc_array_length,
                symb_array_length, className, methodName))
            return;

        Object conc_value = Array.get(conc_array, conc_index);

        ReferenceExpression symb_value;
        if (conc_value == null) {
            symb_value = ExpressionFactory.buildNewNullExpression();
        } else {

            /* Array load expression */
            symb_value = env.heap.getReference(conc_value);
            Type arrayType = Type.getObjectType(conc_array.getClass().getName());
            if (TypeUtil.isStringValue(arrayType.getElementType())) {
                StringValue stringValue = env.heap.arrayLoad(symb_array_reference, symb_index, new StringConstant((String) conc_value));

                // We add the loaded value to the reference string that we were suppose to be using
                env.heap.putField(Types.JAVA_LANG_STRING,
                        SymbolicHeap.$STRING_VALUE,
                        conc_value,
                        symb_value,
                        stringValue);
            } else {
                // TODO: implement general objects
            }

        }
        env.topFrame().operandStack.pushRef(symb_value);
    }

    private boolean indexTooBigViolation(int conc_index,
                                         IntegerValue symb_index, int conc_array_length,
                                         IntegerValue symb_array_length, String className, String methodName) {

        IntegerConstraint indexTooBigConstraint;
        if (conc_index >= conc_array_length) {
            indexTooBigConstraint = ConstraintFactory.gte(symb_index,
                    symb_array_length);
            if (indexTooBigConstraint.getLeftOperand()
                    .containsSymbolicVariable()
                    || indexTooBigConstraint.getRightOperand()
                    .containsSymbolicVariable())
                this.pc.appendArrayAccessCondition(indexTooBigConstraint, className, methodName, true);
            return true;
        } else {
            indexTooBigConstraint = ConstraintFactory.lt(symb_index,
                    symb_array_length);
            if (indexTooBigConstraint.getLeftOperand()
                    .containsSymbolicVariable()
                    || indexTooBigConstraint.getRightOperand()
                    .containsSymbolicVariable())
                this.pc.appendArrayAccessCondition(indexTooBigConstraint, className, methodName, false);
            return false;
        }
    }

    private boolean nullReferenceViolation(ReferenceExpression symb_ref, Object conc_ref) {
        // TODO: Add constraint to path condition
        return conc_ref == null;
    }

    private boolean negativeIndexViolation(int conc_index,
                                           IntegerValue symb_index, String className, String methodName) {
        IntegerConstraint negative_index_constraint;
        if (conc_index < 0) {
            negative_index_constraint = ConstraintFactory.lt(symb_index,
                    ExpressionFactory.ICONST_0);
            if (negative_index_constraint.getLeftOperand()
                    .containsSymbolicVariable()
                    || negative_index_constraint.getRightOperand()
                    .containsSymbolicVariable())
                pc.appendArrayAccessCondition(negative_index_constraint, className, methodName, true);
            return true;
        } else {
            negative_index_constraint = ConstraintFactory.gte(symb_index,
                    ExpressionFactory.ICONST_0);
            if (negative_index_constraint.getLeftOperand()
                    .containsSymbolicVariable()
                    || negative_index_constraint.getRightOperand()
                    .containsSymbolicVariable())
                pc.appendArrayAccessCondition(negative_index_constraint, className, methodName, false);
            return false;
        }
    }

    private boolean negativeArrayLengthViolation(int conc_array_length,
                                                 IntegerValue array_length_index, String className, String methodName) {
        IntegerConstraint negative_array_length_constraint;
        if (conc_array_length < 0) {
            negative_array_length_constraint = ConstraintFactory.lt(
                    array_length_index, ExpressionFactory.ICONST_0);
            if (negative_array_length_constraint.getLeftOperand()
                    .containsSymbolicVariable()
                    || negative_array_length_constraint.getRightOperand()
                    .containsSymbolicVariable())
                pc.appendArrayAccessCondition(negative_array_length_constraint, className, methodName, true);
            return true;
        } else {
            negative_array_length_constraint = ConstraintFactory.gte(
                    array_length_index, ExpressionFactory.ICONST_0);
            if (negative_array_length_constraint.getLeftOperand()
                    .containsSymbolicVariable()
                    || negative_array_length_constraint.getRightOperand()
                    .containsSymbolicVariable())
                pc.appendArrayAccessCondition(negative_array_length_constraint, className, methodName, false);
            return false;
        }
    }

    /**
     * retrieve byte/boolean from array
     */
    @Override
    public void BALOAD(Object conc_array, int conc_index, String className, String methodName) {
        // pop symbolic arguments
        IntegerValue symb_index = env.topFrame().operandStack.popBv32();
        ReferenceExpression array_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_array, array_ref);

        /* null-check */
        if (nullReferenceViolation(array_ref, conc_array)) {
            return;
        }

        /* negative index */
        if (negativeIndexViolation(conc_index, symb_index, className, methodName)) {
            return;
        }

        /* out of bound index */
        ReferenceExpression symb_array_reference = array_ref;
        int conc_array_length = Array.getLength(conc_array);
        IntegerValue symb_array_length = env.heap.getField("", ARRAY_LENGTH,
                conc_array, symb_array_reference, conc_array_length);

        if (indexTooBigViolation(conc_index, symb_index, conc_array_length,
                symb_array_length, className, methodName))
            return;

        Object object = Array.get(conc_array, conc_index);
        int intValue;
        if (object instanceof Boolean) {
            boolean booleanValue = (Boolean) object;
            intValue = booleanValue ? 1 : 0;
        } else {
            assert object instanceof Byte;
            intValue = ((Byte) object).shortValue();
        }

        IntegerValue c = env.heap.arrayLoad(symb_array_reference, symb_index,
                new IntegerConstant(intValue));

        env.topFrame().operandStack.pushBv32(c);

    }

    @Override
    public void CALOAD(Object conc_array, int conc_index, String className, String methodName) {
        // pop symbolic arguments
        IntegerValue symb_index = env.topFrame().operandStack.popBv32();
        ReferenceExpression array_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_array, array_ref);

        /* null-check */
        if (nullReferenceViolation(array_ref, conc_array)) {
            return;
        }

        /* negative index */
        if (negativeIndexViolation(conc_index, symb_index, className, methodName)) {
            return;
        }

        /* out of bound index */
        ReferenceExpression symb_array_reference = array_ref;
        int conc_array_length = Array.getLength(conc_array);
        IntegerValue symb_array_length = env.heap.getField("", ARRAY_LENGTH,
                conc_array, symb_array_reference, conc_array_length);

        if (indexTooBigViolation(conc_index, symb_index, conc_array_length,
                symb_array_length, className, methodName))
            return;

        char bv32 = Array.getChar(conc_array, conc_index);
        IntegerValue c = env.heap.arrayLoad(symb_array_reference, symb_index,
                new IntegerConstant(bv32));
        env.topFrame().operandStack.pushBv32(c);

    }

    @Override
    public void SALOAD(Object conc_array, int conc_index, String className, String methodName) {
        // pop symbolic arguments
        IntegerValue symb_index = env.topFrame().operandStack.popBv32();
        ReferenceExpression array_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_array, array_ref);

        /* null-check */
        if (nullReferenceViolation(array_ref, conc_array)) {
            return;
        }

        /* negative index */
        if (negativeIndexViolation(conc_index, symb_index, className, methodName)) {
            return;
        }

        /* out of bound index */
        ReferenceExpression symb_array_reference = array_ref;
        int conc_array_length = Array.getLength(conc_array);
        IntegerValue symb_array_length = env.heap.getField("", ARRAY_LENGTH,
                conc_array, symb_array_reference, conc_array_length);

        if (indexTooBigViolation(conc_index, symb_index, conc_array_length,
                symb_array_length, className, methodName))
            return;

        short conc_value = Array.getShort(conc_array, conc_index);
        IntegerValue e = env.heap.arrayLoad(symb_array_reference, symb_index,
                new IntegerConstant(conc_value));
        env.topFrame().operandStack.pushBv32(e);

    }

    /**
     * Store the top operand stack value into an array
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#iastore
     */
    @Override
    public void IASTORE(Object conc_array, int conc_index, String className, String methodName) {
        // pop arguments
        IntegerValue symb_value = env.topFrame().operandStack.popBv32();
        IntegerValue symb_index = env.topFrame().operandStack.popBv32();
        ReferenceExpression array_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_array, array_ref);

        /* null-check */
        if (nullReferenceViolation(array_ref, conc_array)) {
            return;
        }

        /* negative index */
        if (negativeIndexViolation(conc_index, symb_index, className, methodName)) {
            return;
        }

        /* out of bound index */
        ReferenceExpression symb_array_reference = array_ref;
        int conc_array_length = Array.getLength(conc_array);
        IntegerValue symb_array_length = env.heap.getField("", ARRAY_LENGTH,
                conc_array, symb_array_reference, conc_array_length);

        if (indexTooBigViolation(conc_index, symb_index, conc_array_length,
                symb_array_length, className, methodName))
            return;

        env.heap.arrayStore(conc_array, symb_array_reference, symb_index, symb_value);
    }

    @Override
    public void LASTORE(Object conc_array, int conc_index, String className, String methodName) {
        // get symbolic arguments
        IntegerValue symb_value = env.topFrame().operandStack.popBv64();
        IntegerValue symb_index = env.topFrame().operandStack.popBv32();
        ReferenceExpression array_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_array, array_ref);

        /* null-check */
        if (nullReferenceViolation(array_ref, conc_array)) {
            return;
        }

        /* negative index */
        if (negativeIndexViolation(conc_index, symb_index, className, methodName)) {
            return;
        }

        /* out of bound index */
        ReferenceExpression symb_array_reference = array_ref;
        int conc_array_length = Array.getLength(conc_array);
        IntegerValue symb_array_length = env.heap.getField("", ARRAY_LENGTH,
                conc_array, symb_array_reference, conc_array_length);

        if (indexTooBigViolation(conc_index, symb_index, conc_array_length,
                symb_array_length, className, methodName))
            return;

        env.heap.arrayStore(conc_array, symb_array_reference, symb_index, symb_value);
    }

    @Override
    public void FASTORE(Object conc_array, int conc_index, String className, String methodName) {
        // get symbolic arguments
        RealValue symb_value = env.topFrame().operandStack.popFp32();
        IntegerValue symb_index = env.topFrame().operandStack.popBv32();
        ReferenceExpression array_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_array, array_ref);

        /* null-check */
        if (nullReferenceViolation(array_ref, conc_array)) {
            return;
        }

        /* negative index */
        if (negativeIndexViolation(conc_index, symb_index, className, methodName)) {
            return;
        }

        /* out of bound index */
        ReferenceExpression symb_array_reference = array_ref;
        int conc_array_length = Array.getLength(conc_array);
        IntegerValue symb_array_length = env.heap.getField("", ARRAY_LENGTH,
                conc_array, symb_array_reference, conc_array_length);

        if (indexTooBigViolation(conc_index, symb_index, conc_array_length,
                symb_array_length, className, methodName))
            return;

        env.heap.arrayStore(conc_array, symb_array_reference, symb_index, symb_value);
    }

    @Override
    public void DASTORE(Object conc_array, int conc_index, String className, String methodName) {
        // get symbolic arguments
        RealValue symb_value = env.topFrame().operandStack.popFp64();
        IntegerValue symb_index = env.topFrame().operandStack.popBv32();
        ReferenceExpression array_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_array, array_ref);

        /* null-check */
        if (nullReferenceViolation(array_ref, conc_array)) {
            return;
        }

        /* negative index */
        if (negativeIndexViolation(conc_index, symb_index, className, methodName)) {
            return;
        }

        /* out of bound index */
        ReferenceExpression symb_array_reference = array_ref;
        int conc_array_length = Array.getLength(conc_array);
        IntegerValue symb_array_length = env.heap.getField("", ARRAY_LENGTH,
                conc_array, symb_array_reference, conc_array_length);

        if (indexTooBigViolation(conc_index, symb_index, conc_array_length,
                symb_array_length, className, methodName))
            return;

        env.heap.arrayStore(conc_array, symb_array_reference, symb_index, symb_value);
    }

    /**
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc
     * .html#aastore
     */
    @Override
    public void AASTORE(Object conc_array, int conc_index, Object conc_value, String className, String methodName) {
        // pop arguments
        @SuppressWarnings("unused")
        ReferenceExpression symb_value = env.topFrame().operandStack.popRef();
        IntegerValue symb_index = env.topFrame().operandStack.popBv32();
        ReferenceExpression array_ref = env.topFrame().operandStack.popRef();

        /* check references initialization */
        env.heap.initializeReference(conc_array, array_ref);
        env.heap.initializeReference(conc_value, symb_value);

        /* array null-check */
        if (nullReferenceViolation(array_ref, conc_array)) {
            return;
        }

        // TODO: check cases for value
        //       When not typing???

        /* negative index */
        if (negativeIndexViolation(conc_index, symb_index, className, methodName)) {
            return;
        }

        /* out of bound index */
        ReferenceExpression symb_array_reference = array_ref;
        int conc_array_length = Array.getLength(conc_array);
        IntegerValue symb_array_length = env.heap.getField("", ARRAY_LENGTH,
                conc_array, symb_array_reference, conc_array_length);

        if (indexTooBigViolation(conc_index, symb_index, conc_array_length,
                symb_array_length, className, methodName))
            return;

        Type arrayType = Type.getObjectType(conc_array.getClass().getName());
        if (TypeUtil.isStringValue(arrayType.getElementType())) {
            StringValue stringValue = env.heap.getField(
                    Types.JAVA_LANG_STRING,
                    SymbolicHeap.$STRING_VALUE,
                    conc_value,
                    env.heap.getReference(conc_value),
                    (String) conc_value
            );
            env.heap.arrayStore(conc_array, symb_array_reference, symb_index, stringValue);
        } else {
            //TODO: implement general objects
        }

        // NonNullReference are not stored in the symbolic heap fields
        return;

    }

    @Override
    public void BASTORE(Object conc_array, int conc_index, String className, String methodName) {
        // pop arguments
        IntegerValue symb_value = env.topFrame().operandStack.popBv32();
        IntegerValue symb_index = env.topFrame().operandStack.popBv32();
        ReferenceExpression array_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_array, array_ref);

        /* null-check */
        if (nullReferenceViolation(array_ref, conc_array)) {
            return;
        }

        /* negative index */
        if (negativeIndexViolation(conc_index, symb_index, className, methodName)) {
            return;
        }

        /* out of bound index */
        ReferenceExpression symb_array_reference = array_ref;
        int conc_array_length = Array.getLength(conc_array);
        IntegerValue symb_array_length = env.heap.getField("", ARRAY_LENGTH,
                conc_array, symb_array_reference, conc_array_length);

        if (indexTooBigViolation(conc_index, symb_index, conc_array_length,
                symb_array_length, className, methodName))
            return;

        env.heap.arrayStore(conc_array, symb_array_reference, symb_index, symb_value);
    }

    @Override
    public void CASTORE(Object conc_array, int conc_index, String className, String methodName) {
        // pop arguments
        IntegerValue symb_value = env.topFrame().operandStack.popBv32();
        IntegerValue symb_index = env.topFrame().operandStack.popBv32();
        ReferenceExpression array_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_array, array_ref);

        /* null-check */
        if (nullReferenceViolation(array_ref, conc_array)) {
            return;
        }

        /* negative index */
        if (negativeIndexViolation(conc_index, symb_index, className, methodName)) {
            return;
        }

        /* out of bound index */
        ReferenceExpression symb_array_reference = array_ref;
        int conc_array_length = Array.getLength(conc_array);
        IntegerValue symb_array_length = env.heap.getField("", ARRAY_LENGTH,
                conc_array, symb_array_reference, conc_array_length);

        if (indexTooBigViolation(conc_index, symb_index, conc_array_length,
                symb_array_length, className, methodName))
            return;

        env.heap.arrayStore(conc_array, symb_array_reference, symb_index, symb_value);
    }

    @Override
    public void SASTORE(Object conc_array, int conc_index, String className, String methodName) {
        // get symbolic arguments
        IntegerValue symb_value = env.topFrame().operandStack.popBv32();
        IntegerValue symb_index = env.topFrame().operandStack.popBv32();
        ReferenceExpression array_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_array, array_ref);

        /* null-check */
        if (nullReferenceViolation(array_ref, conc_array)) {
            return;
        }

        /* negative index */
        if (negativeIndexViolation(conc_index, symb_index, className, methodName)) {
            return;
        }

        /* out of bound index */
        ReferenceExpression symb_array_reference = array_ref;
        int conc_array_length = Array.getLength(conc_array);
        IntegerValue symb_array_length = env.heap.getField("", ARRAY_LENGTH,
                conc_array, symb_array_reference, conc_array_length);

        if (indexTooBigViolation(conc_index, symb_index, conc_array_length,
                symb_array_length, className, methodName))
            return;

        env.heap.arrayStore(conc_array, symb_array_reference, symb_index, symb_value);
    }

    /**
     * Explicit type cast:
     *
     * <pre>
     * RefTypeX x = (RefTypeX) ref;
     * </pre>
     * <p>
     * null is treated as (can be cast to) any reference type. This is
     * consistent with the null type being a subtype of every reference type.
     * Note the different treatment in {@link #INSTANCEOF}.
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc2.html#checkcast
     */
    @Override
    public void CHECKCAST(Object conc_ref, String typeName) {
        ReferenceExpression symb_ref = env.topFrame().operandStack.peekRef();
        env.heap.initializeReference(conc_ref, symb_ref);
    }

    /**
     * Dynamic type check:
     *
     * <pre>
     * (variable instanceof TypeName)
     * </pre>
     * <p>
     * null is not treated as (is not an instance of) any reference type. This
     * requires non-standard treatment of null. Note the different treatment in
     * {@link #CHECKCAST}.
     *
     * <p>
     * If the jvm has not loaded the class/interface named TypeName before, then
     * we load it. TODO: Is this a problem?
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#instanceof
     */
    @Override
    public void INSTANCEOF(Object conc_ref, String typeName) {
        /* pop symbolic arguments */
        ReferenceExpression symb_ref = env.topFrame().operandStack.popRef();

        /* check reference initialization */
        env.heap.initializeReference(conc_ref, symb_ref);
        Type type = Type.getObjectType(typeName);

        Class<?> myClazz = classLoader.getClassForType(type);
        boolean instanceOf = myClazz.isInstance(conc_ref);

        IntegerConstant ret;
        if (instanceOf) {
            ret = ExpressionFactory.ICONST_1;
        } else {
            ret = ExpressionFactory.ICONST_0;
        }

        /* push symbolic arguments */
        env.topFrame().operandStack.pushBv32(ret);
    }
}