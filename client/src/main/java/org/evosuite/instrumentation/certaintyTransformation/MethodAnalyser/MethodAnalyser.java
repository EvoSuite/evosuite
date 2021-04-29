package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ArrayLoadInstructions.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ArrayStoreInstructions.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxAtoB_BinaryInstructions.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxBtoA_BinaryInstructions.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.DoubleBinaryInstructions.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.FloatBinaryInstructions.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.IntBinaryInstructions.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.LongBinaryInstructions.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.FieldInstructions.GetFieldInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.FieldInstructions.GetStaticInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.FieldInstructions.PutFieldInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.FieldInstructions.PutStaticInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.InvokeInstructions.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.BinaryJumpInstructionPlaceholders.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.GotoInstructionPlaceholder;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.JumpInstructionPlaceholder;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.JumpSubroutineInstructionPlaceholder;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.UnaryJumpInstructionPlaceholders.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.ConditionalJumpInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.JumpInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.JumpSubroutineInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.RetInstructionPlaceholder;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions.BipushInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions.ConstantInstructions.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions.LoadInstructions.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions.NewInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions.SipushInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ReturnInstructions.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.StackInstructions.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.StoreInstructions.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.SwitchInstructions.LookupSwitchInstructionPlaceholder;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.SwitchInstructions.SwitchInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.SwitchInstructions.SwitchInstructionPlaceholder;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.SwitchInstructions.TableSwitchInstructionPlaceholder;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.DoubleUnaryInstructions.DNegInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.FloatUnaryInstructions.FNegInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.IntUnaryInstructions.INegInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.LongUnaryInstructions.LNegInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.MixedUnaryInstructions.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph.Edge;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph.*;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.MethodAnalysisResult;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.MethodAnalysisResultStorage;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.MethodIdentifier;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.TryCatch.UnresolvedTryCatchBlock;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Variables.UnresolvedVariableLifetime;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Variables.VariableLifetime;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Variables.VariableTable;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.InstructionInputOutputFrames;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.*;

import java.util.*;
import java.util.stream.Collectors;

public class MethodAnalyser extends MethodVisitor {

    private String className;
    private String methodName;
    private final String descriptor;
    private final MethodAnalysisResultStorage storage;
    private final boolean isStatic;
    private DirectedGraphBuilder<ByteCodeInstruction> directedGraphBuilder = new MutableDirectedGraphBuilder<>();
    private int lineNumber = -1;
    private int instructionNumber = 0;
    private Label nextLabel;
    private Map<ByteCodeInstruction, Label> jumpsTo = new HashMap<>();
    private Map<ByteCodeInstruction, Label> switchDefaultsTo = new HashMap<>();
    private Map<ByteCodeInstruction, Collection<Label>> switchJumpsTo = new HashMap<>();
    private Map<Label, ByteCodeInstruction> labelToInstruction = new HashMap<>();
    private List<UnresolvedTryCatchBlock> tryCatchBlocks = new ArrayList<>();
    private List<UnresolvedVariableLifetime> unresolvedVariableLifetimes = new ArrayList<>();
    public final static int METHOD_ENTER_ORDER = -1;
    public final static int METHOD_EXIT_ORDER = -2;
    private boolean needFrames = false;


    public MethodAnalyser(MethodVisitor methodVisitor, String className, String methodName, String descriptor,
                          MethodAnalysisResultStorage storage, boolean isStatic) {
        super(Opcodes.ASM7, methodVisitor);
        this.className = className;
        this.methodName = methodName;
        this.descriptor = descriptor;
        this.storage = storage;
        this.isStatic = isStatic;
    }

    @Override
    public void visitCode() {
        super.visitCode();
    }

    void logMessage(String message) {
        // System.out.println("MethodAnalyser.MethodAnalyzer " + className + ":" + methodName + ":" + message);
    }

