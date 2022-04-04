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
package org.evosuite.dse;

/*
    This class is taken and adapted from the DSC tool developed by Christoph Csallner.
    Link at :
    http://ranger.uta.edu/~csallner/dsc/index.html
 */

/**
 * @author csallner@uta.edu (Christoph Csallner)
 */
public interface IVM {

    /**
     * Pass a caller's nr-th concrete method argument value (which is sitting on
     * the caller's operand stack). There will be no such call for a potentially
     * present receiver instance ("this" parameter).
     * <p>
     * There is no CALLER_STACK_RECEIVER, as the receiver is already passed by
     * the corresponding INVOKE.
     *
     * @param nr                index of the parameter, not counting the receiver and ignoring
     *                          the additional width of category-2 parameters. E.g., in
     *                          foo(double a, int b) parameter b would always have nr 1,
     *                          regardless of foo being an instance method or not.
     * @param calleeLocalsIndex is the index in which this value is stored in the receiver
     *                          frame. In contrast to nr, this accounts for the receiver and
     *                          the different widths of category-1 and category-2 parameter
     *                          types.
     */
    void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, int value);

    void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, boolean value);

    void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, short value);

    void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, byte value);

    void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, char value);

    void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, long value);

    void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, float value);

    void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, double value);

    void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, Object value);

    /**
     * Line number in the Java source code.
     */
    void SRC_LINE_NUMBER(int lineNr);

    /**
     * Start of a new method
     */
    void METHOD_BEGIN(int access, String className, String methName,
                      String methDesc);

    /**
     * Max values of a method
     */
    void METHOD_MAXS(String className, String methName, String methDesc,
                     int maxStack, int maxLocals);

    /**
     * Pass index-th concrete parameter of the just called method. There will be
     * no such call for a potentially present receiver instance ("this"
     * parameter).
     *
     * @param nr                index of the parameter, not counting the receiver and ignoring
     *                          the additional width of category-2 parameters. E.g., in
     *                          foo(double a, int b) parameter b would always have nr 1,
     *                          regardless of foo being an instance method or not.
     * @param calleeLocalsIndex is the index in which this value is stored in the receiver
     *                          frame. In contrast to nr, this accounts for the receiver and
     *                          the different widths of category-1 and category-2 parameter
     *                          types.
     */
    void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, int value);

    void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, boolean value);

    void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, short value);

    void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, byte value);

    void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, char value);

    void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, long value);

    void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, float value);

    void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, double value);

    void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, Object value);

    /**
     * METHOD_BEGIN_PARAM for the receiver instance ("this"), if this method is
     * a non-constructor instance method.
     */
    void METHOD_BEGIN_RECEIVER(Object value);

    /**
     * Value returned by the just completed method call
     */
    void CALL_RESULT(String owner, String name, String desc);

    void CALL_RESULT(boolean res, String owner, String name, String desc);

    void CALL_RESULT(int res, String owner, String name, String desc);

    void CALL_RESULT(long res, String owner, String name, String desc);

    void CALL_RESULT(double res, String owner, String name, String desc);

    void CALL_RESULT(float res, String owner, String name, String desc);

    void CALL_RESULT(Object res, String owner, String name, String desc);

    /**
     * Start of a new basic block
     */
    void BB_BEGIN();

    void HANDLER_BEGIN(int access, String className, String methName,
                       String methDesc);

    /*
     * Some 200 JVM ByteCode instructions
     */

    void NOP();

    void ACONST_NULL();

    void ICONST_M1();

    void ICONST_0();

    void ICONST_1();

    void ICONST_2();

    void ICONST_3();

    void ICONST_4();

    void ICONST_5();

    void LCONST_0();

    void LCONST_1();

    void FCONST_0();

    void FCONST_1();

    void FCONST_2();

    void DCONST_0();

    void DCONST_1();

    void BIPUSH(int value);

    void SIPUSH(int value);

    /**
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc8. html#ldc
     */
    void LDC(String x);

    void LDC(Class<?> x);

    void LDC(int x);

    void LDC(float x);

    void LDC_W();

    void LDC2_W(long x);

    void LDC2_W(double x);

    /**
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6. html#iload
     */
    void ILOAD(int i);

    void LLOAD(int i);

    void FLOAD(int i);

    void DLOAD(int i);

    void ALOAD(int i);

    void ILOAD_0();

    void ILOAD_1();

    void ILOAD_2();

    void ILOAD_3();

    void LLOAD_0();

    void LLOAD_1();

    void LLOAD_2();

    void LLOAD_3();

    void FLOAD_0();

    void FLOAD_1();

    void FLOAD_2();

    void FLOAD_3();

    void DLOAD_0();

    void DLOAD_1();

    void DLOAD_2();

    void DLOAD_3();

    void ALOAD_0();

    void ALOAD_1();

    void ALOAD_2();

    void ALOAD_3();

    /**
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#iaload
     */
    void IALOAD(Object receiver, int index, String className, String methodName);

    void LALOAD(Object receiver, int index, String className, String methodName);

    void FALOAD(Object receiver, int index, String className, String methodName);

    void DALOAD(Object receiver, int index, String className, String methodName);

    void AALOAD(Object receiver, int index, String className, String methodName);

    void BALOAD(Object receiver, int index, String className, String methodName);

    void CALOAD(Object receiver, int index, String className, String methodName);

    void SALOAD(Object receiver, int index, String className, String methodName);

    /**
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#istore
     */
    void ISTORE(int i);

    void LSTORE(int i);

    void FSTORE(int i);

    void DSTORE(int i);

    void ASTORE(int i);

    void ISTORE_0();

    void ISTORE_1();

    void ISTORE_2();

    void ISTORE_3();

    void LSTORE_0();

    void LSTORE_1();

    void LSTORE_2();

    void LSTORE_3();

    void FSTORE_0();

    void FSTORE_1();

    void FSTORE_2();

    void FSTORE_3();

    void DSTORE_0();

    void DSTORE_1();

    void DSTORE_2();

    void DSTORE_3();

    void ASTORE_0();

    void ASTORE_1();

    void ASTORE_2();

    void ASTORE_3();

    void IASTORE(Object receiver, int index, String className, String methodName);

    void LASTORE(Object receiver, int index, String className, String methodName);

    void FASTORE(Object receiver, int index, String className, String methodName);

    void DASTORE(Object receiver, int index, String className, String methodName);

    void BASTORE(Object receiver, int index, String className, String methodName);

    void CASTORE(Object receiver, int index, String className, String methodName);

    void SASTORE(Object receiver, int index, String className, String methodName);

    void AASTORE(Object receiver, int index, Object value, String className, String methodName);

    void POP();

    void POP2();

    void DUP();

    void DUP_X1();

    void DUP_X2();

    void DUP2();

    void DUP2_X1();

    void DUP2_X2();

    void SWAP();

    /**
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6. html#iadd
     */
    void IADD();

    void LADD();

    void FADD();

    void DADD();

    void ISUB();

    void LSUB();

    void FSUB();

    void DSUB();

    void IMUL();

    void LMUL();

    void FMUL();

    void DMUL();

    void IDIV(int rhs);

    void LDIV(long rhs);

    void FDIV(float rhs);

    void DDIV(double rhs);

    void IREM(int rhs);

    void LREM(long rhs);

    void FREM(float rhs);

    void DREM(double rhs);

    void INEG();

    void LNEG();

    void FNEG();

    void DNEG();

    void ISHL();

    void LSHL();

    void ISHR();

    void LSHR();

    void IUSHR();

    void LUSHR();

    void IAND();

    void LAND();

    void IOR();

    void LOR();

    void IXOR();

    void LXOR();

    void IINC(int i, int value);

    void I2L();

    void I2F();

    void I2D();

    void L2I();

    void L2F();

    void L2D();

    void F2I();

    void F2L();

    void F2D();

    void D2I();

    void D2L();

    void D2F();

    void I2B();

    void I2C();

    void I2S();

    void LCMP();

    void FCMPL();

    void FCMPG();

    void DCMPL();

    void DCMPG();

    void IFEQ(String className, String methNane, int branchIndex,
              int param);

    void IFNE(String className, String methNane, int branchIndex,
              int param);

    void IFLT(String className, String methNane, int branchIndex,
              int param);

    void IFGE(String className, String methNane, int branchIndex,
              int param);

    void IFGT(String className, String methNane, int branchIndex,
              int param);

    void IFLE(String className, String methNane, int branchIndex,
              int param);

    void IF_ICMPEQ(String className, String methNane, int branchIndex,
                   int left, int right);

    void IF_ICMPNE(String className, String methNane, int branchIndex,
                   int left, int right);

    void IF_ICMPLT(String className, String methNane, int branchIndex,
                   int left, int right);

    void IF_ICMPGE(String className, String methNane, int branchIndex,
                   int left, int right);

    void IF_ICMPGT(String className, String methNane, int branchIndex,
                   int left, int right);

    void IF_ICMPLE(String className, String methNane, int branchIndex,
                   int left, int right);

    void IF_ACMPEQ(String className, String methNane, int branchIndex,
                   Object left, Object right);

    void IF_ACMPNE(String className, String methNane, int branchIndex,
                   Object left, Object right);

    void GOTO();

    void JSR();

    void RET();

    void TABLESWITCH(String className, String methName, int branchIndex,
                     int target, int min, int max);

    void LOOKUPSWITCH(String className, String methName,
                      int branchIndex, int target, int[] goals);

    void IRETURN();

    void LRETURN();

    void FRETURN();

    void DRETURN();

    void ARETURN();

    void RETURN();

    void GETSTATIC(String owner, String name, String desc);

    void PUTSTATIC(String owner, String name, String desc);

    void GETFIELD(Object receiver, String owner, String name, String desc);

    void PUTFIELD(Object receiver, String owner, String name, String desc);

    void INVOKESTATIC(String owner, String name, String desc);

    void INVOKESPECIAL(String owner, String name, String desc);

    void INVOKESPECIAL(Object receiver, String owner, String name,
                       String desc);

    void INVOKEVIRTUAL(Object receiver, String owner, String name,
                       String desc);

    void INVOKEINTERFACE(Object receiver, String owner, String name,
                         String desc);

    void INVOKEDYNAMIC(Object clazz, String owner);

    void INVOKEDYNAMIC(String concatenationResult, String stringOwnerClass, String stringRecipe);

    void UNUSED();

    void NEW(String typeName);

    void NEWARRAY(int length, Class<?> componentType, String className, String methodName);

    void ANEWARRAY(int length, String componentTypeName, String className, String typeName);

    void ARRAYLENGTH(Object reference);

    void ATHROW(Throwable object);

    void CHECKCAST(Object reference, String typeName);

    void INSTANCEOF(Object reference, String typeName);

    void MONITORENTER();

    void MONITOREXIT();

    void WIDE();

    void MULTIANEWARRAY(String arrayTypeDesc, int nrDimensions, String className, String methodName);

    void IFNULL(String className, String methNane, int branchIndex,
                Object param);

    void IFNONNULL(String className, String methNane, int branchIndex,
                   Object param);

    void GOTO_W();

    void JSR_W();

    void cleanUp();
}