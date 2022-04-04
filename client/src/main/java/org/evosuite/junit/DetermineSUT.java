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

package org.evosuite.junit;

import org.apache.commons.lang3.StringUtils;
import org.evosuite.PackageInfo;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.annotations.EvoSuiteTest;
import org.evosuite.classpath.ClassPathHacker;
import org.evosuite.classpath.ResourceList;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * For a given JUnit test class, determine which class it seems to be testing.
 *
 * @author Gordon Fraser
 */
public class DetermineSUT {

    private final static Logger logger = LoggerFactory.getLogger(DetermineSUT.class);

    private String targetName = "";

    private Set<String> superClasses = new HashSet<>();

    public static class NoJUnitClassException extends Exception {

        private static final long serialVersionUID = -7035856440476749976L;

    }

    private static class TargetClassSorter implements Comparator<String> {
        private final String targetClass;

        public TargetClassSorter(String target) {
            this.targetClass = target;
        }

        @Override
        public int compare(String arg0, String arg1) {
            double distance1 = StringUtils.getLevenshteinDistance(targetClass, arg0);
            double distance2 = StringUtils.getLevenshteinDistance(targetClass, arg1);
            return Double.compare(distance1, distance2);
        }
    }

    public String getSUTName(String fullyQualifiedTargetClass, String targetClassPath)
            throws NoJUnitClassException {
        this.targetName = fullyQualifiedTargetClass;
        try {
            ClassPathHacker.addFile(targetClassPath);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        Set<String> targetClasses = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllClasses(targetClassPath, false);
        Set<String> candidateClasses = new HashSet<>();
        boolean hasJUnit = false;
        try {
            candidateClasses.addAll(determineCalledClasses(fullyQualifiedTargetClass,
                    targetClasses));
            hasJUnit = true;
        } catch (ClassNotFoundException e) {
            // Ignore, the set will be empty?
            logger.error("Class not found: " + e, e);
            return "";
        } catch (NoJUnitClassException e) {

        }

        if (!hasJUnit)
            throw new NoJUnitClassException();

        if (candidateClasses.isEmpty())
            return "<UNKNOWN>";

        List<String> sortedNames = new ArrayList<>(candidateClasses);
        sortedNames.sort(new TargetClassSorter(fullyQualifiedTargetClass));

        //System.out.println("Sorted candidate classes: " + sortedNames);
        return sortedNames.get(0);
    }

    public Set<String> determineCalledClasses(String fullyQualifiedTargetClass,
                                              Set<String> targetClasses) throws ClassNotFoundException,
            NoJUnitClassException {
        Set<String> calledClasses = new HashSet<>();

        String className = fullyQualifiedTargetClass.replace('.', '/');
        try {

            InputStream is = ClassLoader.getSystemResourceAsStream(className + ".class");
            if (is == null) {
                throw new ClassNotFoundException("Class '" + className + ".class"
                        + "' should be in target project, but could not be found!");
            }
            ClassReader reader = new ClassReader(is);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, ClassReader.SKIP_FRAMES);
            superClasses = getSuperClasses(classNode);

            if (isJUnitTest(classNode)) {
                handleClassNode(calledClasses, classNode, targetClasses);
            } else {
                throw new NoJUnitClassException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        calledClasses.remove("java.lang.Object");
        calledClasses.remove(fullyQualifiedTargetClass);

        return calledClasses;
    }


    @SuppressWarnings("unchecked")
    private void handleClassNode(Set<String> calledClasses, ClassNode cn,
                                 Set<String> targetClasses) throws IOException {
        List<MethodNode> methods = cn.methods;
        for (MethodNode mn : methods) {
            handleMethodNode(calledClasses, cn, mn, targetClasses);
        }
    }

    /**
     * <p>
     * isJavaClass
     * </p>
     *
     * @param classNameWithDots a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean isJavaClass(String classNameWithDots) {
        return classNameWithDots.startsWith("java.") //
                || classNameWithDots.startsWith("javax.") //
                || classNameWithDots.startsWith("sun.") //
                || classNameWithDots.startsWith("apple.")
                || classNameWithDots.startsWith("com.apple.");
    }

    private boolean isValidClass(String name) throws IOException {
        if (isJavaClass(name))
            return false;

        if (name.startsWith("junit"))
            return false;

        if (name.startsWith("org.junit"))
            return false;

        if (name.startsWith(targetName))
            return false;

        if (superClasses.contains(name))
            return false;

        ClassNode sutNode = loadClassNode(name);
        return !isJUnitTest(sutNode);
    }

    @SuppressWarnings("unchecked")
    private void handleMethodNode(Set<String> calledClasses, ClassNode cn, MethodNode mn,
                                  Set<String> targetClasses) throws IOException {
        InsnList instructions = mn.instructions;
        Iterator<AbstractInsnNode> iterator = instructions.iterator();

        while (iterator.hasNext()) {
            AbstractInsnNode insn = iterator.next();
            if (insn instanceof MethodInsnNode) {
                String name = ResourceList.getClassNameFromResourcePath(((MethodInsnNode) insn).owner);
                if (!targetClasses.contains(name))
                    continue;

                if (isValidClass(name))
                    calledClasses.add(name);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean isJUnitTest(ClassNode cn) throws IOException {
        // We do not consider abstract classes
        if ((cn.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT)
            return false;

        if (hasJUnitSuperclass(cn))
            return true;

        List<MethodNode> methods = cn.methods;
        for (MethodNode mn : methods) {
            List<AnnotationNode> annotations = mn.visibleAnnotations;
            if (annotations != null) {
                for (AnnotationNode an : annotations) {
                    if (an.desc.equals("Lorg/junit/Test;") ||
                            an.desc.equals("L" + PackageInfo.getNameWithSlash(EvoSuiteTest.class) + ";"))
                        return true;
                }
            }
        }

        return false;
    }

    private Set<String> getSuperClasses(ClassNode cn) throws IOException {
        Set<String> superClasses = new HashSet<>();
        String currentSuper = cn.superName;
        while (!currentSuper.equals("java/lang/Object")) {
            superClasses.add(ResourceList.getClassNameFromResourcePath(currentSuper));
            ClassNode superNode = loadClassNode(currentSuper);
            currentSuper = superNode.superName;
        }
        return superClasses;
    }

    private boolean hasJUnitSuperclass(ClassNode cn) throws IOException {
        if (cn.superName.equals("java/lang/Object"))
            return false;

        if (cn.superName.equals("junit/framework/TestCase"))
            return true;

        ClassNode superClass = loadClassNode(cn.superName);
        return hasJUnitSuperclass(superClass);
    }

    private ClassNode loadClassNode(String className) throws IOException {
        ClassReader reader = new ClassReader(ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getClassAsStream(className));

        ClassNode cn = new ClassNode();
        reader.accept(cn, ClassReader.SKIP_FRAMES); // | ClassReader.SKIP_DEBUG);
        return cn;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Expected parameters: <TestCase> <Target Classpath>");
            return;
        }
        Properties.getInstanceSilent();
        DetermineSUT det = new DetermineSUT();
        try {
            System.out.println(det.getSUTName(args[0], args[1]));
        } catch (NoJUnitClassException e) {
            System.err.println("Found no JUnit test case");
        }
    }

}
