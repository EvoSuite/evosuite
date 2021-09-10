/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.setup;

import org.evosuite.instrumentation.ExceptionTransformationClassAdapter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

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
        if (targetClass != null) {
            for (MethodNode mn : targetClass.methods) {
                logger.debug("Method: " + mn.name);
                handleMethodNode(targetClass, mn);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void handleMethodNode(ClassNode cn, MethodNode mn) {

        // TODO: Integrate this properly - it is currently an unexpected side-effect
        if (!ExceptionTransformationClassAdapter.methodExceptionMap.containsKey(cn.name))
            ExceptionTransformationClassAdapter.methodExceptionMap.put(cn.name, new LinkedHashMap<>());

        String methodNameDesc = mn.name + mn.desc;
        Set<Type> exceptionTypes = new LinkedHashSet<>();
        if (mn.exceptions != null) {
            for (String exceptionName : mn.exceptions) {
                exceptionTypes.add(Type.getObjectType(exceptionName));
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
                MethodInsnNode minsn = (MethodInsnNode) insn;
                handleDependency(minsn.owner);
            }
        }
    }
}
