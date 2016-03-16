/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
	 * @param nr
	 *            index of the parameter, not counting the receiver and ignoring
	 *            the additional width of category-2 parameters. E.g., in
	 *            foo(double a, int b) parameter b would always have nr 1,
	 *            regardless of foo being an instance method or not.
	 * @param calleeLocalsIndex
	 *            is the index in which this value is stored in the receiver
	 *            frame. In contrast to nr, this accounts for the receiver and
	 *            the different widths of category-1 and category-2 parameter
	 *            types.
	 */
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, int value);

	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, boolean value);

	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, short value);

	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, byte value);

	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, char value);

	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, long value);

	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, float value);

	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, double value);

	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, Object value);

	/**
	 * Line number in the Java source code.
	 */
	public void SRC_LINE_NUMBER(int lineNr);

	/**
	 * Start of a new method
	 */
	public void METHOD_BEGIN(int access, String className, String methName,
			String methDesc);

	/**
	 * Max values of a method
	 */
	public void METHOD_MAXS(String className, String methName, String methDesc,
			int maxStack, int maxLocals);

	/**
	 * Pass index-th concrete parameter of the just called method. There will be
	 * no such call for a potentially present receiver instance ("this"
	 * parameter).
	 * 
	 * @param nr
	 *            index of the parameter, not counting the receiver and ignoring
	 *            the additional width of category-2 parameters. E.g., in
	 *            foo(double a, int b) parameter b would always have nr 1,
	 *            regardless of foo being an instance method or not.
	 * @param calleeLocalsIndex
	 *            is the index in which this value is stored in the receiver
	 *            frame. In contrast to nr, this accounts for the receiver and
	 *            the different widths of category-1 and category-2 parameter
	 *            types.
	 */
	public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, int value);

	public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, boolean value);

	public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, short value);

	public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, byte value);

	public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, char value);

	public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, long value);

	public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, float value);

	public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, double value);

	public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, Object value);

	/**
	 * METHOD_BEGIN_PARAM for the receiver instance ("this"), if this method is
	 * a non-constructor instance method.
	 */
	public void METHOD_BEGIN_RECEIVER(Object value);

	/**
	 * Value returned by the just completed method call
	 */
	public void CALL_RESULT(String owner, String name, String desc);

	public void CALL_RESULT(boolean res, String owner, String name, String desc);

	public void CALL_RESULT(int res, String owner, String name, String desc);

	public void CALL_RESULT(long res, String owner, String name, String desc);

	public void CALL_RESULT(double res, String owner, String name, String desc);

	public void CALL_RESULT(float res, String owner, String name, String desc);

	public void CALL_RESULT(Object res, String owner, String name, String desc);

	/**
	 * Start of a new basic block
	 */
	public void BB_BEGIN();

	public void HANDLER_BEGIN(int access, String className, String methName,
			String methDesc);

	/*
	 * Some 200 JVM ByteCode instructions
	 */

	public void NOP();

	public void ACONST_NULL();

	public void ICONST_M1();

	public void ICONST_0();

	public void ICONST_1();

	public void ICONST_2();

	public void ICONST_3();

	public void ICONST_4();

	public void ICONST_5();

	public void LCONST_0();

	public void LCONST_1();

	public void FCONST_0();

	public void FCONST_1();

	public void FCONST_2();

	public void DCONST_0();

	public void DCONST_1();

	public void BIPUSH(int value);

	public void SIPUSH(int value);

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc8. html#ldc
	 */
	public void LDC(String x);

	public void LDC(Class<?> x);

	public void LDC(int x);

	public void LDC(float x);

	public void LDC_W();

	public void LDC2_W(long x);

	public void LDC2_W(double x);

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6. html#iload
	 */
	public void ILOAD(int i);

	public void LLOAD(int i);

	public void FLOAD(int i);

	public void DLOAD(int i);

	public void ALOAD(int i);

	public void ILOAD_0();

	public void ILOAD_1();

	public void ILOAD_2();

	public void ILOAD_3();

	public void LLOAD_0();

	public void LLOAD_1();

	public void LLOAD_2();

	public void LLOAD_3();

	public void FLOAD_0();

	public void FLOAD_1();

	public void FLOAD_2();

	public void FLOAD_3();

	public void DLOAD_0();

	public void DLOAD_1();

	public void DLOAD_2();

	public void DLOAD_3();

	public void ALOAD_0();

	public void ALOAD_1();

	public void ALOAD_2();

	public void ALOAD_3();

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6.html#iaload
	 */
	public void IALOAD(Object receiver, int index);

	public void LALOAD(Object receiver, int index);

	public void FALOAD(Object receiver, int index);

	public void DALOAD(Object receiver, int index);

	public void AALOAD(Object receiver, int index);

	public void BALOAD(Object receiver, int index);

	public void CALOAD(Object receiver, int index);

	public void SALOAD(Object receiver, int index);

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6.html#istore
	 */
	public void ISTORE(int i);

	public void LSTORE(int i);

	public void FSTORE(int i);

	public void DSTORE(int i);

	public void ASTORE(int i);

	public void ISTORE_0();

	public void ISTORE_1();

	public void ISTORE_2();

	public void ISTORE_3();

	public void LSTORE_0();

	public void LSTORE_1();

	public void LSTORE_2();

	public void LSTORE_3();

	public void FSTORE_0();

	public void FSTORE_1();

	public void FSTORE_2();

	public void FSTORE_3();

	public void DSTORE_0();

	public void DSTORE_1();

	public void DSTORE_2();

	public void DSTORE_3();

	public void ASTORE_0();

	public void ASTORE_1();

	public void ASTORE_2();

	public void ASTORE_3();

	public void IASTORE(Object receiver, int index);

	public void LASTORE(Object receiver, int index);

	public void FASTORE(Object receiver, int index);

	public void DASTORE(Object receiver, int index);

	public void AASTORE(Object receiver, int index);

	public void BASTORE(Object receiver, int index);

	public void CASTORE(Object receiver, int index);

	public void SASTORE(Object receiver, int index);

	public void POP();

	public void POP2();

	public void DUP();

	public void DUP_X1();

	public void DUP_X2();

	public void DUP2();

	public void DUP2_X1();

	public void DUP2_X2();

	public void SWAP();

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6. html#iadd
	 */
	public void IADD();

	public void LADD();

	public void FADD();

	public void DADD();

	public void ISUB();

	public void LSUB();

	public void FSUB();

	public void DSUB();

	public void IMUL();

	public void LMUL();

	public void FMUL();

	public void DMUL();

	public void IDIV(int rhs);

	public void LDIV(long rhs);

	public void FDIV(float rhs);

	public void DDIV(double rhs);

	public void IREM(int rhs);

	public void LREM(long rhs);

	public void FREM(float rhs);

	public void DREM(double rhs);

	public void INEG();

	public void LNEG();

	public void FNEG();

	public void DNEG();

	public void ISHL();

	public void LSHL();

	public void ISHR();

	public void LSHR();

	public void IUSHR();

	public void LUSHR();

	public void IAND();

	public void LAND();

	public void IOR();

	public void LOR();

	public void IXOR();

	public void LXOR();

	public void IINC(int i, int value);

	public void I2L();

	public void I2F();

	public void I2D();

	public void L2I();

	public void L2F();

	public void L2D();

	public void F2I();

	public void F2L();

	public void F2D();

	public void D2I();

	public void D2L();

	public void D2F();

	public void I2B();

	public void I2C();

	public void I2S();

	public void LCMP();

	public void FCMPL();

	public void FCMPG();

	public void DCMPL();

	public void DCMPG();

	public void IFEQ(String className, String methNane, int branchIndex,
			int param);

	public void IFNE(String className, String methNane, int branchIndex,
			int param);

	public void IFLT(String className, String methNane, int branchIndex,
			int param);

	public void IFGE(String className, String methNane, int branchIndex,
			int param);

	public void IFGT(String className, String methNane, int branchIndex,
			int param);

	public void IFLE(String className, String methNane, int branchIndex,
			int param);

	public void IF_ICMPEQ(String className, String methNane, int branchIndex,
			int left, int right);

	public void IF_ICMPNE(String className, String methNane, int branchIndex,
			int left, int right);

	public void IF_ICMPLT(String className, String methNane, int branchIndex,
			int left, int right);

	public void IF_ICMPGE(String className, String methNane, int branchIndex,
			int left, int right);

	public void IF_ICMPGT(String className, String methNane, int branchIndex,
			int left, int right);

	public void IF_ICMPLE(String className, String methNane, int branchIndex,
			int left, int right);

	public void IF_ACMPEQ(String className, String methNane, int branchIndex,
			Object left, Object right);

	public void IF_ACMPNE(String className, String methNane, int branchIndex,
			Object left, Object right);

	public void GOTO();

	public void JSR();

	public void RET();

	public void TABLESWITCH(String className, String methName, int branchIndex,
			int target, int min, int max);

	public void LOOKUPSWITCH(String className, String methName,
			int branchIndex, int target, int[] goals);

	public void IRETURN();

	public void LRETURN();

	public void FRETURN();

	public void DRETURN();

	public void ARETURN();

	public void RETURN();

	public void GETSTATIC(String owner, String name, String desc);

	public void PUTSTATIC(String owner, String name, String desc);

	public void GETFIELD(Object receiver, String owner, String name, String desc);

	public void PUTFIELD(Object receiver, String owner, String name, String desc);

	public void INVOKESTATIC(String owner, String name, String desc);

	public void INVOKESPECIAL(String owner, String name, String desc);

	public void INVOKESPECIAL(Object receiver, String owner, String name,
			String desc);

	public void INVOKEVIRTUAL(Object receiver, String owner, String name,
			String desc);

	public void INVOKEINTERFACE(Object receiver, String owner, String name,
			String desc);

	public void UNUSED();

	public void NEW(String typeName);

	public void NEWARRAY(int length, Class<?> componentType);

	public void ANEWARRAY(int length, String typeName);

	public void ARRAYLENGTH(Object reference);

	public void ATHROW(Throwable object);

	public void CHECKCAST(Object reference, String typeName);

	public void INSTANCEOF(Object reference, String typeName);

	public void MONITORENTER();

	public void MONITOREXIT();

	public void WIDE();

	public void MULTIANEWARRAY(String arrayTypeDesc, int nrDimensions);

	public void IFNULL(String className, String methNane, int branchIndex,
			Object param);

	public void IFNONNULL(String className, String methNane, int branchIndex,
			Object param);

	public void GOTO_W();

	public void JSR_W();
}
