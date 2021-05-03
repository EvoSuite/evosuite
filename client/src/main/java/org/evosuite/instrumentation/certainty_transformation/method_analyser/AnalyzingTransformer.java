package org.evosuite.instrumentation.certainty_transformation.method_analyser;

import org.apache.commons.lang3.tuple.Pair;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.MethodAnalysisResultStorage;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.function.Predicate;

public class AnalyzingTransformer implements ClassFileTransformer {

    private final MethodAnalysisResultStorage storage;
    private Predicate<Pair<String, String>> analyzeMethod;

    public AnalyzingTransformer(MethodAnalysisResultStorage storage, Predicate<Pair<String, String>> analyzeMethod){
        this.storage = storage;
        this.analyzeMethod = analyzeMethod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] transform(final ClassLoader pLoader, final String pClassName, final Class<?> pClassBeingRedefined,
                            final ProtectionDomain pProtectionDomain, final byte[] pClassFileBuffer) {
        ControlFlowAnalyser analyzer = new ControlFlowAnalyser(pClassName, storage, analyzeMethod);
        ClassReader reader = new ClassReader(pClassFileBuffer);
        reader.accept(analyzer, 0);
        ClassWriter writer = new ClassWriter(reader, 0);
        return pClassFileBuffer;
    }
}
