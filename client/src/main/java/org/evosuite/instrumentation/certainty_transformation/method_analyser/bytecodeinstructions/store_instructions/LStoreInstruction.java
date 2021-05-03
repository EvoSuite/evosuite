package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.store_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.LSTORE;

public class LStoreInstruction extends StoreInstruction {
    public LStoreInstruction(String className, String methodName, int line, String methodDescriptor,int localVariableIndex,
                             int instructionNumber) {
        super(className, methodName, line, methodDescriptor,"LSTORE " + localVariableIndex, localVariableIndex, instructionNumber,
                StackTypeSet.LONG, LSTORE);
    }
}
