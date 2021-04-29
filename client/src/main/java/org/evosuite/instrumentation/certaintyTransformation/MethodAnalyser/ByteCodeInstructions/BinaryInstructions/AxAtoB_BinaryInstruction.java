package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

public class AxAtoB_BinaryInstruction extends BinaryInstruction {
    public AxAtoB_BinaryInstruction(String className, String methodName, int lineNumber,String methodDescriptor, String label,
                                    int instructionNumber, StackTypeSet operand, StackTypeSet resultType, int opcode) {
        super(className, methodName, lineNumber, methodDescriptor, label, instructionNumber, operand, operand,
                resultType, opcode);
    }
}
