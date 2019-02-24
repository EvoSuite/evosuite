package org.evosuite.instrumentation.coverage;

import org.evosuite.PackageInfo;
import org.evosuite.coverage.dataflow.Feature;
import org.evosuite.coverage.dataflow.FeatureFactory;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.ControlFlowEdge;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The FeatureInstrumentation class helps in identifying & Tracking the features.
 *
 * @author Prathmesh Halgekar
 */
public class FeatureInstrumentation implements MethodInstrumentation {
    private static Logger logger = LoggerFactory.getLogger(FeatureInstrumentation.class);

    @Override
    public void analyze(ClassLoader classLoader, MethodNode mn, String className, String methodName, int access) {

        RawControlFlowGraph graph = GraphPool.getInstance(classLoader).getRawCFG(className, methodName);
        Iterator<AbstractInsnNode> j = mn.instructions.iterator();
        boolean staticAccess = ((access & Opcodes.ACC_STATIC) > 0);
        if(methodName.startsWith("<init>")) // avoiding instrumenting ctor
            return;
        if(mn.localVariables.size() == 1) // methods without parameters
            return;
        //instrumenting on non-static methods
        // start -- do only for non-static and non ctor methods
        // total number of local variables
        // method description -> params and types
        // depending on params and types load the local local variables and call ExecutionTraceImpl
        InsnList instrumentation = new InsnList();
        LabelNode methodStart = (LabelNode) mn.instructions.getFirst();
        for(LocalVariableNode localVariableNode : mn.localVariables){
            if(localVariableNode.start.equals(methodStart) && localVariableNode.index !=0){
                // these are the method params and visible at the beginning of the method
                String desc = localVariableNode.desc;
                int index = localVariableNode.index;
                FeatureFactory.registerAsFeature(localVariableNode.name, methodName);
                instrumentation.add(new VarInsnNode(Opcodes.ALOAD, 0));
                instrumentation.add(new VarInsnNode(getOpcode(desc), index));
                instrumentation.add(new LdcInsnNode(localVariableNode.name));
                addExecutionTracerMethod(desc, instrumentation);
            }
        }
        if(instrumentation.size() == 0)
            return;

        /*AbstractInsnNode insNode = mn.instructions.get(1);
        insNode*/
        // end
        int count = 0;
        if(!staticAccess) {
            AbstractInsnNode in = j.next().getNext(); // label node followed by Line number node

            Set<BytecodeInstruction> vertexSet = graph.vertexSet();
            for (BytecodeInstruction vertex : vertexSet) {

                if(in.equals(vertex.getASMNode())){
                    // insert the instrumentation after the first label, line number node.
                    mn.instructions.insert(vertex.getASMNode(), instrumentation);
                }

                //Identify and store the features in FeatureFactory
                /*if (in.equals(vertex.getASMNode())) {
                    if(vertex.isFeature()){
                        // keep track of data storing or updating operations
                        if(FeatureFactory.registerAsFeature(vertex)){
                            InsnList instrumentation = getInstrumentation(vertex, vertexSet,
                                    className,
                                    methodName,
                                    mn,
                                    graph);
                            if (instrumentation == null)
                                throw new IllegalStateException("error instrumenting node "
                                        + vertex.toString());
                            mn.instructions.insert(vertex.getASMNode(), instrumentation);
                        }
                    }

                }*/
                // Add the instrumentation
                /**
                 * It makes more sense to load all the identified feature variables once before the 'return' instruction
                 * and then store them using a method of ExecutionTraceImpl.
                 * This makes the resulting bytecode less complex and much efficient instead of reading the variables
                 * for every modification operation.*/

                /*if (in.equals(vertex.getASMNode()) && vertex.isReturn() && !FeatureFactory.getFeatures().isEmpty()) {
                    InsnList instrumentation = getInstrumentation(vertex, vertexSet,
                            className,
                            methodName,
                            mn, graph);
                    if (instrumentation == null)
                        throw new IllegalStateException("error instrumenting node "
                                + vertex.toString());
                    // that is all the instructions added are 'Use' and not 'Definition'
                    mn.instructions.insertBefore(vertex.getASMNode(), instrumentation);
                }*/

            }
        }

    }

