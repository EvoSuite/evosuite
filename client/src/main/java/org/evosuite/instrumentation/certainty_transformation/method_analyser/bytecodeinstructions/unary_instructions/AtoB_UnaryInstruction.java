package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.unary_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AtoB_UnaryInstruction extends ByteCodeInstruction {
    private final StackTypeSet operand;
    private final StackTypeSet returnType;

    public AtoB_UnaryInstruction(String className, String methodName, int lineNumber,
                                 String methodDescriptor, String label,
                                 int instructionNumber, int operand, int returnType, int opcode) {
        super(className, methodName, lineNumber, methodDescriptor, label, instructionNumber, opcode);
        this.operand = StackTypeSet.of(operand);
        this.returnType = StackTypeSet.of(returnType);
    }

    public AtoB_UnaryInstruction(String className, String methodName, int lineNumber,
                                 String methodDescriptor, String label,
                                 int instructionNumber, StackTypeSet operand, StackTypeSet returnType, int opcode) {
        super(className, methodName, lineNumber, methodDescriptor, label, instructionNumber, opcode);
        this.operand = operand;
        this.returnType = returnType;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.singletonList(new StackTypeSet(operand));
    }

    @Override
    public StackTypeSet pushedToStack() {
        return returnType;
    }

    @Override
    public boolean writesVariable(int localVariableIndex) {
        return false;
    }

    @Override
    public Set<Integer> writesVariables() {
        return Collections.emptySet();
    }

    @Override
    public Set<Integer> readsVariables() {
        return Collections.emptySet();
    }
}
