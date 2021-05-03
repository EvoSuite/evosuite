package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.stack_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.variables.VariableTable;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.FrameLayout;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.TypeStack;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.TypeStackManipulation;

import java.util.Arrays;

import static org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet.ANY;
import static org.objectweb.asm.Opcodes.DUP2_X1;

public class Dup2X1Instruction extends StackInstruction {
    public Dup2X1Instruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "DUP_X2", instructionNumber, DUP2_X1);
    }

    private static class Dup2X1StackManipulation extends TypeStackManipulation{

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
            return null;
        }

        @Override
        public FrameLayout applyBackwards(FrameLayout frameLayout) {
            return null;
        }

        @Override
        public FrameLayout computeMinimalBefore() {
            return new FrameLayout(Arrays.asList(ANY, ANY), true);
        }

        @Override
        public FrameLayout computeMinimalAfter() {
            return new FrameLayout(Arrays.asList(ANY,ANY,ANY), true);
        }
    }

    @Override
    public TypeStackManipulation getStackManipulation(VariableTable table, ByteCodeInstruction instruction) {
        return new DupX1Instruction.DupX1StackManipulation();
    }
}