    private int getOpcode(String desc) {
        switch (desc) {

            //TODO; handle 'C', 'B', 'S', 'Ljava/lang/Object;', '[I', '[[Ljava/lang/Object;'
            // boolean is treated as integer in bytecode representation
            case "Z":
            case "I":
                return Opcodes.ILOAD;
            case "J":
                return Opcodes.LLOAD;
            case "F":
                return Opcodes.FLOAD;
            case "D":
                return Opcodes.DLOAD;
            default:
                // this should be the object type
                // handles 'Ljava/lang/Object;', '[I' and '[[Ljava/lang/Object;'
                return Opcodes.ALOAD;
        }
    }


    /**
     * Creates the instrumentation needed to instrument all the data updating operations.
     */
    private InsnList getInstrumentation(BytecodeInstruction v, Set<BytecodeInstruction> bytecodeInstructionSet,
                                        String className, String methodName, MethodNode mn, RawControlFlowGraph graph) {
        InsnList instrumentation = new InsnList();

        /*if (!v.isReturn()) {
            logger.warn("unexpected FeatureInstrumentation call for a non-store-instruction");
            return instrumentation;
        }*/

        if(v.isWithinConstructor())
            return instrumentation;

        if (v.getASMNode().getOpcode() == Opcodes.PUTSTATIC) {
            instrumentation.add(new VarInsnNode(Opcodes.ALOAD, 0));
            instrumentation.add(new FieldInsnNode(Opcodes.GETSTATIC, ((FieldInsnNode) v.getASMNode()).owner,
                    ((FieldInsnNode) v.getASMNode()).name, ((FieldInsnNode) v.getASMNode()).desc));
        } else if (v.getASMNode().getOpcode() == Opcodes.PUTFIELD) {
            // add 'this' on to the stack first
            instrumentation.add(new VarInsnNode(Opcodes.ALOAD, 0));
            instrumentation.add(new FieldInsnNode(Opcodes.GETFIELD, ((FieldInsnNode) v.getASMNode()).owner,
                    ((FieldInsnNode) v.getASMNode()).name, ((FieldInsnNode) v.getASMNode()).desc));
        } else {
            instrumentation.add(new VarInsnNode(Opcodes.ALOAD, 0));
            instrumentation.add(new VarInsnNode(getOpcodeForLoadingVariable(v), ((VarInsnNode) v.getASMNode()).var));
        }

        instrumentation.add(new LdcInsnNode(v.getVariableName()));// or we can also use the name from feature.getVariableName()
        addExecutionTracerMethod(v, instrumentation);



        /*
         * For each of the feature, do following:
         * load the variable on the stack,
         * load the variable name on the stack
         * call the method of ExecutionTracer
         */
        /*Map<Integer, Feature> featureMap = FeatureFactory.getFeatures();
        for (Map.Entry<Integer, Feature> entry : featureMap.entrySet()) {
            boolean isVariableLoaded = false;
            BytecodeInstruction bytecodeInstruction = FeatureFactory.getInstructionById(entry.getKey());

            // for each method add to the Tracer the local variables of that method plus the class variables
            //TODO: No instrumentation added to the constructors. To be safe. Needs further analysis.
            if(!bytecodeInstruction.getMethodName().equals(v.getMethodName()))
                continue;

            if(bytecodeInstruction.isWithinConstructor())
                continue;

            if (bytecodeInstruction.getASMNode().getOpcode() == Opcodes.PUTSTATIC) {
                instrumentation.add(new FieldInsnNode(Opcodes.GETSTATIC, ((FieldInsnNode) bytecodeInstruction.getASMNode()).owner,
                        ((FieldInsnNode) bytecodeInstruction.getASMNode()).name, ((FieldInsnNode) bytecodeInstruction.getASMNode()).desc));
                isVariableLoaded = true;
            } else if (bytecodeInstruction.getASMNode().getOpcode() == Opcodes.PUTFIELD) {
                // add 'this' on to the stack first
                if(canLoadVariable(bytecodeInstruction, v, graph)){
                    instrumentation.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    instrumentation.add(new FieldInsnNode(Opcodes.GETFIELD, ((FieldInsnNode) bytecodeInstruction.getASMNode()).owner,
                            ((FieldInsnNode) bytecodeInstruction.getASMNode()).name, ((FieldInsnNode) bytecodeInstruction.getASMNode()).desc));
                    isVariableLoaded = true;
                }

            } else {
                // check if the variable can be loaded or not
                if(canLoadVariable(bytecodeInstruction, v, graph)){
                    instrumentation.add(new VarInsnNode(getOpcodeForLoadingVariable(bytecodeInstruction), ((VarInsnNode) bytecodeInstruction.getASMNode()).var));
                    isVariableLoaded = true;
                }
            }
            if(isVariableLoaded){
                instrumentation.add(new LdcInsnNode(bytecodeInstruction.getVariableName()));// or we can also use the name from feature.getVariableName()
                addExecutionTracerMethod(bytecodeInstruction, instrumentation);
            }
        }*/
        // changes end



        return instrumentation;
    }

