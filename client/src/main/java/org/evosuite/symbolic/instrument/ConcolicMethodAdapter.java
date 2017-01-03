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
package org.evosuite.symbolic.instrument;

import static org.evosuite.dse.util.Assertions.check;
import static org.evosuite.dse.util.Assertions.notNull;
import static org.evosuite.symbolic.instrument.ConcolicConfig.BII_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.BYTECODE_NAME;
import static org.evosuite.symbolic.instrument.ConcolicConfig.CII_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.DGGG_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.DII_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.D_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.FGGG_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.FII_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.F_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.GGGII_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.GGG_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.GI_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.G_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.IGGG_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.IG_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.III_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.II_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.INT;
import static org.evosuite.symbolic.instrument.ConcolicConfig.INT_ARR;
import static org.evosuite.symbolic.instrument.ConcolicConfig.I_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.JGGG_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.JII_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.J_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.LGGG_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.LG_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.LII_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.LI_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.L_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.REF;
import static org.evosuite.symbolic.instrument.ConcolicConfig.SII_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.STR;
import static org.evosuite.symbolic.instrument.ConcolicConfig.VM_FQ;
import static org.evosuite.symbolic.instrument.ConcolicConfig.VOID;
import static org.evosuite.symbolic.instrument.ConcolicConfig.V_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.ZGGG_V;
import static org.evosuite.symbolic.instrument.ConcolicConfig.ZII_V;
import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARRAYLENGTH;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BALOAD;
import static org.objectweb.asm.Opcodes.BASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CALOAD;
import static org.objectweb.asm.Opcodes.CASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DALOAD;
import static org.objectweb.asm.Opcodes.DASTORE;
import static org.objectweb.asm.Opcodes.DDIV;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DREM;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.FALOAD;
import static org.objectweb.asm.Opcodes.FASTORE;
import static org.objectweb.asm.Opcodes.FDIV;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FREM;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IALOAD;
import static org.objectweb.asm.Opcodes.IASTORE;
import static org.objectweb.asm.Opcodes.IDIV;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFGE;
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.IFLE;
import static org.objectweb.asm.Opcodes.IFLT;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.IF_ACMPEQ;
import static org.objectweb.asm.Opcodes.IF_ACMPNE;
import static org.objectweb.asm.Opcodes.IF_ICMPEQ;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.IF_ICMPGT;
import static org.objectweb.asm.Opcodes.IF_ICMPLE;
import static org.objectweb.asm.Opcodes.IF_ICMPLT;
import static org.objectweb.asm.Opcodes.IF_ICMPNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IREM;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.JSR;
import static org.objectweb.asm.Opcodes.LALOAD;
import static org.objectweb.asm.Opcodes.LASTORE;
import static org.objectweb.asm.Opcodes.LDC;
import static org.objectweb.asm.Opcodes.LDIV;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LOOKUPSWITCH;
import static org.objectweb.asm.Opcodes.LREM;
import static org.objectweb.asm.Opcodes.MULTIANEWARRAY;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.SALOAD;
import static org.objectweb.asm.Opcodes.SASTORE;
import static org.objectweb.asm.Opcodes.SIPUSH;
import static org.objectweb.asm.Opcodes.TABLESWITCH;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.LabelNode;

/*
    This class is taken and adapted from the DSC tool developed by Christoph Csallner.
    Link at :
    http://ranger.uta.edu/~csallner/dsc/index.html
 */

/**
 * Main instrumentation class
 * 
 * <p>
 * Before each user ByteCode instruction, add a call to one of our static
 * methods, that reflects the particular ByteCode. In a few cases noted below,
 * we add our callback after the user ByteCode, instead of before.
 * 
 * @author csallner@uta.edu (Christoph Csallner)
 */
public final class ConcolicMethodAdapter extends GeneratorAdapter {
	private static final String THIS$0 = "this$0";
	private static final String INIT = "<init>"; //$NON-NLS-1$
	private static final String CLINIT = "<clinit>"; //$NON-NLS-1$

