package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.JumpSubroutineInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.JSR;

public class JumpSubroutineInstructionPlaceholder extends UnconditionalJumpPlaceholder {

    public JumpSubroutineInstructionPlaceholder(
            String className, String methodName, int lineNUmber,String methodDescriptor,
            int instructionNumber) {
        super(JumpSubroutineInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber, JSR);
    }

    @Override
    public StackTypeSet pushedToStack() {
        return JumpSubroutineInstruction.PUSHED_TO_STACK_TYPE;
    }

    @Override
    public JumpSubroutineInstruction setDestination(ByteCodeInstruction instruction) {
        return new JumpSubroutineInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
