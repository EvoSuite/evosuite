package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.FloatUnaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.AtoA_UnaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

public class FloatUnaryInstruction extends AtoA_UnaryInstruction {
    public FloatUnaryInstruction(FLOAT_UNARY_INSTRUCTION instruction, String className, String methodName,
                                 int lineNumber,String methodDescriptor, int instructionNumber, int opcode) {
        super(className, methodName, lineNumber, methodDescriptor,instruction.toString(), instructionNumber, StackTypeSet.FLOAT, opcode);
    }

    protected enum FLOAT_UNARY_INSTRUCTION {
        FNEG
    }
}
