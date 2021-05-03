package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class BinaryInstruction extends ByteCodeInstruction {
    private final StackTypeSet operand1;
    private final StackTypeSet operand2;
    private final StackTypeSet resultType;
    private final List<StackTypeSet> stackTypeSets;

    public BinaryInstruction(String className, String methodName, int lineNumber,String methodDescriptor, String label, int instructionNumber
            , StackTypeSet operand1, StackTypeSet operand2, StackTypeSet resultType, int opcode) {
        super(className, methodName, lineNumber, methodDescriptor, label, instructionNumber, opcode);
        this.operand1 = new StackTypeSet(operand1);
        this.operand2 = new StackTypeSet(operand2);
        this.resultType = new StackTypeSet(resultType);
        stackTypeSets = Arrays.asList(new StackTypeSet(operand1), new StackTypeSet(operand2));
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return stackTypeSets;
    }

    @Override
    public StackTypeSet pushedToStack() {
        return new StackTypeSet(resultType);
    }

    @Override
    public boolean writesVariable(int index){
        return false;
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
