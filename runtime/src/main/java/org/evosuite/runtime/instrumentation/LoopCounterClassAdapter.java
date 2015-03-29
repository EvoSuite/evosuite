package org.evosuite.runtime.instrumentation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by Andrea Arcuri on 29/03/15.
 */
public class LoopCounterClassAdapter extends ClassVisitor {

    public LoopCounterClassAdapter(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {

        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        // Don't touch bridge and synthetic methods
        if ((access & Opcodes.ACC_SYNTHETIC) > 0
                || (access & Opcodes.ACC_BRIDGE) > 0) {
            return mv;
        }

        if (name.equals("<clinit>")){
            //should not stop a static initializer
            return mv;
        }


        return new LoopCounterMethodAdapter(mv, name, desc);
    }
}
