package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.arry_load_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.BALOAD;

public class BArrayLoadInstruction extends ArrayLoadInstruction {
    public BArrayLoadInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "BALOAD", instructionNumber, StackTypeSet.TWO_COMPLEMENT, BALOAD);
    }
}
