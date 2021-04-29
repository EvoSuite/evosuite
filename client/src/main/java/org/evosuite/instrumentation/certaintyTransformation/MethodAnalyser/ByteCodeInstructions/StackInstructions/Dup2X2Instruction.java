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
import static org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet.TWO_SLOT_TYPES;
import static org.objectweb.asm.Opcodes.DUP2_X2;

public class Dup2X2Instruction extends StackInstruction {

    private static TypeStackManipulation stackManipulation = new Dup2X2StackManipulation();
    public Dup2X2Instruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor,"DUP2_X2", instructionNumber, DUP2_X2);
    }

    @Override
    public TypeStackManipulation getStackManipulation(VariableTable table, ByteCodeInstruction instruction) {
        return stackManipulation;
    }

    private static class Dup2X2StackManipulation extends TypeStackManipulation{

        @Override
        public TypeStack apply(TypeStack s) {
            throw new UnsupportedOperationException("NOT IMPLEMENTED");
        }

        @Override
        public TypeStack applyBackwards(TypeStack s) {
            throw new UnsupportedOperationException("NOT IMPLEMENTED");
        }

        @Override
        public FrameLayout apply(FrameLayout frameLayout) {
            List<StackTypeSet> types = frameLayout.getTypes();
            int size = types.size();
            StackTypeSet tos = types.get(size - 1);
            boolean tosIs2Slots = !tos.intersection(TWO_SLOT_TYPES).isEmpty();
            if(tosIs2Slots){
                StackTypeSet tosM1 = types.get(size - 2);
                boolean tosM12Slots = !tosM1.intersection(TWO_SLOT_TYPES).isEmpty();
                if(tosM12Slots){
                    types.add(size-2,tos);
                } else {
                    types.add(size-3, tos);
                }
                return new FrameLayout(types, frameLayout.hasUnknownLeadingTypes());
            }
            if(size < 4 && !frameLayout.hasUnknownLeadingTypes())
                throw new IllegalArgumentException("That does not work");
            throw new IllegalStateException("That should not happen");
        }

        @Override
        public FrameLayout applyBackwards(FrameLayout frameLayout) {
            List<StackTypeSet> types = frameLayout.getTypes();
            int size = types.size();
            StackTypeSet tos = types.get(size - 1);
            StackTypeSet tosM1 = types.get(size - 2);
            StackTypeSet tosM2 = types.get(size - 3);
            boolean tosIs2Slots = !tos.intersection(TWO_SLOT_TYPES).isEmpty();
            boolean tosM1Is2Slots = !tosM1.intersection(TWO_SLOT_TYPES).isEmpty();
            boolean tosM2Is2Slots = !tosM2.intersection(TWO_SLOT_TYPES).isEmpty();
            if(tosIs2Slots){
                if(tosM1Is2Slots && tosM2Is2Slots)
                    types.remove(size-3);
                else
                    types.remove(size-4);
                return new FrameLayout(types, frameLayout.hasUnknownLeadingTypes());
            }
            throw new IllegalStateException("That should not happen");
        }

        @Override
        public FrameLayout computeMinimalBefore() {
            return new FrameLayout(Arrays.asList(ANY,ANY), true);
        }

        @Override
        public FrameLayout computeMinimalAfter() {
            return new FrameLayout(Arrays.asList(ANY,ANY,ANY), true);
        }
    }
}