    /**
     *
     * This method checks if the variable can be loaded or not before the particular RETURN
     * instruction.
     * As a pat of the instrumentation we load all the local variables before every RETURN
     * instruction. But sometimes due to local scope of variables some variables cannot be
     * loaded on the Stack. This method takes care of such scenarios.
     * @param var
     * @param retNode
     * @param graph
     * @return
     */
    private boolean canLoadVariable(BytecodeInstruction var, BytecodeInstruction retNode, RawControlFlowGraph graph){
        return true;

        /*Set<ControlFlowEdge> edgeSet = graph.edgeSet();
        Iterator<ControlFlowEdge> itr = edgeSet.iterator();
        while (itr.hasNext()){
            ControlFlowEdge cfg = itr.next();
            BytecodeInstruction sourceInstruction = (BytecodeInstruction)cfg.getSource();
            if(sourceInstruction.getASMNode().equals(var.getASMNode())){
                // start traversing
                BytecodeInstruction targetInstruction = (BytecodeInstruction)cfg.getTarget();
                while (itr.hasNext()){
                    ControlFlowEdge cfg1 = itr.next();
                    BytecodeInstruction sourceInstruction1 = (BytecodeInstruction)cfg1.getSource();
                    if(sourceInstruction1.getASMNode().equals(targetInstruction.getASMNode())){
                        targetInstruction = (BytecodeInstruction)cfg1.getTarget();
                        if(targetInstruction.isReturn() && targetInstruction.getASMNode().equals(retNode.getASMNode())){
                            return true;
                        }
                    }
                }
            }
        }
        return false;*/
    }

    private int getOpcodeForLoadingVariable(BytecodeInstruction v) {
        int instOpcode = v.getASMNode().getOpcode();
        switch (instOpcode) {
            case Opcodes.ALOAD:
            case Opcodes.ASTORE:
                return Opcodes.ALOAD;
            case Opcodes.ILOAD:
            case Opcodes.ISTORE:
                return Opcodes.ILOAD;

            case Opcodes.LLOAD:
            case Opcodes.LSTORE:
                return Opcodes.LLOAD;

            case Opcodes.FLOAD:
            case Opcodes.FSTORE:
                return Opcodes.FLOAD;

            case Opcodes.DLOAD:
            case Opcodes.DSTORE:
                return Opcodes.DLOAD;
            /*case Opcodes.IINC:
                methodName = "featureVisitedIntIncr";
                methodDesc = "(ILjava/lang/Object;Ljava/lang/Object;)V";
                break;*/
            /*case Opcodes.POP:
                methodName = "featureVisitedObjUpdate";
                methodDesc = "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V";
                break;*/
            case Opcodes.PUTFIELD:
                String type = v.getFieldType();

                //TODO: take care of "C", "B", "S", "[I" and "[[L"
                switch (type) {
                    // boolean is treated as integer in bytecode representation
                    case "Z":
                    case "I":
                        return Opcodes.ILOAD;
                    case "J":
                        return Opcodes.LLOAD;
                    case "F":
                        return Opcodes.FLOAD;
                    case "D":
                        return Opcodes.DLOAD;
                    default:
                        // this should be the object type
                        return Opcodes.ALOAD;
                }
            default:
                break;
        }
        return Opcodes.ACONST_NULL;
    }

