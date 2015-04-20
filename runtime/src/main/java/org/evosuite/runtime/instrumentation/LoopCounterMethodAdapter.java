package org.evosuite.runtime.instrumentation;

import org.evosuite.runtime.LoopCounter;
import org.evosuite.runtime.thread.KillSwitchHandler;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Add loop check before each jump instruction.
 *
 * <p>
 * Note: not all jumps represent a loop (eg, for/while).
 * Non-loops are for examples if/switch.
 * However, such extra instrumentation should not really be a problem
 *
 * Created by Andrea Arcuri on 29/03/15.
 */
public class LoopCounterMethodAdapter extends MethodVisitor {

    private static final String LOOP_COUNTER = Type.getInternalName(LoopCounter.class);

    public LoopCounterMethodAdapter(MethodVisitor mv, String methodName, String desc) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack+2, maxLocals);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        addInstrumentation(); //add instrumentation before of the jump
        super.visitJumpInsn(opcode, label);
    }

    private void addInstrumentation(){

        int index = LoopCounter.getInstance().getNewIndex();

        mv.visitMethodInsn(Opcodes.INVOKESTATIC,LOOP_COUNTER,
                "getInstance", "()L"+LOOP_COUNTER+";" , false);

        mv.visitLdcInsn(index);

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, LOOP_COUNTER,
                "checkLoop", "(I)V", false);
    }
}
