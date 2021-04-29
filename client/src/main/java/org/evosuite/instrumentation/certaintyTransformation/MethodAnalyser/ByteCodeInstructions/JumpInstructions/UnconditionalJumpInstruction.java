package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import java.util.Collections;
import java.util.List;

public abstract class UnconditionalJumpInstruction extends JumpInstruction {
    public UnconditionalJumpInstruction(JUMP_TYPE jumpType, String className, String methodName, int lineNUmber,String methodDescriptor,
                                        int instructionNumber, ByteCodeInstruction jmpDestination, int opcode) {
        super(jumpType, className, methodName, lineNUmber,methodDescriptor, instructionNumber, jmpDestination, opcode);
    }

    @Override
    public List<Integer> getSuccessors() {
        return Collections.singletonList(jmpDestination);
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.emptyList();
    }
}
