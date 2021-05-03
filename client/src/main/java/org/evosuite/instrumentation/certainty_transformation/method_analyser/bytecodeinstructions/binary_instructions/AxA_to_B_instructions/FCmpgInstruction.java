package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.AxA_to_B_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.AxAtoB_BinaryInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.FCMPG;

public class FCmpgInstruction extends AxAtoB_BinaryInstruction {
    public FCmpgInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "FCMPG", instructionNumber, StackTypeSet.FLOAT, StackTypeSet.INT,
                FCMPG);
    }
}
