package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Variables.VariableTable;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.FrameLayout;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.TypeStack;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.TypeStackManipulation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.MethodAnalyser.METHOD_EXIT_ORDER;

public class MethodExit extends ByteCodeInstruction {

    private static class MethodExitStackManipulation extends TypeStackManipulation{

        @Override
        public TypeStack apply(TypeStack s) {
            s.clear();
            return s;
        }

        @Override
        public TypeStack applyBackwards(TypeStack s) {
            s.clear();
            return s;
        }

        @Override
        public FrameLayout apply(FrameLayout frameLayout) {
            return new FrameLayout(Collections.emptyList(), true);
        }

        @Override
        public FrameLayout applyBackwards(FrameLayout frameLayout) {
            return new FrameLayout(Collections.emptyList(), false);
        }

        @Override
        public FrameLayout computeMinimalBefore() {
            return new FrameLayout(Collections.emptyList(), false);
        }

        @Override
        public FrameLayout computeMinimalAfter() {
            return new FrameLayout(Collections.emptyList(), false);
        }
    }

    public static final int METHOD_EXIT_OPCODE = -2;

    public MethodExit(String className, String methodName, String methodDescriptor) {
        super(className, methodName, METHOD_EXIT_ORDER, methodDescriptor, "Method Exit " + String.join(":", className, methodName,
                methodDescriptor), METHOD_EXIT_ORDER, -2);
    }

    @Override
    public Collection<Integer> getSuccessors() {
        return Collections.emptyList();
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.emptyList();
    }

    @Override
    public TypeStackManipulation getStackManipulation(VariableTable table, ByteCodeInstruction instruction) {
        return new MethodExitStackManipulation();
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
