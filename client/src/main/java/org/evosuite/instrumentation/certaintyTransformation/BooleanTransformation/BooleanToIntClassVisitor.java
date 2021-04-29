package org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation;

import org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation.InstrumentationListeners.InstrumentationListener;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.MethodAnalysisResultStorage;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.MethodIdentifier;
import org.evosuite.runtime.instrumentation.AnnotatedLabel;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.LabelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;

import static org.objectweb.asm.Opcodes.*;

public class BooleanToIntClassVisitor extends ClassVisitor {

    private final static Logger logger = LoggerFactory.getLogger(BooleanToIntClassVisitor.class);

    private final String internalName;
    private final Predicate<String> classIsInstrumented;
    private final MethodAnalysisResultStorage storage;
    private final String className;
    private String superClass;
    private String[] interfaces;
    private int access;
    private final ClassLoader loader;
    private Collection<MethodReaderClassVisitor.MethodInformation> inheritedMethods = Collections.emptyList();
    private Collection<MethodIdentifier> declaredMethods = new HashSet<>();
    private boolean useCdgToForConstantsReplacement;
    private final boolean classIsInitializable;
    private final Set<InstrumentationListener> instrumentationListeners = new HashSet<>();
    private final boolean addOriginalMethods;
    private String signature;

    public void notifyHelperMethod(String className, String methodName, String desc) {
        instrumentationListeners.forEach(l -> l.notifyHelperMethod(new MethodIdentifier(className, methodName, desc)));
    }


    /**
     * Constructor for a ClassVisitor that transforms all booleans to integers.
     *
     * @param visitor                         super visitor
     * @param internalName                    internal class name of the class to be transformed
     * @param classIsInstrumented             Predicate that is only true, if the provided class will be instrumented
     * @param storage                         Storage object for the results of the {@code MethodAnalyser}
     * @param className                       class Name
     * @param loader                          the class loader, that loads the class. (Necessary if inherited methods
     *                                       will be added)
     * @param useCdgToForConstantsReplacement Whether to use the Control Dependence Graph to estimate the certainty
     *                                        of booleanns.
     *                                        (False = use Control Flow Graph)
     * @param instrumentationListeners        Collection of listeners, that will be notified, for certain events
     *                                        during the
     *                                        instrumentation ( see {@code InstrumentationListeners})
     * @param addOriginalMethods              Whether original and inherited methods should be added.
     * @param classIsInitializable            Whether the class is initializable.
     */
    public BooleanToIntClassVisitor(ClassVisitor visitor, String internalName, Predicate<String> classIsInstrumented,
                                    MethodAnalysisResultStorage storage, String className, ClassLoader loader,
                                    boolean useCdgToForConstantsReplacement,
                                    Set<InstrumentationListener> instrumentationListeners, boolean addOriginalMethods
            , boolean classIsInitializable) {
        super(ASM7, visitor);
        this.internalName = internalName;
        this.classIsInstrumented = classIsInstrumented;
        this.storage = storage;
        this.className = className;
        this.loader = loader;
        this.useCdgToForConstantsReplacement = useCdgToForConstantsReplacement;
        this.classIsInitializable = classIsInitializable;
        this.instrumentationListeners.addAll(instrumentationListeners);
        this.addOriginalMethods = addOriginalMethods;
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        if (name.startsWith(this.className.replace(".", "/"))) {
            for (InstrumentationListener instrumentationListener : instrumentationListeners) {
                instrumentationListener.notifyInnerClass(name);
            }
        }
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        logger.debug("Instrumenting class: {} extends {} implements {}", name, superName, Arrays.toString(interfaces));
        this.superClass = superName;
        this.interfaces = Arrays.copyOf(interfaces, interfaces.length);
        this.access = access;
        inheritedMethods = getAllMethodsOf(superName);
        this.signature = signature;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    /**
     * Analyzes the class {@param superClass} and lists all Methods that can be invoked from outside the defining class.
     * (e.g. private methods are omitted).
     * This method does NOT load the class.
     * {@code MethodReaderClassVisitor} is used to read the methods.
     *
     * @param superClass internal name of the class
     * @return Collection of MethodIdentifiers containing the defining class, method name and descriptor.
     */
    private Collection<MethodReaderClassVisitor.MethodInformation> getAllMethodsOf(String superClass) {
        logger.debug("Computing all methods of super class {}, base class {}", superClass, this.className);
        Optional<String> current = Optional.of(superClass);
        Collection<MethodReaderClassVisitor.MethodInformation> accessibleMethods = new HashSet<>();
        while (current.isPresent()) {
            // System.out.println("==== ANALYZING CLASS: " + className + " " + current.get() + " ====");
            InputStream superClassAsStream = loader.getResourceAsStream(current.get() + ".class");
            MethodReaderClassVisitor methodReaderClassVisitor = new MethodReaderClassVisitor(current.get());
            if (superClassAsStream == null) {
                throw new IllegalStateException("Could not analyze super class: " + current.get() + " with loader: " + loader);
            }
            try {
                byte[] bytes = superClassAsStream.readAllBytes();
                ClassReader reader = new ClassReader(bytes);
                reader.accept(methodReaderClassVisitor, 0);
                ClassWriter writer = new ClassWriter(reader, 0);
                writer.toByteArray();
            } catch (IOException e) {
                throw new IllegalStateException("Could not analyze super class", e);
            }
            accessibleMethods.addAll(methodReaderClassVisitor.getAccessibleMethods());
            current = methodReaderClassVisitor.getSuperClass();
        }
        return accessibleMethods;
    }

    @Override
    public void visitEnd() {
        if (addOriginalMethods) {
            addInheritedMethods();
        }
        logger.debug("Finished instrumenting class {}", className);
        super.visitEnd();
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if (!descriptor.equals("Z")) return super.visitField(access, name, descriptor, signature, value);
        if (name.endsWith(BooleanToIntTransformer.instrumentingSuffix))
            throw new IllegalStateException("A boolean field of the instrumented class ends with: " + BooleanToIntTransformer.instrumentingSuffix);
        /* if (value == null)
            value = false;*/
        Object instrumentedValue = computeInstrumentedValueForField(value);
        FieldVisitor intField = super.visitField(access, name + BooleanToIntTransformer.instrumentingSuffix, "I", signature, instrumentedValue);
        intField.visitEnd();
        return super.visitField(access, name, descriptor, signature, value);
    }

    private Object computeInstrumentedValueForField(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) {
            return value;
        }
        if (!(value instanceof Boolean))
            throw new IllegalArgumentException("Cannot compute instrumented value for non-Boolean values");
        return (Boolean) value ? 1 : 0;
    }

