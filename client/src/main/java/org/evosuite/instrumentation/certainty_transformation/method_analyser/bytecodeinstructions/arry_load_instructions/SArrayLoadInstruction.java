package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.arry_load_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.SALOAD;

public class SArrayLoadInstruction extends ArrayLoadInstruction {
    public SArrayLoadInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor,"SALOAD", instructionNumber, StackTypeSet.SHORT, SALOAD);
    }
}
