package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.BinaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.BinaryJumpInstructions.ObjectCmpNeInstruction;
import org.objectweb.asm.Opcodes;

public class ObjectCmpNeInstructionPlaceholder extends BinaryJumpInstructionPlaceholder {

    public ObjectCmpNeInstructionPlaceholder(String className, String methodName, int lineNUmber,
                                             String methodDescriptor, int instructionNumber) {
        super(ObjectCmpNeInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber,
                ObjectCmpNeInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IF_ACMPNE);
    }

    @Override
    public ObjectCmpNeInstruction setDestination(ByteCodeInstruction instruction) {
        return new ObjectCmpNeInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
