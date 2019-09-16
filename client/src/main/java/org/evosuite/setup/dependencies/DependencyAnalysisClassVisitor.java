package org.evosuite.setup.dependencies;

import org.evosuite.graphs.ddg.DataDependenceGraph;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

// inspired by PurityAnalysisClassVisitor
public class DependencyAnalysisClassVisitor extends ClassVisitor {
    private final String className;
    private final DataDependenceGraph graph;

    public DependencyAnalysisClassVisitor(ClassVisitor visitor, String className,
                                          DataDependenceGraph graph) {
        super(Opcodes.ASM5, visitor);
        this.className = className;
        this.graph = graph;
    }

    @Override
    public MethodVisitor visitMethod(int methodAccess, String name,
                                     String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(methodAccess, name, descriptor, signature, exceptions);
        return new DependencyAnalysisMethodVisitor(className, name, descriptor, mv, graph);
    }
}