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
import static org.objectweb.asm.Opcodes.DUP_X1;

public class DupX1Instruction extends StackInstruction {
    public DupX1Instruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor,"DUP_X1", instructionNumber, DUP_X1);
    }

    @Override
    public TypeStackManipulation getStackManipulation(VariableTable table, ByteCodeInstruction instruction) {
        return new DupX1StackManipulation();
    }

    public static class DupX1StackManipulation extends TypeStackManipulation{

        @Override
        public TypeStack apply(TypeStack s) {
            if(s.size() < 2)
                throw new IllegalArgumentException("DupX1 is not applicable to the given stack");
            StackTypeSet top = s.pop();
            StackTypeSet top1 = s.pop();
            s.push(top);
            s.push(top1);
            s.push(top);
            return s;
        }

        @Override
        public TypeStack applyBackwards(TypeStack s) {
            StackTypeSet top = s.pop();
            StackTypeSet top1 = s.pop();
            StackTypeSet top2 = s.pop();
            if(top.matches(top2))
               throw new IllegalArgumentException("DupX1 is not applicable to the given stack backwards");
            s.push(top1);
            s.push(top);
            return s;
        }

        @Override
        public FrameLayout apply(FrameLayout frameLayout) {
            List<StackTypeSet> types = frameLayout.getTypes();
            if(types.size() < 2 && !frameLayout.hasUnknownLeadingTypes())
                throw new IllegalArgumentException("DupX1 is not applicable to the given Frame layout");
            if(types.size() < 2) {
                if(types.size() == 0)
                    return frameLayout;
                types.add(0, ANY);
            }
            types.add(types.size() - 2, types.get(types.size()-1));
            return new FrameLayout(types, frameLayout.hasUnknownLeadingTypes());
        }

        @Override
        public FrameLayout applyBackwards(FrameLayout frameLayout) {
            List<StackTypeSet> types = frameLayout.getTypes();
            if(types.size() < 3 && !frameLayout.hasUnknownLeadingTypes())
                throw new IllegalArgumentException("DupX1 is not applicable to the given Frame layout");
            if(types.size() < 3)
                return frameLayout;
            types.remove(types.size()-3);
            return new FrameLayout(types, frameLayout.hasUnknownLeadingTypes());
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
