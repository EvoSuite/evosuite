package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.JumpSubroutineInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

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
