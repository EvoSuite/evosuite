package org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation;

import org.apache.commons.lang3.tuple.Pair;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.ConditionalJumpInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.SwitchInstructions.SwitchInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph.ControlFlowGraph;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph.Node;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.*;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;

public class RegisterRuntimeStorage implements DistanceRuntimeStorage {

    protected final Map<ConditionalJumpInstruction, Integer> localVariableIndexMap;
    protected final int localVariableTableSize;
    protected final MethodVisitor methodVisitor;
    protected final Label methodStartLabel;
    protected final Label methodEndLabel;
    private int temporaryRegistersForFunctionCall;
    protected final Map<BooleanToIntMethodVisitor.DependentUpdate, Integer> dependentUpdateIndexMap;
    // Map of Pair (Instruction, Destination) -> Distance
    protected final Map<Pair<SwitchInstruction, ByteCodeInstruction>, Integer> switchIndexMap;
    private static final int DEFAULT_FALSE = Integer.MIN_VALUE;
    private final static String DISTANCE_VARIABLE_PREFIX = "distance";
    private final static String DEPENDENT_UPDATE_VARIABLE_PREFIX = "dependent_update";
    private final static String FUNCTION_CALL_VARIABLE_PREFIX = "function_parameter";
    private Map<Integer,String> functionCallRegisters = new HashMap<>();

    public RegisterRuntimeStorage(ControlFlowGraph controlFlowGraph,
                                  int localVariableTableSize,
                                  MethodVisitor methodVisitor,
                                  Label methodStartLabel,
                                  Label methodEndLabel,
                                  Set<BooleanToIntMethodVisitor.DependentUpdate> dependentUpdates) {
        this.localVariableTableSize = localVariableTableSize;
        this.methodVisitor = methodVisitor;
        this.methodStartLabel = methodStartLabel;
        this.methodEndLabel = methodEndLabel;
        localVariableIndexMap = computeLocalVariableIndexMap(controlFlowGraph, localVariableTableSize);
        dependentUpdateIndexMap =
                computeDependentUpdateIndexMap(localVariableTableSize + localVariableIndexMap.size(), dependentUpdates);
        int i = localVariableTableSize + localVariableIndexMap.size() + dependentUpdateIndexMap.size();
        switchIndexMap =
                computeSwitchIndexMap(i,
                        controlFlowGraph.getNodes().stream()
                                .map(Node::getContent)
                                .filter(x -> x instanceof SwitchInstruction)
                                .map(x -> (SwitchInstruction) x).
                                collect(Collectors.toSet()));
    }

    private static Map<Pair<SwitchInstruction, ByteCodeInstruction>, Integer> computeSwitchIndexMap(int offset,
                                                                                                    Collection<SwitchInstruction> tableSwitches) {
        Map<Pair<SwitchInstruction, ByteCodeInstruction>, Integer> tableSwitchMap = new HashMap<>();
        for (SwitchInstruction tableSwitch : tableSwitches) {
            List<ByteCodeInstruction> destinations = tableSwitch.getDestinations();
            for (int i = 0; i < destinations.size(); i++) {
                tableSwitchMap.put(Pair.of(tableSwitch, destinations.get(i)), offset++);
            }
            tableSwitchMap.put(Pair.of(tableSwitch, tableSwitch.getDefault()), offset++);
        }
        return tableSwitchMap;
    }

    static Map<ConditionalJumpInstruction, Integer> computeLocalVariableIndexMap(ControlFlowGraph controlFlowGraph,
                                                                                 int localVariableTableSize) {
        Map<ConditionalJumpInstruction, Integer> localVariableIndexMap = new HashMap<>();
        Collection<ByteCodeInstruction> conditionalJumps =
                controlFlowGraph.getFilteredNodeSet(n -> n instanceof ConditionalJumpInstruction);
        int counter = localVariableTableSize;
        for (ByteCodeInstruction conditionalJump : conditionalJumps) {
            if (conditionalJump instanceof ConditionalJumpInstruction)
                localVariableIndexMap.put((ConditionalJumpInstruction) conditionalJump, counter++);
            else
                throw new IllegalStateException("controlFlowGraph.getFilteredNodeSet returned a Bytecode instruction " +
                        "not assignable to ConditionalJump");
        }
        return localVariableIndexMap;
    }

