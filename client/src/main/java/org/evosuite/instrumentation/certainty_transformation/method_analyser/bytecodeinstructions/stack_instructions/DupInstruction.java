package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.stack_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.variables.VariableTable;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.FrameLayout;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.TypeStack;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.TypeStackManipulation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.objectweb.asm.Opcodes.DUP;

public class DupInstruction extends StackInstruction {

    private static class DupStackManipulation extends TypeStackManipulation {

        @Override
        public TypeStack apply(TypeStack s) {
            StackTypeSet pop = s.pop();
            s.push(pop);
            s.push(pop);
            return s;
        }

        @Override
        public TypeStack applyBackwards(TypeStack s) {
            StackTypeSet pop1 = s.pop();
            StackTypeSet pop2 = s.pop();
            if (!pop1.equals(pop2))
                throw new IllegalArgumentException("Dup can not be applied backwards to the given Stack");
            s.push(pop1);
            return s;
        }

        @Override
        public FrameLayout apply(FrameLayout frameLayout) {
            List<StackTypeSet> types = frameLayout.getTypes();
            if(types.size() == 0 && frameLayout.hasUnknownLeadingTypes()) {
                return new FrameLayout(Arrays.asList(StackTypeSet.ANY, StackTypeSet.ANY), true);
            }
            types.add(types.get(types.size()-1));
            return new FrameLayout(types, frameLayout.hasUnknownLeadingTypes());
        }

        @Override
        public FrameLayout applyBackwards(FrameLayout frameLayout) {
            List<StackTypeSet> types = frameLayout.getTypes();
            if(types.size() < 2 && frameLayout.hasUnknownLeadingTypes())
                return frameLayout;
            StackTypeSet remove = types.remove(types.size() - 1);
            StackTypeSet remove1 = types.remove(types.size() - 1);
            if(remove.intersection(remove1).isEmpty())
                throw new IllegalStateException("Can not apply Backwards");
            types.add(StackTypeSet.of(remove.intersection(remove1)));
            return new FrameLayout(types, frameLayout.hasUnknownLeadingTypes());
        }

        @Override
        public FrameLayout computeMinimalBefore() {
            return new FrameLayout(Collections.emptyList(), true);
        }

        @Override
        public FrameLayout computeMinimalAfter() {
            return new FrameLayout(Collections.emptyList(), true);
        }
    }

    public DupInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor,"DUP", instructionNumber, DUP);
    }

    @Override
    public TypeStackManipulation getStackManipulation(VariableTable table,
                                                      ByteCodeInstruction instruction) {
        return new DupStackManipulation();
    }
}
