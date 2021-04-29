package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.UnaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.UnaryJumpInstructions.IfNeInstruction;
import org.objectweb.asm.Opcodes;

public class IfNeInstructionPlaceholder extends UnaryJumpInstructionPlaceholder {
    public IfNeInstructionPlaceholder(String className, String methodName, int lineNUmber,String descriptor,
                                      int instructionNumber) {
        super(IfNeInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,descriptor,
                instructionNumber,
                IfNeInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IFNE);
    }

    @Override
    public IfNeInstruction setDestination(ByteCodeInstruction instruction) {
        return new IfNeInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