    static Map<BooleanToIntMethodVisitor.DependentUpdate, Integer> computeDependentUpdateIndexMap(int offset,
                                                                                                  Set<BooleanToIntMethodVisitor.DependentUpdate> dependentUpdates) {
        Map<BooleanToIntMethodVisitor.DependentUpdate, Integer> dependentUpdateIntegerMap = new HashMap<>();
        int counter = offset;
        for (BooleanToIntMethodVisitor.DependentUpdate dependentUpdate : dependentUpdates) {
            dependentUpdateIntegerMap.put(dependentUpdate, counter++);
        }
        return dependentUpdateIntegerMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initInstrumentation() {
        localVariableIndexMap.forEach((key, value) -> {
            methodVisitor.visitLdcInsn(DEFAULT_FALSE);
            methodVisitor.visitVarInsn(ISTORE, value);
        });
        dependentUpdateIndexMap.forEach((key,value) -> {
            methodVisitor.visitInsn(ICONST_M1);
            methodVisitor.visitVarInsn(ISTORE, value);
        });
        switchIndexMap.forEach((key,value) -> {
            methodVisitor.visitLdcInsn(DEFAULT_FALSE);
            methodVisitor.visitVarInsn(ISTORE,value);
        }) ;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeValueOf(ConditionalJumpInstruction instruction) {
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ISTORE, localVariableIndexMap.get(instruction));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pushDistanceToOperandStack(ConditionalJumpInstruction instruction) {
        methodVisitor.visitVarInsn(ILOAD, localVariableIndexMap.get(instruction));
    }

    private int getAdditionalRegisterCount(){
        return localVariableIndexMap.size() + dependentUpdateIndexMap.size() + switchIndexMap.size() + functionCallRegisters.values().stream().mapToInt(d -> Type.getType(d).getSize()).sum();
    }

    @Override
    public int finishInstrumentation() {
        localVariableIndexMap.forEach((key, value) -> methodVisitor.visitLocalVariable(getVariableName(key), "I",
                null, methodStartLabel, methodEndLabel, value));
        dependentUpdateIndexMap.forEach((key, value) -> methodVisitor.visitLocalVariable(getDependentUpdateName(key), "I", null,
                methodStartLabel, methodEndLabel, value));
        switchIndexMap.forEach((key,value) -> methodVisitor.visitLocalVariable(getSwitchName(key), "I", null, methodStartLabel, methodEndLabel,
                value));
        functionCallRegisters.forEach((key,value) -> methodVisitor.visitLocalVariable(getRegisterForFunctionCallName(key), value, null, methodStartLabel,
                methodEndLabel,key));
        return getAdditionalRegisterCount();
    }

    @Override
    public int getMaxLocalVariable(){
        int max = localVariableTableSize;
        max = Math.max(localVariableIndexMap.values().stream().mapToInt(x->x).sum(), max);
        max = Math.max(dependentUpdateIndexMap.values().stream().mapToInt(x->x).sum(), max);
        max = Math.max(switchIndexMap.values().stream().mapToInt(x->x).sum(), max);
        max = Math.max(functionCallRegisters.keySet().stream().mapToInt(x->x).sum(), max);
        return max;
    }

    private String getRegisterForFunctionCallName(Integer key) {
        return FUNCTION_CALL_VARIABLE_PREFIX + "_" +key;
    }

    private String getSwitchName(Pair<SwitchInstruction, ByteCodeInstruction> key) {
        return DISTANCE_VARIABLE_PREFIX + "_switch_" + Math.abs(key.hashCode());
    }

    @Override
    public void markDependentUpdateEntered(BooleanToIntMethodVisitor.DependentUpdate dependentUpdate) {
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitVarInsn(ISTORE, dependentUpdateIndexMap.get(dependentUpdate));
    }

    @Override
    public void markDependentUpdateWritten(BooleanToIntMethodVisitor.DependentUpdate dependentUpdate) {
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitVarInsn(ISTORE, dependentUpdateIndexMap.get(dependentUpdate));
    }

    @Override
    public void markDependentUpdateExited(BooleanToIntMethodVisitor.DependentUpdate dependentUpdate) {
        methodVisitor.visitInsn(ICONST_M1);
        methodVisitor.visitVarInsn(ISTORE, dependentUpdateIndexMap.get(dependentUpdate));
    }

    @Override
    public void dependentUpdateValue(BooleanToIntMethodVisitor.DependentUpdate dependentUpdate) {
        methodVisitor.visitVarInsn(ILOAD, dependentUpdateIndexMap.get(dependentUpdate));
    }

    private static String getVariableName(ConditionalJumpInstruction instr) {
        return DISTANCE_VARIABLE_PREFIX + "ConditionalJump" + instr.getOrder();
    }

    private static String getDependentUpdateName(BooleanToIntMethodVisitor.DependentUpdate dependentUpdate){
        return DEPENDENT_UPDATE_VARIABLE_PREFIX + "_" + dependentUpdate.getStart().getOrder() + "_" + dependentUpdate.getEnd().getOrder() + "_" + dependentUpdate.getLocalVariableIndex();
    }

    @Override
    public void storeValueOf(SwitchInstruction instruction, ByteCodeInstruction key) {
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ISTORE, switchIndexMap.get(Pair.of(instruction, key)));
    }

    @Override
    public void pushDistanceToOperandStack(SwitchInstruction instruction, ByteCodeInstruction key) {
        methodVisitor.visitVarInsn(ILOAD, switchIndexMap.get(Pair.of(instruction,key)));
    }

    @Override
    public int addTemporaryRegister(String desc){
        int i = getAdditionalRegisterCount() + localVariableTableSize;
        functionCallRegisters.put(i,desc);
        return i;
    }
}
