package org.evosuite.instrumentation.coverage;

import org.evosuite.PackageInfo;
import org.evosuite.coverage.dataflow.Feature;
import org.evosuite.coverage.dataflow.FeatureFactory;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
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
        while (j.hasNext()) {
            AbstractInsnNode in = j.next();
            Set<BytecodeInstruction> vertexSet = graph.vertexSet();
            for (BytecodeInstruction vertex : vertexSet) {

                //Identify and store the features in FeatureFactory
                if (in.equals(vertex.getASMNode()) && (vertex.isDefinition() || vertex.isUse())) {
                    if (vertex.isMethodCallOfField()) {
                        //TODO: what is this. Remove this. Or we need more Analysis?
                    } else {
                        // keep track of data storing or updating operations
                        if (vertex.isDefinition() || vertex.isUse())
                            FeatureFactory.registerAsFeature(vertex);
                    }
                }
                // Add the instrumentation
                /**
                 * It makes more sense to load all the identified feature variables once before the 'return' instruction
                 * and then store them using a method of ExecutionTraceImpl.
                 * This makes the resulting bytecode less complex and much efficient instead of reading the variables
                 * for every modification operation.
                 */
                if (in.equals(vertex.getASMNode()) && vertex.isReturn() && !FeatureFactory.getFeatures().isEmpty()) {
                        /*boolean staticContext = vertex.isStaticDefUse()
                                || ((access & Opcodes.ACC_STATIC) > 0);*/
                    // adding instrumentation for defuse-coverage
                    InsnList instrumentation = getInstrumentation(vertex, vertexSet,
                            className,
                            methodName,
                            mn);
                    if (instrumentation == null)
                        throw new IllegalStateException("error instrumenting node "
                                + vertex.toString());
                    if (vertex.isMethodCallOfField())
                        mn.instructions.insertBefore(vertex.getASMNode(), instrumentation);
                    else if (vertex.isArrayStoreInstruction())
                        mn.instructions.insertBefore(vertex.getSourceOfArrayReference().getASMNode(), instrumentation);

                    mn.instructions.insertBefore(vertex.getASMNode(), instrumentation);
                }

            }
        }

    }


    /**
     * Creates the instrumentation needed to instrument all the data updating operations.
     */
    private InsnList getInstrumentation(BytecodeInstruction v, Set<BytecodeInstruction> bytecodeInstructionSet,
                                        String className, String methodName, MethodNode mn) {
        InsnList instrumentation = new InsnList();

        if (!v.isReturn()) {
            logger.warn("unexpected FeatureInstrumentation call for a non-store-instruction");
            return instrumentation;
        }

        /*
         * For each of the feature, do following:
         * load the variable on the stack,
         * load the variable name on the stack
         * call the method of ExecutionTracer
         */
        Map<Integer, Feature> featureMap = FeatureFactory.getFeatures();
        for (Map.Entry<Integer, Feature> entry : featureMap.entrySet()) {
            BytecodeInstruction bytecodeInstruction = FeatureFactory.getInstructionById(entry.getKey());

            // for each method add to the Tracer the local variables of that method plus the class variables
            if(!bytecodeInstruction.getMethodName().equals(v.getMethodName()) && !"<init>()V".equals(bytecodeInstruction.getMethodName()))
                continue;

            if (bytecodeInstruction.getASMNode().getOpcode() == Opcodes.PUTSTATIC) {
                instrumentation.add(new FieldInsnNode(Opcodes.GETSTATIC, ((FieldInsnNode) bytecodeInstruction.getASMNode()).owner,
                        ((FieldInsnNode) bytecodeInstruction.getASMNode()).name, ((FieldInsnNode) bytecodeInstruction.getASMNode()).desc));
            } else if (bytecodeInstruction.getASMNode().getOpcode() == Opcodes.PUTFIELD) {
                instrumentation.add(new FieldInsnNode(Opcodes.GETFIELD, ((FieldInsnNode) bytecodeInstruction.getASMNode()).owner,
                        ((FieldInsnNode) bytecodeInstruction.getASMNode()).name, ((FieldInsnNode) bytecodeInstruction.getASMNode()).desc));
            } else
                instrumentation.add(new VarInsnNode(getOpcodeForLoadingVariable(bytecodeInstruction), ((VarInsnNode) bytecodeInstruction.getASMNode()).var));

            instrumentation.add(new LdcInsnNode(bytecodeInstruction.getVariableName()));// or we can also use the name from feature.getVariableName()
            addExecutionTracerMethod(bytecodeInstruction, instrumentation);
        }
        // changes end
        return instrumentation;
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
                methodDesc = "(ILjava/lang/Object;)V";
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
                        methodDesc = "(ILjava/lang/Object;)V";
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
