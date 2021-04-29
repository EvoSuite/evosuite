package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.UnaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.UnaryJumpInstructions.IfGeInstruction;
import org.objectweb.asm.Opcodes;

public class IfGeInstructionPlaceholder extends UnaryJumpInstructionPlaceholder {
    public IfGeInstructionPlaceholder(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(IfGeInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber,
                IfGeInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IFGE);
    }

    @Override
    public IfGeInstruction setDestination(ByteCodeInstruction instruction) {
        return new IfGeInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
