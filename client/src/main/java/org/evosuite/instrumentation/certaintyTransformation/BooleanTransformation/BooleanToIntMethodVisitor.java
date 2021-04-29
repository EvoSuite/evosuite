package org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation;

import org.apache.commons.lang3.tuple.Pair;
import org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation.InstrumentationListeners.InstrumentationListener;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxAtoB_BinaryInstructions.DCmpgInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxAtoB_BinaryInstructions.DCmplInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxAtoB_BinaryInstructions.FCmpgInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxAtoB_BinaryInstructions.FCmplInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.InvokeInstructions.InvokeSpecialInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.ConditionalJumpInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.JumpInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.UnaryJumpInstructions.IfEqInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.UnaryJumpInstructions.IfNeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.MethodEnter;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions.ConstantInstructions.ConstantInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions.ConstantInstructions.IntConstantInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.StoreInstructions.IStoreInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.SwitchInstructions.LookupSwitchInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.SwitchInstructions.SwitchInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.SwitchInstructions.TableSwitchInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph.Edge;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.MethodAnalysisResult;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Variables.VariableLifetime;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Variables.VariableTable;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.InstructionInputOutputFrames;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.evosuite.runtime.instrumentation.AnnotatedLabel;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.LabelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;

public class BooleanToIntMethodVisitor extends MethodVisitor {

    private final static Logger logger = LoggerFactory.getLogger(BooleanToIntMethodVisitor.class);

    private final String methodName;
    private final Predicate<String> classIsInstrumented;
    private final ControlFlowGraph controlFlowGraph;
    private final VariableTable variableTable;
    private final Map<Integer, List<ByteCodeInstruction>> groupByOpcode;
    private final Map<ByteCodeInstruction, InstructionInputOutputFrames> framesMap;
    private final int localVariableTableSize;
    private final Label methodStartLabel = new Label();
    private final Label methodEndLabel = new Label();
    private final DistanceRuntimeStorage runtimeStorage;
    private final BooleanToIntLogic logic;
    private final Set<DependentUpdate> dependentUpdates;
    private final int access;
    private final String descriptor;
    private final boolean useCdgForConstantsReplacement;
    private final boolean hasJumps;
    private boolean hasVariableTable = false;
    private int superConstructorOrder;

    public boolean hasVariableTable() {
        return hasVariableTable;
    }

    private final Set<InstrumentationListener> instrumentationListeners = new HashSet<>();

    /**
     * Constructor for a BooleanToIntMethodVisitor.
     *
     * @param methodVisitor                 for chaining of MethodVisitors.
     * @param methodName                    the methodName of the function this Visitor visits.
     * @param classIsInstrumentedPredicate  A java.util.{@link Predicate} to determine whether a class is instrumented.
     * @param methodAnalysisResult          a result of the MethodAnalyser method visitor. (provides a CFG, variable table and
     * @param instrumentationListeners
     * @param access
     * @param descriptor
     * @param useCdgForConstantsReplacement
     */
    public BooleanToIntMethodVisitor(MethodVisitor methodVisitor,
                                     String methodName,
                                     Predicate<String> classIsInstrumentedPredicate,
                                     MethodAnalysisResult methodAnalysisResult,
                                     Set<InstrumentationListener> instrumentationListeners,
                                     int access,
                                     String descriptor,
                                     boolean useCdgForConstantsReplacement) {
        super(ASM7, methodVisitor);
        this.instrumentationListeners.addAll(instrumentationListeners);
        this.descriptor = descriptor;
        this.useCdgForConstantsReplacement = useCdgForConstantsReplacement;
        Objects.requireNonNull(methodAnalysisResult);
        this.methodName = methodName;
        this.classIsInstrumented = classIsInstrumentedPredicate;
        controlFlowGraph = methodAnalysisResult.getControlFlowGraph();
        variableTable = methodAnalysisResult.getVariableTable();
        framesMap = methodAnalysisResult.getFramesMap();
        hasJumps = methodAnalysisResult.isHasJumps();
        this.access = access;
        groupByOpcode = controlFlowGraph.groupBy(ByteCodeInstruction::getOpcode);
        localVariableTableSize = computeLocalVariableTableSize() + 1; //(ACC_STATIC & access) != 0 ? 0:1;
        dependentUpdates = findDependentUpdates(true);
        this.runtimeStorage =
                new RegisterRuntimeStorage(controlFlowGraph, localVariableTableSize, mv, methodStartLabel,
                        methodEndLabel, dependentUpdates);
        this.logic = new StaticMethodsLogic(mv);
        //new MapRuntimeStorage(controlFlowGraph, localVariableTableSize, mv, methodStartLabel, methodEndLabel);
    }

    private int computeLocalVariableTableSize() {
        for (ByteCodeInstruction content : controlFlowGraph.getContents()) {
            Set<Integer> integers = content.writesVariables();
            if(integers == null)
                throw new IllegalStateException();
        }
        int maxWrite =
                controlFlowGraph.getContents().stream().map(ByteCodeInstruction::writesVariables).flatMap(Set::stream).mapToInt(n->n).max().orElse(0);
        int maxRead =
                controlFlowGraph.getContents().stream().map(ByteCodeInstruction::readsVariables).flatMap(Set::stream).mapToInt(n->n).max().orElse(0);
        return Math.max(maxRead, maxWrite)+1;
        /*
        return variableTable.stream().map(variableLifetime -> variableLifetime.getIndex() + Type.getType
                (variableLifetime.getDesc()).getSize()).reduce(Integer::max).orElse(0) + 2;
                */
    }

