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
package org.evosuite.instrumentation;

import org.evosuite.assertion.CheapPurityAnalyzer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;

/**
 * It launches a <code>PurityAnalysisMethodVisitor</code> on each method.
 * This class only reads the existing bytecode.
 *
 * @author Juan Galeotti
 */
public class PurityAnalysisClassVisitor extends ClassVisitor {

    private final CheapPurityAnalyzer purityAnalyzer;

    public static class MethodEntry {
        private final String className;
        private final String methodName;
        private final String descriptor;

        public MethodEntry(String className, String methodName,
                           String descriptor) {
            this.className = className;
            this.methodName = methodName;
            this.descriptor = descriptor;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null)
                return false;

            if (!o.getClass().equals(MethodEntry.class))
                return false;

            MethodEntry that = (MethodEntry) o;

            return this.className.equals(that.className)
                    && this.methodName.equals(that.methodName)
                    && this.descriptor.equals(that.descriptor);
        }

        @Override
        public int hashCode() {
            return this.className.hashCode() + this.methodName.hashCode()
                    + this.descriptor.hashCode();
        }
    }

    private final String className;
    private final HashMap<MethodEntry, PurityAnalysisMethodVisitor> method_adapters = new HashMap<>();

    /**
     * <p>
     * Constructor for StaticInitializationClassAdapter.
     * </p>
     *
     * @param visitor   a {@link org.objectweb.asm.ClassVisitor} object.
     * @param className a {@link java.lang.String} object.
     */
    public PurityAnalysisClassVisitor(ClassVisitor visitor, String className,
                                      CheapPurityAnalyzer purityAnalyzer) {
        super(Opcodes.ASM9, visitor);
        this.className = className;
        this.purityAnalyzer = purityAnalyzer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MethodVisitor visitMethod(int methodAccess, String name,
                                     String descriptor, String signature, String[] exceptions) {


        if (visitingInterface == true) {
            purityAnalyzer.addInterfaceMethod(className.replace('/', '.'),
                    name, descriptor);
        } else {
            purityAnalyzer.addMethod(className.replace('/', '.'), name,
                    descriptor);
            if ((methodAccess & Opcodes.ACC_ABSTRACT) != Opcodes.ACC_ABSTRACT) {
                purityAnalyzer.addMethodWithBody(className.replace('/', '.'), name,
                        descriptor);
            } else {
                // The declaration of this method is abstract. So
                // there is no method body for this method in this class
            }
        }

        MethodVisitor mv = super.visitMethod(methodAccess, name, descriptor,
                signature, exceptions);
        PurityAnalysisMethodVisitor purityAnalysisMethodVisitor = new PurityAnalysisMethodVisitor(
                className, name, descriptor, mv, purityAnalyzer);
        MethodEntry methodEntry = new MethodEntry(className, name, descriptor);
        this.method_adapters.put(methodEntry, purityAnalysisMethodVisitor);
        return purityAnalysisMethodVisitor;
    }

    @Override
    public void visitEnd() {
        for (MethodEntry method_entry : this.method_adapters.keySet()) {

            if (this.method_adapters.get(method_entry).updatesField()) {
                purityAnalyzer.addUpdatesFieldMethod(method_entry.className,
                        method_entry.methodName, method_entry.descriptor);
            }
        }
    }

    private boolean visitingInterface = false;

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        if ((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE) {
            visitingInterface = true;
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }
}