	private static final String METHOD_BEGIN = "METHOD_BEGIN"; //$NON-NLS-1$
	private static final String METHOD_BEGIN_PARAM = "METHOD_BEGIN_PARAM"; //$NON-NLS-1$
	private static final String METHOD_BEGIN_RECEIVER = "METHOD_BEGIN_RECEIVER"; //$NON-NLS-1$

	private static final String CALL_RESULT = "CALL_RESULT"; //$NON-NLS-1$

	private final int access;
	private final String className;
	private final String methName;
	private final String methDescription;

	private final OperandStack stack;

	/**
	 * Constructor
	 */
	ConcolicMethodAdapter(MethodVisitor mv, int access, String className, String methName,
	        String desc) {
		super(Opcodes.ASM4, mv, access, methName, desc);

		this.access = access;
		this.className = notNull(className);
		this.methName = notNull(methName);
		this.methDescription = notNull(desc);

		this.stack = new OperandStack(mv);
	}

	/**
	 * Before first ByteCode of the method/constructor
	 * 
	 * <p>
	 * Issue one call per argument, excluding the "this" receiver for
	 * non-constructor instance methods. Work left to right, starting with
	 * receiver.
	 * 
	 * <ol>
	 * <li>
	 * METHOD_BEGIN_RECEIVER(value) -- optional</li>
	 * <li>
	 * METHOD_BEGIN_PARAM(value, 0, 0)</li>
	 * <li>
	 * ...</li>
	 * <li>
	 * METHOD_BEGIN_PARAM(value, nrArgs-1, localsIndex)</li>
	 * </ol>
	 */
	@Override
	public void visitCode() {
		super.visitCode();

		/*
		 * standard access flags may also fit into signed 16-bit. That would
		 * allow using short and sipush. But Asm pseudo access flag
		 * ACC_DEPRECATED is too large.
		 */
		stack.pushInt(access);
		stack.pushStrings(className, methName, methDescription);
		mv.visitMethodInsn(INVOKESTATIC, VM_FQ, METHOD_BEGIN, IGGG_V);

		final boolean needThis = !AccessFlags.isStatic(access)
		        && !CLINIT.equals(methName);
		if (needThis && !INIT.equals(methName)) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, METHOD_BEGIN_RECEIVER, L_V);
		}

		Type[] paramTypes = Type.getArgumentTypes(methDescription); // does not
		                                                            // include
		                                                            // "this"

		/*
		 * Category-2 parameters require two slots in locals, so the index into
		 * the locals increases by two.
		 */
		int paramNr = 0;
		int calleeLocalsIndex = 0;
		if (needThis)
			calleeLocalsIndex += 1;

		for (Type type : paramTypes) {
			String dscMethParamSign = null;
			switch (type.getSort()) {
			case Type.BOOLEAN:
				mv.visitVarInsn(ILOAD, calleeLocalsIndex);
				dscMethParamSign = ZII_V;
				break;
			case Type.BYTE:
				mv.visitVarInsn(ILOAD, calleeLocalsIndex);
				dscMethParamSign = BII_V;
				break;
			case Type.CHAR:
				mv.visitVarInsn(ILOAD, calleeLocalsIndex);
				dscMethParamSign = CII_V;
				break;
			case Type.SHORT:
				mv.visitVarInsn(ILOAD, calleeLocalsIndex);
				dscMethParamSign = SII_V;
				break;
			case Type.INT:
				mv.visitVarInsn(ILOAD, calleeLocalsIndex);
				dscMethParamSign = III_V;
				break;
			case Type.LONG:
				mv.visitVarInsn(LLOAD, calleeLocalsIndex);
				dscMethParamSign = JII_V;
				break;
			case Type.DOUBLE:
				mv.visitVarInsn(DLOAD, calleeLocalsIndex);
				dscMethParamSign = DII_V;
				break;
			case Type.FLOAT:
				mv.visitVarInsn(FLOAD, calleeLocalsIndex);
				dscMethParamSign = FII_V;
				break;
			case Type.ARRAY:
			case Type.OBJECT:
				mv.visitVarInsn(ALOAD, calleeLocalsIndex);
				dscMethParamSign = LII_V;
				break;
			}

			stack.pushInt(paramNr);
			stack.pushInt(calleeLocalsIndex);
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, METHOD_BEGIN_PARAM, dscMethParamSign);

			paramNr += 1;
			calleeLocalsIndex += type.getSize();
		}
	}

	/**
	 * Insert call to our method directly before the corresponding user
	 * instruction
	 */
	@Override
	public void visitInsn(int opcode) {
		/* Divide instructions: Pass second argument (top of stack) to listeners */
		switch (opcode) {
		case IDIV:
		case IREM:
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], I_V);
			break;
		case LDIV:
		case LREM:
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], J_V);
			break;
		case FDIV:
		case FREM:
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], F_V);
			break;
		case DDIV:
		case DREM:
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], D_V);
			break;

		/*
		 * ..., arrayref, index ==> ..., value
		 */
		case IALOAD: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc6.html#iaload
		case LALOAD:
		case DALOAD: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc3.html#daload
		case FALOAD:
		case AALOAD:
		case BALOAD:
		case CALOAD:
		case SALOAD:
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], LI_V);
			break;

		/*
		 * ..., arrayref, index, value ==> ...
		 */
		case IASTORE: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc6.html#iastore
		case FASTORE:
		case AASTORE:
		case BASTORE:
		case CASTORE:
		case SASTORE:
			/* ..., arrayref, index, value */
			stack.c1b1a1__c1b1a1c1();
			/* ..., arrayref, index, value, arrayref */
			stack.c1b1a1__c1b1a1c1();
			/* ..., arrayref, index, value, arrayref, index */
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], LI_V);
			break;

		case LASTORE:
		case DASTORE:
			/* ..., arrayref, index, value */
			stack.c1b1a2__c1b1a2c1();
			/* ..., arrayref, index, value, arrayref */
			stack.c1b2a1__c1b2a1c1();
			/* ..., arrayref, index, value, arrayref, index */
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], LI_V);
			break;

		case ATHROW:
		case ARRAYLENGTH: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html#ARRAYLENGTH
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], L_V);
			break;

		default:
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], V_V);
		}

		super.visitInsn(opcode); // user code ByteCode instruction
	}

	private int branchCounter = 1;

	@Override
	public void visitJumpInsn(int opcode, Label label) {
		
		// The use of branchCounter is inlined so that branchIds match
		// what EvoSuite produces
		//
		switch (opcode) {
		case IFEQ: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc6.html#ifcond
		case IFNE:
		case IFLT:
		case IFGE:
		case IFGT:
		case IFLE:
			mv.visitInsn(DUP);
			mv.visitLdcInsn(className);
			mv.visitLdcInsn(methName);
			mv.visitLdcInsn(branchCounter++);
			String IGGI_V = "(" + INT + STR + STR + INT + ")" + VOID;
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], IGGI_V);
			break;

		case IF_ICMPEQ: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc6.html#if_icmpcond
		case IF_ICMPNE:
		case IF_ICMPLT:
		case IF_ICMPGE:
		case IF_ICMPGT:
		case IF_ICMPLE:
			mv.visitInsn(DUP2);
			mv.visitLdcInsn(className);
			mv.visitLdcInsn(methName);
			mv.visitLdcInsn(branchCounter++);
			String IIGGI_V = "(" + INT + INT + STR + STR + INT + ")" + VOID;
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], IIGGI_V);
			break;

		case IF_ACMPEQ: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc6.html#if_acmpcond
		case IF_ACMPNE:
			mv.visitInsn(DUP2);
			mv.visitLdcInsn(className);
			mv.visitLdcInsn(methName);
			mv.visitLdcInsn(branchCounter++);
			String LLGGI_V = "(" + REF + REF + STR + STR + INT + ")" + VOID;
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], LLGGI_V);
			break;

		case IFNULL: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc6.html#ifnull
		case IFNONNULL:
			mv.visitInsn(DUP);
			mv.visitLdcInsn(className);
			mv.visitLdcInsn(methName);
			mv.visitLdcInsn(branchCounter++);
			String LGGI_V = "(" + REF + STR + STR + INT + ")" + VOID;
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], LGGI_V);
			break;

		case GOTO: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc5.html#goto
		case JSR:
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], V_V);
			break;

		case 200: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc5.html#goto_w
		case 201:
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], V_V);
			break;

		default:
			check(false, BYTECODE_NAME[opcode] + " is not a jump instruction.");
		}

		/* All our code is added now to the basic block, before the jump */

		super.visitJumpInsn(opcode, label); // user's jump instruction
	}

	@Override
	public void visitLineNumber(int line, Label start) {
		stack.pushInt(line);
		mv.visitMethodInsn(INVOKESTATIC, VM_FQ, "SRC_LINE_NUMBER", I_V); //$NON-NLS-1$

		super.visitLineNumber(line, start);
	}

	/**
	 * Pseudo-instruction, inserted directly before the corresponding target
	 * instruction.
	 * 
	 * <p>
	 * Our instrumentation code does not change the shape of the instrumented
	 * method's control flow graph. So hopefully we do not need to modify any
	 * label and trust that ASM will recompute the concrete offsets correctly
	 * for us.
	 */
	@Override
	public void visitLabel(Label label) {
		super.visitLabel(label);

		if (!exceptionHandlers.contains(label)) {
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, "BB_BEGIN", V_V); //$NON-NLS-1$
			return;
		}

		/* Exception handler basic block */

		stack.pushInt(access);
		stack.pushStrings(className, methName, methDescription);
		mv.visitMethodInsn(INVOKESTATIC, VM_FQ, "HANDLER_BEGIN", IGGG_V); //$NON-NLS-1$
	}

	/**
	 * <ul>
	 * <li>
	 * BIPUSH: push one byte from instruction stream to operand stack</li>
	 * <li>
	 * SIPUSH: push two bytes from instruction stream to operand stack</li>
	 * <li>
	 * NEWARRAY</li>
	 * </ul>
	 */
	@Override
	public void visitIntInsn(int opcode, int operand) {
		switch (opcode) {
		case BIPUSH: // @see
			         // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc1.html#bipush
		case SIPUSH: // @see
			         // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc13.html#sipush
			super.visitIntInsn(opcode, operand); // user ByteCode instruction
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], I_V);
			return;

		case NEWARRAY: // @see
			           // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc10.html#newarray
			mv.visitInsn(DUP); // duplicate array length
			mv.visitIntInsn(BIPUSH, operand); // push array componenet type
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], II_V);
			super.visitIntInsn(opcode, operand); // user ByteCode instruction
			return;

		default:
			check(false);
		}
	}

	/**
	 * <ul>
	 * <li>
	 * LDC, (LDC_W) -- push category one constant from constant pool</li>
	 * <li>
	 * LDC2_W -- push category two constant from constant pool</li>
	 * </ul>
	 * 
	 * Insert call to our method after user ByteCode instruction, allows us to
	 * use the result of LDC.
	 * 
	 * @see http 
	 *      ://java.sun.com/docs/books/jvms/second_edition/html/Instructions2
	 *      .doc8.html#ldc
	 * @see http 
	 *      ://java.sun.com/docs/books/jvms/second_edition/html/Instructions2
	 *      .doc8.html#ldc2_w
	 */
	@Override
	public void visitLdcInsn(Object constant) {
		super.visitLdcInsn(constant); // Put our callback after LDC instruction,
		                              // so we can send result

		notNull(constant);

		if (constant instanceof Integer) {
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[LDC], I_V);
		} else if (constant instanceof Float) {
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[LDC], F_V);
		} else if (constant instanceof String) {
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[LDC], G_V);
		} else if (constant instanceof Type) {
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[LDC],
			                   "(Ljava/lang/Class;)V"); //$NON-NLS-1$
		} else { /* LDC2_W */
			mv.visitInsn(DUP2);

			if (constant instanceof Long)
				mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[20], J_V);
			else if (constant instanceof Double)
				mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[20], D_V);
			else
				check(false);
		}
	}

	/**
	 * ILOAD, ISTORE, ILOAD_0, ILOAD_1, etc.
	 * 
	 * <p>
	 * These may follow a WIDE instruction.
	 */
	@Override
	public void visitVarInsn(int opcode, int var) {
		check(var >= 0);

		if ((ILOAD <= opcode) && (opcode <= ALOAD) || (ISTORE <= opcode)
		        && (opcode <= ASTORE)) {
			/*
			 * Bytecode has an explicit parameter, e.g., ILOAD 33. In this case,
			 * we need to push value 33 onto the user's operand stack.
			 */
			stack.pushInt(var); // push local variable index
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], I_V);
		}

		else {
			/*
			 * Bytecode does not have an explicit parameter, the parameter is
			 * hard-coded into the ByteCode, e.g., ILOAD_2
			 */
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], V_V);
		}

		super.visitVarInsn(opcode, var); // user ByteCode instruction
	}

	/**
	 * GETFIELD, PUTFIELD, GETSTATIC, PUTSTATIC
	 * 
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc5.html#getfield
	 * http://java.sun.com/docs/books/jvms/second_edition/html
	 * /Instructions2.doc11.html#putfield
	 */
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {

		if (!name.equals(THIS$0)) {
			String signInvoke = GGG_V;
			Type fieldType = Type.getType(desc);

			/*
			 * ..., objectref ==> ..., value
			 */
			if (opcode == GETFIELD) { // top of stack == receiver instance
				mv.visitInsn(DUP);
				signInvoke = LGGG_V;
			}

			/*
			 * ..., objectref, value ==> ...
			 */
			if (opcode == PUTFIELD) { // top-1 == receiver instance
				signInvoke = LGGG_V;
				if (fieldType.getSize() == 1)
					stack.b1a1__b1a1b1();
				else if (fieldType.getSize() == 2)
					stack.b1a2__b1a2b1();
				else
					check(false);
			}

			stack.pushStrings(owner, name, desc);
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], signInvoke);

		}
		super.visitFieldInsn(opcode, owner, name, desc); // user ByteCode
		                                                 // instruction
	}

	/**
	 * Invoke a method/constructor/class initializer:
	 * <ol>
	 * <li>
	 * INVOKE_X -- signature and receiver (!) of invoked method</li>
	 * <li>
	 * CALLER_STACK_PARAM -- last parameter</li>
	 * <li>
	 * CALLER_STACK_PARAM -- next-to-last parameter</li>
	 * <li>
	 * make call</li>
	 * <li>
	 * CALL_RESULT</li>
	 * </ol>
	 * 
	 * <p>
	 * Add callback before any invocation. Besides all method calls (static,
	 * instance, private, public, native, etc.) and all constructor calls, this
	 * also works for the special case of the (implicit) super call that the
	 * Java compiler adds to the beginning of each constructor:
	 * "InvokeSpecial MySuperType <init>"
	 * 
	 * <p>
	 * Although the Java compiler does not allow any statement before this super
	 * call, we can still add a callback at the JVM level. It seems that the
	 * Java compiler (and language) is overly restrictive here.
	 * 
	 * <p>
	 * TODO: Pass receiver for more than two parameters.
	 * 
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Concepts.doc.html
	 * #16411
	 */
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		Type[] argTypes = Type.getArgumentTypes(desc); // does not include
		                                               // "this"

		String signInvoke = GGG_V;

		if (opcode == INVOKEVIRTUAL || opcode == INVOKEINTERFACE
		        || (opcode == INVOKESPECIAL && !INIT.equals(name))) {

			// pop arguments
			Type[] args = Type.getArgumentTypes(desc);
			Map<Integer, Integer> to = new HashMap<Integer, Integer>();
			for (int i = args.length - 1; i >= 0; i--) {
				int loc = newLocal(args[i]);
				storeLocal(loc);
				to.put(i, loc);
			}

			// duplicate receiver
			dup();// callee

			// push arguments
			for (int i = 0; i < args.length; i++) {
				loadLocal(to.get(i));
				Type ownerType = Type.getType(owner);
				swap(ownerType, args[i]);
			}

			/* INVOKE_X with receiver */

			// signInvoke = LGGG_V;
			signInvoke = LGGG_V;
			stack.pushStrings(owner, name, desc);
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], signInvoke);

		} else {

			/* INVOKE_X with no receiver */

			signInvoke = GGG_V;
			stack.pushStrings(owner, name, desc);
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], signInvoke);

		}

		/*
		 * CALLER_STACK_PARAM Pass non-receiver-parameters one at a time, right
		 * to left, downwards the operand stack
		 */
		final boolean needThis = opcode == INVOKEVIRTUAL || opcode == INVOKEINTERFACE
		        || opcode == INVOKESPECIAL;
		passCallerStackParams(argTypes, needThis);

		/* User's actual invoke instruction */

		super.visitMethodInsn(opcode, owner, name, desc);

		/* CALL_RESULT */

		Type returnType = Type.getReturnType(desc);
		if (returnType.getSort() == Type.VOID) {
			/* return; */// returns nothing
		}
		/* Pass returned value and method signature to our VM */
		else if (returnType.getSize() == 2)
			mv.visitInsn(DUP2);
		else
			mv.visitInsn(DUP);

		String signResult = null;
		switch (returnType.getSort()) {
		case Type.VOID:
			signResult = GGG_V;
			break;
		case Type.DOUBLE:
			signResult = DGGG_V;
			break;
		case Type.FLOAT:
			signResult = FGGG_V;
			break;
		case Type.LONG:
			signResult = JGGG_V;
			break;
		case Type.OBJECT:
			signResult = LGGG_V;
			break;
		case Type.ARRAY:
			signResult = LGGG_V;
			break;
		case Type.BOOLEAN:
			signResult = ZGGG_V;
			break;
		case Type.SHORT:
		case Type.BYTE:
		case Type.CHAR:
		case Type.INT:
			signResult = IGGG_V;
			break;
		default:
			check(false);
		}

		stack.pushStrings(owner, name, desc);
		mv.visitMethodInsn(INVOKESTATIC, VM_FQ, CALL_RESULT, signResult);
	}

	private void passCallerStackParams(Type[] argTypes, boolean needThis) {

		if (argTypes == null || argTypes.length == 0)
			return;

		// pop arguments and store them as locals
		Map<Integer, Integer> to = new HashMap<Integer, Integer>();
		for (int i = argTypes.length - 1; i >= 0; i--) {
			int loc = newLocal(argTypes[i]);
			storeLocal(loc);
			to.put(i, loc);
		}

		// restore all arguments for user invocation
		for (int i = 0; i < argTypes.length; i++) {
			loadLocal(to.get(i));
		}

		// push arguments copy and paramNr and calleLocalsIndex
		int calleeLocalsIndex = calculateCalleeLocalsIndex(argTypes, needThis);

		for (int i = argTypes.length - 1; i >= 0; i--) {
			int localVarIndex = to.get(i);
			loadLocal(localVarIndex);
			Type argType = argTypes[i];
			stack.passCallerStackParam(argType, i, calleeLocalsIndex);
			calleeLocalsIndex -= argType.getSize();
		}

	}

	private int calculateCalleeLocalsIndex(Type[] argTypes, boolean needThis) {
		int calleeLocalsIndex = 0;
		for (Type type : argTypes)
			calleeLocalsIndex += type.getSize();
		if (needThis)
			calleeLocalsIndex += 1;
		return calleeLocalsIndex;
	}

	/**
	 * IINC
	 * 
	 * <p>
	 * Increment i-th local (int) variable by constant (int) value.
	 * 
	 * <p>
	 * May follow a WIDE instruction.
	 */
	@Override
	public void visitIincInsn(int i, int value) {
		stack.pushInt(i); // push local variable index
		stack.pushInt(value); // push increment value
		mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[132], II_V);

		super.visitIincInsn(i, value); // user ByteCode instruction
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
	public void visitMultiANewArrayInsn(String arrayTypeDesc, int nrDimensions) {

		// FIXME: Also pass concrete dimension ints
		mv.visitLdcInsn(arrayTypeDesc); // push name of nrDimensions-dimensional
		                                // array type
		mv.visitIntInsn(SIPUSH, nrDimensions); // push number of dimensions
		                                       // (unsigned byte)
		mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[MULTIANEWARRAY], GI_V);

		super.visitMultiANewArrayInsn(arrayTypeDesc, nrDimensions); // user
		                                                            // ByteCode
		                                                            // instruction
	}

	/**
	 * TABLESWITCH
	 */
	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
		
		
		int currentBranchIndex = branchCounter++;

		mv.visitInsn(DUP); // pass concrete int value
		stack.pushInt(min);
		stack.pushInt(max);
		// push branch className, methName and index
		mv.visitLdcInsn(className);
		mv.visitLdcInsn(methName);
		mv.visitLdcInsn(currentBranchIndex);
		String IIIGGI_V = "(" + INT + INT + INT + STR + STR + INT + ")" + VOID;
		mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[TABLESWITCH], IIIGGI_V);

		super.visitTableSwitchInsn(min, max, dflt, labels); // user ByteCode
		                                                    // instruction
	}

	/**
	 * LOOKUPSWITCH
	 * 
	 * <p>
	 * TODO: Optimize this. Do we really need to create a new array every time
	 * we execute this switch statement?
	 */
	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {

		
		int currentBranchIndex = branchCounter++;

		mv.visitInsn(DUP); // pass concrete int value
		stack.pushInt(keys.length); // pass keys as a new array
		mv.visitIntInsn(NEWARRAY, 10);

		for (int i = 0; i < keys.length; i++) {
			mv.visitInsn(DUP);
			stack.pushInt(i);
			stack.pushInt(keys[i]);
			mv.visitInsn(IASTORE); // write the i-th case target into the new
			                       // array
		}

		// push branch className, methName and index

		mv.visitLdcInsn(className);
		mv.visitLdcInsn(methName);
		mv.visitLdcInsn(currentBranchIndex);

		String IRGGI_V = "(" + INT + INT_ARR + STR + STR + INT + ")" + VOID;

		mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[LOOKUPSWITCH], IRGGI_V);

		super.visitLookupSwitchInsn(dflt, keys, labels); // user ByteCode
		                                                 // instruction
	}

	/**
	 * CHECKCAST, INSTANCEOF, NEW, ANEWARRAY
	 */
	@Override
	public void visitTypeInsn(int opcode, String type) {

		switch (opcode) {
		case CHECKCAST:
		case INSTANCEOF:
			mv.visitInsn(DUP); // duplicate reference to be cast
			mv.visitLdcInsn(type); // pass name of type to be cast to
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], LG_V);
			break;

		case NEW:
			mv.visitLdcInsn(type);
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], G_V);
			break;

		case ANEWARRAY:
			mv.visitInsn(DUP); // duplicate array length
			mv.visitLdcInsn(type); // name of component type
			mv.visitMethodInsn(INVOKESTATIC, VM_FQ, BYTECODE_NAME[opcode], IG_V);
			break;

		default:
			check(false);
		}

		super.visitTypeInsn(opcode, type); // user ByteCode instruction
	}

	private final Set<Label> exceptionHandlers = new HashSet<Label>();

	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		exceptionHandlers.add(handler);
		super.visitTryCatchBlock(start, end, handler, type);
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		stack.pushStrings(className, methName, methDescription);
		stack.pushInt(maxStack);
		stack.pushInt(maxLocals);
		mv.visitMethodInsn(INVOKESTATIC, VM_FQ, "METHOD_MAXS", GGGII_V); //$NON-NLS-1$
		super.visitMaxs(maxStack, maxLocals);
	}
}
