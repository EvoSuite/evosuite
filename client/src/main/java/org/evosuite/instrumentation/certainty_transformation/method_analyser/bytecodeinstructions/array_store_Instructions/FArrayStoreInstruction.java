package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.array_store_Instructions;


import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.FASTORE;

public class FArrayStoreInstruction extends ArrayStoreInstructions {
    public FArrayStoreInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "FASTORE", instructionNumber, StackTypeSet.FLOAT, FASTORE);
    }
}
