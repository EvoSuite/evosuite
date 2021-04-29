package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.StackInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Variables.VariableTable;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.FrameLayout;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.TypeStack;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.TypeStackManipulation;

import java.util.Collections;
import java.util.List;

import static org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet.*;
import static org.objectweb.asm.Opcodes.POP2;

public class Pop2Instruction extends StackInstruction {
    public Pop2Instruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "POP2", instructionNumber, POP2);
    }

    private static class Pop2StackManipulation extends TypeStackManipulation{

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
            if(types.isEmpty() && !frameLayout.hasUnknownLeadingTypes())
                throw new IllegalStateException();
            if(types.isEmpty())
                return frameLayout;
            int size = types.size();
            StackTypeSet tos = types.remove(size - 1);
            if(tos.equals(LONG) || tos.equals(DOUBLE))
                return new FrameLayout(types,frameLayout.hasUnknownLeadingTypes());
            if(size>=2)
                types.remove(size-2);
            return new FrameLayout(types, frameLayout.hasUnknownLeadingTypes());
        }

        @Override
        public FrameLayout applyBackwards(FrameLayout frameLayout) {
            List<StackTypeSet> types = frameLayout.getTypes();
            types.add(ANY);
            return new FrameLayout(types,frameLayout.hasUnknownLeadingTypes());
        }

        @Override
        public FrameLayout computeMinimalBefore() {
            return new FrameLayout(Collections.singletonList(ANY), true);
        }

        @Override
        public FrameLayout computeMinimalAfter() {
            return new FrameLayout(Collections.emptyList(), true);
        }
    }

    @Override
    public TypeStackManipulation getStackManipulation(VariableTable table,
                                                      ByteCodeInstruction instruction) {
        return new Pop2StackManipulation();
    }
}
