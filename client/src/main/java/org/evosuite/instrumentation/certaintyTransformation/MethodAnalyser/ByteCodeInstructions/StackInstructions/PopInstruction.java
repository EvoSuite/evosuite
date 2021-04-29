package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.StackInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Variables.VariableTable;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.FrameLayout;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.TypeStack;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.TypeStackManipulation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet.ANY;
import static org.objectweb.asm.Opcodes.POP;

public class PopInstruction extends StackInstruction {
    public static class PopStackManipulation extends TypeStackManipulation {

        private static FrameLayout MINIMAL_BEFORE = new FrameLayout(Collections.singletonList(ANY), true);
        private static FrameLayout MINIMAL_AFTER = new FrameLayout(Arrays.asList(ANY, ANY), true);

        @Override
        public TypeStack apply(TypeStack s) {
            s.pop();
            return s;
        }

        @Override
        public TypeStack applyBackwards(TypeStack s) {
            s.push(ANY);
            return s;
        }

        @Override
        public FrameLayout apply(FrameLayout frameLayout) {
            List<StackTypeSet> types = frameLayout.getTypes();
            types.remove(types.size() -1);
            return new FrameLayout(types, frameLayout.hasUnknownLeadingTypes());
        }

        @Override
        public FrameLayout applyBackwards(FrameLayout frameLayout) {
            List<StackTypeSet> types = frameLayout.getTypes();
            types.add(ANY);
            return new FrameLayout(types, frameLayout.hasUnknownLeadingTypes());
        }

        @Override
        public FrameLayout computeMinimalBefore() {
            return MINIMAL_BEFORE;
        }

        @Override
        public FrameLayout computeMinimalAfter() {
            return MINIMAL_AFTER;
        }
    }

    public PopInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "POP", instructionNumber, POP);
    }

    @Override
    public TypeStackManipulation getStackManipulation(VariableTable table, ByteCodeInstruction instruction) {
        return new PopStackManipulation();
    }
}
