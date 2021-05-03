package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.store_instructions;


import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.ISTORE;

public class IStoreInstruction extends StoreInstruction {
    public IStoreInstruction(String className, String methodName, int line,String methodDescriptor, int localVariableIndex,
                             int instructionNumber) {
        super(className, methodName, line,methodDescriptor, "ISTORE " + localVariableIndex, localVariableIndex, instructionNumber,
                StackTypeSet.TWO_COMPLEMENT, ISTORE);
    }
}
