package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.BinaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.BinaryJumpInstructions.IntCmpGtInstruction;
import org.objectweb.asm.Opcodes;

public class IntCmpGtInstructionPlaceholder extends BinaryJumpInstructionPlaceholder {

    public IntCmpGtInstructionPlaceholder(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(IntCmpGtInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber,
                IntCmpGtInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IF_ICMPGT);
    }

    @Override
    public IntCmpGtInstruction setDestination(ByteCodeInstruction instruction) {
        return new IntCmpGtInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
