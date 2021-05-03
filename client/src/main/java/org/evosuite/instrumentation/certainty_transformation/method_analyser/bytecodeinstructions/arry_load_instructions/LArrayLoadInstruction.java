package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.arry_load_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;
import org.objectweb.asm.Opcodes;

public class LArrayLoadInstruction extends ArrayLoadInstruction {

    public LArrayLoadInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                                 int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor, "LALOAD", instructionNumber, StackTypeSet.LONG, Opcodes.LALOAD);
    }
}
