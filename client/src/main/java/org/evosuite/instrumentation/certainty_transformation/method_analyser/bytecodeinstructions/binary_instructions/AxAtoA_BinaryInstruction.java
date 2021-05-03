package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

public abstract class AxAtoA_BinaryInstruction extends AxBtoA_BinaryInstruction {
    private final StackTypeSet type;

    public AxAtoA_BinaryInstruction(String className, String methodName, String label, int lineNUmber,String methodDescriptor,
                                    int instructionNumber, StackTypeSet type, int opcode) {
        super(className, methodName, lineNUmber,methodDescriptor, label, instructionNumber, type, type, opcode);
        this.type = new StackTypeSet(type);
    }

}
