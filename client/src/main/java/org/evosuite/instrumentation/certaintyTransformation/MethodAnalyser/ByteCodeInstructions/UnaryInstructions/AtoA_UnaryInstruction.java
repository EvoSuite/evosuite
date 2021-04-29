package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

public class AtoA_UnaryInstruction extends AtoB_UnaryInstruction {


    public AtoA_UnaryInstruction(String className, String methodName, int lineNumber,String methodDescriptor, String label, int instructionNumber,
                                 int type, int opcode) {
        super(className, methodName, lineNumber,methodDescriptor, label, instructionNumber,type, type, opcode);
    }

    public AtoA_UnaryInstruction(String className, String methodName, int lineNumber, String methodDescriptor, String label, int instructionNumber,
                                 StackTypeSet type, int opcode) {
        super(className, methodName, lineNumber, methodDescriptor, label, instructionNumber,type, type, opcode);
    }
}
