package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.Opcodes;

import java.util.*;
import java.util.stream.Collectors;

public class RetInstruction extends ByteCodeInstruction {

    private final Set<ByteCodeInstruction> targets;

    public RetInstruction(String className,
                          String methodName,
                          int lineNumber,String methodDescriptor,
                          int instructionNumber,
                          Set<ByteCodeInstruction> targets) {
        super(className, methodName, lineNumber, methodDescriptor, "RET", instructionNumber, Opcodes.RET);
        this.targets = new HashSet<>(targets);
    }

    @Override
    public Collection<Integer> getSuccessors() {
        return targets.stream().map(ByteCodeInstruction::getOrder).collect(Collectors.toSet());
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
    public boolean writesVariable(int localVariableIndex) {
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
