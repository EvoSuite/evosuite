package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.BinaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.BinaryJumpInstructions.IntCmpLeInstruction;
import org.objectweb.asm.Opcodes;

public class IntCmpLeInstructionPlaceholder extends BinaryJumpInstructionPlaceholder {

    public IntCmpLeInstructionPlaceholder(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(IntCmpLeInstruction.JUMP_TYPE, className, methodName, lineNUmber,methodDescriptor, instructionNumber,
                IntCmpLeInstruction.CONSUMED_FROM_STACK_TYPE,
                Opcodes.IF_ICMPLE);
    }

    @Override
    public IntCmpLeInstruction setDestination(ByteCodeInstruction instruction) {
        return new IntCmpLeInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
