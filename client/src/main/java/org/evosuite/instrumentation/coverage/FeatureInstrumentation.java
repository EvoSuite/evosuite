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
                if (in.equals(vertex.getASMNode()) && vertex.isDataUpding() ) {
                    boolean isValidDef = false;
                    if (vertex.isMethodCallOfField()) {
                        //TODO: what is this. Remove this.
                        isValidDef = DefUsePool.addAsFieldMethodCall(vertex);
                    } else {
                        // keep track of data storing or updating operations
                        if (vertex.isDataUpding())
                            isValidDef = FeatureFactory.registerAsFeature(vertex);
                    }
                    if (isValidDef ) {
                        boolean staticContext = vertex.isStaticDefUse()
                                || ((access & Opcodes.ACC_STATIC) > 0);
                        // adding instrumentation for defuse-coverage
                        InsnList instrumentation = getInstrumentation(vertex, vertexSet, staticContext,
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

                        if(vertex.isUse() || vertex.getASMNode().getOpcode() == Opcodes.POP){
                            mn.instructions.insert(vertex.getASMNode(), instrumentation);
                        }else{
                            mn.instructions.insertBefore(vertex.getASMNode(), instrumentation);
                        }
                    }
                    // else block
                    // stumbling upon a "POP" instruction, do following
                    // iterate backwards till you reach opcode 25 - which is aload
                    // get the index of the asmNode.
                    // using that index as key fetch the ByteCodeInstruction from "vertexSet"
                    // because we need the corresponding variable Name - i.e the feature ot be updated
                    // So effectively we must be able to send a new updated object, variableName to the instrumented method
                    // That's the goal.
                    // We need to do all the above because we do not rely on any 'XLOAD' instructions which are used as 'vertex.isUse'
                    // in DefUseInstrumentation
                    // TODO: do it.
                    else if(vertex.getASMNode().getOpcode() == Opcodes.POP){

                    }
                }
            }
        }

    }

    /**
     * Helper function. Assume we have following bytecode instruction list:
     *        ...
     *        4: aload_2
     *        5: iconst_2
     *        6: invokestatic  #2                  // Method java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
     *        9: invokeinterface #3,  2            // InterfaceMethod java/util/List.add:(Ljava/lang/Object;)Z
     *        14: pop
     *        ...
     * POP is used for all the data manipulation operations on the interface defined in java.util.Collection.
     * So the rationale is that whenever a POP instruction is encountered - it suggests us that a corresponding variable
     * must be updated. And to be sure that variable is related to a class of java.util.Collections interface
     * 'isValidCollectionMethod' is used.
     * The goal of this method is to find such the name and index of such a variable.
     *
     * @param instruction
     */
    private Map<String, Integer> findCorrespondingVariableForPop(BytecodeInstruction instruction, Set<BytecodeInstruction> instructionSet){

        /**
         * check if the immediate previous asmNode is a MethodInsNode i.e. we need to be sure that
         * the previous instruction is actually a call to one of the permitted methods of the interface(Collection interface)
         *
         * TODO: handle all the cases for all the Collection implementation
         *
         */

        String owner = ((MethodInsnNode) instruction.getASMNode().getPrevious()).owner;
        String name = ((MethodInsnNode) instruction.getASMNode().getPrevious()).name;
        String desc = ((MethodInsnNode) instruction.getASMNode().getPrevious()).desc;

        int index = 0;
        int varIndex = 0;
        Map<String, Integer> varNameIndexMap = new HashMap<>(); // in case below condition is false we still send a empty but non null Map.
        if (isValidCollectionMethod(owner, name, desc)) {
            boolean search = true;
            index = instruction.getInstructionId();
            AbstractInsnNode insnNode = instruction.getASMNode();
            while (search) {
                if (insnNode.getOpcode() == Opcodes.ALOAD) {
                    varIndex = ((VarInsnNode) insnNode).var;
                    search = false;
                } else {
                    insnNode = insnNode.getPrevious();
                    index--; // we need ot do this because 'prev' instance of AbstractInsnNode is private-package visible and thus cannot be used
                }
            }
            final int indexCopy = index; // price we need to pay for using lambdas
            BytecodeInstruction instr = instructionSet.stream().filter(e -> e.getInstructionId() == indexCopy).findAny().get();
            String varName = instr.getVariableName(); // we did all the above things, just to get this. I.e to find out which variable does
            // the pop instruction corresponds to.
            varNameIndexMap.put(varName, varIndex);
            return varNameIndexMap;
        }

        return varNameIndexMap;


    }

    private boolean isValidCollectionMethod(String owner, String name, String desc){
        if("java/util/List".equals(owner) && "add".equals(name) && "(Ljava/lang/Object;)Z".equals(desc)){
            return true;
        }
        return false;
    }


    /**
     * Creates the instrumentation needed to instrument all the data updating operations.
     */
    private InsnList getInstrumentation(BytecodeInstruction v, Set<BytecodeInstruction> bytecodeInstructionSet, boolean staticContext,
                                        String className, String methodName, MethodNode mn) {
        InsnList instrumentation = new InsnList();

        if (!v.isDataUpding()) {
            logger.warn("unexpected FeatureInstrumentation call for a non-store-instruction");
            return instrumentation;
        }
        if (FeatureFactory.isKnownAsDefinition(v)) {
            // The actual object that is defined is on the stack _before_ the store instruction
            addObjectInstrumentation(v, instrumentation, mn);
            addCallingObjectInstrumentation(staticContext, instrumentation);
            instrumentation.add(new LdcInsnNode(FeatureFactory.getDefCounter()));
            addExecutionTracerMethod(v, instrumentation);

        }else if(v.getASMNode().getOpcode() == Opcodes.IINC){
            addObjectInstrumentation(v, instrumentation, mn);
            addCallingObjectInstrumentation(staticContext, instrumentation);
            instrumentation.add(new LdcInsnNode(v.getVariableName()));
            addExecutionTracerMethod(v, instrumentation);
        }else if(v.getASMNode().getOpcode() == Opcodes.POP){
            Map<String, Integer> varNameIndex = findCorrespondingVariableForPop(v, bytecodeInstructionSet);
            /*instrumentation.add(new VarInsnNode(Opcodes.ALOAD, ));*/
            String varName = null;
            int varIndex = 0;
            for (String key : varNameIndex.keySet()) {
                varName = key;
                varIndex = varNameIndex.get(varName);
            }
            instrumentation.add(new VarInsnNode(Opcodes.ALOAD, varIndex));
            addCallingObjectInstrumentation(staticContext, instrumentation);
            instrumentation.add(new LdcInsnNode(varName));
            addExecutionTracerMethod(v, instrumentation);
        }

        return instrumentation;
    }

    private void addExecutionTracerMethod(BytecodeInstruction v, InsnList instrumentation){

        // TODO: take care of iinc and arraystore instructions
        int instOpcode = v.getASMNode().getOpcode();
        String methodName = "";
        String methodDesc = "";
        switch(instOpcode){
            case Opcodes.ASTORE:
                methodName = "featureVisitedObj";
                methodDesc = "(Ljava/lang/Object;Ljava/lang/Object;I)V";
                break;
            case Opcodes.ISTORE:
                methodName = "featureVisitedInt";
                methodDesc = "(ILjava/lang/Object;I)V";
                break;
            case Opcodes.LSTORE:
                methodName = "featureVisitedLon";
                methodDesc = "(JLjava/lang/Object;I)V";
                break;
            case Opcodes.FSTORE:
                methodName = "featureVisitedFlo";
                methodDesc = "(FLjava/lang/Object;I)V";
                break;
            case Opcodes.DSTORE:
                methodName = "featureVisitedDou";
                methodDesc = "(DLjava/lang/Object;I)V";
                break;
            case Opcodes.IINC:
                methodName = "featureVisitedIntIncr";
                methodDesc = "(ILjava/lang/Object;Ljava/lang/Object;)V";
                break;
            case Opcodes.POP:
                methodName = "featureVisitedObjUpdate";
                methodDesc = "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V";
                break;
            case Opcodes.PUTFIELD:
                String type = v.getFieldType();

                //TODO: take care of "C", "B", "S", "[I" and "[[L"
                switch(type){
                    // boolean is treated as integer in bytecode representation
                    case "Z":
                    case "I":
                        methodName = "featureVisitedInt";
                        methodDesc = "(ILjava/lang/Object;I)V";
                        break;
                    case "J":
                        methodName = "featureVisitedLon";
                        methodDesc = "(JLjava/lang/Object;I)V";
                        break;
                    case "F":
                        methodName = "featureVisitedFlo";
                        methodDesc = "(FLjava/lang/Object;I)V";
                        break;
                    case "D":
                        methodName = "featureVisitedDou";
                        methodDesc = "(DLjava/lang/Object;I)V";
                        break;
                    default:
                        // this should be the object type
                        methodName = "featureVisitedObj";
                        methodDesc = "(Ljava/lang/Object;Ljava/lang/Object;I)V";
                        break;
                }
            default:
                break;
        }
        instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                PackageInfo.getNameWithSlash(org.evosuite.testcase.execution.ExecutionTracer.class), methodName,
                methodDesc));
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
        /**
         * Check if instruction is VariableDefinition and if yes,
         *  1) if it is local variable definition then,
         *      we have implemented what is need to be done
         *  2) else if it is filed definition
         *      we need to check the type just as we do for local variable to
         *      see if the variable is of the type Long, or Double
         */

        if (instruction.isDefinition()) {
            if (instruction.isLocalVariableDefinition()) {
                if (instruction.getASMNode().getOpcode() == Opcodes.LSTORE || instruction.getASMNode().getOpcode() == Opcodes.DSTORE) {
                    // these instructions take 2 slots each
                    instrumentation.add(new InsnNode(Opcodes.DUP2));
                } else if(instruction.getASMNode().getOpcode() == Opcodes.IINC){
                    int value = ((IincInsnNode) instruction.getASMNode()).incr;
                    instrumentation.add(new LdcInsnNode(value));
                }

                else {
                    instrumentation.add(new InsnNode(Opcodes.DUP));
                }
                //ACONST_NULL We can't do a ACONST_NULL because we are concerned with the actual value rather than just knowing if the variable has been defined or not.
                // This means we need to manually check which type the VariableDefinition is
                // and accordingly call a method from ExecutionTracer.
                // TODO : do it.
            }else{
                // it should be field definition
                // fetch the field type and act accordingly
                //TODO: Handle for iinc as above
                String type = instruction.getFieldType();
                switch(type){
                    case "J":
                    case "D":
                        instrumentation.add(new InsnNode(Opcodes.DUP2));
                        break;
                    default:
                        instrumentation.add(new InsnNode(Opcodes.DUP));
                        break;
                }
            }

        }else if(instruction.getASMNode().getOpcode() == Opcodes.POP){
            //TODO: Also take care of POP2
            instrumentation.add(new InsnNode(Opcodes.DUP));
        }

        else if (instruction.isLocalVariableUse()) {
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


            instrumentation.add(new InsnNode(Opcodes.DUP));

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
