package org.evosuite.setup;

import org.evosuite.instrumentation.ExceptionTransformationClassAdapter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ExceptionMapGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionMapGenerator.class);

    public static void initializeExceptionMap(String className) {
        ClassNode targetClass = DependencyAnalysis.getClassNode(className);
        if (targetClass != null) {
            for (MethodNode mn : targetClass.methods) {
                logger.debug("Method: " + mn.name);
                handleMethodNode(targetClass, mn);
                handleMethodCalls(targetClass, mn);
            }

        }
    }

    private static void handleDependency(String className) {
        ClassNode targetClass = DependencyAnalysis.getClassNode(className);
        if(targetClass != null){
            for(MethodNode mn : targetClass.methods) {
                logger.debug("Method: " + mn.name);
                handleMethodNode(targetClass, mn);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void handleMethodNode(ClassNode cn, MethodNode mn) {

        // TODO: Integrate this properly - it is currently an unexpected side-effect
        if(!ExceptionTransformationClassAdapter.methodExceptionMap.containsKey(cn.name))
            ExceptionTransformationClassAdapter.methodExceptionMap.put(cn.name, new LinkedHashMap<>());

        String methodNameDesc = mn.name + mn.desc;
        Set<Type> exceptionTypes = new LinkedHashSet<>();
        if(mn.exceptions != null) {
            for (String exceptionName : mn.exceptions) {
                exceptionTypes.add(Type.getType(exceptionName));
                logger.debug("Method {} throws {}", mn.name, exceptionName);
            }
        }
        ExceptionTransformationClassAdapter.methodExceptionMap.get(cn.name).put(methodNameDesc, exceptionTypes);

    }

    private static void handleMethodCalls(ClassNode cn, MethodNode mn) {
        InsnList instructions = mn.instructions;
        Iterator<AbstractInsnNode> iterator = instructions.iterator();

        // TODO: This really shouldn't be here but in its own class
        while (iterator.hasNext()) {
            AbstractInsnNode insn = iterator.next();
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode minsn = (MethodInsnNode)insn;
                handleDependency(minsn.owner);
            }
        }
    }
}
