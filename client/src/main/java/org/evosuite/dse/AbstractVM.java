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

import org.evosuite.dse.util.Assertions;

/*
    This class is taken and adapted from the DSC tool developed by Christoph Csallner.
    Link at :
    http://ranger.uta.edu/~csallner/dsc/index.html
 */

/**
 * VM ByteCode instruction listener that does not have access to any shared
 * state of the VM. Still useful for implementing a ByteCode instruction logger.
 * 
 * @author csallner@uta.edu (Christoph Csallner)
 */
public abstract class AbstractVM implements IVM {

	protected MainConfig conf = MainConfig.get();

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, int value) { /* stub */
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, boolean value) { /* stub */
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, short value) { /* stub */
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, byte value) { /* stub */
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, char value) { /* stub */
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, long value) { /* stub */
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, float value) { /* stub */
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, double value) { /* stub */
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, Object value) { /* stub */
	}

	@Override
	public void METHOD_BEGIN(int access, String className, String methName,
			String methDesc) { /* stub */
	}

	@Override
	public void METHOD_MAXS(String className, String methName, String methDesc,
			int maxStack, int maxLocals) {
		// empty
	}

	/**
	 * @param index
	 *            is the index into the locals. I.e., index increases by two for
	 *            each category-2 parameter
	 */
	@Override
	public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, int value) { /* stub */
	}

	@Override
	public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, boolean value) { /* stub */
	}

	@Override
	public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, short value) { /* stub */
	}

	@Override
	public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, byte value) { /* stub */
	}

	@Override
	public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, char value) { /* stub */
	}

	@Override
	public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, long value) { /* stub */
	}

	@Override
	public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, float value) { /* stub */
	}

	@Override
	public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, double value) { /* stub */
	}

	@Override
	public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, Object value) { /* stub */
	}

	@Override
	public void METHOD_BEGIN_RECEIVER(Object value) { /* stub */
	}

	@Override
	public void SRC_LINE_NUMBER(int lineNr) { /* stub */
	}

	@Override
	public void BB_BEGIN() { /* stub */
	}

	@Override
	public void HANDLER_BEGIN(int access, String className, String methName,
			String methDesc) { /* stub */
	}

	@Override
	public void CALL_RESULT(String owner, String name, String desc) { /* stub */
	}

	@Override
	public void CALL_RESULT(boolean res, String owner, String name, String desc) { /* stub */
	}

	@Override
	public void CALL_RESULT(int res, String owner, String name, String desc) { /* stub */
	}

	@Override
	public void CALL_RESULT(long res, String owner, String name, String desc) { /* stub */
	}

	@Override
	public void CALL_RESULT(double res, String owner, String name, String desc) { /* stub */
	}

	@Override
	public void CALL_RESULT(float res, String owner, String name, String desc) { /* stub */
	}

	@Override
	public void CALL_RESULT(Object res, String owner, String name, String desc) { /* stub */
	}

	@Override
	public void AALOAD(Object receiver, int index) { /* stub */
	}

	@Override
	public void AASTORE(Object receiver, int index) { /* stub */
	}

	@Override
	public void ACONST_NULL() { /* stub */
	}

	@Override
	public void ALOAD(int i) { /* stub */
	}

	@Override
	public void ANEWARRAY(int length, String typeName) { /* stub */
	}

	@Override
	public void ARETURN() { /* stub */
	}

	@Override
	public void ARRAYLENGTH(Object reference) { /* stub */
	}

	@Override
	public void ASTORE(int i) { /* stub */
	}

	@Override
	public void ATHROW(Throwable throwable) { /* stub */
	}

	@Override
	public void BALOAD(Object receiver, int index) { /* stub */
	}

	@Override
	public void BASTORE(Object receiver, int index) { /* stub */
	}

	@Override
	public void BIPUSH(int value) { /* stub */
	}

	@Override
	public void CALOAD(Object receiver, int index) { /* stub */
	}

	@Override
	public void CASTORE(Object receiver, int index) { /* stub */
	}

	@Override
	public void CHECKCAST(Object reference, String typeName) { /* stub */
	}

	@Override
	public void D2F() { /* stub */
	}

	@Override
	public void D2I() { /* stub */
	}

	@Override
	public void D2L() { /* stub */
	}

	@Override
	public void DADD() { /* stub */
	}

	@Override
	public void DALOAD(Object receiver, int index) { /* stub */
	}

	@Override
	public void DASTORE(Object receiver, int index) { /* stub */
	}

	@Override
	public void DCMPG() { /* stub */
	}

	@Override
	public void DCMPL() { /* stub */
	}

	@Override
	public void DCONST_0() { /* stub */
	}

	@Override
	public void DCONST_1() { /* stub */
	}

	@Override
	public void DDIV(double rhs) { /* stub */
	}

	@Override
	public void DLOAD(int i) { /* stub */
	}

	@Override
	public void DMUL() { /* stub */
	}

	@Override
	public void DNEG() { /* stub */
	}

	@Override
	public void DREM(double rhs) { /* stub */
	}

	@Override
	public void DRETURN() { /* stub */
	}

	@Override
	public void DSTORE(int i) { /* stub */
	}

	@Override
	public void DSUB() { /* stub */
	}

	@Override
	public void DUP() { /* stub */
	}

	@Override
	public void DUP2() { /* stub */
	}

	@Override
	public void DUP2_X1() { /* stub */
	}

	@Override
	public void DUP2_X2() { /* stub */
	}

	@Override
	public void DUP_X1() { /* stub */
	}

	@Override
	public void DUP_X2() { /* stub */
	}

	@Override
	public void F2D() { /* stub */
	}

	@Override
	public void F2I() { /* stub */
	}

	@Override
	public void F2L() { /* stub */
	}

	@Override
	public void FADD() { /* stub */
	}

	@Override
	public void FALOAD(Object receiver, int index) { /* stub */
	}

	@Override
	public void FASTORE(Object receiver, int index) { /* stub */
	}

	@Override
	public void FCMPG() { /* stub */
	}

	@Override
	public void FCMPL() { /* stub */
	}

	@Override
	public void FCONST_0() { /* stub */
	}

	@Override
	public void FCONST_1() { /* stub */
	}

	@Override
	public void FCONST_2() { /* stub */
	}

	@Override
	public void FDIV(float rhs) { /* stub */
	}

	@Override
	public void FLOAD(int i) { /* stub */
	}

	@Override
	public void FMUL() { /* stub */
	}

	@Override
	public void FNEG() { /* stub */
	}

	@Override
	public void FREM(float rhs) { /* stub */
	}

	@Override
	public void FRETURN() { /* stub */
	}

	@Override
	public void FSTORE(int i) { /* stub */
	}

	@Override
	public void FSUB() { /* stub */
	}

	@Override
	public void GETFIELD(Object receiver, String owner, String name, String desc) { /* stub */
	}

	@Override
	public void GETSTATIC(String owner, String name, String desc) { /* stub */
	}

	@Override
	public void GOTO() { /* stub */
	}

	@Override
	public void GOTO_W() { /* stub */
	}

	@Override
	public void I2B() { /* stub */
	}

	@Override
	public void I2C() { /* stub */
	}

	@Override
	public void I2D() { /* stub */
	}

	@Override
	public void I2F() { /* stub */
	}

	@Override
	public void I2L() { /* stub */
	}

	@Override
	public void I2S() { /* stub */
	}

	@Override
	public void IADD() { /* stub */
	}

	@Override
	public void IALOAD(Object receiver, int index) { /* stub */
	}

	@Override
	public void IAND() { /* stub */
	}

	@Override
	public void IASTORE(Object receiver, int index) { /* stub */
	}

	@Override
	public void ICONST_0() { /* stub */
	}

	@Override
	public void ICONST_1() { /* stub */
	}

	@Override
	public void ICONST_2() { /* stub */
	}

	@Override
	public void ICONST_3() { /* stub */
	}

	@Override
	public void ICONST_4() { /* stub */
	}

	@Override
	public void ICONST_5() { /* stub */
	}

	@Override
	public void ICONST_M1() { /* stub */
	}

	@Override
	public void IDIV(int rhs) { /* stub */
	}

	@Override
	public void IFEQ(String className, String methNane, int branchIndex, int p) { /* stub */
	}

	@Override
	public void IFGE(String className, String methNane, int branchIndex, int p) { /* stub */
	}

	@Override
	public void IFGT(String className, String methNane, int branchIndex, int p) { /* stub */
	}

	@Override
	public void IFLE(String className, String methNane, int branchIndex, int p) { /* stub */
	}

	@Override
	public void IFLT(String className, String methNane, int branchIndex, int p) { /* stub */
	}

	@Override
	public void IFNE(String className, String methNane, int branchIndex, int p) { /* stub */
	}

	@Override
	public void IFNONNULL(String className, String methNane, int branchIndex,
			Object p) { /* stub */
	}

	@Override
	public void IFNULL(String className, String methNane, int branchIndex,
			Object p) { /* stub */
	}

	@Override
	public void IF_ACMPEQ(String className, String methNane, int branchIndex,
			Object left, Object right) { /* stub */
	}

	@Override
	public void IF_ACMPNE(String className, String methNane, int branchIndex,
			Object left, Object right) { /* stub */
	}

	@Override
	public void IF_ICMPEQ(String className, String methNane, int branchIndex,
			int left, int right) { /* stub */
	}

	@Override
	public void IF_ICMPGE(String className, String methNane, int branchIndex,
			int left, int right) { /* stub */
	}

	@Override
	public void IF_ICMPGT(String className, String methNane, int branchIndex,
			int left, int right) { /* stub */
	}

	@Override
	public void IF_ICMPLE(String className, String methNane, int branchIndex,
			int left, int right) { /* stub */
	}

	@Override
	public void IF_ICMPLT(String className, String methNane, int branchIndex,
			int left, int right) { /* stub */
	}

	@Override
	public void IF_ICMPNE(String className, String methNane, int branchIndex,
			int left, int right) { /* stub */
	}

	@Override
	public void IINC(int i, int value) { /* stub */
	}

	@Override
	public void ILOAD(int i) { /* stub */
	}

	@Override
	public void IMUL() { /* stub */
	}

	@Override
	public void INEG() { /* stub */
	}

	@Override
	public void INSTANCEOF(Object reference, String typeName) { /* stub */
	}

	@Override
	public void INVOKESTATIC(String owner, String name, String desc) { /* stub */
	}

	@Override
	public void INVOKESPECIAL(String owner, String name, String desc) { /* stub */
	}

	@Override
	public void INVOKESPECIAL(Object receiver, String owner, String name,
			String desc) { /* stub */
	}

	@Override
	public void INVOKEINTERFACE(Object receiver, String owner, String name,
			String desc) { /* stub */
	}

	@Override
	public void INVOKEVIRTUAL(Object receiver, String owner, String name,
			String desc) { /* stub */
	}

	@Override
	public void IOR() { /* stub */
	}

	@Override
	public void IREM(int rhs) { /* stub */
	}

	@Override
	public void IRETURN() { /* stub */
	}

	@Override
	public void ISHL() { /* stub */
	}

	@Override
	public void ISHR() { /* stub */
	}

	@Override
	public void ISTORE(int i) { /* stub */
	}

	@Override
	public void ISUB() { /* stub */
	}

	@Override
	public void IUSHR() { /* stub */
	}

	@Override
	public void IXOR() { /* stub */
	}

	@Override
	public void JSR() { /* stub */
	}

	@Override
	public void JSR_W() { /* stub */
	}

	@Override
	public void L2D() { /* stub */
	}

	@Override
	public void L2F() { /* stub */
	}

	@Override
	public void L2I() { /* stub */
	}

	@Override
	public void LADD() { /* stub */
	}

	@Override
	public void LALOAD(Object receiver, int index) { /* stub */
	}

	@Override
	public void LAND() { /* stub */
	}

	@Override
	public void LASTORE(Object receiver, int index) { /* stub */
	}

	@Override
	public void LCMP() { /* stub */
	}

	@Override
	public void LCONST_0() { /* stub */
	}

	@Override
	public void LCONST_1() { /* stub */
	}

	@Override
	public void LDC(String x) { /* stub */
	}

	@Override
	public void LDC(Class<?> x) { /* stub */
	}

	@Override
	public void LDC(int x) { /* stub */
	}

	@Override
	public void LDC(float x) { /* stub */
	}

	@Override
	public void LDC2_W(long x) { /* stub */
	}

	@Override
	public void LDC2_W(double x) { /* stub */
	}

	@Override
	public void LDIV(long rhs) { /* stub */
	}

	@Override
	public void LLOAD(int i) { /* stub */
	}

	@Override
	public void LMUL() { /* stub */
	}

	@Override
	public void LNEG() { /* stub */
	}

	@Override
	public void LOOKUPSWITCH(String className, String methName,
			int branchIndex, int target, int[] goals) { /* stub */
	}

	@Override
	public void LOR() { /* stub */
	}

	@Override
	public void LREM(long rhs) { /* stub */
	}

	@Override
	public void LRETURN() { /* stub */
	}

	@Override
	public void LSHL() { /* stub */
	}

	@Override
	public void LSHR() { /* stub */
	}

	@Override
	public void LSTORE(int i) { /* stub */
	}

	@Override
	public void LSUB() { /* stub */
	}

	@Override
	public void LUSHR() { /* stub */
	}

	@Override
	public void LXOR() { /* stub */
	}

	@Override
	public void MONITORENTER() { /* stub */
	}

	@Override
	public void MONITOREXIT() { /* stub */
	}

	@Override
	public void MULTIANEWARRAY(String arrayTypeDesc, int nrDimensions) { /* stub */
	}

	@Override
	public void NEW(String typeName) { /* stub */
	}

	@Override
	public void NEWARRAY(int length, Class<?> componentType) { /* stub */
	}

	@Override
	public void NOP() { /* stub */
	}

	@Override
	public void POP() { /* stub */
	}

	@Override
	public void POP2() { /* stub */
	}

	@Override
	public void PUTFIELD(Object receiver, String owner, String name, String desc) { /* stub */
	}

	@Override
	public void PUTSTATIC(String owner, String name, String desc) { /* stub */
	}

	@Override
	public void RET() { /* stub */
	}

	@Override
	public void RETURN() { /* stub */
	}

	@Override
	public void SALOAD(Object receiver, int index) { /* stub */
	}

	@Override
	public void SASTORE(Object receiver, int index) { /* stub */
	}

	@Override
	public void SIPUSH(int value) { /* stub */
	}

	@Override
	public void SWAP() { /* stub */
	}

	@Override
	public void TABLESWITCH(String className, String methName, int branchIndex,
			int target, int min, int max) { /* stub */
	}

	@Override
	public void UNUSED() { /* stub */
	}

	@Override
	public void WIDE() { /* stub */
	}

	/**
	 * ASM handles following by the more general XLOAD(int)
	 */
	@Override
	final public void ILOAD_0() {
		Assertions.check(false);
	} // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc6.html#iload_n

	@Override
	final public void ILOAD_1() {
		Assertions.check(false);
	}

	@Override
	final public void ILOAD_2() {
		Assertions.check(false);
	}

	@Override
	final public void ILOAD_3() {
		Assertions.check(false);
	}

	@Override
	final public void LLOAD_0() {
		Assertions.check(false);
	} // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc8.html#lload_n

	@Override
	final public void LLOAD_1() {
		Assertions.check(false);
	}

	@Override
	final public void LLOAD_2() {
		Assertions.check(false);
	}

	@Override
	final public void LLOAD_3() {
		Assertions.check(false);
	}

	@Override
	final public void FLOAD_0() {
		Assertions.check(false);
	} // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc4.html#fload_n

	@Override
	final public void FLOAD_1() {
		Assertions.check(false);
	}

	@Override
	final public void FLOAD_2() {
		Assertions.check(false);
	}

	@Override
	final public void FLOAD_3() {
		Assertions.check(false);
	}

	@Override
	final public void DLOAD_0() {
		Assertions.check(false);
	} // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc3.html#dload_n

	@Override
	final public void DLOAD_1() {
		Assertions.check(false);
	}

	@Override
	final public void DLOAD_2() {
		Assertions.check(false);
	}

	@Override
	final public void DLOAD_3() {
		Assertions.check(false);
	}

	@Override
	final public void ALOAD_0() {
		Assertions.check(false);
	} // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html#aload_n

	@Override
	final public void ALOAD_1() {
		Assertions.check(false);
	}

	@Override
	final public void ALOAD_2() {
		Assertions.check(false);
	}

	@Override
	final public void ALOAD_3() {
		Assertions.check(false);
	}

	/**
	 * ASM handles following by the more general XSTORE(int)
	 */
	@Override
	final public void ISTORE_0() {
		Assertions.check(false);
	} // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc6.html#istore_n

	@Override
	final public void ISTORE_1() {
		Assertions.check(false);
	}

	@Override
	final public void ISTORE_2() {
		Assertions.check(false);
	}

	@Override
	final public void ISTORE_3() {
		Assertions.check(false);
	}

	@Override
	final public void LSTORE_0() {
		Assertions.check(false);
	} // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc8.html#lstore_n

	@Override
	final public void LSTORE_1() {
		Assertions.check(false);
	}

	@Override
	final public void LSTORE_2() {
		Assertions.check(false);
	}

	@Override
	final public void LSTORE_3() {
		Assertions.check(false);
	}

	@Override
	final public void FSTORE_0() {
		Assertions.check(false);
	} // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc4.html#fstore_n

	@Override
	final public void FSTORE_1() {
		Assertions.check(false);
	}

	@Override
	final public void FSTORE_2() {
		Assertions.check(false);
	}

	@Override
	final public void FSTORE_3() {
		Assertions.check(false);
	}

	@Override
	final public void DSTORE_0() {
		Assertions.check(false);
	} // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc3.html#dstore_n

	@Override
	final public void DSTORE_1() {
		Assertions.check(false);
	}

	@Override
	final public void DSTORE_2() {
		Assertions.check(false);
	}

	@Override
	final public void DSTORE_3() {
		Assertions.check(false);
	}

	@Override
	final public void ASTORE_0() {
		Assertions.check(false);
	} // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html#astore_n

	@Override
	final public void ASTORE_1() {
		Assertions.check(false);
	}

	@Override
	final public void ASTORE_2() {
		Assertions.check(false);
	}

	@Override
	final public void ASTORE_3() {
		Assertions.check(false);
	}

	@Override
	final public void LDC_W() {
		Assertions.check(false,
				"Should never be called, as ASM redirects all LDC_W calls to LDC.");
	}

	// FIXME: Move this to a better place.
	static final String[] BYTECODE_NAME = new String[] { "NOP", //$NON-NLS-1$
			"ACONST_NULL", //$NON-NLS-1$
			"ICONST_M1", //$NON-NLS-1$
			"ICONST_0", //$NON-NLS-1$
			"ICONST_1", //$NON-NLS-1$
			"ICONST_2", //$NON-NLS-1$
			"ICONST_3", //$NON-NLS-1$
			"ICONST_4", //$NON-NLS-1$
			"ICONST_5", //$NON-NLS-1$
			"LCONST_0", //$NON-NLS-1$
			"LCONST_1", //$NON-NLS-1$
			"FCONST_0", //$NON-NLS-1$
			"FCONST_1", //$NON-NLS-1$
			"FCONST_2", //$NON-NLS-1$
			"DCONST_0", //$NON-NLS-1$
			"DCONST_1", //$NON-NLS-1$
			"BIPUSH", //$NON-NLS-1$
			"SIPUSH", //$NON-NLS-1$
			"LDC", //$NON-NLS-1$
			"LDC_W", //$NON-NLS-1$
			"LDC2_W", //$NON-NLS-1$
			"ILOAD", //$NON-NLS-1$
			"LLOAD", //$NON-NLS-1$
			"FLOAD", //$NON-NLS-1$
			"DLOAD", //$NON-NLS-1$
			"ALOAD", //$NON-NLS-1$
			"ILOAD_0", //$NON-NLS-1$
			"ILOAD_1", //$NON-NLS-1$
			"ILOAD_2", //$NON-NLS-1$
			"ILOAD_3", //$NON-NLS-1$
			"LLOAD_0", //$NON-NLS-1$
			"LLOAD_1", //$NON-NLS-1$
			"LLOAD_2", //$NON-NLS-1$
			"LLOAD_3", //$NON-NLS-1$
			"FLOAD_0", //$NON-NLS-1$
			"FLOAD_1", //$NON-NLS-1$
			"FLOAD_2", //$NON-NLS-1$
			"FLOAD_3", //$NON-NLS-1$
			"DLOAD_0", //$NON-NLS-1$
			"DLOAD_1", //$NON-NLS-1$
			"DLOAD_2", //$NON-NLS-1$
			"DLOAD_3", //$NON-NLS-1$
			"ALOAD_0", //$NON-NLS-1$
			"ALOAD_1", //$NON-NLS-1$
			"ALOAD_2", //$NON-NLS-1$
			"ALOAD_3", //$NON-NLS-1$
			"IALOAD", //$NON-NLS-1$
			"LALOAD", //$NON-NLS-1$
			"FALOAD", //$NON-NLS-1$
			"DALOAD", //$NON-NLS-1$
			"AALOAD", //$NON-NLS-1$
			"BALOAD", //$NON-NLS-1$
			"CALOAD", //$NON-NLS-1$
			"SALOAD", //$NON-NLS-1$
			"ISTORE", //$NON-NLS-1$
			"LSTORE", //$NON-NLS-1$
			"FSTORE", //$NON-NLS-1$
			"DSTORE", //$NON-NLS-1$
			"ASTORE", //$NON-NLS-1$
			"ISTORE_0", //$NON-NLS-1$
			"ISTORE_1", //$NON-NLS-1$
			"ISTORE_2", //$NON-NLS-1$
			"ISTORE_3", //$NON-NLS-1$
			"LSTORE_0", //$NON-NLS-1$
			"LSTORE_1", //$NON-NLS-1$
			"LSTORE_2", //$NON-NLS-1$
			"LSTORE_3", //$NON-NLS-1$
			"FSTORE_0", //$NON-NLS-1$
			"FSTORE_1", //$NON-NLS-1$
			"FSTORE_2", //$NON-NLS-1$
			"FSTORE_3", //$NON-NLS-1$
			"DSTORE_0", //$NON-NLS-1$
			"DSTORE_1", //$NON-NLS-1$
			"DSTORE_2", //$NON-NLS-1$
			"DSTORE_3", //$NON-NLS-1$
			"ASTORE_0", //$NON-NLS-1$
			"ASTORE_1", //$NON-NLS-1$
			"ASTORE_2", //$NON-NLS-1$
			"ASTORE_3", //$NON-NLS-1$
			"IASTORE", //$NON-NLS-1$
			"LASTORE", //$NON-NLS-1$
			"FASTORE", //$NON-NLS-1$
			"DASTORE", //$NON-NLS-1$
			"AASTORE", //$NON-NLS-1$
			"BASTORE", //$NON-NLS-1$
			"CASTORE", //$NON-NLS-1$
			"SASTORE", //$NON-NLS-1$
			"POP", //$NON-NLS-1$
			"POP2", //$NON-NLS-1$
			"DUP", //$NON-NLS-1$
			"DUP_X1", //$NON-NLS-1$
			"DUP_X2", //$NON-NLS-1$
			"DUP2", //$NON-NLS-1$
			"DUP2_X1", //$NON-NLS-1$
			"DUP2_X2", //$NON-NLS-1$
			"SWAP", //$NON-NLS-1$
			"IADD", //$NON-NLS-1$
			"LADD", //$NON-NLS-1$
			"FADD", //$NON-NLS-1$
			"DADD", //$NON-NLS-1$
			"ISUB", //$NON-NLS-1$
			"LSUB", //$NON-NLS-1$
			"FSUB", //$NON-NLS-1$
			"DSUB", //$NON-NLS-1$
			"IMUL", //$NON-NLS-1$
			"LMUL", //$NON-NLS-1$
			"FMUL", //$NON-NLS-1$
			"DMUL", //$NON-NLS-1$
			"IDIV", //$NON-NLS-1$
			"LDIV", //$NON-NLS-1$
			"FDIV", //$NON-NLS-1$
			"DDIV", //$NON-NLS-1$
			"IREM", //$NON-NLS-1$
			"LREM", //$NON-NLS-1$
			"FREM", //$NON-NLS-1$
			"DREM", //$NON-NLS-1$
			"INEG", //$NON-NLS-1$
			"LNEG", //$NON-NLS-1$
			"FNEG", //$NON-NLS-1$
			"DNEG", //$NON-NLS-1$
			"ISHL", //$NON-NLS-1$
			"LSHL", //$NON-NLS-1$
			"ISHR", //$NON-NLS-1$
			"LSHR", //$NON-NLS-1$
			"IUSHR", //$NON-NLS-1$
			"LUSHR", //$NON-NLS-1$
			"IAND", //$NON-NLS-1$
			"LAND", //$NON-NLS-1$
			"IOR", //$NON-NLS-1$
			"LOR", //$NON-NLS-1$
			"IXOR", //$NON-NLS-1$
			"LXOR", //$NON-NLS-1$
			"IINC", //$NON-NLS-1$
			"I2L", //$NON-NLS-1$
			"I2F", //$NON-NLS-1$
			"I2D", //$NON-NLS-1$
			"L2I", //$NON-NLS-1$
			"L2F", //$NON-NLS-1$
			"L2D", //$NON-NLS-1$
			"F2I", //$NON-NLS-1$
			"F2L", //$NON-NLS-1$
			"F2D", //$NON-NLS-1$
			"D2I", //$NON-NLS-1$
			"D2L", //$NON-NLS-1$
			"D2F", //$NON-NLS-1$
			"I2B", //$NON-NLS-1$
			"I2C", //$NON-NLS-1$
			"I2S", //$NON-NLS-1$
			"LCMP", //$NON-NLS-1$
			"FCMPL", //$NON-NLS-1$
			"FCMPG", //$NON-NLS-1$
			"DCMPL", //$NON-NLS-1$
			"DCMPG", //$NON-NLS-1$
			"IFEQ", //$NON-NLS-1$
			"IFNE", //$NON-NLS-1$
			"IFLT", //$NON-NLS-1$
			"IFGE", //$NON-NLS-1$
			"IFGT", //$NON-NLS-1$
			"IFLE", //$NON-NLS-1$
			"IF_ICMPEQ", //$NON-NLS-1$
			"IF_ICMPNE", //$NON-NLS-1$
			"IF_ICMPLT", //$NON-NLS-1$
			"IF_ICMPGE", //$NON-NLS-1$
			"IF_ICMPGT", //$NON-NLS-1$
			"IF_ICMPLE", //$NON-NLS-1$
			"IF_ACMPEQ", //$NON-NLS-1$
			"IF_ACMPNE", //$NON-NLS-1$
			"GOTO", //$NON-NLS-1$
			"JSR", //$NON-NLS-1$
			"RET", //$NON-NLS-1$
			"TABLESWITCH", //$NON-NLS-1$
			"LOOKUPSWITCH", //$NON-NLS-1$
			"IRETURN", //$NON-NLS-1$
			"LRETURN", //$NON-NLS-1$
			"FRETURN", //$NON-NLS-1$
			"DRETURN", //$NON-NLS-1$
			"ARETURN", //$NON-NLS-1$
			"RETURN", //$NON-NLS-1$
			"GETSTATIC", //$NON-NLS-1$
			"PUTSTATIC", //$NON-NLS-1$
			"GETFIELD", //$NON-NLS-1$
			"PUTFIELD", //$NON-NLS-1$
			"INVOKEVIRTUAL", //$NON-NLS-1$
			"INVOKESPECIAL", //$NON-NLS-1$
			"INVOKESTATIC", //$NON-NLS-1$
			"INVOKEINTERFACE", //$NON-NLS-1$
			"UNUSED", //$NON-NLS-1$
			"NEW", //$NON-NLS-1$
			"NEWARRAY", //$NON-NLS-1$
			"ANEWARRAY", //$NON-NLS-1$
			"ARRAYLENGTH", //$NON-NLS-1$
			"ATHROW", //$NON-NLS-1$
			"CHECKCAST", //$NON-NLS-1$
			"INSTANCEOF", //$NON-NLS-1$
			"MONITORENTER", //$NON-NLS-1$
			"MONITOREXIT", //$NON-NLS-1$
			"WIDE", //$NON-NLS-1$
			"MULTIANEWARRAY", //$NON-NLS-1$
			"IFNULL", //$NON-NLS-1$
			"IFNONNULL", //$NON-NLS-1$
			"GOTO_W", //$NON-NLS-1$
			"JSR_W" }; //$NON-NLS-1$
}
