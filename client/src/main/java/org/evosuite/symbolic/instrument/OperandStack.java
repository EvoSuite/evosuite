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
import static org.evosuite.symbolic.instrument.ConcolicConfig.VM_FQ;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.DUP2_X1;
import static org.objectweb.asm.Opcodes.DUP2_X2;
import static org.objectweb.asm.Opcodes.DUP_X1;
import static org.objectweb.asm.Opcodes.DUP_X2;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.ICONST_5;
import static org.objectweb.asm.Opcodes.ICONST_M1;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;
import static org.objectweb.asm.Opcodes.SIPUSH;
import static org.objectweb.asm.Opcodes.SWAP;
import static org.objectweb.asm.Type.INT_TYPE;
import static org.objectweb.asm.Type.VOID_TYPE;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/*
    This class is taken and adapted from the DSC tool developed by Christoph Csallner.
    Link at :
    http://ranger.uta.edu/~csallner/dsc/index.html
 */

/**
 * Helper class
 * 
 * Methods to generate bytecodes that mutate the
 * operand stack
 * 
 * TODO: Handle all cases via local variables, via LocalVariablesSorter:
 * http://asm.ow2.org/asm30/javadoc/user/org/objectweb/asm/commons/LocalVariablesSorter.html
 * 
 * @author csallner@uta.edu (Christoph Csallner)
 */
final class OperandStack {

	private final MethodVisitor mv;
	private static final String CALLER_STACK_PARAM = "CALLER_STACK_PARAM"; //$NON-NLS-1$
	private static final Type 	TYPE_OBJECT = Type.getObjectType("java/lang/Object"); //$NON-NLS-1$
	
	OperandStack(MethodVisitor mv) {
		this.mv = mv;	
	}

	private String desc(Type returnType, Type... argumentTypes) {
		StringBuffer desc = new StringBuffer();
		desc.append("("); //$NON-NLS-1$
		for (Type type: argumentTypes)
			desc.append(type.getDescriptor());
		desc.append(")"); //$NON-NLS-1$
		desc.append(returnType.getDescriptor());
		return desc.toString();
	}

	/**
	 * Push i onto our operand stack
	 */
	void pushInt(int i) {
		switch (i) {
		case -1: 
			mv.visitInsn(ICONST_M1);
			return;
		case 0: 
			mv.visitInsn(ICONST_0);
			return;
		case 1: 
			mv.visitInsn(ICONST_1);
			return;
		case 2: 
			mv.visitInsn(ICONST_2);
			return;
		case 3: 
			mv.visitInsn(ICONST_3);
			return;
		case 4: 
			mv.visitInsn(ICONST_4);
			return;
		case 5: 
			mv.visitInsn(ICONST_5);
			return;
		}
		if (i>=Byte.MIN_VALUE && i<=Byte.MAX_VALUE) {
			mv.visitIntInsn(BIPUSH, i);
			return;
		}
		if (i>=Short.MIN_VALUE && i<=Short.MAX_VALUE) {
			mv.visitIntInsn(SIPUSH, i);
			return;
		}
		mv.visitLdcInsn(Integer.valueOf(i));
	}
	
	
	/**
	 * ...
	 * ==>
	 * ..., a, b, c
	 */
	void pushStrings(String a, String b, String c) {
		mv.visitLdcInsn(a);
		mv.visitLdcInsn(b);
		mv.visitLdcInsn(c);
	}

	/** 
	 * ..., b1, a1
	 * ==>
	 * ..., b1, a1, b1
	 */
	void b1a1__b1a1b1() {
		/* ..., b1, a1*/
		mv.visitInsn(SWAP);
		/* ..., a1, b1*/
		mv.visitInsn(DUP_X1);
		/* ..., b1, a1, b1*/
	}

	/**
	 * ..., b1, a2 
	 * ==> 
	 * ..., b1, a2, b1
	 */
	void b1a2__b1a2b1() {
		/* ..., b1, a2 */
		mv.visitInsn(DUP2_X1);
		/* ..., a2, b1, a2 */
		mv.visitInsn(POP2);
		/* ..., a2, b1 */
		mv.visitInsn(DUP_X2);
		/* ..., b1, a2, b1 */
	}

	/** 
	 * ..., b2, a1
	 * ==>
	 * ..., b2, a1, b2
	 */
	void b2a1__b2a1b2() {
		/* ..., b2, a1 */
		mv.visitInsn(DUP_X2);
		/* ..., a1, b2, a1 */
		mv.visitInsn(POP);
		/* ..., a1, b2 */
		mv.visitInsn(DUP2_X1);
		/* ..., b2, a1, b2 */
	}

	/** 
	 * ..., b2, a2
	 * ==>
	 * ..., b2, a2, b2
	 */
	void b2a2__b2a2b2() {
		/* ..., b2, a2 */
		mv.visitInsn(DUP2_X2);
		/* ..., a2, b2, a2 */
		mv.visitInsn(POP2);
		/* ..., a2, b2 */
		mv.visitInsn(DUP2_X2);
		/* ..., b2, a2, b2 */
	}

