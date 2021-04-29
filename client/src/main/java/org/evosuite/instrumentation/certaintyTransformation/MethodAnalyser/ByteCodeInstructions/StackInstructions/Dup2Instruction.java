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
import java.util.Set;

import static org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet.ANY;
import static org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet.TWO_SLOT_TYPES;
import static org.objectweb.asm.Opcodes.DUP2;

public class Dup2Instruction extends StackInstruction {
    public Dup2Instruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "DUP2", instructionNumber, DUP2);
    }

    private static class Dup2StackManipulation extends TypeStackManipulation{

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
            if(!types.isEmpty() && !types.get(types.size() - 1).intersection(TWO_SLOT_TYPES).isEmpty()){
                StackTypeSet tos = types.get(types.size()-1);
                types.add(tos);
                return new FrameLayout(types, frameLayout.hasUnknownLeadingTypes());
            }

            if(types.size() < 2 && !frameLayout.hasUnknownLeadingTypes())
                throw new IllegalArgumentException("FrameLayout not supported");
            if(types.size() == 1){
                StackTypeSet tos = types.get(0);
                return new FrameLayout(Arrays.asList(ANY,tos,ANY,tos), true);
            }
            if(types.size() == 0){
                return new FrameLayout(Arrays.asList(ANY,ANY,ANY,ANY), true);
            }
            StackTypeSet tos = types.get(types.size() - 1);
            StackTypeSet tosM1 = types.get(types.size() - 2);
            types.add(tosM1);
            types.add(tos);
            return new FrameLayout(types, frameLayout.hasUnknownLeadingTypes());

        }

        @Override
        public FrameLayout applyBackwards(FrameLayout frameLayout) {
            List<StackTypeSet> types = frameLayout.getTypes();
            int size = types.size();
            if(size > 1 && !types.get(size-1).intersection(TWO_SLOT_TYPES).isEmpty()){
                StackTypeSet tos = types.get(size-1);
                StackTypeSet tosM1 = types.get(size-2);
                Set<Integer> intersection = tos.intersection(tosM1);
                if(intersection.isEmpty())
                    throw new IllegalArgumentException("FrameLayout not supported");
                types.remove(size-1);
                return new FrameLayout(types, frameLayout.hasUnknownLeadingTypes());
            }
            if(size < 4 && !frameLayout.hasUnknownLeadingTypes())
                throw new IllegalArgumentException("FrameLayout not supported");
            if(size == 3 || size == 2){
                StackTypeSet tos = types.get(size - 1);
                StackTypeSet tosM1 = types.get(size - 2);
                return new FrameLayout(Arrays.asList(tosM1,tos), true);
            }
            if(size == 1){
                return new FrameLayout(Arrays.asList(ANY,types.get(size -1)),true);
            }
            if(size == 0){
                return new FrameLayout(Arrays.asList(ANY,ANY), true);
            }
            StackTypeSet tos = types.remove(size-1);
            StackTypeSet tosM1 = types.remove(size - 2);
            StackTypeSet tosM2 = types.remove(size - 3);
            StackTypeSet tosM3 = types.remove(size - 4);
            if(tos.intersection(tosM2).isEmpty())
                throw new IllegalStateException("tos and tos-2 are not intersecting");
            if(tosM1.intersection(tosM3).isEmpty())
                throw new IllegalStateException("tos-1 and tos-3 are not intersecting");
            types.add(StackTypeSet.of(tosM1.intersection(tosM3)));
            types.add(StackTypeSet.of(tos.intersection(tosM2)));
            return new FrameLayout(types, frameLayout.hasUnknownLeadingTypes());
        }

        @Override
        public FrameLayout computeMinimalBefore() {
            return new FrameLayout(Collections.singletonList(ANY), true);
        }

        @Override
        public FrameLayout computeMinimalAfter() {
            return new FrameLayout(Arrays.asList(ANY,ANY),true);
        }
    }

    @Override
    public TypeStackManipulation getStackManipulation(VariableTable table, ByteCodeInstruction instruction) {
        return new Dup2StackManipulation();
    }
}
