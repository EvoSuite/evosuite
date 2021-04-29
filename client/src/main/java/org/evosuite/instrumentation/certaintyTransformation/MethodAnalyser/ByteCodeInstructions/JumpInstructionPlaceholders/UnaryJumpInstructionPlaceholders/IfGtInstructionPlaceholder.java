package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.UnaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.UnaryJumpInstructions.IfGtInstruction;

import static org.objectweb.asm.Opcodes.IFGT;

public class IfGtInstructionPlaceholder extends UnaryJumpInstructionPlaceholder {
    public IfGtInstructionPlaceholder(String className, String methodName, int lineNUmber,
                                      String methodDescriptor, int instructionNumber) {
        super(IfGtInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber,
                IfGtInstruction.CONSUMED_FROM_STACK_TYPE, IFGT);
    }

    @Override
    public IfGtInstruction setDestination(ByteCodeInstruction instruction) {
        return new IfGtInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