    void addEdges() {
        Set<Node<ByteCodeInstruction>> nodes = directedGraphBuilder.getNodes();
        int size = nodes.size();
        int i = 0;
        for (Node<ByteCodeInstruction> instructionNode : nodes) {
            addEdgesForNode(instructionNode);
        }
    }

    void addEdgesForNode(Node<ByteCodeInstruction> node) {
        Objects.requireNonNull(node);
        Collection<Integer> successors =
                node.getContent()
                        .getSuccessors(tryCatchBlocks.stream().map(x -> x.resolveLabels(labelToInstruction))
                                .collect(Collectors.toList()));
        if (directedGraphBuilder.getNodes().size() == 2){
            // Method does not contain any instruction:
            // => is interface or abstract method
            successors = Collections.singleton(METHOD_EXIT_ORDER);
        } else if (successors.isEmpty() && node.getContent().getOrder() != METHOD_EXIT_ORDER) {
            successors = Collections.singleton(METHOD_EXIT_ORDER);
        }
        List<Edge<ByteCodeInstruction>> collect =
                successors.stream()
                        .map(directedGraphBuilder::getByOrder)
                        .map(n -> this.generateEdge(node, n))
                        .collect(Collectors.toList());
        directedGraphBuilder = directedGraphBuilder.addEdges(collect);
    }

    Edge<ByteCodeInstruction> generateEdge(Node<ByteCodeInstruction> source, Node<ByteCodeInstruction> destination){
        if(source.getContent() instanceof ConditionalJumpInstruction){
            return new EdgeLabelAdapter<>(new UnweightedEdge<>(source,destination),
                    source.getContent().getOrder() + 1 == destination.getContent().getOrder()? "F" : "T");
        }
        return new UnweightedEdge<>(source,destination);
    }

    void replacePlaceholder() {
        Collection<Node<ByteCodeInstruction>> nodes = directedGraphBuilder.build().getNodes();
        List<Node<ByteCodeInstruction>> collect = nodes.stream().map(n -> {
            if (n.getContent() instanceof JumpInstructionPlaceholder) {
                JumpInstructionPlaceholder content = (JumpInstructionPlaceholder) n.getContent();
                JumpInstruction jumpInstruction =
                        content.setDestination(this.labelToInstruction.get(this.jumpsTo.get(content)));
                return new Node<ByteCodeInstruction>(jumpInstruction);
            } else if (n.getContent() instanceof SwitchInstructionPlaceholder) {
                SwitchInstructionPlaceholder content = (SwitchInstructionPlaceholder) n.getContent();
                List<ByteCodeInstruction> jumps = this.switchJumpsTo.get(content).stream().
                        map(labelToInstruction::get).collect(Collectors.toList());
                SwitchInstruction switchInstruction =
                        content.setDestinations(labelToInstruction.get(this.switchDefaultsTo.get(content)), jumps);
                return new Node<ByteCodeInstruction>(switchInstruction);
            } else return n;
        }).collect(Collectors.toList());
        List<Node<ByteCodeInstruction>> finalCollect = collect;
        Set<ByteCodeInstruction> jsrInsn =
                collect.stream().filter(n->n.getContent() instanceof JumpSubroutineInstruction)
                        .map(n-> finalCollect.stream().filter(m-> m.getContent().getOrder() == n.getContent().getOrder()+1).findFirst().get())
                        .map(Node::getContent)
                        .collect(Collectors.toSet());
        collect = collect.stream().map(n -> {
            if (n.getContent() instanceof RetInstructionPlaceholder) {
                return new Node<ByteCodeInstruction>(((RetInstructionPlaceholder) n.getContent()).setTargets(jsrInsn));
            } else
                return n;
        }).collect(Collectors.toList());
        directedGraphBuilder = new ImmutableDirectedGraphBuilder<>(collect, directedGraphBuilder.build().getEdges());
    }

    private void pushInstruction(ByteCodeInstruction instruction) {
        if (this.nextLabel != null) {
            this.labelToInstruction.put(nextLabel, instruction);
            nextLabel = null;
        }
        directedGraphBuilder = directedGraphBuilder.addNode(new Node<>(instruction));
    }

