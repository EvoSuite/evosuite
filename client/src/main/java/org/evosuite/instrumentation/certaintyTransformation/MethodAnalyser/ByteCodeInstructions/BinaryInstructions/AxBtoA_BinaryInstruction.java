package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

public class AxBtoA_BinaryInstruction extends BinaryInstruction {
    public AxBtoA_BinaryInstruction(String className, String methodName, int lineNumber, String methodDescriptor,String label,
                                    int instructionNumber, StackTypeSet operand1, StackTypeSet operand2, int opcode) {
        super(className, methodName, lineNumber, methodDescriptor, label, instructionNumber, operand1, operand2,
                operand1, opcode);
    }
}
