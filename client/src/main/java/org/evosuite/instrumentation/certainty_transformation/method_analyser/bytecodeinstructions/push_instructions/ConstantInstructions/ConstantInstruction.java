package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.push_instructions.ConstantInstructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.push_instructions.PushInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

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
