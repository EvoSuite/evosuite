package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.UnaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.ConditionalJumpInstructionPlaceholder;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.JumpInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import java.util.Collections;
import java.util.List;

public abstract class UnaryJumpInstructionPlaceholder extends ConditionalJumpInstructionPlaceholder {

    private final StackTypeSet consumedType;

    public UnaryJumpInstructionPlaceholder(JumpInstruction.JUMP_TYPE jumpType,
                                           String className,
                                           String methodName,
                                           int lineNUmber, String methodDescriptor,
                                           int instructionNumber,
                                           StackTypeSet consumedType, int opcode) {
        super(jumpType, className, methodName, lineNUmber,methodDescriptor, instructionNumber, opcode);
        this.consumedType = consumedType;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.singletonList(consumedType);
    }
}
