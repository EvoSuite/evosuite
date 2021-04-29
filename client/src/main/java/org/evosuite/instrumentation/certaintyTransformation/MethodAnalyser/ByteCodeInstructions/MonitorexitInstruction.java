package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MonitorexitInstruction extends ByteCodeInstruction{
    public MonitorexitInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber,
                                  int opcode) {
        super(className, methodName, lineNumber, methodDescriptor, "MONITOREXIT", instructionNumber, opcode);
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.singletonList(StackTypeSet.AO);
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
