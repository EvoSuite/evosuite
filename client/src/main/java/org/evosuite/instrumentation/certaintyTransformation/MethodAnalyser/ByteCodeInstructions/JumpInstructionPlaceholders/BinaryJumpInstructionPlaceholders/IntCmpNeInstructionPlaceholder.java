package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.BinaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.BinaryJumpInstructions.IntCmpNeInstruction;
import org.objectweb.asm.Opcodes;

public class IntCmpNeInstructionPlaceholder extends BinaryJumpInstructionPlaceholder {

    public IntCmpNeInstructionPlaceholder(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(IntCmpNeInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber,
                IntCmpNeInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IF_ICMPNE);
    }

    @Override
    public IntCmpNeInstruction setDestination(ByteCodeInstruction instruction) {
        return new IntCmpNeInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
