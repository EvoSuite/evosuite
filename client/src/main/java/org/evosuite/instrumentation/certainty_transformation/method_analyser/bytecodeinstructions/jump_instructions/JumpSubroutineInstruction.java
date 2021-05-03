package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.JSR;

public class JumpSubroutineInstruction extends UnconditionalJumpInstruction {



    public final static StackTypeSet PUSHED_TO_STACK_TYPE = StackTypeSet.OBJECT;
    public final static JUMP_TYPE JUMP_TYPE = JumpInstruction.JUMP_TYPE.JSR;

    public JumpSubroutineInstruction(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber, ByteCodeInstruction jmpDestination) {
        super(JUMP_TYPE, className, methodName, lineNUmber,methodDescriptor, instructionNumber, jmpDestination, JSR);
    }

    @Override
    public StackTypeSet pushedToStack() {
        return PUSHED_TO_STACK_TYPE;
    }
}
