/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.setup;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.*;

/**
 * Collects a set of MethodIdentifier of those
 * classes in the callTree including an update to a static field
 * that is used in the GetStatic relation.
 *
 * @author galeotti
 */
public class PutStaticMethodCollector {

    private static final String CLINIT = "<clinit>";

    public static class MethodIdentifier {
        private final String className;
        private final String methodName;
        private final String desc;

        public MethodIdentifier(String className, String methodName, String desc) {
            this.className = className;
            this.methodName = methodName;
            this.desc = desc;
        }

        public String toString() {
            return className + "." + methodName + this.desc;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((className == null) ? 0 : className.hashCode());
            result = prime * result + ((desc == null) ? 0 : desc.hashCode());
            result = prime * result
                    + ((methodName == null) ? 0 : methodName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MethodIdentifier other = (MethodIdentifier) obj;
            if (className == null) {
                if (other.className != null)
                    return false;
            } else if (!className.equals(other.className))
                return false;
            if (desc == null) {
                if (other.desc != null)
                    return false;
            } else if (!desc.equals(other.desc))
                return false;
            if (methodName == null) {
                return other.methodName == null;
            } else return methodName.equals(other.methodName);
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getDesc() {
            return desc;
        }
    }

    //private static final Logger logger = LoggerFactory
    //		.getLogger(PutStaticMethodCollector.class);

    private static Map<String, Set<String>> createStaticFields(
            String targetClassName) {
        GetStaticGraph getStaticGraph = GetStaticGraphGenerator
                .generate(targetClassName);
        return getStaticGraph.getStaticFields();
    }

    public PutStaticMethodCollector(String targetClassName) {
        this(targetClassName, createStaticFields(targetClassName));
    }

    public PutStaticMethodCollector(String targetClassName,
                                    Map<String, Set<String>> getStaticFields) {
        this.getStaticFields = getStaticFields;
        // this.targetClassName = targetClassName;
    }

    private final Map<String, Set<String>> getStaticFields;

    @SuppressWarnings("unchecked")
    public Set<MethodIdentifier> collectMethods() {

        Set<MethodIdentifier> methods = new LinkedHashSet<>();

        for (String calledClassName : getStaticFields.keySet()) {
            ClassNode classNode = DependencyAnalysis
                    .getClassNode(calledClassName);
            List<MethodNode> classMethods = classNode.methods;
            for (MethodNode mn : classMethods) {
                if (mn.name.equals(CLINIT))
                    continue;

                InsnList instructions = mn.instructions;
                Iterator<AbstractInsnNode> it = instructions.iterator();
                while (it.hasNext()) {
                    AbstractInsnNode insn = it.next();
                    if (insn instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                        if (fieldInsn.getOpcode() != Opcodes.PUTSTATIC) {
                            continue;
                        }
                        String calleeClassName = fieldInsn.owner.replaceAll(
                                "/", ".");
                        String calleeFieldName = fieldInsn.name;

                        if (contains(getStaticFields, calleeClassName,
                                calleeFieldName)) {

                            MethodIdentifier methodIdentifier = new MethodIdentifier(
                                    calledClassName, mn.name, mn.desc);
                            methods.add(methodIdentifier);

                        }
                    }
                }

            }

        }
        return methods;
    }

    // private final String targetClassName;

    private boolean contains(Map<String, Set<String>> fields, String className,
                             String fieldName) {
        if (!fields.containsKey(className))
            return false;

        return fields.get(className).contains(fieldName);
    }

}
