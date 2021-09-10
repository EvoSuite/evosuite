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

package org.evosuite.instrumentation.error;

import org.evosuite.Properties;
import org.evosuite.runtime.instrumentation.AnnotatedLabel;
import org.evosuite.runtime.instrumentation.AnnotatedMethodNode;
import org.evosuite.utils.ArrayUtil;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * ErrorConditionMethodAdapter class.
 * </p>
 *
 * @author Gordon Fraser
 */
public class ErrorConditionMethodAdapter extends GeneratorAdapter {

    protected final static Logger logger = LoggerFactory.getLogger(ErrorConditionMethodAdapter.class);

    private final String className;

    private final String methodName;

    private final MethodVisitor next;

    protected List<ErrorBranchInstrumenter> instrumentation;


    /**
     * <p>
     * Constructor for ErrorConditionMethodAdapter.
     * </p>
     *
     * @param mv         a {@link org.objectweb.asm.MethodVisitor} object.
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     * @param access     a int.
     * @param desc       a {@link java.lang.String} object.
     */
    public ErrorConditionMethodAdapter(MethodVisitor mv, String className,
                                       String methodName, int access, String desc) {
        //super(Opcodes.ASM9, mv, access, methodName, desc);
        super(Opcodes.ASM9,
                new AnnotatedMethodNode(access, methodName, desc, null, null), access,
                methodName, desc);
        this.className = className;
        this.methodName = methodName;
        next = mv;
        initErrorBranchInstrumenters();
    }

    private void initErrorBranchInstrumenters() {
        instrumentation = new ArrayList<>();
        if (ArrayUtil.contains(Properties.ERROR_INSTRUMENTATION, Properties.ErrorInstrumentation.ARRAY))
            instrumentation.add(new ArrayInstrumentation(this));
        if (ArrayUtil.contains(Properties.ERROR_INSTRUMENTATION, Properties.ErrorInstrumentation.LIST))
            instrumentation.add(new ListInstrumentation(this));
		/*if(ArrayUtil.contains(Properties.ERROR_INSTRUMENTATION, Properties.ErrorInstrumentation.ARRAYLIST))
			instrumentation.add(new ArrayListInstrumentation(this));*/
        if (ArrayUtil.contains(Properties.ERROR_INSTRUMENTATION, Properties.ErrorInstrumentation.CAST))
            instrumentation.add(new CastErrorInstrumentation(this));
        if (ArrayUtil.contains(Properties.ERROR_INSTRUMENTATION, Properties.ErrorInstrumentation.DEQUE))
            instrumentation.add(new DequeInstrumentation(this));
        if (ArrayUtil.contains(Properties.ERROR_INSTRUMENTATION, Properties.ErrorInstrumentation.DIVISIONBYZERO))
            instrumentation.add(new DivisionByZeroInstrumentation(this));
        if (ArrayUtil.contains(Properties.ERROR_INSTRUMENTATION, Properties.ErrorInstrumentation.LINKEDHASHSET))
            instrumentation.add(new LinkedHashSetInstrumentation(this));
        // instrumentation.add(new ListInstrumentation(this));
		/*if(ArrayUtil.contains(Properties.ERROR_INSTRUMENTATION, Properties.ErrorInstrumentation.LINKEDLIST))
			instrumentation.add(new LinkedListInstrumentation(this));*/
        if (ArrayUtil.contains(Properties.ERROR_INSTRUMENTATION, Properties.ErrorInstrumentation.NPE))
            instrumentation.add(new NullPointerExceptionInstrumentation(this));
        if (ArrayUtil.contains(Properties.ERROR_INSTRUMENTATION, Properties.ErrorInstrumentation.OVERFLOW))
            instrumentation.add(new OverflowInstrumentation(this));
        if (ArrayUtil.contains(Properties.ERROR_INSTRUMENTATION, Properties.ErrorInstrumentation.QUEUE))
            instrumentation.add(new QueueInstrumentation(this));
        if (ArrayUtil.contains(Properties.ERROR_INSTRUMENTATION, Properties.ErrorInstrumentation.STACK))
            instrumentation.add(new StackInstrumentation(this));
        if (ArrayUtil.contains(Properties.ERROR_INSTRUMENTATION, Properties.ErrorInstrumentation.VECTOR))
            instrumentation.add(new VectorInstrumentation(this));
    }


    protected boolean inInstrumentation = false;

    @Override
    public void visitLabel(Label label) {
        if (label instanceof AnnotatedLabel) {
            AnnotatedLabel aLabel = (AnnotatedLabel) label;
            inInstrumentation = aLabel.isStartTag();
        }
        super.visitLabel(label);
    }

    public void tagBranch() {
        Label dummyTag = new AnnotatedLabel(false, true);
        // dummyTag.info = Boolean.TRUE;
        super.visitLabel(dummyTag);
    }

    public void tagBranchExit() {
        Label dummyTag = new AnnotatedLabel(false, false);
        // dummyTag.info = Boolean.FALSE;
        super.visitLabel(dummyTag);
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitMethodInsn(int, java.lang.String, java.lang.String, java.lang.String)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (!inInstrumentation) {
            inInstrumentation = true;
            for (ErrorBranchInstrumenter instrumenter : instrumentation) {
                instrumenter.visitMethodInsn(opcode, owner, name, desc, itf);
            }
            inInstrumentation = false;
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitFieldInsn(int, java.lang.String, java.lang.String, java.lang.String)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (!inInstrumentation) {
            inInstrumentation = true;
            for (ErrorBranchInstrumenter instrumenter : instrumentation) {
                instrumenter.visitFieldInsn(opcode, owner, name, desc);
            }
            inInstrumentation = false;
        }
        super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        if (!inInstrumentation) {
            inInstrumentation = true;
            for (ErrorBranchInstrumenter instrumenter : instrumentation) {
                instrumenter.visitIntInsn(opcode, operand);
            }
            inInstrumentation = false;
        }
        super.visitIntInsn(opcode, operand);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitTypeInsn(int, java.lang.String)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (!inInstrumentation) {
            inInstrumentation = true;
            for (ErrorBranchInstrumenter instrumenter : instrumentation) {
                instrumenter.visitTypeInsn(opcode, type);
            }
            inInstrumentation = false;
        }
        super.visitTypeInsn(opcode, type);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitInsn(int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitInsn(int opcode) {
        if (!inInstrumentation) {
            inInstrumentation = true;
            for (ErrorBranchInstrumenter instrumenter : instrumentation) {
                instrumenter.visitInsn(opcode);
            }
            inInstrumentation = false;
        }
        super.visitInsn(opcode);
    }
	
	/*
	public Frame currentFrame = null;
	
	@Override
	public void visitFrame(int type, int nLocal, Object[] local, int nStack,
			Object[] stack) {
		super.visitFrame(type, nLocal, local, nStack, stack);
		// this.currentFrame = frames[numFrame++];
	}
	
	
	protected int numFrame = 0;
	
	protected Frame[] frames;

	@Override
	public void visitCode() {
		MethodNode mn = (MethodNode) mv;
		try {
			Analyzer a = new Analyzer(new ThisInterpreter());
			a.analyze(className, mn);
			frames = a.getFrames();
			logger.info("Computed frames: "+frames.length);
		} catch (Exception e) {
			logger.info("Error during frame analysis: "+e);
			frames = new Frame[0];
		}
		super.visitCode();
	}
	*/

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitEnd()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitEnd() {
        MethodNode mn = (MethodNode) mv;
        mn.accept(next);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.commons.LocalVariablesSorter#visitMaxs(int, int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack + 4, maxLocals);
    }

}
