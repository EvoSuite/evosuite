package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.BinaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.BinaryJumpInstructions.IntCmpEqInstruction;
import org.objectweb.asm.Opcodes;

public class IntCmpEqInstructionPlaceholder extends BinaryJumpInstructionPlaceholder {

    public IntCmpEqInstructionPlaceholder(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(IntCmpEqInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber,
                IntCmpEqInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IF_ICMPEQ);
    }

    @Override
    public IntCmpEqInstruction setDestination(ByteCodeInstruction instruction) {
        return new IntCmpEqInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
