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
package org.evosuite.seeding;

import org.evosuite.setup.DependencyAnalysis;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * <p>
 * PrimitivePoolMethodAdapter class.
 * </p>
 *
 * @author Gordon Fraser
 */
public class PrimitivePoolMethodAdapter extends MethodVisitor {

    // private final PrimitivePool constantPool = PrimitivePool.getInstance();

    private final ConstantPoolManager poolManager = ConstantPoolManager.getInstance();

    private final String className;

    /**
     * <p>
     * Constructor for PrimitivePoolMethodAdapter.
     * </p>
     *
     * @param mv a {@link org.objectweb.asm.MethodVisitor} object.
     */
    public PrimitivePoolMethodAdapter(MethodVisitor mv, String className) {
        super(Opcodes.ASM9, mv);
        this.className = className;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This is a hack to avoid deadlocks because we are only testing single
     * threaded stuff. This will be replaced with something nicer once Sebastian
     * has found a solution
     */
	/*
	@Override
	public void visitInsn(int opcode) {
		if (opcode != Opcodes.MONITORENTER && opcode != Opcodes.MONITOREXIT)
			super.visitInsn(opcode);
		else
			super.visitInsn(Opcodes.POP);

	}
	*/

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodAdapter#visitLookupSwitchInsn(org.objectweb.asm.Label, int[], org.objectweb.asm.Label[])
     */
    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        for (int key : keys) {
            // constantPool.add(key);
            if (DependencyAnalysis.isTargetClassName(className)) {
                poolManager.addSUTConstant(key);
            } else {
                poolManager.addNonSUTConstant(key);
            }
        }
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitIntInsn(int opcode, int operand) {
        if (opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH) {
            // constantPool.add(operand);
            if (DependencyAnalysis.isTargetClassName(className)) {
                poolManager.addSUTConstant(operand);
            } else {
                poolManager.addNonSUTConstant(operand);
            }
        }
        super.visitIntInsn(opcode, operand);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitLdcInsn(Object cst) {
        // constantPool.add(cst);
        if (DependencyAnalysis.isTargetClassName(className)) {
            poolManager.addSUTConstant(cst);
        } else {
            poolManager.addNonSUTConstant(cst);
        }
        super.visitLdcInsn(cst);
    }

    @Override
    public void visitInsn(int opcode) {
        Object constant = null;
        switch (opcode) {
            case Opcodes.ICONST_0:
                constant = 0;
                break;
            case Opcodes.ICONST_1:
                constant = 1;
                break;
            case Opcodes.ICONST_2:
                constant = 2;
                break;
            case Opcodes.ICONST_3:
                constant = 3;
                break;
            case Opcodes.ICONST_4:
                constant = 4;
                break;
            case Opcodes.ICONST_5:
                constant = 5;
                break;
            case Opcodes.ICONST_M1:
                constant = -1;
                break;
            case Opcodes.LCONST_0:
                constant = 0L;
                break;
            case Opcodes.LCONST_1:
                constant = 1L;
                break;
            case Opcodes.DCONST_0:
                constant = 0.0;
                break;
            case Opcodes.DCONST_1:
                constant = 1.0;
                break;
            case Opcodes.FCONST_0:
                constant = 0f;
                break;
            case Opcodes.FCONST_1:
                constant = 1f;
                break;
            case Opcodes.FCONST_2:
                constant = 2f;
                break;
        }
        if (constant != null) {
            if (DependencyAnalysis.isTargetClassName(className)) {
                poolManager.addSUTConstant(constant);
            } else {
                poolManager.addNonSUTConstant(constant);
            }
        }
        super.visitInsn(opcode);
    }
}
