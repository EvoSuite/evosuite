package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.BinaryJumpInstructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.ConditionalJumpInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

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
