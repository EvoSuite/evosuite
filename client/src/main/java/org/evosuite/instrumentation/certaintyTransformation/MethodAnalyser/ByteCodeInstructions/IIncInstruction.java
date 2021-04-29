package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class IIncInstruction extends ByteCodeInstruction {

    private final int localVariableIndex;
    private final int incrementation;

    public IIncInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber,
                           int localVariableIndex, int incrementation) {
        super(className, methodName, lineNumber, methodDescriptor, String.join(" ",
                "IINC",
                Integer.toString(localVariableIndex),
                Integer.toString(incrementation)),
                instructionNumber, Opcodes.IINC);
        this.localVariableIndex = localVariableIndex;
        this.incrementation = incrementation;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.emptyList();
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.VOID;
    }

    @Override
    public boolean writesVariable(int index){
        return localVariableIndex == index;
    }
    @Override
    public Set<Integer> readsVariables(){
        return Collections.singleton(localVariableIndex);
    }
    @Override
    public Set<Integer> writesVariables(){
        return Collections.singleton(localVariableIndex);
    }

}
