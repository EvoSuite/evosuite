package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.UnaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.UnaryJumpInstructions.IfNonNullInstruction;
import org.objectweb.asm.Opcodes;

public class IfNonNullInstructionPlaceholder extends UnaryJumpInstructionPlaceholder {

    public IfNonNullInstructionPlaceholder(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(IfNonNullInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber,
                IfNonNullInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IFNONNULL);
    }

    @Override
    public IfNonNullInstruction setDestination(ByteCodeInstruction instruction) {
        return new IfNonNullInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
