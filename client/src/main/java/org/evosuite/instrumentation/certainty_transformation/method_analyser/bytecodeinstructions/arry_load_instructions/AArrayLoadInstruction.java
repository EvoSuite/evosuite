package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.arry_load_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.AALOAD;

public class AArrayLoadInstruction extends ArrayLoadInstruction {

    public AArrayLoadInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor, "AALOAD", instructionNumber, StackTypeSet.AO,
                AALOAD);
    }
}
