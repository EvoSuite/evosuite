package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

public abstract class AxAtoA_BinaryInstruction extends AxBtoA_BinaryInstruction {
    private final StackTypeSet type;

    public AxAtoA_BinaryInstruction(String className, String methodName, String label, int lineNUmber,String methodDescriptor,
                                    int instructionNumber, StackTypeSet type, int opcode) {
        super(className, methodName, lineNUmber,methodDescriptor, label, instructionNumber, type, type, opcode);
        this.type = new StackTypeSet(type);
    }

}
