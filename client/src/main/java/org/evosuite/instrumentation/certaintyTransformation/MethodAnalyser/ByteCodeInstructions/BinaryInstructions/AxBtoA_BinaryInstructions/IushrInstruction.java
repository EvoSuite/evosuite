package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxBtoA_BinaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxAtoA_BinaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.Opcodes;

public class IushrInstruction extends AxAtoA_BinaryInstruction {
    public IushrInstruction(String className, String methodName, int lineNUmber,String methodDescriptor,
                            int instructionNumber) {
        super(className, methodName, "IUSHR", lineNUmber,methodDescriptor, instructionNumber, StackTypeSet.INT, Opcodes.IUSHR);
    }
}
