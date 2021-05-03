package org.evosuite.instrumentation.certainty_transformation.method_analyser;

import org.apache.commons.lang3.tuple.Pair;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.MethodAnalysisResultStorage;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.MethodIdentifier;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.variables.VariableTable;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;

public class ControlFlowAnalyser extends ClassVisitor {

    private final String className;
    private final MethodAnalysisResultStorage storage;
    private final Predicate<Pair<String,String>> analyzeMethod;
    // Whether the Class can be initialized
    private boolean initializable = false;

    public ControlFlowAnalyser(String className, MethodAnalysisResultStorage storage, Predicate<Pair<String, String>> analyzeMethod) {
        super(Opcodes.ASM7);
        this.className = className;
        this.storage = storage;
        this.analyzeMethod = analyzeMethod;
    }

    public ControlFlowAnalyser(ClassVisitor classVisitor, String className, MethodAnalysisResultStorage storage, Predicate<Pair<String, String>> analyzeMethod) {
        super(Opcodes.ASM7, classVisitor);
        this.className = className;
        this.storage = storage;
        this.analyzeMethod = analyzeMethod;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                     String[] exceptions) {
        // System.out.println("visiting method: " + name);
        initializableCheck(access,descriptor,name);
        if(!analyzeMethod.test(Pair.of(name, descriptor)))
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        return new MethodAnalyser(methodVisitor, className, name, descriptor, storage,
                (access & Opcodes.ACC_STATIC) != 0);
    }

    @Override
    public void visitEnd() {
        Map<MethodIdentifier, VariableTable> variableTableMap = storage.getVariableTableMap();
        boolean hasVariableTable = false;
        for (Map.Entry<MethodIdentifier, VariableTable> entry : variableTableMap.entrySet()) {
            MethodIdentifier id = entry.getKey();
            VariableTable variableTable = entry.getValue();
            if(id.getInternalClassName().equals(className)){
                if(!variableTable.isEmpty()){
                    hasVariableTable = true;
                    break;
                }

            }
        }
//        System.out.printf("Has Variable Table: %s,%s\n",className,hasVariableTable);
        super.visitEnd();
    }

    void initializableCheck(int access, String descriptor, String name){
        // If a non-private Constructor exists
        if((access & Opcodes.ACC_PRIVATE) == 0 && name.equals("<init>"))
            initializable = true;
        // If a method returns an instance of the class
        if(Type.getReturnType(descriptor).getClassName().equals(className))
            initializable = true;
        // If a method takes an instance as argument
        if(Arrays.stream(Type.getArgumentTypes(descriptor)).anyMatch(t -> t.getClassName().equals(className)))
            initializable = true;
    }

    public boolean isInitializable() {
        return initializable;
    }

    public MethodAnalysisResultStorage getStorage() {
        return new MethodAnalysisResultStorage(storage);
    }
}
