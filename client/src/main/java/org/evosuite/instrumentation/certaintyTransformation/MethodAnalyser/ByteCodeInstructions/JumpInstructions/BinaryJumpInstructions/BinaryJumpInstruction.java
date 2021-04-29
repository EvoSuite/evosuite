package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.BinaryJumpInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.ConditionalJumpInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import java.util.Arrays;
import java.util.List;

public abstract class BinaryJumpInstruction extends ConditionalJumpInstruction {

    private final StackTypeSet consumedType;

    public BinaryJumpInstruction(JUMP_TYPE jumpType, String className, String methodName, int lineNUmber, String methodDescriptor,
                                 int instructionNumber, ByteCodeInstruction destination, StackTypeSet consumedType,
                                 int opcode) {
        super(jumpType, className, methodName, lineNUmber,methodDescriptor, instructionNumber, destination, opcode);
        this.consumedType = new StackTypeSet(consumedType);
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Arrays.asList(new StackTypeSet(consumedType), new StackTypeSet(consumedType));
    }
}