    private void addExecutionTracerMethod(String desc, InsnList instrumentation) {

        // TODO: take care of iinc and arraystore instructions
        String methodName = "";
        String methodDesc = "";
        switch (desc) {
            case "I":
            case "Z":
                methodName = "featureVisitedInt";
                methodDesc = "(Ljava/lang/Object;ILjava/lang/Object;)V";
                break;

            case "J":
                methodName = "featureVisitedLon";
                methodDesc = "(JLjava/lang/Object;)V";
                break;

            case "F":
                methodName = "featureVisitedFlo";
                methodDesc = "(FLjava/lang/Object;)V";
                break;

            case "D":
                methodName = "featureVisitedDou";
                methodDesc = "(DLjava/lang/Object;)V";
                break;

            default:
                // this should be the object type
                methodName = "featureVisitedObj";
                methodDesc = "(Ljava/lang/Object;Ljava/lang/Object;)V";
                break;

        }
        instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                PackageInfo.getNameWithSlash(org.evosuite.testcase.execution.ExecutionTracer.class), methodName,
                methodDesc));
    }

    private void addExecutionTracerMethod(BytecodeInstruction v, InsnList instrumentation) {

        // TODO: take care of iinc and arraystore instructions
        int instOpcode = v.getASMNode().getOpcode();
        String methodName = "";
        String methodDesc = "";
        switch (instOpcode) {

            case Opcodes.ALOAD:
            case Opcodes.ASTORE:
                methodName = "featureVisitedObj";
                methodDesc = "(Ljava/lang/Object;Ljava/lang/Object;)V";
                break;
            case Opcodes.ILOAD:
            case Opcodes.ISTORE:
                methodName = "featureVisitedInt";
                methodDesc = "(Ljava/lang/Object;ILjava/lang/Object;)V";
                break;

            case Opcodes.LLOAD:
            case Opcodes.LSTORE:
                methodName = "featureVisitedLon";
                methodDesc = "(JLjava/lang/Object;)V";
                break;

            case Opcodes.FLOAD:
            case Opcodes.FSTORE:
                methodName = "featureVisitedFlo";
                methodDesc = "(FLjava/lang/Object;)V";
                break;

            case Opcodes.DLOAD:
            case Opcodes.DSTORE:
                methodName = "featureVisitedDou";
                methodDesc = "(DLjava/lang/Object;)V";
                break;
            //TODO: take care of this
            case Opcodes.IINC:
                methodName = "featureVisitedIntIncr";
                methodDesc = "(ILjava/lang/Object;Ljava/lang/Object;)V";
                break;
            case Opcodes.POP:
                methodName = "featureVisitedObjUpdate";
                methodDesc = "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V";
                break;
            case Opcodes.PUTSTATIC:
            case Opcodes.PUTFIELD:
                String type = v.getFieldType();

                //TODO: take care of "C", "B", "S", "[I" and "[[L"
                switch (type) {
                    // boolean is treated as integer in bytecode representation
                    case "Z":
                    case "I":
                        methodName = "featureVisitedInt";
                        methodDesc = "(Ljava/lang/Object;ILjava/lang/Object;)V";
                        break;
                    case "J":
                        methodName = "featureVisitedLon";
                        methodDesc = "(JLjava/lang/Object;)V";
                        break;
                    case "F":
                        methodName = "featureVisitedFlo";
                        methodDesc = "(FLjava/lang/Object;)V";
                        break;
                    case "D":
                        methodName = "featureVisitedDou";
                        methodDesc = "(DLjava/lang/Object;)V";
                        break;
                    default:
                        // this should be the object type
                        methodName = "featureVisitedObj";
                        methodDesc = "(Ljava/lang/Object;Ljava/lang/Object;)V";
                        break;
                }
            default:
                break;
        }
        instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                PackageInfo.getNameWithSlash(org.evosuite.testcase.execution.ExecutionTracer.class), methodName,
                methodDesc));
    }

    @Override
    public boolean executeOnMainMethod() {
        return false;
    }

    @Override
    public boolean executeOnExcludedMethods() {
        return false;
    }
}
