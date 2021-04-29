package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.BinaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.BinaryJumpInstructions.IntCmpGeInstruction;
import org.objectweb.asm.Opcodes;

public class IntCmpGeInstructionPlaceholder extends BinaryJumpInstructionPlaceholder {

    public IntCmpGeInstructionPlaceholder(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(IntCmpGeInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber,
                IntCmpGeInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IF_ICMPGE);
    }

    @Override
    public IntCmpGeInstruction setDestination(ByteCodeInstruction instruction) {
        return new IntCmpGeInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
