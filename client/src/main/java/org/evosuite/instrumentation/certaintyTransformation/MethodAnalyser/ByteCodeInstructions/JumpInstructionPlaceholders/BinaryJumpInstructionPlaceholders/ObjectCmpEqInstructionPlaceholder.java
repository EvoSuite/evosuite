package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.BinaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.BinaryJumpInstructions.ObjectCmpEqInstruction;
import org.objectweb.asm.Opcodes;

public class ObjectCmpEqInstructionPlaceholder extends BinaryJumpInstructionPlaceholder {

    public ObjectCmpEqInstructionPlaceholder(String className, String methodName, int lineNUmber, String methodDescriptor,int instructionNumber) {
        super(ObjectCmpEqInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber,
                ObjectCmpEqInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IF_ACMPEQ);
    }

    @Override
    public ObjectCmpEqInstruction setDestination(ByteCodeInstruction instruction) {
        return new ObjectCmpEqInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