    private void notifyMethodIdentifierChanges(MethodIdentifier from, MethodIdentifier to) {
        instrumentationListeners.forEach(x -> x.notifyMethodIdentifierChange(from, to));
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                     String[] exceptions) {
        String instrumentedName = name;
        String instrumentedDescriptor = descriptor;
        String instrumentedSignature = signature;
        declaredMethods.add(new MethodIdentifier(className, name, descriptor));
        if (!name.equals("<init>") && !name.equals("<clinit>") && BooleanToIntTransformer.changes(descriptor)) {
            instrumentedName = BooleanToIntTransformer.instrumentedMethodName(name, descriptor);
            instrumentedDescriptor = BooleanToIntTransformer.instrumentedMethodDescriptor(descriptor);
            instrumentedSignature = BooleanToIntTransformer.instrumentedMethodSignature(signature);
            addOriginalMethod(access, name, descriptor, signature, exceptions);
            notifyMethodIdentifierChanges(new MethodIdentifier("", name, descriptor),
                    new MethodIdentifier("", instrumentedName, instrumentedDescriptor));
        }
        MethodVisitor superVisitor = super.visitMethod(access, instrumentedName, instrumentedDescriptor,
                instrumentedSignature, exceptions);
        BooleanToIntMethodVisitor visitor = new BooleanToIntMethodVisitor(superVisitor, name, classIsInstrumented,
                storage.get(new MethodIdentifier(className, name, descriptor)), instrumentationListeners, access,
                descriptor, useCdgToForConstantsReplacement);
        return visitor;
    }

    private void addOriginalMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (!addOriginalMethods) return;
        logger.debug("Adding original method {}:{}", name, descriptor);
        notifyHelperMethod(className, name, descriptor);
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        /*
            If the class is an interface or the method is abstract, don't do anything
         */
        if ((this.access & ACC_INTERFACE) != 0 || (access & ACC_ABSTRACT) != 0) {
            mv.visitEnd();
            return;
        }

