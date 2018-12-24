package org.evosuite.instrumentation.coverage;

import org.evosuite.PackageInfo;
import org.evosuite.coverage.dataflow.DefUsePool;
import org.evosuite.coverage.dataflow.FeatureFactory;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The FeatureInstrumentation class helps in identifying features.
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
            for(BytecodeInstruction vertex: vertexSet){
                if (in.equals(vertex.getASMNode()) && vertex.isDefinition()) {
                    boolean isValidDef = false;
                    if (vertex.isMethodCallOfField()) {
                        isValidDef = DefUsePool.addAsFieldMethodCall(vertex);
                    } else {
                        // keep track of definitions
                        if (vertex.isDefinition())
                            isValidDef = FeatureFactory.registerAsFeature(vertex);
                    }
                    if (isValidDef) {
                        boolean staticContext = vertex.isStaticDefUse()
                                || ((access & Opcodes.ACC_STATIC) > 0);
                        // adding instrumentation for defuse-coverage
                        InsnList instrumentation = getInstrumentation(vertex, staticContext,
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
            /*vertexSet.forEach(vertex -> {
                if (in.equals(vertex.getASMNode()) && vertex.isDefinition()) {
                    boolean isValidDef = false;
                    if (vertex.isMethodCallOfField()) {
                        isValidDef = DefUsePool.addAsFieldMethodCall(vertex);
                    } else {
                        // keep track of definitions
                        if (vertex.isDefinition())
                            isValidDef = FeatureFactory.registerAsFeature(vertex);
                    }
                    if (isValidDef) {
                        boolean staticContext = vertex.isStaticDefUse()
                                || ((access & Opcodes.ACC_STATIC) > 0);
                        // adding instrumentation for defuse-coverage
                        InsnList instrumentation = getInstrumentation(vertex, staticContext,
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
            });*/
        }

    }

    /**
     * Creates the instrumentation needed to track defs
     */
    private InsnList getInstrumentation(BytecodeInstruction v, boolean staticContext,
                                        String className, String methodName, MethodNode mn) {
        InsnList instrumentation = new InsnList();

        if (!v.isDefinition()) {
            logger.warn("unexpected FeatureInstrumentation call for a non-definition-instruction");
            return instrumentation;
        }

        /*if (DefUsePool.isKnownAsFieldMethodCall(v)) {
            return getMethodInstrumentation(v, staticContext, instrumentation, mn);
        }*/

        if (FeatureFactory.isKnownAsDefinition(v)) {
            // The actual object that is defined is on the stack _before_ the store instruction
            addObjectInstrumentation(v, instrumentation, mn);
            addCallingObjectInstrumentation(staticContext, instrumentation);
            instrumentation.add(new LdcInsnNode(FeatureFactory.getDefCounter()));
            instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    PackageInfo.getNameWithSlash(org.evosuite.testcase.execution.ExecutionTracer.class), "featureVisited",
                    "(ILjava/lang/Object;I)V"));
        }

        return instrumentation;
    }

    private void addCallingObjectInstrumentation(boolean staticContext,
                                                 InsnList instrumentation) {
        // the object on which the DU is covered is passed by the
        // instrumentation.
        // If we are in a static context, null is passed instead
        if (staticContext) {
            instrumentation.add(new InsnNode(Opcodes.ACONST_NULL));
        } else {
            instrumentation.add(new VarInsnNode(Opcodes.ALOAD, 0)); // "this"
        }
    }

    private void addObjectInstrumentation(BytecodeInstruction instruction, InsnList instrumentation, MethodNode mn) {
        if (instruction.isLocalVariableDefinition()) {
            if (instruction.getASMNode().getOpcode() == Opcodes.ALOAD) {
                instrumentation.add(new InsnNode(Opcodes.DUP));
            } else {
                instrumentation.add(new InsnNode(Opcodes.DUP));//ACONST_NULL
            }
        } else if (instruction.isLocalVariableUse()) {
            if (instruction.getASMNode().getOpcode() == Opcodes.ASTORE) {
                instrumentation.add(new InsnNode(Opcodes.DUP));
            } else {
                instrumentation.add(new InsnNode(Opcodes.ACONST_NULL));
            }
        } else if (instruction.isArrayStoreInstruction()) {
            // Object, index, value
            instrumentation.add(new InsnNode(Opcodes.DUP));
        } else if (instruction.isArrayLoadInstruction()) {
            instrumentation.add(new InsnNode(Opcodes.DUP));
        } else if (instruction.isFieldNodeDU()) {
            // TODO: FieldNodeDU takes care of ArrayStore - why?
            Type type = Type.getType(instruction.getFieldType());
            if (type.getSort() == Type.OBJECT) {
                instrumentation.add(new InsnNode(Opcodes.DUP));
            } else {
                instrumentation.add(new InsnNode(Opcodes.ACONST_NULL));
            }
        } else if (instruction.isMethodCall()) {
            Type type = Type.getReturnType(instruction.getMethodCallDescriptor());
            if (type.getSort() == Type.OBJECT) {
                instrumentation.add(new InsnNode(Opcodes.DUP));
            } else {
                instrumentation.add(new InsnNode(Opcodes.ACONST_NULL));
            }
        }
//		else if( instruction.getASMNode().getOpcode()== Opcodes.ARRAYLENGTH){
//			instrumentation.add(new InsnNode(Opcodes.DUP));
//		}
        else {
            assert false;
        }
    }

    private int getNextLocalNum(MethodNode mn) {
        List<LocalVariableNode> variables = mn.localVariables;
        int max = 0;
        for (LocalVariableNode node : variables) {
            if (node.index > max)
                max = node.index;
        }
        return max + 1;
    }

    private InsnList getMethodInstrumentation(BytecodeInstruction call, boolean staticContext, InsnList instrumentation, MethodNode mn) {


        String descriptor = call.getMethodCallDescriptor();
        Type[] args = Type.getArgumentTypes(descriptor);
        int loc = getNextLocalNum(mn);
        Map<Integer, Integer> to = new HashMap<Integer, Integer>();
        for (int i = args.length - 1; i >= 0; i--) {
            Type type = args[i];
            instrumentation.add(new VarInsnNode(type.getOpcode(Opcodes.ISTORE), loc));
            to.put(i, loc);
            loc++;
        }

        // instrumentation.add(new InsnNode(Opcodes.DUP));//callee
        addObjectInstrumentation(call, instrumentation, mn);
        addCallingObjectInstrumentation(staticContext, instrumentation);
        // field method calls get special treatment:
        // during instrumentation it is not clear whether a field method
        // call constitutes a definition or a use. So the instrumentation
        // will call a special function of the ExecutionTracer which will
        // redirect the call to either passedUse() or passedDefinition()
        // using the information available during runtime (the CCFGs)
        instrumentation.add(new LdcInsnNode(DefUsePool.getDefUseCounter()));
        instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                PackageInfo.getNameWithSlash(ExecutionTracer.class), "passedFieldMethodCall",
                "(Ljava/lang/Object;Ljava/lang/Object;I)V"));


        for (int i = 0; i < args.length; i++) {
            Type type = args[i];
            instrumentation.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), to.get(i)));
        }

        return instrumentation;
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
