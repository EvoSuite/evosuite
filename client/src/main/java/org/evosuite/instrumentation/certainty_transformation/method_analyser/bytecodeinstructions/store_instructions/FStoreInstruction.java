package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.store_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.FSTORE;

public class FStoreInstruction extends StoreInstruction {
    public FStoreInstruction(String className, String methodName, int line,String methodDescriptor,
                             int localVariableIndex,
                             int instructionNumber) {
        super(className, methodName, line, methodDescriptor,"FSTORE " + localVariableIndex, localVariableIndex, instructionNumber,
                StackTypeSet.FLOAT, FSTORE);
    }
}