        mv.visitCode();
        Label startIgnore = new AnnotatedLabel(true, true);
        startIgnore.info = new LabelNode(startIgnore);
        mv.visitLabel(startIgnore);
        Label methodStart = new Label();
        Label returnStatementLabel = new Label();
        mv.visitLabel(methodStart);
        final int static_offset = ((Opcodes.ACC_STATIC & access) == 0) ? 1 : 0;
        if ((Opcodes.ACC_STATIC & access) == 0) {
            pushThisReference(mv);
        }
        Type[] argumentTypes = Type.getArgumentTypes(descriptor);
        int offset = 0;
        for (int i = 0; i < argumentTypes.length; i++) {
            Type argumentType = argumentTypes[i];
            BooleanToIntClassVisitor.pushArgument(mv, argumentType, i + offset + static_offset, () -> {
                mv.visitMethodInsn(INVOKESTATIC, BooleanToIntTransformer.BOOLEAN_TO_INT_UTIL_NAME, BooleanToIntTransformer.TO_INT_NAME_AND_DESCRIPTOR.getName(),
                        BooleanToIntTransformer.TO_INT_NAME_AND_DESCRIPTOR.getDescriptor(), false);

            });
            offset += argumentType.getSize() - 1;
        }
        callInstrumentedMethod(mv, access, name, descriptor);
        mv.visitLabel(returnStatementLabel);
        addReturnStatement(mv, descriptor, () -> {
            mv.visitMethodInsn(INVOKESTATIC, BooleanToIntTransformer.BOOLEAN_TO_INT_UTIL_NAME, BooleanToIntTransformer.FROM_INT_NAME_AND_DESCRIPTOR.getName(),
                    BooleanToIntTransformer.FROM_INT_NAME_AND_DESCRIPTOR.getDescriptor(), false);
        });
        offset = 0;
        for (int i = 0; i < argumentTypes.length; i++) {
            Type argumentType = argumentTypes[i];
            mv.visitLocalVariable("localVariable_" + i, argumentType.getDescriptor(), null, methodStart,
                    returnStatementLabel, i + offset + static_offset);
            offset += argumentType.getSize() - 1;
        }
        int max = static_offset + Arrays.stream(argumentTypes).mapToInt(Type::getSize).sum();
        Label endIgnore = new AnnotatedLabel(true, false);
        endIgnore.info = new LabelNode(endIgnore);
        mv.visitLabel(endIgnore);
        mv.visitMaxs(max == 0 ? Type.getReturnType(descriptor).getSize() : max, max);
        mv.visitEnd();
    }

    private static void pushThisReference(MethodVisitor mv) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
    }

    private static void pushArgument(MethodVisitor mv, Type argumentType, int index, VoidCallable booleanConversion) {
        String s = argumentType.toString();
        switch (argumentType.toString()) {
            case "Z":
                mv.visitVarInsn(ILOAD, index);
                booleanConversion.call();
                break;
            case "B":
            case "S":
            case "C":
            case "I":
                mv.visitVarInsn(Opcodes.ILOAD, index);
                break;
            case "J":
                mv.visitVarInsn(Opcodes.LLOAD, index);
                break;
            case "F":
                mv.visitVarInsn(Opcodes.FLOAD, index);
                break;
            case "D":
                mv.visitVarInsn(Opcodes.DLOAD, index);
                break;
            default:
                mv.visitVarInsn(Opcodes.ALOAD, index);
        }
    }

    private void callInstrumentedMethod(MethodVisitor mv, int access, String name, String descriptor) {
        int invocationType = (access & Opcodes.ACC_STATIC) == 0 ? INVOKESPECIAL : Opcodes.INVOKESTATIC;
        mv.visitMethodInsn(invocationType, internalName.replaceAll("\\.", "/"), BooleanToIntTransformer.instrumentedMethodName(name,
                descriptor), BooleanToIntTransformer.instrumentedMethodDescriptor(descriptor), false);
    }

    private static void addReturnStatement(MethodVisitor mv, String descriptor, VoidCallable booleanConversion) {
        Type returnType = Type.getReturnType(descriptor);
        switch (returnType.toString()) {
            case "V":
                mv.visitInsn(Opcodes.RETURN);
                break;
            case "Z":
                booleanConversion.call();
            case "B":
            case "S":
            case "C":
            case "I":
                mv.visitInsn(Opcodes.IRETURN);
                break;
            case "J":
                mv.visitInsn(Opcodes.LRETURN);
                break;
            case "F":
                mv.visitInsn(Opcodes.FRETURN);
                break;
            case "D":
                mv.visitInsn(Opcodes.DRETURN);
                break;
            default:
                mv.visitInsn(Opcodes.ARETURN);
        }
    }

    void addInheritedMethod(MethodReaderClassVisitor.MethodInformation information) {
        if ((this.access & ACC_INTERFACE) != 0) return;
        if (!this.classIsInitializable) return;
        logger.debug("Adding inherited method: {}::{}::{} to {}", information.getClassName(),
                information.getMethodName(), information.getDescriptor(), this.className);
        String descriptor = information.getDescriptor();
        if (!BooleanToIntTransformer.changes(descriptor)) return;
        String methodName = information.getMethodName();
        String instrumentedMethodName = BooleanToIntTransformer.instrumentedMethodName(methodName, descriptor);
        String instrumentedMethodDescriptor = BooleanToIntTransformer.instrumentedMethodDescriptor(descriptor);
        notifyHelperMethod(className, instrumentedMethodName, instrumentedMethodDescriptor);
        MethodVisitor mv = super.visitMethod(ACC_PUBLIC, instrumentedMethodName, instrumentedMethodDescriptor, null,
                information.getExceptions());
        mv.visitCode();
        pushThisReference(mv);
        int static_offset = 1;
        int offset = 0;
        Type[] argumentTypes = Type.getArgumentTypes(descriptor);
        if (classIsInstrumented.test(information.getClassName())) {
            for (int i = 0; i < argumentTypes.length; i++) {
                Type argumentType = argumentTypes[i];
                pushArgument(mv, argumentType, i + offset + static_offset, () -> {
                });
                offset = offset + argumentType.getSize() - 1;
            }
            mv.visitMethodInsn(INVOKESPECIAL, information.getClassName(), BooleanToIntTransformer.instrumentedMethodName(methodName,
                    descriptor), BooleanToIntTransformer.instrumentedMethodDescriptor(descriptor), false);
            addReturnStatement(mv, descriptor, () -> {
            });
        } else {
            // class is not Instrumented
            for (int i = 0; i < argumentTypes.length; i++) {
                Type argumentType = argumentTypes[i];
                pushArgument(mv, argumentType, i + offset + static_offset, () -> {
                    mv.visitMethodInsn(INVOKESTATIC, BooleanToIntTransformer.BOOLEAN_TO_INT_UTIL_NAME,
                            BooleanToIntTransformer.FROM_INT_NAME_AND_DESCRIPTOR.getName()
                            , BooleanToIntTransformer.FROM_INT_NAME_AND_DESCRIPTOR.getDescriptor(), false);
                });
                offset = offset + argumentType.getSize() - 1;
            }
            mv.visitMethodInsn(INVOKESPECIAL, information.getClassName(), methodName, descriptor, false);
            addReturnStatement(mv, descriptor, () -> mv.visitMethodInsn(INVOKESTATIC, BooleanToIntTransformer.BOOLEAN_TO_INT_UTIL_NAME,
                    BooleanToIntTransformer.TO_INT_NAME_AND_DESCRIPTOR.getName(), BooleanToIntTransformer.TO_INT_NAME_AND_DESCRIPTOR.getDescriptor(), false));
        }
        int maxStack = Arrays.stream(Type.getArgumentTypes(descriptor)).mapToInt(Type::getSize).sum() + 1;
        mv.visitMaxs(maxStack, maxStack);
        mv.visitEnd();
    }

    void addInheritedMethods() {
        inheritedMethods.stream().filter(m -> declaredMethods.stream().filter(dm -> dm.matchesDescriptor(m.getDescriptor())).noneMatch(dm -> dm.matchesMethod(m.getMethodName()))).forEach(this::addInheritedMethod);
    }
}
