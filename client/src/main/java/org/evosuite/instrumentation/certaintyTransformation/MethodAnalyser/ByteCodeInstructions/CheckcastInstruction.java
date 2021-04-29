package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.CHECKCAST;

public class CheckcastInstruction extends ByteCodeInstruction {
    private final String type;

    public CheckcastInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber,
                                String type) {
        super(className, methodName, lineNumber, methodDescriptor, "CHECKCAST " + type, instructionNumber, CHECKCAST);
        this.type = type;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.singletonList(StackTypeSet.AO);
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.AO;
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
