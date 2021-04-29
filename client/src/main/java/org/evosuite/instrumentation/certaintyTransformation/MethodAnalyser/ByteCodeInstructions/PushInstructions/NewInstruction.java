package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import java.util.Collections;
import java.util.Set;

import static org.objectweb.asm.Opcodes.NEW;

public class NewInstruction extends PushInstruction {
    public NewInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber,
                          String classConstructed) {
        super(className, methodName, lineNumber, methodDescriptor, "NEW " + classConstructed, instructionNumber,
                StackTypeSet.AO, NEW);
    }

    @Override
    public Set<Integer> readsVariables(){
        return Collections.emptySet();
    }
    @Override
    public Set<Integer> writesVariables(){
        return Collections.emptySet();
    }
}