	/**
	 * Transform top of operand stack, assuming each operand is of category-1:
	 * 
	 * ..., c1, b1, a1
	 * ==>
	 * ..., c1, b1, a1, c1
	 */
	void c1b1a1__c1b1a1c1() {
		/* ..., 2, 1, 0*/
		mv.visitInsn(DUP_X2);
		/* ..., 0, 2, 1, 0*/
		mv.visitInsn(POP);
		/* ..., 0, 2, 1 */
		mv.visitInsn(DUP_X2);
		/* ..., 1, 0, 2, 1 */
		mv.visitInsn(POP);
		/* ..., 1, 0, 2 */
		mv.visitInsn(DUP_X2);
		/* ..., 2, 1, 0, 2 */
	}

	/**
	 * ..., c1, b1, a2
	 * ==>
	 * ..., c1, b1, a2, c1
	 */
	void c1b1a2__c1b1a2c1() {
		/* ..., c1, b1, a2 */
		mv.visitInsn(DUP2_X2);
		/* ..., a2, c1, b1, a2 */
		mv.visitInsn(POP2);
		/* ..., a2, c1, b1 */
		mv.visitInsn(DUP2_X2);
		/* ..., c1, b1, a2, c1, b1 */
		mv.visitInsn(POP);
		/* ..., c1, b1, a2, c1 */
	}

	/**
	 * ..., c1, b2, a1
	 * ==>
	 * ..., c1, b2, a1, c1
	 */
	void c1b2a1__c1b2a1c1() {
		/* ..., c1, b2, a1 */
		mv.visitInsn(DUP_X2);						// Form 2
		/* ..., c1, a1, b2, a1 */
		mv.visitInsn(POP);
		/* ..., c1, a1, b2 */
		mv.visitInsn(DUP2_X2);					// Form 2
		/* ..., b2, c1, a1, b2 */
		mv.visitInsn(POP2);
		/* ..., b2, c1, a1 */
		mv.visitInsn(DUP2_X2);					// Form 3
		/* ..., c1, a1, b2, c1, a1 */
		mv.visitInsn(POP);
		/* ..., c1, a1, b2, c1 */
		mv.visitInsn(DUP_X2);						// Form 2
		/* ..., c1, a1, c1, b2, c1 */
		mv.visitInsn(POP);
		/* ..., c1, a1, c1, b2 */
		mv.visitInsn(DUP2_X2);					// Form 2
		/* ..., c1, b2, a1, c1, b2 */
		mv.visitInsn(POP2);
		/* ..., c1, b2, a1, c1 */
	}

	/**
	 * Pass a single parameter
	 */
	void passCallerStackParam(Type argType, int paramNr, int calleeLocalsIndex) {
		/* Replace concrete complex type by java.lang.Object */
		Type type = argType;
		if (type.getSort()==Type.OBJECT || type.getSort()==Type.ARRAY)
			type = TYPE_OBJECT;
	
		pushInt(paramNr);
		pushInt(calleeLocalsIndex);
		String signature = desc(VOID_TYPE, type, INT_TYPE, INT_TYPE);
		mv.visitMethodInsn(INVOKESTATIC, VM_FQ, CALLER_STACK_PARAM, signature);
	}

	/**
	 * Pass non-receiver-parameters one at a time, 
	 * right to left, downwards the operand stack
	 */
	void passCallerStackParams(Type[] argTypes, boolean needThis) {
		if (argTypes==null || argTypes.length==0)
			return;
		
		int calleeLocalsIndex = 0;
		for (Type type: argTypes)
			calleeLocalsIndex += type.getSize();
		if (needThis)
			calleeLocalsIndex += 1;
		
		int paramNr = argTypes.length - 1;	// work right to left
		
		
		
		
		if (argTypes[paramNr].getSize()==1) {
			calleeLocalsIndex -= 1;
			mv.visitInsn(DUP);
			passCallerStackParam(argTypes[paramNr], paramNr, calleeLocalsIndex);
			if (argTypes.length==1)
				return;
			paramNr -= 1;
			
			if (argTypes[paramNr].getSize()==1) {
				calleeLocalsIndex -= 1;
				b1a1__b1a1b1();
				passCallerStackParam(argTypes[paramNr], paramNr, calleeLocalsIndex);
			}
			else if (argTypes[paramNr].getSize()==2) {
				calleeLocalsIndex -= 2;
				b2a1__b2a1b2();
				passCallerStackParam(argTypes[paramNr], paramNr, calleeLocalsIndex);
			}
			else
				check(false);  		
		}
		else if (argTypes[paramNr].getSize()==2) {
			calleeLocalsIndex -= 2;
			mv.visitInsn(DUP2);
			passCallerStackParam(argTypes[paramNr], paramNr, calleeLocalsIndex);
			if (argTypes.length==1)
				return;
			paramNr -= 1;
			if (argTypes[paramNr].getSize()==1) {
				calleeLocalsIndex -= 1;
				b1a2__b1a2b1();
				passCallerStackParam(argTypes[paramNr], paramNr, calleeLocalsIndex);
			}
			else if (argTypes[paramNr].getSize()==2) {
				calleeLocalsIndex -= 2;
				b2a2__b2a2b2();
				passCallerStackParam(argTypes[paramNr], paramNr, calleeLocalsIndex);
			}
			else
				check(false);
		}
		else
			check(false);
	}
}
