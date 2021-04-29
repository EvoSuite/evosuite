package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.InvokeInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.TryCatch.TryCatchBlock;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Variables.VariableTable;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.ConsumePushStackManipulation;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.ThrowExceptionStackManipulation;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.TypeStackManipulation;
import org.objectweb.asm.Type;

import java.util.*;
import java.util.stream.Collectors;

public class InvokeInstruction extends ByteCodeInstruction {


    private INVOKATION_TYPE invokationType;
    protected final String owner;
    protected final String name;
    protected final String descriptor;

    public InvokeInstruction(String className, String methodName, int line,
                             String methodDescriptor, INVOKATION_TYPE invokationType,
                             String owner, String name, String descriptor, int instructionNumber, int opcode) {

        super(className, methodName, line, methodDescriptor, invokationType + " \"" + String.join(":", owner, name, descriptor)+"\"",
                instructionNumber, opcode);
        this.invokationType = invokationType;
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
    }

    @Override
    public Collection<Integer> getSuccessors(List<TryCatchBlock> handlers) {
        Collection<Integer> next = new ArrayList<>(getSuccessors());
        // TODO decide.
        /*List<Integer> exceptionHandler =
                handlers.stream().filter(this::tryCatchBlockInRange)
                        .map(TryCatchBlock::getHandler)
                        .map(ByteCodeInstruction::getOrder)
                        .collect(Collectors.toList());
        next.addAll(exceptionHandler);
        next.add(-2);*/
        return next;
    }

    @Override
    public TypeStackManipulation getStackManipulation(VariableTable table, ByteCodeInstruction instruction) {
        if(instruction.getOrder() == this.instructionNumber +1){
            return new ConsumePushStackManipulation(consumedFromStack(), pushedToStack());
        }
        return new ThrowExceptionStackManipulation();
    }

    @Override
    public boolean writesVariable(int localVariableIndex) {
        return false;
    }

    @Override
    public Set<Integer> writesVariables() {
        return Collections.emptySet();
    }

    @Override
    public Set<Integer> readsVariables() {
        return Collections.emptySet();
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        List<StackTypeSet> types =
                Arrays.stream(Type.getArgumentTypes(descriptor)).map(StackTypeSet::ofMergeTypes).collect(Collectors.toList());
        types.add(0, StackTypeSet.of(Type.getObjectType(owner).getSort()));
        return types;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.ofMergeTypes(Type.getReturnType(descriptor));
    }

    protected enum INVOKATION_TYPE {
        INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC, INVOKEINTERFACE, INVOKEDYNAMIC
    }
}
