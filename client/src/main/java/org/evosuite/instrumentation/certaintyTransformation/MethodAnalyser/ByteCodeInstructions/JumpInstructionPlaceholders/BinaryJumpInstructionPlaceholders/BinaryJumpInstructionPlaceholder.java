package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.BinaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.ConditionalJumpInstructionPlaceholder;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.JumpInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import java.util.Arrays;
import java.util.List;

public abstract class BinaryJumpInstructionPlaceholder extends ConditionalJumpInstructionPlaceholder {

    private final StackTypeSet consumedType;

    public BinaryJumpInstructionPlaceholder(JumpInstruction.JUMP_TYPE jumpType, String className, String methodName,
                                            int lineNUmber, String methodDescriptor, int instructionNumber, StackTypeSet consumedType, int opcode) {
        super(jumpType, className, methodName, lineNUmber,methodDescriptor, instructionNumber, opcode);
        this.consumedType = consumedType;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Arrays.asList(consumedType, consumedType);
    }
}
