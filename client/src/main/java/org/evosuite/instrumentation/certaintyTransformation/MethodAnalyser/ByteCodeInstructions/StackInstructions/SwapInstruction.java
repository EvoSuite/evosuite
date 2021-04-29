package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.StackInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Variables.VariableTable;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.FrameLayout;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.TypeStack;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.TypeStackManipulation;

import java.util.Arrays;
import java.util.List;

import static org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet.ANY;
import static org.objectweb.asm.Opcodes.SWAP;

public class SwapInstruction extends StackInstruction {

    public SwapInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "SWAP", instructionNumber, SWAP);
    }

    @Override
    public TypeStackManipulation getStackManipulation(VariableTable table, ByteCodeInstruction instruction) {
        return new SwapStackManipulation();
    }

    private static class SwapStackManipulation extends TypeStackManipulation{

        @Override
        public TypeStack apply(TypeStack s) {
            return null;
        }

        @Override
        public TypeStack applyBackwards(TypeStack s) {
            return null;
        }

        @Override
        public FrameLayout apply(FrameLayout frameLayout) {
            List<StackTypeSet> types = frameLayout.getTypes();
            if(types.size() < 2 && !frameLayout.hasUnknownLeadingTypes())
                throw new IllegalStateException("not applicable");
            while (types.size() < 2)
                types.add(0, ANY);
            StackTypeSet remove = types.remove(types.size() - 1);
            StackTypeSet remove1 = types.remove(types.size() - 1);
            types.add(remove);
            types.add(remove1);
            return new FrameLayout(types,frameLayout.hasUnknownLeadingTypes());
        }

        @Override
        public FrameLayout applyBackwards(FrameLayout frameLayout) {
            return this.apply(frameLayout);
        }

        @Override
        public FrameLayout computeMinimalBefore() {
            return new FrameLayout(Arrays.asList(ANY,ANY),true);
        }

        @Override
        public FrameLayout computeMinimalAfter() {
            return new FrameLayout(Arrays.asList(ANY,ANY),true);
        }
    }
}
