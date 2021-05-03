package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;

import java.util.Arrays;
import java.util.List;

public abstract class ConditionalJumpInstruction extends JumpInstruction {

    public ConditionalJumpInstruction(JUMP_TYPE jumpType, String className, String methodName,
                                      int lineNUmber, String methodDescriptor, int instructionNumber, ByteCodeInstruction destination,
                                      int opcode) {
        super(jumpType, className, methodName, lineNUmber,methodDescriptor, instructionNumber, destination, opcode);
    }

    @Override
    public List<Integer> getSuccessors() {
        return Arrays.asList(instructionNumber+1,jmpDestination);
    }
}
