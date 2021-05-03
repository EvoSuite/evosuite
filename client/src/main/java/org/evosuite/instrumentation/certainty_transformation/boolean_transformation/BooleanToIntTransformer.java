package org.evosuite.instrumentation.certainty_transformation.boolean_transformation;

import org.evosuite.instrumentation.certainty_transformation.boolean_transformation.boolean_to_int_instrumened_signatures.BooleanToIntInstrumentedSignature;
import org.evosuite.instrumentation.certainty_transformation.boolean_transformation.instrumentation_listeners.InstrumentationListener;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.ControlFlowAnalyser;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.MethodAnalysisResultStorage;
import org.evosuite.runtime.util.ComputeClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BooleanToIntTransformer implements ClassFileTransformer {

    private final static Logger logger = LoggerFactory.getLogger(BooleanToIntTransformer.class);

    private final boolean useCdgForConstantsReplacement;
    private boolean addOriginalMethod;
    private Collection<String> classRegexCollection;
    private final boolean debug;
    private Predicate<String> classIsInstrumented;
    private final static boolean THROW_EXCEPTION_ON_FAILURE = true;
    private final static boolean TRANSFORM = true;
    private final static boolean CHECK_CLASS = false;
    private Set<InstrumentationListener> instrumentationListeners = new HashSet<>();

    /**
     * Constructor for a collection of regex expression.
     * Every class that matches at least one regex will be instrumented.
     *
     * @param classRegexCollection Collection of regex expressions
     * @param debug If the classes should be written to the dist. Typically used for debug purposes, false is recommended
     * @param useCdgForConstantsReplacement
     */
    public BooleanToIntTransformer(Collection<String> classRegexCollection, boolean debug, boolean useCdgForConstantsReplacement) {
        this(classRegexCollection, debug, s -> false, useCdgForConstantsReplacement,true);
    }

    public BooleanToIntTransformer(Predicate<String> classIsInstrumented, boolean useCdgForConstantsReplacement){
        this(Collections.emptyList(), false, classIsInstrumented, useCdgForConstantsReplacement,true);
    }

    public BooleanToIntTransformer(Predicate<String> classIsInstrumented, boolean useCdgForConstantsReplacement,
                                   boolean debug){
        this(Collections.emptyList(), debug, classIsInstrumented, useCdgForConstantsReplacement,true);
    }

    public BooleanToIntTransformer(Collection<String> classRegexCollection, boolean debug, Predicate<String> classIsInstrumented, boolean useCdgForConstantsReplacement, boolean addOriginalMethod) {
        this.useCdgForConstantsReplacement = useCdgForConstantsReplacement;
        this.classRegexCollection = classRegexCollection;
        this.debug = debug;
        this.classIsInstrumented = classIsInstrumented;
        this.addOriginalMethod = addOriginalMethod;
    }

    public BooleanToIntTransformer() {
        this(Collections.singleton(".*"),
                false, s -> false,
                false,
                true);
    }

    public boolean addInstrumentationListener(InstrumentationListener listener){
        return this.instrumentationListeners.add(listener);
    }

    public boolean isAddOriginalMethod() {
        return addOriginalMethod;
    }

    public void setAddOriginalMethod(boolean addOriginalMethod) {
        this.addOriginalMethod = addOriginalMethod;
    }

    void notifyInstrumentationStarted(final String className){
        instrumentationListeners.forEach(l-> l.notifyInstrumentationStarted(className));
    }

    void notifyInstrumentationFinished(final String className){
        instrumentationListeners.forEach(l-> l.notifyInstrumentationFinished(className));
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        try {
            if(classBeingRedefined != null)
                logger.debug("====== Redefining Class: {} ======\n", classBeingRedefined.getName());
            if (this.classIsInstrumented(className)) {
                notifyInstrumentationStarted(className);
                logger.info("====== INSTRUMENTING CLASS: {} =======\n", className);
                if(!TRANSFORM)
                    return classfileBuffer;
                ClassReader reader = new ClassReader(classfileBuffer);
                MethodAnalysisResultStorage storage = new MethodAnalysisResultStorage();
                ClassWriter writer = new ComputeClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES, loader);
                ControlFlowAnalyser controlFlowAnalyser = new ControlFlowAnalyser(className, storage, x->true);
                reader.accept(controlFlowAnalyser, 0);
                boolean initializable = controlFlowAnalyser.isInitializable();
                // reader.accept(writer,0);
                ClassVisitor mv = writer;
                if(CHECK_CLASS) mv = new CheckClassAdapter(writer, true);
                BooleanToIntClassVisitor visitor = new BooleanToIntClassVisitor(mv, className,
                        this::classIsInstrumented, storage, className, loader, useCdgForConstantsReplacement,
                        instrumentationListeners,addOriginalMethod,initializable);
                reader.accept(visitor,ClassReader.EXPAND_FRAMES);
                byte[] bytes = writer.toByteArray();
                if (debug) {
                    FileOutputStream fos = null;
                    try {
                        File file =
                                new File("InstrumentedClasses" + File.separator + className.replaceAll("/", ".").replaceAll("\\$", "_") +
                                ".class");
                        fos = new FileOutputStream(file);
                        fos.write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                notifyInstrumentationFinished(className);
                return bytes;
            } else {
                // out.println("====== NOT INSTRUMENTING CLASS: " + className + " =======");
                // out.flush();
                return classfileBuffer;
            }
        } catch (Exception e) {
        // } catch (Exception e){
            logger.error("Exception during instrumentation: {}", e.getMessage());
            if(THROW_EXCEPTION_ON_FAILURE) {
                throw new IllegalStateException("Error during Instrumentation", e);
            }
            return classfileBuffer;
        }
    }

    public ClassWriter transformToWriter(ClassNode node, ClassLoader loader){
        notifyInstrumentationStarted(node.name);
        MethodAnalysisResultStorage storage = new MethodAnalysisResultStorage();
        ControlFlowAnalyser classVisitor = new ControlFlowAnalyser(node.name, storage, x -> true);
        node.accept(classVisitor);
        boolean initializable = classVisitor.isInitializable();
        ClassWriter writer = new ComputeClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, loader);
        BooleanToIntClassVisitor visitor = new BooleanToIntClassVisitor(writer, node.name, classIsInstrumented,
                storage, node.name, loader, useCdgForConstantsReplacement,
                instrumentationListeners,addOriginalMethod,initializable);
        try {
            new ClassReader(writer.toByteArray()).accept(visitor,0);
        } catch (Exception e){
            throw new Error("Error During instrumentation",e);
        }
        notifyInstrumentationFinished(node.name);
        return writer;
    }

    public ClassNode transform(ClassNode node, ClassLoader loader){
        logger.info("Instrumenting class {}",node.name);
        notifyInstrumentationStarted(node.name);
        MethodAnalysisResultStorage storage = new MethodAnalysisResultStorage();
        ControlFlowAnalyser classVisitor = new ControlFlowAnalyser(node.name, storage, x -> true);
        node.accept(classVisitor);
        boolean initializable = classVisitor.isInitializable();
        // ClassWriter writer = new MyComputeClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, loader);
        ClassNode result =  new ClassNode();
        BooleanToIntClassVisitor visitor = new BooleanToIntClassVisitor(result, node.name, classIsInstrumented,
                storage, node.name, loader, useCdgForConstantsReplacement,
                instrumentationListeners,addOriginalMethod, initializable);
        node.accept(visitor);
        notifyInstrumentationFinished(node.name);
        logger.info("Finished instrumenting class{}", node.name);
        return result;
    }

    public boolean classIsInstrumented(String internalName) {
        if(internalName == null)
            return false;
        if(internalName.startsWith("java"))
            return false;
        if(internalName.startsWith("BooleanTransformation"))
            return false;
        if(this.classIsInstrumented.test(internalName))
            return true;
        String declaringClass = internalName.split("\\$")[0];
        return classRegexCollection.stream().anyMatch(declaringClass::matches);
    }


    public static boolean changes(String descriptor){
        return !descriptor.equals(instrumentedMethodDescriptor(descriptor));
    }

    /**
     * Computes the name of a instrumented Method based on the name of the original method.
     *
     * @param originalName
     * @return
     */
    public static String instrumentedMethodName(String originalName, String originalDescriptor) {
        if (originalName.equals("<init>"))
            return originalName;
        else
            return originalName + instrumentingSuffix + "_" + Math.abs(originalDescriptor.hashCode());
    }

    /**
     * Computes the descriptor of a instrumented Method based on the signature.
     *
     * @param originalDescriptor the descriptor of the original Method
     * @return the descriptor of the instrumented Method.
     */
    public static String instrumentedMethodDescriptor(String originalDescriptor) {
        Type[] originalArgumentTypes = Type.getArgumentTypes(originalDescriptor);
        Function<Type, Type> instrumentTypeFunction = t -> t.equals(Type.BOOLEAN_TYPE) ? Type.INT_TYPE : t;
        String instrumentedArgumentTypes =
                Arrays.stream(originalArgumentTypes).map(instrumentTypeFunction).map(Type::toString
                ).collect(Collectors.joining(""));
        Type originalReturnType = Type.getReturnType(originalDescriptor);
        Type instrumentedReturnType = instrumentTypeFunction.apply(originalReturnType);
        return String.format("(%s)%s", instrumentedArgumentTypes, instrumentedReturnType);
    }

    /**
     * Computes the signature of a instrumented Method based on the signature.
     *
     * @param originalSignature the signature of the original Method.
     * @return the signature of the instrumented Method.
     */
    public static String instrumentedMethodSignature(String originalSignature) {
        BooleanToIntInstrumentedSignature signature =
                BooleanToIntInstrumentedSignature.generate(originalSignature);
        BooleanToIntInstrumentedSignature replace =
                signature.replace(Type.BOOLEAN_TYPE, Type.INT_TYPE);//.replace(Type.getType(Boolean.class),
        //Type.getType(Integer.class));
        return replace.toString();
    }


    /**
     * Defines what static methods shall be used.
     */
    public static class UtilFunctionNameAndDescriptor {
        private final String name;
        private final String descriptor;

        private UtilFunctionNameAndDescriptor(String name, String descriptor) {
            this.name = name;
            this.descriptor = descriptor;
        }

        public String getName() {
            return name;
        }

        public String getDescriptor() {
            return descriptor;
        }
    }

    // Suffix appended to all fields/methods that needs to duplicated.
    public final static String instrumentingSuffix = "__BoolToIntInstrumented";
    public final static String toDescriptor = Type.INT_TYPE.getDescriptor();
    public final static String fromDescriptor = Type.BOOLEAN_TYPE.getDescriptor();
    public final static String BOOLEAN_TO_INT_UTIL_NAME =
            BooleanToIntUtil.class.getCanonicalName().replace(".", "/");

    public final static UtilFunctionNameAndDescriptor FROM_INT_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("fromInt", "(I)Z");

    public final static UtilFunctionNameAndDescriptor TO_INT_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("toInt", "(Z)I");

    public final static UtilFunctionNameAndDescriptor INT_CMP_EQ_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("intCmpEqBoolean", "(II)I");

    public final static UtilFunctionNameAndDescriptor INT_CMP_NE_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("intCmpNeBoolean", "(II)I");

    public final static UtilFunctionNameAndDescriptor INT_CMP_LE_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("intCmpLeBoolean", "(II)I");

    public final static UtilFunctionNameAndDescriptor INT_CMP_LT_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("intCmpLtBoolean", "(II)I");

    public final static UtilFunctionNameAndDescriptor INT_CMP_GT_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("intCmpGtBoolean", "(II)I");

    public final static UtilFunctionNameAndDescriptor INT_CMP_GE_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("intCmpGeBoolean", "(II)I");

    public final static UtilFunctionNameAndDescriptor IFEQ_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("ifEqBoolean", "(I)I");

    public final static UtilFunctionNameAndDescriptor IFNE_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("ifNeBoolean", "(I)I");

    public final static UtilFunctionNameAndDescriptor IFLE_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("ifLeBoolean", "(I)I");

    public final static UtilFunctionNameAndDescriptor IFLT_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("ifLtBoolean", "(I)I");

    public final static UtilFunctionNameAndDescriptor IFGE_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("ifGeBoolean", "(I)I");

    public final static UtilFunctionNameAndDescriptor IFGT_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("ifGtBoolean", "(I)I");

    public final static UtilFunctionNameAndDescriptor A_CMP_EQ_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("aCmpEq", "(Ljava/lang/Object;Ljava/lang/Object;)I");
    public final static UtilFunctionNameAndDescriptor A_CMP_NE_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("aCmpNe", "(Ljava/lang/Object;Ljava/lang/Object;)I");

    public final static UtilFunctionNameAndDescriptor BIN_LOGICAL_AND_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("BinaryLAnd", "(II)I");

    public final static UtilFunctionNameAndDescriptor LOGICAL_AND_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("lAnd", "([I)I");

    public final static UtilFunctionNameAndDescriptor BIN_LOGICAL_OR_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("binaryLor", "(II)I");

    public final static UtilFunctionNameAndDescriptor LOGICAL_OR_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("lOr", "([I)I");

    public final static UtilFunctionNameAndDescriptor LOGICAL_XOR_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("lXor", "([I)I");

    public final static UtilFunctionNameAndDescriptor NEG_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("neg", "(I)I");

    public final static UtilFunctionNameAndDescriptor DCMPG_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("dCmpG", "(DD)I");

    public final static UtilFunctionNameAndDescriptor DCMPL_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("dCmpL", "(DD)I");

    public final static UtilFunctionNameAndDescriptor FCMPG_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("fCmpG", "(FF)I");

    public final static UtilFunctionNameAndDescriptor FCMPL_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("fCmpL", "(FF)I");

    public final static UtilFunctionNameAndDescriptor UPDATE_II_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("update", "(II)I");
    public final static UtilFunctionNameAndDescriptor UPDATE_III_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("update", "(III)I");
    public final static UtilFunctionNameAndDescriptor IF_NULL_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("ifNull", "(Ljava/lang/Object;)I");
    public final static UtilFunctionNameAndDescriptor IF_NON_NULL_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("ifNonNull", "(Ljava/lang/Object;)I");
    public final static UtilFunctionNameAndDescriptor CMP_BOOLEAN_EQ_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("cmpBooleanEq", "(II)I");
    public final static UtilFunctionNameAndDescriptor CMP_BOOLEAN_NE_NAME_AND_DESCRIPTOR =
            new UtilFunctionNameAndDescriptor("cmpBooleanNe", "(II)I");
    public final static UtilFunctionNameAndDescriptor ENFORCE_NEGATIVE_CONSTRAINT =
            new UtilFunctionNameAndDescriptor("enforceNegativeConstraint","(I)I");
    public final static UtilFunctionNameAndDescriptor ENFORCE_POSITIVE_CONSTRAINT =
            new UtilFunctionNameAndDescriptor("enforcePositiveConstraint","(I)I");
}
