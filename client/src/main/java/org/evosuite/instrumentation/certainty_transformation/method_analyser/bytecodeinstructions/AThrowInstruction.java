package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.try_catch.TryCatchBlock;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.variables.VariableTable;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.ThrowExceptionStackManipulation;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.TypeStackManipulation;
import org.objectweb.asm.Opcodes;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AThrowInstruction extends ByteCodeInstruction {



    public AThrowInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor, "ATHROW", instructionNumber, Opcodes.ATHROW);
    }

    @Override
    public Collection<Integer> getSuccessors(List<TryCatchBlock> handlers) {
        List<TryCatchBlock> handlerInRange =
                handlers.stream().filter(this::tryCatchBlockInRange).collect(Collectors.toList());
        List<Integer> collect = handlerInRange.stream().map(x -> x.getHandler().getOrder()).collect(Collectors.toList());
        collect.add(-2);
        return collect;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        throw new UnsupportedOperationException("ATHROW-Instruction has a complex Stack Manipulation");
    }

    @Override
    public StackTypeSet pushedToStack() {
        throw new UnsupportedOperationException("ATHROW-Instruction has a complex Stack Manipulation");
    }

    @Override
    public TypeStackManipulation getStackManipulation(VariableTable table,
                                                      ByteCodeInstruction instruction) {
        return new ThrowExceptionStackManipulation();
    }

    @Override
    public Collection<Integer> getSuccessors() {
        throw new UnsupportedOperationException("Need the try catch handlers to calculate successors");
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
