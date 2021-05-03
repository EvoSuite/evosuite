package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.store_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.ASTORE;

public class AStoreInstruction extends StoreInstruction {
    public AStoreInstruction(String className, String methodName, int line,String methodDescriptor, int localVariableIndex,
                             int instructionNumber) {
        super(className, methodName, line, methodDescriptor, "ASTORE " + localVariableIndex, localVariableIndex, instructionNumber,
                StackTypeSet.AO, ASTORE);
    }
}
