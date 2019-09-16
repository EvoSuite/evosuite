package org.evosuite.setup.dependencies;

import org.evosuite.graphs.ddg.FieldEntry;
import org.evosuite.graphs.ddg.DataDependenceGraph;
import org.evosuite.graphs.ddg.MethodEntry;
import org.evosuite.runtime.classhandling.ClassResetter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

// Inspired by PurityAnalysisMethodVisitor
public class DependencyAnalysisMethodVisitor extends MethodVisitor {
    private final String classNameWithDots;
    private final String methodName;
    private final String descriptor;
    private final DataDependenceGraph graph;

    /**
     * Creates a new method visitor.
     *
     * @param className  the fully qualified name (with dots instead of slashes) of the owner class
     *                   of the visited method
     * @param methodName the name of the visited method
     * @param descriptor the descriptor of the visited method
     * @param mv         the method visitor to which this visitor must delegate method calls
     * @param graph      the method dependence graph used to record the writing or reading of fields
     */
    public DependencyAnalysisMethodVisitor(String className, String methodName,
                                           String descriptor, MethodVisitor mv,
                                           DataDependenceGraph graph) {
        super(Opcodes.ASM5, mv);
        this.classNameWithDots = className.replace('/', '.');
        this.methodName = methodName;
        this.descriptor = descriptor;
        this.graph = graph;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        /*
         * A field instruction is an instruction that loads or stores the value of a field of an
         * object. For every such instruction, we check if it writes a field (PUTSTATIC or
         * PUTFIELD) or if it reads a field (GETSTATIC or GETFIELD). Afterwards, we record the
         * fact that the method containing this field instruction writes or reads a certain field
         * by inserting appropriate nodes and edges into the method dependence graph.
         * We ignore the constructors <init> and <clinit> of a class, as well as EvoSuite's class
         * resetter. They're both irrelevant for our purposes.
         */
        if (!(methodName.equals("<clinit>") || methodName.equals("<init>")
                || methodName.equals(ClassResetter.STATIC_RESET))) {
            final MethodEntry method = new MethodEntry(classNameWithDots, methodName, descriptor);
            final FieldEntry field = new FieldEntry(owner.replace('/', '.'), name);

            if (opcode == Opcodes.PUTSTATIC || opcode == Opcodes.PUTFIELD) {
                graph.methodWritesField(method, field);
            } else if (opcode == Opcodes.GETSTATIC || opcode == Opcodes.GETFIELD) {
                graph.methodReadsField(method, field);
            }
        }

        super.visitFieldInsn(opcode, owner, name, desc);
    }
}