package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.JumpInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import java.util.Collections;
import java.util.List;

public abstract class UnconditionalJumpPlaceholder extends JumpInstructionPlaceholder {
    public UnconditionalJumpPlaceholder(JumpInstruction.JUMP_TYPE jumpType, String className, String methodName,
                                        int lineNUmber, String methodDescriptor, int instructionNumber, int opcode) {
        super(jumpType, className, methodName, lineNUmber,methodDescriptor, instructionNumber, opcode);
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.emptyList();
    }
}
