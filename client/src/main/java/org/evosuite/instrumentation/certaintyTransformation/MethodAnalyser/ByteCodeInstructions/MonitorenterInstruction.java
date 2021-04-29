package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MonitorenterInstruction extends ByteCodeInstruction {
    private static final List<StackTypeSet> CONSUMED_FROM_STACK = Collections.singletonList(StackTypeSet.AO);
    public MonitorenterInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                                   int instructionNumber, int opcode) {
        super(className, methodName, lineNumber, methodDescriptor, "MONITORENTER", instructionNumber, opcode);
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {

        return CONSUMED_FROM_STACK;
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.VOID;
    }

    @Override
    public boolean writesVariable(int index){
        return false;
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
