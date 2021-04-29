package org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.ConditionalJumpInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.SwitchInstructions.SwitchInstruction;

public interface DistanceRuntimeStorage {

    /**
     * Code, which should be executed before the actual method
     */
    void initInstrumentation();

    /**
     * Stores the top of stack (tos) for the conditional jump instruction.
     * The operand stack will remain the same.
     *
     * @param instruction The instruction tos is associated with.
     */
    void storeValueOf(ConditionalJumpInstruction instruction);

    /**
     * Pushes the stored distance to the Operand stack:
     * Stack: [...] -> [...I]
     *
     * @param instruction The instruction of which the corresponding distance should be pushed to the operand stack.
     */
    void pushDistanceToOperandStack(ConditionalJumpInstruction instruction);

    /**
     * This function will be called before visitMaxs
     *
     * @return returns by how much the locals are increased.
     */
    int finishInstrumentation();

    void markDependentUpdateEntered(BooleanToIntMethodVisitor.DependentUpdate dependentUpdate);

    void markDependentUpdateWritten(BooleanToIntMethodVisitor.DependentUpdate dependentUpdate);

    void markDependentUpdateExited(BooleanToIntMethodVisitor.DependentUpdate dependentUpdateExited);

    /**
     * Pushes the state of the dependent update to the stack
     *
     * @param dependentUpdate
     */
    void dependentUpdateValue(BooleanToIntMethodVisitor.DependentUpdate dependentUpdate);

    /**
     * Stores the top of stack (tos) for the switch case.
     * The operand stack will remain the same.
     *
     * @param instruction
     * @param key
     */
    void storeValueOf(SwitchInstruction instruction, ByteCodeInstruction key);

    void pushDistanceToOperandStack(SwitchInstruction instruction, ByteCodeInstruction key);

    int addTemporaryRegister(String desc);

    int getMaxLocalVariable();
}
