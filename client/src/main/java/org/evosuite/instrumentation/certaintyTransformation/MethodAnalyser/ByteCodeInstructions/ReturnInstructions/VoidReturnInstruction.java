package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ReturnInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import java.util.Collections;
import java.util.List;

import static org.objectweb.asm.Opcodes.RETURN;

public class VoidReturnInstruction extends ReturnInstruction {


    public VoidReturnInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(StackTypeSet.VOID, className, methodName, "RETURN", lineNumber, methodDescriptor,instructionNumber, RETURN);
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.emptyList();
    }
}
