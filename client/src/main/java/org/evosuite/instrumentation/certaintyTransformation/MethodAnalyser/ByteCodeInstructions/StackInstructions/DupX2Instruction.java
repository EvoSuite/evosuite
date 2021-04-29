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
import static org.objectweb.asm.Opcodes.DUP_X2;

public class DupX2Instruction extends StackInstruction {
    public DupX2Instruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor,"DUP_X2", instructionNumber, DUP_X2);
    }

    private static class DupX2StackManipulation extends TypeStackManipulation{

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
            int size = types.size();
            if(size < 3 && !frameLayout.hasUnknownLeadingTypes()){
                throw new IllegalStateException();
            }
            if(size == 2){
                types.add(0, ANY);
                types.add(0,types.get(size));
                return new FrameLayout(types, true);
            }
            if(size == 1){
                types.add(0, ANY);
                types.add(0, ANY);
                types.add(0,types.get(size+1));
                return new FrameLayout(types, true);
            }
            if(size == 0){
                return new FrameLayout(Arrays.asList(ANY,ANY,ANY,ANY),true);
            }
            types.add(size-3,types.get(size-1));
            return new FrameLayout(types, true);
        }

        @Override
        public FrameLayout applyBackwards(FrameLayout frameLayout) {
            List<StackTypeSet> types = frameLayout.getTypes();
            int size = types.size();
            if(size < 4)
                throw new IllegalStateException();
            types.remove(size-4);
            return new FrameLayout(types, frameLayout.hasUnknownLeadingTypes());
        }

        @Override
        public FrameLayout computeMinimalBefore() {
            return new FrameLayout(Arrays.asList(ANY,ANY,ANY), true);
        }

        @Override
        public FrameLayout computeMinimalAfter() {
            return new FrameLayout(Arrays.asList(ANY,ANY,ANY,ANY),true);
        }
    }

    @Override
    public TypeStackManipulation getStackManipulation(VariableTable table,
                                                      ByteCodeInstruction instruction) {
        return new DupX2StackManipulation();
    }
}