    @Override
    public void visitLocalVariable(String name,
                                   String descriptor,
                                   String signature,
                                   Label start,
                                   Label end,
                                   int index) {
        this.hasVariableTable = true;
        String desc = descriptor.equals(BooleanToIntTransformer.fromDescriptor) ? BooleanToIntTransformer.toDescriptor : descriptor;
        super.visitLocalVariable(name, desc, signature, start,
                end, index);
    }

    @Override
    public void visitCode() {
        super.visitCode();
        super.visitLabel(methodStartLabel);
        if(!methodName.equals("<init>")){
            runtimeStorage.initInstrumentation();
        }else {
            Set<Node<ByteCodeInstruction>> collect = controlFlowGraph.getNodes().stream().filter(n -> n.getContent() instanceof InvokeSpecialInstruction).filter(
                    i -> ((InvokeSpecialInstruction) i.getContent()).getName().equals("<init>")).collect(Collectors.toSet());
            int min = collect.stream().mapToInt(i -> i.getContent().getOrder()).min().orElse(-1);
            if(min == -1)
                runtimeStorage.initInstrumentation();
            else
                this.superConstructorOrder = min;
        }
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        handleDependentUpdates(groupByOpcode.get(opcode).remove(0));
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        ByteCodeInstruction remove = groupByOpcode.get(opcode).remove(0);
        handleDependentUpdates(remove);
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        handleDependentUpdates(groupByOpcode.get(INVOKEDYNAMIC).remove(0));
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitLdcInsn(Object value) {
        handleDependentUpdates(groupByOpcode.get(LDC).remove(0));
        super.visitLdcInsn(value);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
        this.visitMethodInsn(opcode, owner, name, descriptor, opcode == INVOKEINTERFACE);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        handleDependentUpdates(groupByOpcode.get(IINC).remove(0));
        super.visitIincInsn(var, increment);
    }

    void switchDefaultDistance(SwitchInstruction instruction) {
        // Operand Stack: [..key] -> [..key,size]
        super.visitIntInsn(SIPUSH, instruction.getDestinations().size());
        // [..key,size] -> [..key,Arr]
        super.visitIntInsn(NEWARRAY, T_INT);
        List<ByteCodeInstruction> destinations = instruction.getDestinations();
        for (int i = 0; i < destinations.size(); i++) {
            ByteCodeInstruction destination = destinations.get(i);
            // [..key,Arr] -> [key,Arr,Arr]
            super.visitInsn(DUP);
            // [..key,Arr,Arr] -> [..key,Arr,Arr,idx]
            super.visitIntInsn(SIPUSH, i);
            // [..key,Arr,Arr,idx] -> [..key,Arr,Arr,idx,dist]
            runtimeStorage.pushDistanceToOperandStack(instruction, destination);
            // [..key,Arr,Arr,idx,dist] -> [..key,Arr]
            super.visitInsn(IASTORE);
        }
        logic.callLogicalOr();
        logic.callLogicalNeg();
        runtimeStorage.storeValueOf(instruction, instruction.getDefault());
        super.visitInsn(POP);
    }

    void beforeTableSwitch(TableSwitchInstruction instruction) {
        // Operand stack: [..key]
        List<ByteCodeInstruction> destinations = instruction.getDestinations();
        for (int i = 0; i < destinations.size(); i++) {
            ByteCodeInstruction destination = destinations.get(i);
            // [..key] -> [..key,key]
            super.visitInsn(DUP);
            // [..key] -> [..key,key,i]
            super.visitIntInsn(SIPUSH, i);
            // [..key] -> [..key,dist]
            logic.callIfIntCmpEq();
            // [..key,dist] -> [..key,dist]
            runtimeStorage.storeValueOf(instruction, destination);
            super.visitInsn(POP);
        }
        switchDefaultDistance(instruction);
    }

    void beforeLookupSwitch(LookupSwitchInstruction instruction) {
        // Operand stack: [..key]
        Set<Map.Entry<Integer, ByteCodeInstruction>> entries = instruction.getDestinationMap().entrySet();
        for (Map.Entry<Integer, ByteCodeInstruction> entry : entries) {
            ByteCodeInstruction destination = entry.getValue();
            super.visitInsn(DUP);
            super.visitIntInsn(SIPUSH, entry.getKey());
            logic.callIfIntCmpEq();
            runtimeStorage.storeValueOf(instruction, destination);
            super.visitInsn(POP);
        }
        switchDefaultDistance(instruction);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        ByteCodeInstruction remove = groupByOpcode.get(TABLESWITCH).remove(0);
        handleDependentUpdates(remove);
        beforeTableSwitch((TableSwitchInstruction) remove);
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        ByteCodeInstruction remove = groupByOpcode.get(LOOKUPSWITCH).remove(0);
        handleDependentUpdates(remove);
        beforeLookupSwitch((LookupSwitchInstruction) remove);
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        handleDependentUpdates(groupByOpcode.get(MULTIANEWARRAY).remove(0));
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
    }


    @Override
    public void visitTypeInsn(int opcode, String type) {
        handleDependentUpdates(groupByOpcode.get(opcode).remove(0));
        super.visitTypeInsn(opcode, type);
    }


    private void transformIfFlagJump(StackTypeSet tos, ConditionalJumpInstruction jumpInstruction,
                                     VoidCallable ifBoolean, VoidCallable _else,
                                     Label label) {
        if (tos.equals(StackTypeSet.BOOLEAN)) {
            // Assumption: Rest of Bytecode is instrumented. Therefore when executing this statement tos will
            // be an integer.
            // now jump if tos is less or equal to 0.
            ifBoolean.call();
            notifyFlagJump(jumpInstruction);
        } else {
            _else.call();
        }
        prepareJump(jumpInstruction, label);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitLabel(methodEndLabel);
        int additionalLocalVariables = runtimeStorage.finishInstrumentation();
        super.visitMaxs(maxStack + 100, additionalLocalVariables > 0 ? runtimeStorage.getMaxLocalVariable() + 1 : localVariableTableSize);
    }

    void visitConditionalJump(int opcode, Label label, ConditionalJumpInstruction jumpInstruction) {
        StackTypeSet tos, tosM1;
        List<StackTypeSet> incoming;
        switch (opcode) {
            case IF_ICMPEQ:
                // TODO check if boolean comparison
                incoming = framesMap.get(jumpInstruction).getIncoming();
                tos = incoming.get(incoming.size() - 1);
                tosM1 = incoming.get(incoming.size() - 2);
                if (tos.equals(StackTypeSet.BOOLEAN) && tosM1.equals(StackTypeSet.BOOLEAN)) {
                    transformIfFlagJump(tos, jumpInstruction, logic::callBooleanEq, logic::callIfIntCmpEq, label);
                } else {
                    logic.callIfIntCmpEq();
                    prepareJump(jumpInstruction, label);
                }
                break;
            case IF_ICMPNE:
                // TODO check if boolean comparison
                incoming = framesMap.get(jumpInstruction).getIncoming();
                tos = incoming.get(incoming.size() - 1);
                tosM1 = incoming.get(incoming.size() - 2);
                if (tos.equals(StackTypeSet.BOOLEAN) && tosM1.equals(StackTypeSet.BOOLEAN)) {
                    transformIfFlagJump(tos, jumpInstruction, logic::callBooleanNe, logic::callIfIntCmpNe, label);
                } else {
                    logic.callIfIntCmpNe();
                    prepareJump(jumpInstruction, label);
                }
                break;
            case IF_ICMPLE:
                logic.callIfIntCmpLe();
                prepareJump(jumpInstruction, label);
                break;
            case IF_ICMPLT:
                logic.callIfIntCmpLt();
                prepareJump(jumpInstruction, label);
                break;
            case IF_ICMPGT:
                logic.callIfIntCmpGt();
                prepareJump(jumpInstruction, label);
                break;
            case IF_ICMPGE:
                logic.callIfIntCmpGe();
                prepareJump(jumpInstruction, label);
                break;
            case IFLE:
                logic.callIfCmpLe();
                prepareJump(jumpInstruction, label);
                break;
            case IFLT:
                logic.callIfCmpLt();
                prepareJump(jumpInstruction, label);
                break;
            case IFGE:
                logic.callIfCmpGe();
                prepareJump(jumpInstruction, label);
                break;
            case IFGT:
                logic.callIfCmpGt();
                prepareJump(jumpInstruction, label);
                break;
            case IF_ACMPEQ:
                logic.callIfACmpEq();
                prepareJump(jumpInstruction, label);
                break;
            case IF_ACMPNE:
                logic.callIfACmpNe();
                prepareJump(jumpInstruction, label);
                break;
            case IFEQ:
                // Analyze whether the top of the stack is a boolean based on the original CFG.
                // controlFlowGraph.getPredecessors(new IfEqInstruction())
                if (!(jumpInstruction instanceof IfEqInstruction))
                    throw new IllegalStateException("No ifEqInstructions left in the provided CFG");
                incoming = framesMap.get(jumpInstruction).getIncoming();
                tos = incoming.get(incoming.size() - 1);
                transformIfFlagJump(tos, jumpInstruction, logic::callIfCmpLe,
                        logic::callIfCmpEq,
                        label);
                break;
            // Current runtime stack:
            // [..., top] with top being a integer, byte, char or
            // We should only transform the jump if it operates on booleans.
            case IFNE:
                if (!(jumpInstruction instanceof IfNeInstruction))
                    throw new IllegalStateException("No IfNeInstructions left in the provided CFG");
                // framesMap + " !");
                incoming = framesMap.get(jumpInstruction).getIncoming();
                tos = incoming.get(incoming.size() - 1);
                // controlFlowGraph.computeTypeOfTopAt(jumpInstruction, variableTable);
                transformIfFlagJump(tos, jumpInstruction, logic::callIfCmpGt, logic::callIfCmpNe, label);
                break;
            case IFNONNULL:
                logic.callIfNonNull();
                prepareJump(jumpInstruction, label);
                break;
            case IFNULL:
                logic.callIfNull();
                prepareJump(jumpInstruction, label);
                break;
            default:
                throw new IllegalArgumentException("Default case when instrumenting a conditional flag instruction");
                // super.visitJumpInsn(opcode, label);
        }
    }



    @Override
    public void visitJumpInsn(int opcode, Label label) {
        ByteCodeInstruction instruction = groupByOpcode.get(opcode).remove(0);
        handleDependentUpdates(instruction);
        if (!(instruction instanceof JumpInstruction))
            throw new IllegalStateException("Could not find a matching Jump Instruction to the provided opcode");
        if (instruction instanceof ConditionalJumpInstruction) {
            ConditionalJumpInstruction conditionalJumpInstruction = (ConditionalJumpInstruction) instruction;
            notifyConditionalJump(conditionalJumpInstruction);
            visitConditionalJump(opcode, label, conditionalJumpInstruction);
        } else {
            super.visitJumpInsn(opcode, label);
        }
    }


    @Override
    public void visitInsn(int opcode) {
        List<StackTypeSet> types;
        ByteCodeInstruction remove = groupByOpcode.get(opcode).remove(0);
        handleDependentUpdates(remove);
        switch (opcode) {
            case ICONST_0:
                if (!(remove instanceof IntConstantInstruction))
                    throw new IllegalStateException("");
                // IntConstantInstruction always has 1 successor.
                if(!hasJumps){
                    /*ByteCodeInstruction next = controlFlowGraph.getSuccessors(remove).iterator().next();
                    List<StackTypeSet> types_ = next.consumedFromStack();
                    StackTypeSet type = types_.size() > 0 ? types_.get(types_.size()-1): StackTypeSet.ANY;
                    if(type.equals(StackTypeSet.BOOLEAN)){
                        super.visitLdcInsn(Integer.MIN_VALUE);
                    } else {
                        super.visitInsn(opcode);
                    }*/
                    super.visitInsn(opcode);
                    return;
                }
                types = framesMap.get(remove).getOutputFrames().values().iterator().next().getTypes();
                if (types.get(types.size() - 1).equals(StackTypeSet.BOOLEAN)) {
                    handleFalseConstant((IntConstantInstruction) remove);
                    logic.enforceNegativeConstraint();
                } else {
                    super.visitInsn(opcode);
                    return;
                }
                break;
            case ICONST_1:
                if (!(remove instanceof IntConstantInstruction))
                    throw new IllegalStateException("");
                // IntConstantInstruction always has 1 successor.
                if(!hasJumps){
                    /*ByteCodeInstruction next = controlFlowGraph.getSuccessors(remove).iterator().next();
                    List<StackTypeSet> types_ = next.consumedFromStack();
                    StackTypeSet type = types_.size() > 0 ? types_.get(types_.size()-1): StackTypeSet.ANY;
                    if(type.equals(StackTypeSet.BOOLEAN)){
                        super.visitLdcInsn(Integer.MIN_VALUE);
                    } else {
                        super.visitInsn(opcode);
                    }*/
                    super.visitInsn(opcode);
                    return;
                }
                types = framesMap.get(remove).getOutputFrames().values().iterator().next().getTypes();
                if (types.get(types.size() - 1).equals(StackTypeSet.BOOLEAN)) {
                    handleTrueConstant((IntConstantInstruction) remove);
                    logic.enforcePositiveConstraint();
                } else {
                    super.visitInsn(opcode);
                    return;
                }
                break;
            case DCMPL:
                if (!(remove instanceof DCmplInstruction))
                    throw new IllegalStateException("");
                logic.callDCMPL();
                break;
            case DCMPG:
                if (!(remove instanceof DCmpgInstruction))
                    throw new IllegalStateException("");
                // DCMPG consumes to double values from the stack and pushes the comparison to the stack:
                // [..d1,d2] -> [..res]
                //           1 if d1 > d2
                // res =     0 if d1 == d2
                //          -1 if d1 < d2
                logic.callDCMPG();
                break;
            case FCMPG:
                if (!(remove instanceof FCmpgInstruction))
                    throw new IllegalStateException("");
                logic.callFCMPG();
                break;
            case FCMPL:
                if (!(remove instanceof FCmplInstruction))
                    throw new IllegalStateException("");
                logic.callFCMPL();
                break;
            case IOR:
                types = framesMap.get(remove).getOutputFrames().values().iterator().next().getTypes();
                if(types.get(types.size()-1).equals(StackTypeSet.BOOLEAN)){
                    logic.callBinaryLogicalOr();
                } else {
                    super.visitInsn(IOR);
                }
                break;
            case IAND:
                types = framesMap.get(remove).getOutputFrames().values().iterator().next().getTypes();
                if(types.get(types.size()-1).equals(StackTypeSet.BOOLEAN)){
                    logic.callBinaryLogicalAnd();
                } else {
                    super.visitInsn(IAND);
                }
                break;
            case INEG:
                types = framesMap.get(remove).getOutputFrames().values().iterator().next().getTypes();
                if(types.get(types.size()-1).equals(StackTypeSet.BOOLEAN))
                    logic.callLogicalNeg();
                else
                    super.visitInsn(INEG);
                break;
            default:
                super.visitInsn(opcode);
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        ByteCodeInstruction remove = groupByOpcode.get(opcode).remove(0);
        handleDependentUpdates(remove);
        if (classIsInstrumented.test(owner) && !name.equals("<init>") && BooleanToIntTransformer.changes(descriptor)) {
            // The owner class of the called method is instrumented. Therefore we can use the instrumented method.
            super.visitMethodInsn(opcode, owner, BooleanToIntTransformer.instrumentedMethodName(name, descriptor),
                    BooleanToIntTransformer.instrumentedMethodDescriptor(descriptor), isInterface);
        } else {
            /*
             Conversion of certainty booleans on the operand stack to native booleans (if used as parameter for this
             function call
             */
            Type[] argumentTypes = Type.getArgumentTypes(descriptor);
            for (int i = 0; i < argumentTypes.length; i++) {
                Type t = argumentTypes[i];
                if (t.getDescriptor().equals("Z")) {
                    List<Pair<String, Integer>> indices = new ArrayList<>();
                    for (int j = argumentTypes.length - 1; j > i; j--) {
                        Type o = argumentTypes[j];
                        String oDescriptor = o.getDescriptor();
                        if (oDescriptor.equals(BooleanToIntTransformer.fromDescriptor)) {
                            oDescriptor = BooleanToIntTransformer.toDescriptor;
                            logic.callFromInt();
                        }
                        int index = runtimeStorage.addTemporaryRegister(oDescriptor);
                        indices.add(0, Pair.of(oDescriptor, index));
                        loadOrStoreAtIndex(index, o.getDescriptor(), ISTORE, DSTORE, FSTORE, LSTORE, ASTORE);
                    }
                    logic.callFromInt();
                    for (Pair<String, Integer> pair : indices) {
                        Integer index = pair.getRight();
                        String oDescriptor = pair.getLeft();
                        loadOrStoreAtIndex(index, oDescriptor, ILOAD, DLOAD, FLOAD, LLOAD, ALOAD);
                    }
                    break;
                }
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            if (Type.getReturnType(descriptor).getDescriptor().equals("Z")) {
                logic.callToInt();
            }
        }
        if(superConstructorOrder == remove.getOrder())
            runtimeStorage.initInstrumentation();
    }

    private void loadOrStoreAtIndex(Integer index, String oDescriptor, int iload, int dload, int fload, int lload, int aload) {
        switch (oDescriptor) {
            case "S":
            case "C":
            case "B":
            case "Z":
            case "I":
                super.visitVarInsn(iload, index);
                break;
            case "D":
                super.visitVarInsn(dload, index);
                break;
            case "F":
                super.visitVarInsn(fload, index);
                break;
            case "J":
                super.visitVarInsn(lload, index);
                break;
            default:
                super.visitVarInsn(aload, index);
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        handleDependentUpdates(groupByOpcode.get(opcode).remove(0));
        // If the Field is not a boolean Field or the owner class will not be instrumented, we dont change the call.
        if (!descriptor.equals("Z") || !classIsInstrumented.test(owner)) {
            super.visitFieldInsn(opcode, owner, name, descriptor);
            return;
        }
        switch (opcode) {
            case Opcodes.GETFIELD:
                // [..objectref]
                super.visitInsn(DUP);
                super.visitInsn(DUP);
                // [..objectref,objectref,objectref]
                super.visitFieldInsn(GETFIELD, owner, name + BooleanToIntTransformer.instrumentingSuffix, BooleanToIntTransformer.toDescriptor);
                // [..objectref,objectref,certaintyBool]
                logic.callFromInt();
                // [..objectref,objectref,field_instr]
                super.visitInsn(SWAP);
                // [..objectref,field_instr, objectref]
                super.visitFieldInsn(GETFIELD, owner, name, descriptor);
                // [..objectref,field_instr, field]
                Label after = new Label();
                startIgnore();
                super.visitJumpInsn(IF_ICMPEQ, after);
                endIgnore();
                // [..objectref]
                super.visitInsn(DUP);
                super.visitInsn(DUP);
                // [..objectref,objectref,objectref]
                super.visitFieldInsn(opcode, owner, name, descriptor);
                // [..objectref,objectref,field]
                logic.callToInt();
                // [..objectref,objcetref,fieldAsCertainty]
                super.visitFieldInsn(PUTFIELD, owner, name + BooleanToIntTransformer.instrumentingSuffix, BooleanToIntTransformer.toDescriptor);
                // [..objectref]
                super.visitLabel(after);
                super.visitFieldInsn(GETFIELD, owner, name + BooleanToIntTransformer.instrumentingSuffix, BooleanToIntTransformer.toDescriptor);
                return;
            case Opcodes.GETSTATIC:
                // [..]
                super.visitFieldInsn(GETSTATIC, owner, name, descriptor);
                // [..field]
                super.visitFieldInsn(GETSTATIC, owner, name + BooleanToIntTransformer.instrumentingSuffix, BooleanToIntTransformer.toDescriptor);
                // [..field,certainty]
                logic.callFromInt();
                // [..field,fieldInstr]
                Label afterStatic = new Label();
                startIgnore();
                super.visitJumpInsn(IF_ICMPEQ, afterStatic);
                endIgnore();
                // [..]
                super.visitFieldInsn(GETSTATIC, owner, name, descriptor);
                logic.callToInt();
                super.visitFieldInsn(PUTSTATIC, owner, name + BooleanToIntTransformer.instrumentingSuffix, BooleanToIntTransformer.toDescriptor);
                super.visitLabel(afterStatic);
                super.visitFieldInsn(opcode, owner, name + BooleanToIntTransformer.instrumentingSuffix, BooleanToIntTransformer.toDescriptor);
                return;
            case Opcodes.PUTFIELD:
                // Operand Stack: [...,object_ref, value] (value is already transformed into an int)
                super.visitInsn(Opcodes.DUP2);
                // Operand Stack: [...,object_ref, value, object_ref, value] (value is already transformed into an int)
                super.visitFieldInsn(opcode, owner, name + BooleanToIntTransformer.instrumentingSuffix, BooleanToIntTransformer.toDescriptor);
                break;
            case Opcodes.PUTSTATIC:
                // Operand Stack: [..., value] (value is already transformed into an int)
                super.visitInsn(Opcodes.DUP);
                // Operand Stack: [..., value, value] (value is already transformed into an int)
                super.visitFieldInsn(opcode, owner, name + BooleanToIntTransformer.instrumentingSuffix, BooleanToIntTransformer.toDescriptor);
                break;
            default:
                throw new IllegalArgumentException("Illegal argument Opcode " + opcode + " for visitFieldInsn");
        }
        super.visitMethodInsn(INVOKESTATIC, BooleanToIntTransformer.BOOLEAN_TO_INT_UTIL_NAME, BooleanToIntTransformer.FROM_INT_NAME_AND_DESCRIPTOR.getName(),
                BooleanToIntTransformer.FROM_INT_NAME_AND_DESCRIPTOR.getDescriptor(), false);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }


    /**
     * Instrumentation of every jump. First an int value representing "how true or false" is calculated. This value
     * gets stored. Then the decision is made best of the int value.
     *
     * @param instruction The instruction, used to determine the index of the value to be stored.
     * @param label       The label, to which the instruction should jump if the value represents true.
     */
    private void prepareJump(ConditionalJumpInstruction instruction,
                             Label label) {
        runtimeStorage.storeValueOf(instruction);
        super.visitJumpInsn(IFGT, label);
    }

    /**
     * This method is invoked, whenever a false constant is pushed to the stack.
     *
     * @param instruction The ICONST_0 instruction, that pushes the false constant.
     */
    void handleFalseConstant(IntConstantInstruction instruction) {
        handleTrueConstant(instruction);
        logic.callLogicalNeg();
    }

    /**
     * Pushes the distance of a path on the operand stack. The distance of the path is the logical and of all
     * branching conditions on the path:
     * condition1 & condition2 & condition3, where condition1-3 are the conditions on the path
     * <p>
     * Operand stack: [..] -> [..I]:
     *
     * @param path The path of which the distance is required.
     */
    void calculateTrueValueForPath(DirectedGraph.Path<ByteCodeInstruction> path) {
        List<Edge<ByteCodeInstruction>> branchingEdges = path.stream()
                .filter(edge -> (edge.getSource().getContent() instanceof ConditionalJumpInstruction))
                .collect(Collectors.toList());
        super.visitIntInsn(BIPUSH, branchingEdges.size());
        super.visitIntInsn(NEWARRAY, T_INT);
        for (int i = 0; i < branchingEdges.size(); ++i) {
            Edge<ByteCodeInstruction> branchingEdge = branchingEdges.get(i);
            ByteCodeInstruction instruction = branchingEdge.getSource().getContent();
            if (!(instruction instanceof ConditionalJumpInstruction)) {
                throw new IllegalStateException("That should not happen");
            }

            // [.. landArrayRef]
            super.visitInsn(DUP);
            // [.. landArrayRef,landArrayRef]
            super.visitIntInsn(BIPUSH, i);
            // [.. landArrayRef,landArrayRef,index]
            runtimeStorage.pushDistanceToOperandStack((ConditionalJumpInstruction) instruction);
            // [.. landArrayRef,landArrayRef,index,dist]
            if (branchingEdge instanceof EdgeLabelAdapter && ((EdgeLabelAdapter<ByteCodeInstruction>) branchingEdge).getLabel().equals("F")) {
                logic.callLogicalNeg();
            }
            super.visitInsn(IASTORE);
            // [... landArrayRef]
        }
        // [.. landArrayRef]
        logic.callLogicalAnd();
        // [.. pathDistance]
    }

    /**
     * This method is invoked, whenever a true constant is pushed to the stack. First all paths from entering the
     * method to {@param instruction} is computed. Then the individual values for each path are computed. At the end
     * all values of the paths are passed to the logical or function. The result of the call to the logical is used
     * as the true value at {@param instruction}.
     *
     * @param instruction The ICONST_1 instruction, that pushes the true constant.
     */
    void handleTrueConstant(IntConstantInstruction instruction) {
        InstructionGraph graph = useCdgForConstantsReplacement ? this.controlFlowGraph.getCDT() :
                this.controlFlowGraph;
        List<DirectedGraph.Path<ByteCodeInstruction>> acyclicPathsFromTo = new ArrayList<>(
                graph.getAllAcyclicPathsFromTo(graph.getInstructionByOpcode(-1).iterator().next(),
                        instruction));
        super.visitIntInsn(BIPUSH, acyclicPathsFromTo.size());
        super.visitIntInsn(NEWARRAY, T_INT);
        // [.. lorArrayRef]
        for (int j = 0; j < acyclicPathsFromTo.size(); ++j) {
            // [.. lorArrayRef]
            DirectedGraph.Path<ByteCodeInstruction> edges = acyclicPathsFromTo.get(j);
            super.visitInsn(DUP);
            // [.. lorArrayRef,lorArrayRef]
            super.visitIntInsn(BIPUSH, j);
            // [.. lorArrayRef,lorArrayRef,lorIndex]
            calculateTrueValueForPath(edges);
            // [.. lorArrayRef,lorArrayRef,lorIndex,pathDistance]
            super.visitInsn(IASTORE);
            // [.. lorArrayRef]
        }
        logic.callLogicalOr();
    }

    public boolean writesBoolean(ByteCodeInstruction instruction) {
        if (instruction instanceof IStoreInstruction) {
            int localVariableIndex = ((IStoreInstruction) instruction).getLocalVariableIndex();
            Set<VariableLifetime> lifetimesAtLocalVariableIndex = variableTable.getLifetimesAtLocalVariableIndex(localVariableIndex);
            Set<VariableLifetime> collect = lifetimesAtLocalVariableIndex.stream().filter(x -> x.isAliveAt(instruction, true)).collect(Collectors.toSet());
            if (collect.size() == 1) {
                return collect.iterator().next().getDesc().equals("Z");
            } else if (collect.size() > 1) {
                throw new IllegalStateException("More than 2 local variables are alive");
            }
        }
        return false;
    }

    public Set<DependentUpdate> findDependentUpdates(boolean immediateControlDependence) {
        Collection<ByteCodeInstruction> iStoreInstructions =
                controlFlowGraph.getFilteredNodeSet(x -> x instanceof IStoreInstruction && writesBoolean(x));
        return iStoreInstructions.stream().map(x -> findDependentUpdatesOf(x, immediateControlDependence))
                .flatMap(Collection::stream).filter(this::variableSurvivesBranch).collect(Collectors.toSet());
    }

    public boolean variableSurvivesBranch(DependentUpdate d) {
        Set<VariableLifetime> lifetimesAtLocalVariableIndex = variableTable.getLifetimesAtLocalVariableIndex(d.getLocalVariableIndex());
        Set<VariableLifetime> aliveAtStart =
                lifetimesAtLocalVariableIndex.stream().filter(l -> l.isAliveAt(d.getStart(), true)).collect(Collectors.toSet());
        if (aliveAtStart.size() > 1) {
            throw new IllegalStateException("That should not happen");
        }
        Set<VariableLifetime> aliveAtEnd = lifetimesAtLocalVariableIndex.stream().filter(l -> l.isAliveAt(d.getEnd(), true)).collect(Collectors.toSet());
        if (aliveAtEnd.size() > 1) {
            throw new IllegalStateException("That should not happen");
        }
        if (aliveAtStart.size() == 1 && aliveAtEnd.size() == 1)
            return aliveAtStart.iterator().next() == aliveAtEnd.iterator().next();
        else
            return false;
    }

    public Set<DependentUpdate> findDependentUpdatesOf(ByteCodeInstruction instruction,
                                                       boolean immediateControlDependence) {
        if (!(instruction instanceof IStoreInstruction))
            return Collections.emptySet();
        Set<ByteCodeInstruction> controlDependencies;
        if (immediateControlDependence) {
            controlDependencies = controlFlowGraph.getCDT().getImmediateControlDependencies(instruction, true);
        } else {
            controlDependencies = controlFlowGraph.getCDT().getControlDependencies(instruction, true);
        }
        Set<DependentUpdate> exclusiveBranches = new HashSet<>();
        Map<Pair<ByteCodeInstruction, ByteCodeInstruction>, Set<ByteCodeInstruction>> found = new HashMap<>();
        for (ByteCodeInstruction controlDependency : controlDependencies) {
            if(controlDependency instanceof MethodEnter)
                continue;
            Optional<ByteCodeInstruction> mostCommonDominator =
                    controlFlowGraph.getPDT().getMostCommonDominator(instruction,
                            controlDependency, true);
            if (mostCommonDominator.isPresent()) {
                ByteCodeInstruction right = mostCommonDominator.get();
                Pair<ByteCodeInstruction, ByteCodeInstruction> key = Pair.of(controlDependency,
                        right);
                HashSet<ByteCodeInstruction> writingInstruction = new HashSet<>(Collections.singleton(instruction));
                Set<DirectedGraph.Path<ByteCodeInstruction>> paths =
                        controlFlowGraph.getAllAcyclicPathsFromTo(controlDependency,
                                right);
                boolean b = false;
                for (DirectedGraph.Path<ByteCodeInstruction> path : paths) {
                    Set<ByteCodeInstruction> collect = path.streamOfElements().
                            filter(i -> i.writesVariable(((IStoreInstruction) instruction)
                                    .getLocalVariableIndex())).collect(Collectors.toSet());
                    if (!collect.isEmpty()) {
                        writingInstruction.addAll(collect);
                    } else {
                        b = true;
                    }
                }
                if (b)
                    exclusiveBranches.add(new DependentUpdate(((IStoreInstruction) instruction).getLocalVariableIndex(),
                            controlDependency, right, writingInstruction));
            }
        }
        return exclusiveBranches;
    }


    public static class DependentUpdate {
        private final int localVariableIndex;
        private final ByteCodeInstruction start;
        private final ByteCodeInstruction end;
        private final Set<ByteCodeInstruction> writingInstructions;

        DependentUpdate(int localVariableIndex, ByteCodeInstruction start, ByteCodeInstruction end,
                        Set<ByteCodeInstruction> writingInstructions) {
            this.localVariableIndex = localVariableIndex;
            this.start = start;
            this.end = end;
            this.writingInstructions = writingInstructions;
        }

        public int getLocalVariableIndex() {
            return localVariableIndex;
        }

        public ByteCodeInstruction getStart() {
            return start;
        }

        public ByteCodeInstruction getEnd() {
            return end;
        }

        public Set<ByteCodeInstruction> getWritingInstructions() {
            return new HashSet<>(writingInstructions);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DependentUpdate)) return false;

            DependentUpdate that = (DependentUpdate) o;

            if (getLocalVariableIndex() != that.getLocalVariableIndex()) return false;
            if (getStart() != null ? !getStart().equals(that.getStart()) : that.getStart() != null) return false;
            if (getEnd() != null ? !getEnd().equals(that.getEnd()) : that.getEnd() != null) return false;
            return getWritingInstructions() != null ? getWritingInstructions().equals(that.getWritingInstructions()) : that.getWritingInstructions() == null;
        }

        @Override
        public int hashCode() {
            int result = getLocalVariableIndex();
            result = 31 * result + (getStart() != null ? getStart().hashCode() : 0);
            result = 31 * result + (getEnd() != null ? getEnd().hashCode() : 0);
            result = 31 * result + (getWritingInstructions() != null ? getWritingInstructions().hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "DependentUpdate{" +
                    "localVariableIndex=" + localVariableIndex +
                    ", start=" + start.getOrder() +
                    ", end=" + end.getOrder() +
                    ", writingInstructions=" + writingInstructions.stream().map(ByteCodeInstruction::getOrder).collect(Collectors.toSet()) +
                    '}';
        }
    }

    private void handleDependentUpdates(ByteCodeInstruction instruction) {
        for (DependentUpdate dependentUpdate : dependentUpdates) {
            if (dependentUpdate.start.equals(instruction)) {
                notifyDependentUpdate(dependentUpdate);
                // handle start of dependent update
                Label afterIf = new Label();
                // curStack: [..]
                runtimeStorage.dependentUpdateValue(dependentUpdate);
                // curStack [..I]
                startIgnore();
                super.visitJumpInsn(IFNE, afterIf);
                endIgnore();
                // curStack [..]
                callUpdate(dependentUpdate);
                runtimeStorage.markDependentUpdateExited(dependentUpdate);

                // curStack[..]
                super.visitLabel(afterIf);
                runtimeStorage.markDependentUpdateEntered(dependentUpdate);
            }
            if (dependentUpdate.end.equals(instruction)) {
                // handle end of dependent update
                Label afterIf = new Label();
                runtimeStorage.dependentUpdateValue(dependentUpdate);
                startIgnore();
                super.visitJumpInsn(IFNE, afterIf);
                endIgnore();
                callUpdate(dependentUpdate);
                super.visitLabel(afterIf);
                runtimeStorage.markDependentUpdateExited(dependentUpdate);
            }
            if (dependentUpdate.writingInstructions.contains(instruction)) {
                // handle write of dependent update
                runtimeStorage.markDependentUpdateWritten(dependentUpdate);
            }
        }
    }

    private void callIIUpdate(DependentUpdate dependentUpdate) {
        super.visitVarInsn(ILOAD, dependentUpdate.getLocalVariableIndex());
        runtimeStorage.pushDistanceToOperandStack((ConditionalJumpInstruction) dependentUpdate.getStart());
        logic.callUpdate();
        super.visitVarInsn(ISTORE, dependentUpdate.getLocalVariableIndex());
    }

    private void callIIIUpdate(DependentUpdate dependentUpdate, int opcode) {
        super.visitVarInsn(ILOAD, dependentUpdate.getLocalVariableIndex());
        runtimeStorage.pushDistanceToOperandStack((ConditionalJumpInstruction) dependentUpdate.getStart());
        super.visitInsn(opcode);
        logic.callUpdateWithReassignmentValue();
        super.visitVarInsn(ISTORE, dependentUpdate.getLocalVariableIndex());
    }

    private void callUpdate(DependentUpdate dependentUpdate) {
        // curStack [..]
        if (dependentUpdate.getStart() instanceof ConditionalJumpInstruction) {
            // check if all writes are all true/false
            Set<ByteCodeInstruction> allPredecessors = dependentUpdate.getWritingInstructions().stream()
                    .map(controlFlowGraph::getPredecessors)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            if (allPredecessors.stream().allMatch(i -> i instanceof IntConstantInstruction)) {
                int sum = allPredecessors.stream().map(i -> (IntConstantInstruction) i).mapToInt(ConstantInstruction::getValue).sum();
                if (sum == allPredecessors.size() || sum == 0) {
                    callIIIUpdate(dependentUpdate, sum > 0 ? ICONST_1 : ICONST_0);
                } else {
                    callIIUpdate(dependentUpdate);
                }
            } else {
                callIIUpdate(dependentUpdate);
            }
        }
    }

    private void startIgnore(){
        AnnotatedLabel label = new AnnotatedLabel(true, true);
        label.info = new LabelNode(label);
        label.setIgnoreFalse(true);
        super.visitLabel(label);
    }

    private void endIgnore(){
        AnnotatedLabel label = new AnnotatedLabel(true, false);
        label.info = new LabelNode(label);
        label.setIgnoreFalse(true);
        super.visitLabel(label);
    }

    void notifyDependentUpdate(DependentUpdate dependentUpdate){
        logger.debug("Found dependent update: {}", dependentUpdate);
        notifyHelper(listener -> listener.notifyDependentUpdate(dependentUpdate));
    }

    void notifyConditionalJump(ConditionalJumpInstruction conditionalJumpInstruction){
        logger.debug("Found conditional jump: {}", conditionalJumpInstruction);
        notifyHelper(listener -> listener.notifyConditionalJump(conditionalJumpInstruction));
    }

    void notifyFlagJump(ConditionalJumpInstruction conditionalJumpInstruction){
        logger.debug("Found flag jump: {}", conditionalJumpInstruction);
        notifyHelper(listener -> listener.notifyFlagJump(conditionalJumpInstruction));
    }

    void notifyHelper(Consumer<InstrumentationListener> consumer){
        instrumentationListeners.forEach(consumer);
    }
}
