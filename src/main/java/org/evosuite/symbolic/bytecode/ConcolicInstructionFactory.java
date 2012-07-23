
/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Gordon Fraser
 */
package org.evosuite.symbolic.bytecode;

import gov.nasa.jpf.jvm.bytecode.InstructionFactory;
public class ConcolicInstructionFactory extends InstructionFactory implements Cloneable {

	/**
	 * <p>Constructor for ConcolicInstructionFactory.</p>
	 */
	public ConcolicInstructionFactory() {
		PathConstraint.init();
	}

	/** {@inheritDoc} */
	@Override
	public Object clone() {
		//yes we do not clone
		return this;
	}

//	@Override
//	public INVOKESPECIAL invokespecial(String clsDescriptor, 
//			String methodName, String signature){
//		return new INVOKESPECIAL(clsDescriptor, methodName, signature);
//	}
	
	/** {@inheritDoc} */
	@Override
	public INVOKEVIRTUAL invokevirtual(String clsName, String methodName,
			String methodSignature){
		return new INVOKEVIRTUAL(clsName, methodName, methodSignature);
	}
	
	/** {@inheritDoc} */
	@Override
	public D2F d2f() {
		return new D2F();
	}

	/** {@inheritDoc} */
	@Override
	public D2I d2i() {
		return new D2I();
	}

	/** {@inheritDoc} */
	@Override
	public D2L d2l() {
		return new D2L();
	}

	/** {@inheritDoc} */
	@Override
	public DADD dadd() {
		return new DADD();
	}

	/** {@inheritDoc} */
	@Override
	public DCMPG dcmpg() {
		return new DCMPG();
	}

	/** {@inheritDoc} */
	@Override
	public DCMPL dcmpl() {
		return new DCMPL();
	}

	/** {@inheritDoc} */
	@Override
	public DDIV ddiv() {
		return new DDIV();
	}

	/** {@inheritDoc} */
	@Override
	public DMUL dmul() {
		return new DMUL();
	}

	/** {@inheritDoc} */
	@Override
	public DNEG dneg() {
		return new DNEG();
	}

	/** {@inheritDoc} */
	@Override
	public DREM drem() {
		return new DREM();
	}

	/** {@inheritDoc} */
	@Override
	public DSUB dsub() {
		return new DSUB();
	}

	/** {@inheritDoc} */
	@Override
	public F2D f2d() {
		return new F2D();
	}

	/** {@inheritDoc} */
	@Override
	public F2I f2i() {
		return new F2I();
	}

	/** {@inheritDoc} */
	@Override
	public F2L f2l() {
		return new F2L();
	}

	/** {@inheritDoc} */
	@Override
	public FADD fadd() {
		return new FADD();
	}

	/** {@inheritDoc} */
	@Override
	public FCMPG fcmpg() {
		return new FCMPG();
	}

	/** {@inheritDoc} */
	@Override
	public FCMPL fcmpl() {
		return new FCMPL();
	}

	/** {@inheritDoc} */
	@Override
	public FDIV fdiv() {
		return new FDIV();
	}

	/** {@inheritDoc} */
	@Override
	public FMUL fmul() {
		return new FMUL();
	}

	/** {@inheritDoc} */
	@Override
	public FNEG fneg() {
		return new FNEG();
	}

	/** {@inheritDoc} */
	@Override
	public FREM frem() {
		return new FREM();
	}

	/** {@inheritDoc} */
	@Override
	public FSUB fsub() {
		return new FSUB();
	}

	/** {@inheritDoc} */
	@Override
	public I2B i2b() {
		return new I2B();
	}

	/** {@inheritDoc} */
	@Override
	public I2C i2c() {
		return new I2C();
	}

	/** {@inheritDoc} */
	@Override
	public I2D i2d() {
		return new I2D();
	}

	/** {@inheritDoc} */
	@Override
	public I2F i2f() {
		return new I2F();
	}

	/** {@inheritDoc} */
	@Override
	public I2L i2l() {
		return new I2L();
	}

	/** {@inheritDoc} */
	@Override
	public I2S i2s() {
		return new I2S();
	}

	/** {@inheritDoc} */
	@Override
	public IADD iadd() {
		return new IADD();
	}

	/** {@inheritDoc} */
	@Override
	public IDIV idiv() {
		return new IDIV();
	}

	/** {@inheritDoc} */
	@Override
	public IF_ICMPEQ if_icmpeq(int targetPc) {
		return new IF_ICMPEQ(targetPc);
	}

	/** {@inheritDoc} */
	@Override
	public IF_ICMPNE if_icmpne(int targetPc) {
		return new IF_ICMPNE(targetPc);
	}

