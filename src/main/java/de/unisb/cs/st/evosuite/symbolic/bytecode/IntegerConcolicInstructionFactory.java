package de.unisb.cs.st.evosuite.symbolic.bytecode;

import gov.nasa.jpf.jvm.bytecode.InstructionFactory;

public class IntegerConcolicInstructionFactory extends InstructionFactory implements
        Cloneable {

	public IntegerConcolicInstructionFactory() {
		PathConstraint.init();
	}

	@Override
	public Object clone() {
		//yes we do not clone
		return this;
	}

	/*
	@Override
	public ASTORE astore(int index) {
		return new ASTORE(index);
	}
	
	@Override
	public ALOAD aload(int index) {
		return new ALOAD(index);
	}
	
	@Override
	public LDC ldc(String s, boolean isClass) {
		return new LDC(s, isClass);
	}
	*/
	@Override
	public INVOKEVIRTUAL invokevirtual(String clsName, String methodName,
	        String methodSignature) {
		return new INVOKEVIRTUAL(clsName, methodName, methodSignature);
	}

	@Override
	public I2B i2b() {
		return new I2B();
	}

	@Override
	public I2C i2c() {
		return new I2C();
	}

	@Override
	public I2L i2l() {
		return new I2L();
	}

	@Override
	public I2S i2s() {
		return new I2S();
	}

	@Override
	public IADD iadd() {
		return new IADD();
	}

	@Override
	public IDIV idiv() {
		return new IDIV();
	}

	@Override
	public IF_ICMPEQ if_icmpeq(int targetPc) {
		return new IF_ICMPEQ(targetPc);
	}

	@Override
	public IF_ICMPNE if_icmpne(int targetPc) {
		return new IF_ICMPNE(targetPc);
	}

	@Override
	public IF_ICMPLT if_icmplt(int targetPc) {
		return new IF_ICMPLT(targetPc);
	}

	@Override
	public IF_ICMPGE if_icmpge(int targetPc) {
		return new IF_ICMPGE(targetPc);
	}

	@Override
	public IF_ICMPGT if_icmpgt(int targetPc) {
		return new IF_ICMPGT(targetPc);
	}

	@Override
	public IF_ICMPLE if_icmple(int targetPc) {
		return new IF_ICMPLE(targetPc);
	}

	@Override
	public IFEQ ifeq(int targetPc) {
		return new IFEQ(targetPc);
	}

	@Override
	public IFNE ifne(int targetPc) {
		return new IFNE(targetPc);
	}

	@Override
	public IFLT iflt(int targetPc) {
		return new IFLT(targetPc);
	}

	@Override
	public IFGE ifge(int targetPc) {
		return new IFGE(targetPc);
	}

	@Override
	public IFGT ifgt(int targetPc) {
		return new IFGT(targetPc);
	}

	@Override
	public IFLE ifle(int targetPc) {
		return new IFLE(targetPc);
	}

	@Override
	public IINC iinc(int localVarIndex, int incConstant) {
		return new IINC(localVarIndex, incConstant);
	}

	@Override
	public IMUL imul() {
		return new IMUL();
	}

	@Override
	public INEG ineg() {
		return new INEG();
	}

	@Override
	public IOR ior() {
		return new IOR();
	}

	@Override
	public IREM irem() {
		return new IREM();
	}

	@Override
	public ISHL ishl() {
		return new ISHL();
	}

	@Override
	public ISHR ishr() {
		return new ISHR();
	}

	@Override
	public ISUB isub() {
		return new ISUB();
	}

	@Override
	public IXOR ixor() {
		return new IXOR();
	}

	@Override
	public L2D l2d() {
		return new L2D();
	}

	@Override
	public L2F l2f() {
		return new L2F();
	}

	@Override
	public L2I l2i() {
		return new L2I();
	}

	@Override
	public LADD ladd() {
		return new LADD();
	}

	@Override
	public LAND land() {
		return new LAND();
	}

	@Override
	public LCMP lcmp() {
		return new LCMP();
	}

	@Override
	public LDIV ldiv() {
		return new LDIV();
	}

	@Override
	public LMUL lmul() {
		return new LMUL();
	}

	@Override
	public LNEG lneg() {
		return new LNEG();
	}

	@Override
	public LOOKUPSWITCH lookupswitch(int defaultTargetPc, int nEntries) {
		return new LOOKUPSWITCH(defaultTargetPc, nEntries);
	}

	@Override
	public LOR lor() {
		return new LOR();
	}

	@Override
	public LREM lrem() {
		return new LREM();
	}

	@Override
	public LSHL lshl() {
		return new LSHL();
	}

	@Override
	public LSHR lshr() {
		return new LSHR();
	}

	@Override
	public LSUB lsub() {
		return new LSUB();
	}

	@Override
	public LXOR lxor() {
		return new LXOR();
	}

	@Override
	public TABLESWITCH tableswitch(int defaultTargetPc, int low, int high) {
		return new TABLESWITCH(defaultTargetPc, low, high);
	}

	//--- the JPF specific ones (only used in synthetic methods)

}
