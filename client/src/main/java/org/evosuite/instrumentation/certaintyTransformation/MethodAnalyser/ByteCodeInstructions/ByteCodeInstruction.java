package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph.NodeContent;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.TryCatch.TryCatchBlock;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Variables.VariableTable;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.ConsumePushStackManipulation;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.TypeStackManipulation;

import java.io.Serializable;
import java.util.*;

/**
 * Abstract ByteCodeInstruction used as content for ControlFlowGraphs
 */
public abstract class ByteCodeInstruction implements NodeContent, Serializable {

    protected final String className;
    protected final String methodName;
    protected final int lineNumber;
    protected final String methodDescriptor;
    protected final String label;
    protected final int instructionNumber;
    protected final int opcode;

    /**
     * Constructor for the abstract ByteCodeInstruction
     *
     * @param className name of the class, the instruction is in
     * @param methodName name of the method the instruction is part of
     * @param lineNumber line in the source code, that contains this instruction
     * @param methodDescriptor descriptor of the method the instruction is part of
     * @param label String representation of the Instruction
     * @param instructionNumber number inside the method
     * @param opcode Opcode according to ASM
     */
    public ByteCodeInstruction(String className, String methodName, int lineNumber, String methodDescriptor, String label,
                               int instructionNumber, int opcode) {
        Objects.requireNonNull(className);
        Objects.requireNonNull(methodName);
        Objects.requireNonNull(label);
        Objects.requireNonNull(methodDescriptor);
        this.opcode = opcode;
        this.methodDescriptor = methodDescriptor;
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
        this.label = label;
        this.instructionNumber = instructionNumber;
    }

    public Collection<Integer> getSuccessors() {
        return Collections.singleton(instructionNumber + 1);
    }

    public Collection<Integer> getSuccessors(List<TryCatchBlock> handlers){
        return getSuccessors();
    }

    public abstract List<StackTypeSet> consumedFromStack();

    public abstract StackTypeSet pushedToStack();

    public List<StackTypeSet> consumedFromStack(VariableTable table){
        return consumedFromStack();
    }

    public StackTypeSet pushedToStack(VariableTable table){
        return pushedToStack();
    }

    public TypeStackManipulation getStackManipulation(VariableTable table, ByteCodeInstruction instruction) {
        return new ConsumePushStackManipulation(consumedFromStack(table), pushedToStack(table));
    }

    public abstract boolean writesVariable(int localVariableIndex);

    protected boolean tryCatchBlockInRange(TryCatchBlock block){
        final int order = getOrder();
        return block.getStart().getOrder() <= order && order < block.getEnd().getOrder();
    }

    public int getOpcode() {
        return opcode;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public String getLabel() {
        return String.join(":", Integer.toString(instructionNumber), label);
    }

    @Override
    public int getOrder() {
        return instructionNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getMethodDescriptor() {
        return methodDescriptor;
    }

    public int getInstructionNumber() {
        return instructionNumber;
    }

    @Override
    public String toString() {
        return "ByteCodeInstruction{" + "className='" + className + '\'' + ", methodName='" + methodName + '\'' + ", "
                + "lineNumber=" + lineNumber + ", label='" + label + '\'' + ", instructionNumber=" + instructionNumber + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ByteCodeInstruction that = (ByteCodeInstruction) o;

        if (lineNumber != that.lineNumber) return false;
        if (instructionNumber != that.instructionNumber) return false;
        if (!className.equals(that.className)) return false;
        if (!methodName.equals(that.methodName)) return false;
        return label.equals(that.label);
    }

    @Override
    public int hashCode() {
        int result = className.hashCode();
        result = 31 * result + methodName.hashCode();
        result = 31 * result + lineNumber;
        result = 31 * result + label.hashCode();
        result = 31 * result + instructionNumber;
        return result;
    }

    public abstract Set<Integer> writesVariables();
    public abstract Set<Integer> readsVariables();
}