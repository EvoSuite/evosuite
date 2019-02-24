package org.evosuite.instrumentation.coverage;

import org.evosuite.PackageInfo;
import org.evosuite.coverage.dataflow.FeatureFactory;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
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
        if (methodName.startsWith("<init>")) // avoiding instrumenting ctor
            return;
        if (mn.localVariables.size() == 1) // methods without parameters
            return;
        if(staticAccess && mn.localVariables.isEmpty()) // // static methods without parameters
            return;
        // start
        // method description -> params and types
        // depending on params and types load the local local variables and call ExecutionTraceImpl
        InsnList instrumentation = new InsnList();
        LabelNode methodStart = (LabelNode) mn.instructions.getFirst();
        for (LocalVariableNode localVariableNode : mn.localVariables) {
            if (localVariableNode.start.equals(methodStart)) {
                if(!staticAccess && localVariableNode.index == 0){
                    continue;
                }
                // these are the method params and visible at the beginning of the method
                String desc = localVariableNode.desc;
                int index = localVariableNode.index;
                FeatureFactory.registerAsFeature(localVariableNode.name, methodName);
                if(staticAccess)
                    instrumentation.add(new InsnNode(Opcodes.ACONST_NULL));
                else
                    instrumentation.add(new VarInsnNode(Opcodes.ALOAD, 0));
                instrumentation.add(new VarInsnNode(getOpcode(desc), index));
                instrumentation.add(new LdcInsnNode(methodName+'_'+localVariableNode.name));
                addExecutionTracerMethod(desc, instrumentation);
            }
        }
        if (instrumentation.size() == 0)
            return;
        // end
        // Add the instrumentation
        /**
         * It makes more sense to load all the identified local variables which are the parameters
         * of the method so that we can be sure that those variables are visible and that atleast
         * we can load them as the first instructions in the method.*/

        AbstractInsnNode in = j.next().getNext(); // label node followed by Line number node
        Set<BytecodeInstruction> vertexSet = graph.vertexSet();
        for (BytecodeInstruction vertex : vertexSet) {
            if (in.equals(vertex.getASMNode())) {
                // insert the instrumentation after the first label, line number node.
                mn.instructions.insert(vertex.getASMNode(), instrumentation);
            }
        }
    }

    private int getOpcode(String desc) {
        switch (desc) {
            case "Z":
            case "C":
            case "B":
            case "S":
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
                // handles 'Ljava/lang/Object;', '[I', '[[Ljava/lang/Object;' and any other non primitive type
                return Opcodes.ALOAD;
        }
    }

    private void addExecutionTracerMethod(String desc, InsnList instrumentation) {

        String methodName = "";
        String methodDesc = "";
        switch (desc) {
            case "Z":
            case "C":
            case "B":
            case "S":
            case "I":
                methodName = "featureVisitedInt";
                methodDesc = "(Ljava/lang/Object;ILjava/lang/Object;)V";
                break;

            case "J":
                methodName = "featureVisitedLon";
                methodDesc = "(Ljava/lang/Object;JLjava/lang/Object;)V";
                break;

            case "F":
                methodName = "featureVisitedFlo";
                methodDesc = "(Ljava/lang/Object;FLjava/lang/Object;)V";
                break;

            case "D":
                methodName = "featureVisitedDou";
                methodDesc = "(Ljava/lang/Object;DLjava/lang/Object;)V";
                break;

            default:
                // this should be the object type
                methodName = "featureVisitedObj";
                methodDesc = "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V";
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
