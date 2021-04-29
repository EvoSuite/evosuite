package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxAtoB_BinaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxAtoB_BinaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.DCMPG;

public class DCmpgInstruction extends AxAtoB_BinaryInstruction {
    public DCmpgInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "DCMPG", instructionNumber, StackTypeSet.DOUBLE, StackTypeSet.INT,
                DCMPG);
    }
}
