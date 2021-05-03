package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.arry_load_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.DALOAD;

public class DArrayLoadInstruction extends ArrayLoadInstruction {
    public DArrayLoadInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                                 int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor, "DALOAD", instructionNumber, StackTypeSet.DOUBLE,
                DALOAD);
    }
}