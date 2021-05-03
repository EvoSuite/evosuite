package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

public class AxAtoB_BinaryInstruction extends BinaryInstruction {
    public AxAtoB_BinaryInstruction(String className, String methodName, int lineNumber,String methodDescriptor, String label,
                                    int instructionNumber, StackTypeSet operand, StackTypeSet resultType, int opcode) {
        super(className, methodName, lineNumber, methodDescriptor, label, instructionNumber, operand, operand,
                resultType, opcode);
    }
}