    public int getInstructionNumber() {
        return instructionNumber;
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        pushInstruction(new MultiANewArrayInstruction(className, methodName, lineNumber,descriptor,
                getNextInstructionNumber(),
                descriptor, numDimensions));
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        unresolvedVariableLifetimes.add(new UnresolvedVariableLifetime(new MethodIdentifier(className, methodName,
                descriptor), name, descriptor, signature, start, end, index));
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    @Override
    public void visitLabel(Label label) {
        // System.out.println("CFG: visiting Label: " + label);
        this.nextLabel = label;
        super.visitLabel(label);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        ByteCodeInstruction instruction;
        switch (opcode) {
            case Opcodes.BIPUSH:
                if (operand > Byte.MAX_VALUE || operand < Byte.MIN_VALUE)
                    throw new IllegalStateException("expecting a byte value for BIPUSH");
                instruction = new BipushInstruction(className, methodName, lineNumber,descriptor,
                        getNextInstructionNumber(),
                        (byte) operand);
                break;
            case Opcodes.SIPUSH:
                if (operand > Short.MAX_VALUE || operand < Short.MIN_VALUE)
                    throw new IllegalStateException("expecting short value for SIPUSH");
                instruction = new SipushInstruction(className, methodName, lineNumber,
                        descriptor, getNextInstructionNumber(),
                        (short) operand);
                break;
            case Opcodes.NEWARRAY:
                instruction = new NewArrayInstruction(className, methodName, lineNumber,descriptor,
                        getNextInstructionNumber(),
                        operand);
                break;
            default:
                throw new IllegalStateException("Forgot " + opcode);
        }
        pushInstruction(instruction);
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        ByteCodeInstruction instruction;
        switch (opcode) {
            case Opcodes.NEW:
                instruction = new NewInstruction(className, methodName, lineNumber,descriptor, this.getNextInstructionNumber(), type);
                break;
            case Opcodes.ANEWARRAY:
                instruction = new ANewArrayInstruction(className, methodName, lineNumber, descriptor,this.getNextInstructionNumber(),
                        type);
                break;
            case Opcodes.CHECKCAST:
                instruction = new CheckcastInstruction(className, methodName, lineNumber,descriptor, this.getNextInstructionNumber(),
                        type);
                break;
            case Opcodes.INSTANCEOF:
                instruction = new InstanceOfInstruction(className, methodName, lineNumber, descriptor,this.getNextInstructionNumber(),
                        type);
                break;
            default:
                throw new IllegalArgumentException("Not implemented Type Instruction: " + opcode);
        }
        pushInstruction(instruction);
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        ByteCodeInstruction instruction;
        switch (opcode) {
            case Opcodes.GETSTATIC:
                instruction = new GetStaticInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber(),
                        owner, name, descriptor);
                break;
            case Opcodes.PUTSTATIC:
                instruction = new PutStaticInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber(),
                        owner, name, descriptor);
                break;
            case Opcodes.GETFIELD:
                instruction = new GetFieldInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber(),
                        owner, name, descriptor);
                break;
            case Opcodes.PUTFIELD:
                instruction = new PutFieldInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber(),
                        owner, name, descriptor);
                break;
            default:
                throw new IllegalArgumentException("Not implemented Type instruction " + opcode);
        }
        pushInstruction(instruction);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        ByteCodeInstruction instruction;
        switch (opcode) {
            case Opcodes.IFEQ:
                needFrames = true;
                instruction = new IfEqInstructionPlaceholder(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.IFNE:
                needFrames = true;
                instruction = new IfNeInstructionPlaceholder(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.IFLT:
                needFrames = true;
                instruction = new IfLtInstructionPlaceholder(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.IFGE:
                needFrames = true;
                instruction = new IfGeInstructionPlaceholder(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.IFGT:
                needFrames = true;
                instruction = new IfGtInstructionPlaceholder(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.IFLE:
                needFrames = true;
                instruction = new IfLeInstructionPlaceholder(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.IF_ICMPEQ:
                needFrames = true;
                instruction = new IntCmpEqInstructionPlaceholder(className, methodName, lineNumber,descriptor,
                        getNextInstructionNumber());
                break;
            case Opcodes.IF_ICMPNE:
                needFrames = true;
                instruction = new IntCmpNeInstructionPlaceholder(className, methodName, lineNumber,descriptor,
                        getNextInstructionNumber());
                break;
            case Opcodes.IF_ICMPLT:
                needFrames = true;
                instruction = new IntCmpLtInstructionPlaceholder(className, methodName, lineNumber,descriptor,
                        getNextInstructionNumber());
                break;
            case Opcodes.IF_ICMPGE:
                needFrames = true;
                instruction = new IntCmpGeInstructionPlaceholder(className, methodName, lineNumber,descriptor,
                        this.getNextInstructionNumber());
                break;
            case Opcodes.IF_ICMPGT:
                needFrames = true;
                instruction = new IntCmpGtInstructionPlaceholder(className, methodName, lineNumber,descriptor,
                        this.getNextInstructionNumber());
                break;
            case Opcodes.IF_ICMPLE:
                needFrames = true;
                instruction = new IntCmpLeInstructionPlaceholder(className, methodName, lineNumber,descriptor,
                        getNextInstructionNumber());
                break;
            case Opcodes.IF_ACMPEQ:
                needFrames = true;
                instruction = new ObjectCmpEqInstructionPlaceholder(className, methodName, lineNumber,descriptor,
                        getNextInstructionNumber());
                break;
            case Opcodes.IF_ACMPNE:
                needFrames = true;
                instruction = new ObjectCmpNeInstructionPlaceholder(className, methodName, lineNumber,descriptor,
                        getNextInstructionNumber());
                break;
            case Opcodes.GOTO:
                needFrames = true;
                instruction = new GotoInstructionPlaceholder(className, methodName, lineNumber,descriptor,
                        this.getNextInstructionNumber());
                break;
            case Opcodes.JSR:
                needFrames = true;
                instruction = new JumpSubroutineInstructionPlaceholder(className, methodName, lineNumber,descriptor,
                        getNextInstructionNumber());
                break;
            case Opcodes.IFNULL:
                needFrames = true;
                instruction = new IfNullInstructionPlaceholder(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.IFNONNULL:
                needFrames = true;
                instruction = new IfNonNullInstructionPlaceholder(className, methodName, lineNumber,descriptor,
                        getNextInstructionNumber());
                break;
            default:
                throw new IllegalArgumentException("Forgot to implement: " + opcode);
        }
        this.jumpsTo.put(instruction, label);
        pushInstruction(instruction);
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        ByteCodeInstruction instruction;
        switch (opcode) {
            case Opcodes.ILOAD:
                instruction = new ILoadInstruction(className, methodName, lineNumber,descriptor, var,
                        this.getNextInstructionNumber(), isStatic);
                break;
            case Opcodes.LLOAD:
                instruction = new LLoadInstruction(className, methodName, lineNumber, descriptor,var, this.getNextInstructionNumber(), isStatic);
                break;
            case Opcodes.FLOAD:
                instruction = new FLoadInstruction(className, methodName, lineNumber,descriptor, var, this.getNextInstructionNumber(), isStatic);
                break;
            case Opcodes.DLOAD:
                instruction = new DLoadInstruction(className, methodName, lineNumber,descriptor, var, this.getNextInstructionNumber(), isStatic);
                break;
            case Opcodes.ALOAD:
                instruction = new ALoadInstruction(className, methodName, lineNumber,descriptor, var,
                        this.getNextInstructionNumber(), isStatic);
                break;
            case Opcodes.ISTORE:
                instruction = new IStoreInstruction(className, methodName, lineNumber,descriptor, var, this.getNextInstructionNumber());
                break;
            case Opcodes.LSTORE:
                instruction = new LStoreInstruction(className, methodName, lineNumber, descriptor,var, this.getNextInstructionNumber());
                break;
            case Opcodes.FSTORE:
                instruction = new FStoreInstruction(className, methodName, lineNumber,descriptor, var, this.getNextInstructionNumber());
                break;
            case Opcodes.DSTORE:
                instruction = new DStoreInstruction(className, methodName, lineNumber,descriptor, var, this.getNextInstructionNumber());
                break;
            case Opcodes.ASTORE:
                instruction = new AStoreInstruction(className, methodName, lineNumber,descriptor, var, this.getNextInstructionNumber());
                break;
            case Opcodes.RET:
                instruction = new RetInstructionPlaceholder(className,methodName,lineNumber,descriptor,
                        this.getNextInstructionNumber());
                break;
            default:
                throw new IllegalArgumentException("Forgot to implement: " + opcode);
        }
        pushInstruction(instruction);
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        InvokeInstruction instruction;
        switch (opcode) {
            case Opcodes.INVOKESPECIAL:
                instruction = new InvokeSpecialInstruction(className, methodName, lineNumber, descriptor,owner, name, descriptor
                        , this.getNextInstructionNumber());
                break;
            case Opcodes.INVOKEVIRTUAL:
                instruction = new InvokeVirtualInstruction(className, methodName, lineNumber,descriptor, owner, name, descriptor
                        , this.getNextInstructionNumber());
                break;
            case Opcodes.INVOKESTATIC:
                instruction = new InvokeStaticInstruction(className, methodName, lineNumber, descriptor,owner, name, descriptor,
                        this.getNextInstructionNumber());
                break;
            case Opcodes.INVOKEINTERFACE:
                instruction = new InvokeInterfaceInstruction(className, methodName, lineNumber,descriptor, owner, name,
                        descriptor, this.getNextInstructionNumber());
                break;
            default:
                throw new IllegalArgumentException("Forgot to implement: " + opcode);
        }
        pushInstruction(instruction);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
                                       Object... bootstrapMethodArguments) {
        ByteCodeInstruction instruction = new InvokeDynamicInstruction(className, methodName, lineNumber,descriptor,
                bootstrapMethodHandle.getOwner(), name, descriptor, getNextInstructionNumber());
        pushInstruction(instruction);
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        IIncInstruction instruction = new IIncInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber(), var
                , increment);
        pushInstruction(instruction);
        super.visitIincInsn(var, increment);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        this.lineNumber = line;
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitLdcInsn(Object cst) {
        int type;
        if (cst instanceof Integer) {
            type = Type.INT;
        } else if (cst instanceof Float) {
            type = Type.FLOAT;
        } else if (cst instanceof Long) {
            type = Type.LONG;
        } else if (cst instanceof Double) {
            type = Type.DOUBLE;
        } else if (cst instanceof String) {
            type = Type.OBJECT;
        } else if (cst instanceof Type) {
            int sort = ((Type) cst).getSort();
            if (sort == Type.OBJECT) {
                type = Type.OBJECT;
            } else if (sort == Type.ARRAY) {
                type = Type.ARRAY;
            } else if (sort == Type.METHOD) {
                type = Type.METHOD;
            } else {
                throw new IllegalStateException("Unknown Constant");
            }
        } else if (cst instanceof Handle) {
            throw new IllegalStateException("Unknown Constant");
        } else if (cst instanceof ConstantDynamic) {
            throw new IllegalStateException("Unknown Constant");
        } else {
            throw new IllegalStateException("Unknown Constant");
        }
        LoadConstantInstruction instruction = new LoadConstantInstruction(className, methodName, lineNumber,descriptor,
                getNextInstructionNumber(), cst, StackTypeSet.of(type));
        pushInstruction(instruction);
        super.visitLdcInsn(cst);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        needFrames = true;
        TableSwitchInstructionPlaceholder instruction = new TableSwitchInstructionPlaceholder(className, methodName,
                lineNumber,descriptor, getNextInstructionNumber(), min, max);
        switchDefaultsTo.put(instruction, dflt);
        switchJumpsTo.put(instruction, Arrays.asList(labels));
        pushInstruction(instruction);
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        needFrames = true;
        LookupSwitchInstructionPlaceholder instruction = new LookupSwitchInstructionPlaceholder(className, methodName,
                lineNumber, descriptor,getNextInstructionNumber(), keys);
        switchDefaultsTo.put(instruction, dflt);
        switchJumpsTo.put(instruction, Arrays.asList(labels));
        pushInstruction(instruction);
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitEnd() {
        MethodEnter methodEnter = new MethodEnter(className, methodName, descriptor);
        MethodExit methodExit = new MethodExit(className, methodName, descriptor);
        directedGraphBuilder = directedGraphBuilder.addNode(new Node<>(methodEnter));
        directedGraphBuilder = directedGraphBuilder.addNode(new Node<>(methodExit));
        replacePlaceholder();
        addEdges();
        List<VariableLifetime> variableLifetimes =
                unresolvedVariableLifetimes.stream().map(v -> v.resolve(labelToInstruction)).collect(Collectors.toList());

        //Collection<Node<ByteCodeInstruction>> nodes = directedGraphBuilder.getGraph().getNodes();
        //System.out.println(nodes.stream().sorted(Comparator.comparingInt(x -> x.getContent().getOrder())).map(n ->
        // n.getContent().getLabel()).collect(Collectors.joining("\n")));
        //directedGraphBuilder.getGraph().getEdges().stream().sorted(Comparator.comparingInt(x -> x.getSource()
        // .getContent().getOrder())).forEach(System.out::println);
        MethodIdentifier identifier = new MethodIdentifier(className, methodName, descriptor);
        if (storage != null) {
            ControlFlowGraph controlFlowGraph = new ControlFlowGraph(directedGraphBuilder.build());
            VariableTable variableTable = new VariableTable(variableLifetimes);
            Map<ByteCodeInstruction, InstructionInputOutputFrames> framesMap = null;
            if(needFrames) {
                framesMap = controlFlowGraph.computeStackFrameLayouts(variableTable);
                // null;
            }
            storage.put(identifier, new MethodAnalysisResult(controlFlowGraph, variableTable, framesMap, needFrames));
        }
        super.visitEnd();
    }

    @Override
    public void visitInsn(int opcode) {
        ByteCodeInstruction instruction;
        switch (opcode) {
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
            case Opcodes.ICONST_M1:
                instruction = new IntConstantInstruction(className, methodName, lineNumber,descriptor, opcode - Opcodes.ICONST_0
                        , this.getNextInstructionNumber());
                break;
            case Opcodes.NOP:
                instruction = new NopInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.ACONST_NULL:
                instruction = new ConstNullInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.LCONST_0:
            case Opcodes.LCONST_1:
                instruction = new LConstantInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber(),
                        opcode - Opcodes.LCONST_0);
                break;
            case Opcodes.FCONST_0:
            case Opcodes.FCONST_1:
            case Opcodes.FCONST_2:
                instruction = new FConstantInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber(),
                        opcode - Opcodes.FCONST_0);
                break;
            case Opcodes.DCONST_0:
            case Opcodes.DCONST_1:
                instruction = new DConstantInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber(),
                        opcode - Opcodes.DCONST_0);
                break;
            case Opcodes.IALOAD:
                instruction = new IArrayLoadInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.LALOAD:
                instruction = new LArrayLoadInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.FALOAD:
                instruction = new FArrayLoadInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.DALOAD:
                instruction = new DArrayLoadInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.AALOAD:
                // TODO validate for 2-Dimensional arrays
                instruction = new AArrayLoadInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.BALOAD:
                instruction = new BArrayLoadInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.CALOAD:
                instruction = new CArrayLoadInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.SALOAD:
                instruction = new SArrayLoadInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.IASTORE:
                instruction = new IArrayStoreInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.LASTORE:
                instruction = new LArrayStoreInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.FASTORE:
                //new MethodAnalyser.ByteCodeInstructions.ArrayStoreInstructions.FArrayStoreInstruction
                instruction = new FArrayStoreInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.DASTORE:
                instruction = new DArrayStoreInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.AASTORE:
                instruction = new AArrayStoreInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.BASTORE:
                instruction = new BArrayStoreInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.CASTORE:
                instruction = new CArrayStoreInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.SASTORE:
                instruction = new SArrayStoreInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.POP:
                instruction = new PopInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.POP2:
                instruction = new Pop2Instruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.DUP:
                instruction = new DupInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.DUP_X1:
                instruction = new DupX1Instruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.DUP_X2:
                instruction = new DupX2Instruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.DUP2:
                instruction = new Dup2Instruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.DUP2_X1:
                instruction = new Dup2X1Instruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.DUP2_X2:
                instruction = new Dup2X2Instruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.SWAP:
                instruction = new SwapInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.IADD:
                instruction = new IAddInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.LADD:
                instruction = new LAddInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.FADD:
                instruction = new FAddInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.DADD:
                instruction = new DAddInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.ISUB:
                instruction = new ISubInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.LSUB:
                instruction = new LSubInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.FSUB:
                instruction = new FSubInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.DSUB:
                instruction = new DSubInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.IMUL:
                instruction = new IMulInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.LMUL:
                instruction = new LMulInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.FMUL:
                instruction = new FMulInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.DMUL:
                instruction = new DMulInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.IDIV:
                instruction = new IDivInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.LDIV:
                instruction = new LDivInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.FDIV:
                instruction = new FDivInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.DDIV:
                instruction = new DDivInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.IREM:
                instruction = new IRemInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.LREM:
                instruction = new LRemInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.FREM:
                instruction = new FRemInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.DREM:
                instruction = new DRemInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.INEG:
                needFrames = true;
                instruction = new INegInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.LNEG:
                instruction = new LNegInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.FNEG:
                instruction = new FNegInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.DNEG:
                instruction = new DNegInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.ISHL:
                instruction = new IshlInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.ISHR:
                instruction = new IshrInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.LSHL:
                instruction = new LshlInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.LSHR:
                instruction = new LshrInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.IUSHR:
                instruction = new IushrInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.LUSHR:
                instruction = new LushrInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.IAND:
                needFrames = true;
                instruction = new IAndInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.LAND:
                instruction = new LAndInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.IOR:
                needFrames = true;
                instruction = new IOrInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.LOR:
                instruction = new LOrInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.IXOR:
                instruction = new IXorInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.LXOR:
                instruction = new LXorInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.I2L:
                instruction = new I2LInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.I2F:
                instruction = new I2FInstruction(className, methodName, lineNumber, descriptor,getNextInstructionNumber());
                break;
            case Opcodes.I2D:
                instruction = new I2DInstruction(className, methodName, lineNumber,  descriptor,getNextInstructionNumber());
                break;
            case Opcodes.L2I:
                instruction = new L2IInstruction(className, methodName, lineNumber, descriptor, getNextInstructionNumber());
                break;
            case Opcodes.L2F:
                instruction = new L2FInstruction(className, methodName, lineNumber, descriptor, getNextInstructionNumber());
                break;
            case Opcodes.L2D:
                instruction = new L2DInstruction(className, methodName, lineNumber, descriptor, getNextInstructionNumber());
                break;
            case Opcodes.F2I:
                instruction = new F2IInstruction(className, methodName, lineNumber, descriptor, getNextInstructionNumber());
                break;
            case Opcodes.F2L:
                instruction = new F2LInstruction(className, methodName, lineNumber,  descriptor,getNextInstructionNumber());
                break;
            case Opcodes.F2D:
                instruction = new F2DInstruction(className, methodName, lineNumber, descriptor, getNextInstructionNumber());
                break;
            case Opcodes.D2I:
                instruction = new D2IInstruction(className, methodName, lineNumber, descriptor, getNextInstructionNumber());
                break;
            case Opcodes.D2L:
                instruction = new D2LInstruction(className, methodName, lineNumber, descriptor, getNextInstructionNumber());
                break;
            case Opcodes.D2F:
                instruction = new D2FInstruction(className, methodName, lineNumber, descriptor, getNextInstructionNumber());
                break;
            case Opcodes.I2B:
                instruction = new I2BInstruction(className, methodName, lineNumber, descriptor, getNextInstructionNumber());
                break;
            case Opcodes.I2C:
                instruction = new I2CInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.I2S:
                instruction = new I2SInstruction(className, methodName, lineNumber, descriptor, getNextInstructionNumber());
                break;
            case Opcodes.LCMP:
                instruction = new LCmpInstruction(className, methodName, lineNumber, descriptor, getNextInstructionNumber());
                break;
            case Opcodes.FCMPL:
                instruction = new FCmplInstruction(className, methodName, lineNumber, descriptor, getNextInstructionNumber());
                break;
            case Opcodes.FCMPG:
                instruction = new FCmpgInstruction(className, methodName, lineNumber,descriptor, getNextInstructionNumber());
                break;
            case Opcodes.DCMPL:
                instruction = new DCmplInstruction(className, methodName, lineNumber,  descriptor,getNextInstructionNumber());
                break;
            case Opcodes.DCMPG:
                instruction = new DCmpgInstruction(className, methodName, lineNumber, descriptor, getNextInstructionNumber());
                break;
            case Opcodes.RETURN:
                instruction = new VoidReturnInstruction(className, methodName, lineNumber, descriptor, getNextInstructionNumber());
                break;
            case Opcodes.IRETURN:
                instruction = new IReturnInstruction(className, methodName, lineNumber, descriptor, getNextInstructionNumber(),
                        StackTypeSet.of(Type.getReturnType(descriptor).getSort()));
                break;
            case Opcodes.DRETURN:
                instruction = new DReturnInstruction(className, methodName, lineNumber, descriptor, getNextInstructionNumber());
                break;
            case Opcodes.FRETURN:
                instruction = new FReturnInstruction(className, methodName, lineNumber,  descriptor,getNextInstructionNumber());
                break;
            case Opcodes.LRETURN:
                instruction = new LReturnInstruction(className, methodName, lineNumber, descriptor, getNextInstructionNumber());
                break;
            case Opcodes.ARETURN:
                instruction = new AReturnInstruction(className, methodName, lineNumber,  descriptor,getNextInstructionNumber());
                break;
            case Opcodes.ARRAYLENGTH:
                instruction = new ArraylengthInstruction(className, methodName, lineNumber,  descriptor,getNextInstructionNumber());
                break;
            case Opcodes.ATHROW:
                instruction = new AThrowInstruction(className, methodName, lineNumber, descriptor, getNextInstructionNumber());
                break;
            case Opcodes.MONITORENTER:
                instruction = new MonitorenterInstruction(className, methodName, lineNumber, descriptor,
                        getNextInstructionNumber(),Opcodes.MONITORENTER);
                break;
            case Opcodes.MONITOREXIT:
                instruction = new MonitorexitInstruction(className, methodName, lineNumber, descriptor,
                        getNextInstructionNumber(), Opcodes.MONITOREXIT);
                break;
            default:
                throw new IllegalArgumentException("Forgot " + opcode);
        }
        pushInstruction(instruction);
        super.visitInsn(opcode);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        this.tryCatchBlocks.add(new UnresolvedTryCatchBlock(start, end, handler, type));
        super.visitTryCatchBlock(start, end, handler, type);
    }

    private int getNextInstructionNumber() {
        return instructionNumber++;
    }
}
