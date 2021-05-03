package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.arry_load_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.FALOAD;

public class FArrayLoadInstruction extends ArrayLoadInstruction {
    public FArrayLoadInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor, "FALOAD", instructionNumber, StackTypeSet.FLOAT, FALOAD);
    }
}
