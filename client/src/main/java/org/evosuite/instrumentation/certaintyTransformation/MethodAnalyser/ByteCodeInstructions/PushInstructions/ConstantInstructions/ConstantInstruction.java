package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions.ConstantInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions.PushInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import java.util.Collections;
import java.util.Set;

public abstract class ConstantInstruction<T> extends PushInstruction {
    T value;

    public ConstantInstruction(String className, String methodName, int lineNumber, String methodDescriptor, String label,
                               int instructionNumber, T value, StackTypeSet pushedType, int opcode) {
        super(className, methodName, lineNumber, methodDescriptor, label, instructionNumber, pushedType, opcode);
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public Set<Integer> writesVariables(){
        return Collections.emptySet();
    }

    @Override
    public Set<Integer> readsVariables(){
        return Collections.emptySet();
    }
}