	/** {@inheritDoc} */
	@Override
	public IF_ICMPLT if_icmplt(int targetPc) {
		return new IF_ICMPLT(targetPc);
	}

	/** {@inheritDoc} */
	@Override
	public IF_ICMPGE if_icmpge(int targetPc) {
		return new IF_ICMPGE(targetPc);
	}

	/** {@inheritDoc} */
	@Override
	public IF_ICMPGT if_icmpgt(int targetPc) {
		return new IF_ICMPGT(targetPc);
	}

	/** {@inheritDoc} */
	@Override
	public IF_ICMPLE if_icmple(int targetPc) {
		return new IF_ICMPLE(targetPc);
	}

	/** {@inheritDoc} */
	@Override
	public IFEQ ifeq(int targetPc) {
		return new IFEQ(targetPc);
	}

	/** {@inheritDoc} */
	@Override
	public IFNE ifne(int targetPc) {
		return new IFNE(targetPc);
	}

	/** {@inheritDoc} */
	@Override
	public IFLT iflt(int targetPc) {
		return new IFLT(targetPc);
	}

	/** {@inheritDoc} */
	@Override
	public IFGE ifge(int targetPc) {
		return new IFGE(targetPc);
	}

	/** {@inheritDoc} */
	@Override
	public IFGT ifgt(int targetPc) {
		return new IFGT(targetPc);
	}

	/** {@inheritDoc} */
	@Override
	public IFLE ifle(int targetPc) {
		return new IFLE(targetPc);
	}

	/** {@inheritDoc} */
	@Override
	public IINC iinc(int localVarIndex, int incConstant) {
		return new IINC(localVarIndex, incConstant);
	}

	/** {@inheritDoc} */
	@Override
	public IMUL imul() {
		return new IMUL();
	}

	/** {@inheritDoc} */
	@Override
	public INEG ineg() {
		return new INEG();
	}

	/** {@inheritDoc} */
	@Override
	public IOR ior() {
		return new IOR();
	}

	/** {@inheritDoc} */
	@Override
	public IREM irem() {
		return new IREM();
	}

	/** {@inheritDoc} */
	@Override
	public ISHL ishl() {
		return new ISHL();
	}

	/** {@inheritDoc} */
	@Override
	public ISHR ishr() {
		return new ISHR();
	}

	/** {@inheritDoc} */
	@Override
	public ISUB isub() {
		return new ISUB();
	}

	/** {@inheritDoc} */
	@Override
	public IXOR ixor() {
		return new IXOR();
	}

	/** {@inheritDoc} */
	@Override
	public L2D l2d() {
		return new L2D();
	}

	/** {@inheritDoc} */
	@Override
	public L2F l2f() {
		return new L2F();
	}

	/** {@inheritDoc} */
	@Override
	public L2I l2i() {
		return new L2I();
	}

	/** {@inheritDoc} */
	@Override
	public LADD ladd() {
		return new LADD();
	}

	/** {@inheritDoc} */
	@Override
	public LAND land() {
		return new LAND();
	}

	/** {@inheritDoc} */
	@Override
	public LCMP lcmp() {
		return new LCMP();
	}

	/** {@inheritDoc} */
	@Override
	public LDIV ldiv() {
		return new LDIV();
	}

	/** {@inheritDoc} */
	@Override
	public LMUL lmul() {
		return new LMUL();
	}

	/** {@inheritDoc} */
	@Override
	public LNEG lneg() {
		return new LNEG();
	}

	/** {@inheritDoc} */
	@Override
	public LOOKUPSWITCH lookupswitch(int defaultTargetPc, int nEntries) {
		return new LOOKUPSWITCH(defaultTargetPc, nEntries);
	}

	/** {@inheritDoc} */
	@Override
	public LOR lor() {
		return new LOR();
	}

	/** {@inheritDoc} */
	@Override
	public LREM lrem() {
		return new LREM();
	}

	/** {@inheritDoc} */
	@Override
	public LSHL lshl() {
		return new LSHL();
	}

	/** {@inheritDoc} */
	@Override
	public LSHR lshr() {
		return new LSHR();
	}

	/** {@inheritDoc} */
	@Override
	public LSUB lsub() {
		return new LSUB();
	}

	/** {@inheritDoc} */
	@Override
	public LXOR lxor() {
		return new LXOR();
	}

	/** {@inheritDoc} */
	@Override
	public TABLESWITCH tableswitch(int defaultTargetPc, int low, int high) {
		return new TABLESWITCH(defaultTargetPc, low, high);
	}

	//--- the JPF specific ones (only used in synthetic methods)

}
