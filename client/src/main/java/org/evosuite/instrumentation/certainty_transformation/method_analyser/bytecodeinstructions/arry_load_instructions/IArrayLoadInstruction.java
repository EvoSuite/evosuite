package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.arry_load_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.IALOAD;

public class IArrayLoadInstruction extends ArrayLoadInstruction {


    public IArrayLoadInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                                 int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor,"IALOAD", instructionNumber, StackTypeSet.INT, IALOAD);
    }
}
