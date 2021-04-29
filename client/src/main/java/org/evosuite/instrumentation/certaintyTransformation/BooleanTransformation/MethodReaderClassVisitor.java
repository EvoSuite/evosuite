package org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.MethodIdentifier;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.*;

public class MethodReaderClassVisitor extends ClassVisitor {

    private final String className;
    private final Collection<MethodInformation> accessibleMethods = new HashSet<>();
    private String superClass;
    private String[] interfaces;
    private int access;
    private String defClassSignature;

    public MethodReaderClassVisitor(String className) {
        super(Opcodes.ASM7);
        this.className = className;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.superClass = superName;
        this.interfaces = Arrays.copyOf(interfaces, interfaces.length);
        this.access = access;
        this.defClassSignature = signature;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if( !isPrivate(access) && !isStatic(access) && !name.equals("<init>") && !name.equals("<clinit>")) {
            // This method is not private
            // We should provide an interface in the subclass.
            MethodIdentifier identifier = new MethodIdentifier(className, name, descriptor);
            accessibleMethods.add(new MethodInformation(identifier, signature,
                    exceptions != null ? Arrays.copyOf(exceptions, exceptions.length): null,
                    this.defClassSignature));
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    static boolean isPrivate(int access){
        return (access & Opcodes.ACC_PRIVATE) != 0;
    }

    static boolean isStatic(int access){
        return (access & Opcodes.ACC_STATIC) != 0;
    }

    public Collection<MethodInformation> getAccessibleMethods() {
        return new HashSet<>(accessibleMethods);
    }

    public Optional<String> getSuperClass() {
        return superClass != null && !superClass.equals(className) ? Optional.of(superClass) : Optional.empty();
    }

    public String[] getInterfaces() {
        return interfaces;
    }

    public int getAccess() {
        return access;
    }

    public static class MethodInformation{
        private final MethodIdentifier identifier;
        private final String signature;
        private final String[] exceptions;
        private final String defClassSignature;

        public MethodInformation(MethodIdentifier identifier,
                                 String signature,
                                 String[] exceptions,
                                 String defClassSignature){
            Objects.requireNonNull(identifier);
            this.identifier = identifier;
            this.signature = signature;
            this.exceptions = exceptions;
            this.defClassSignature = defClassSignature;
        }

        public MethodInformation(MethodIdentifier identifier, String Signature, String[] exceptions){
            this(identifier, Signature, exceptions, null);
        }


        public MethodInformation(String className, String methodName, String methodDescriptor,
                                          String signature,
                                 String[] exceptions){
            this(new MethodIdentifier(className, methodName, methodDescriptor), signature, exceptions);
        }

        public String getClassName(){
            return identifier.getInternalClassName();
        }

        public String getDescriptor(){
            return identifier.getMethodDescriptor();
        }

        public String getMethodName(){
            return identifier.getMethodName();
        }

        public MethodIdentifier getIdentifier() {
            return identifier;
        }

        public String getSignature() {
            return signature;
        }

        public String[] getExceptions() {
            return exceptions;
        }

        public String getDefClassSignature() {
            return defClassSignature;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MethodInformation)) return false;

            MethodInformation that = (MethodInformation) o;

            if (!identifier.matchesMethod(that.getMethodName())) return false;
            return identifier.matchesDescriptor(that.getDescriptor());
        }

        @Override
        public int hashCode() {
            int result = getMethodName().hashCode();
            result = 31 * result + getDescriptor().hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "MethodInformation{" +
                    "identifier=" + identifier +
                    ", signature='" + signature + '\'' +
                    ", exceptions=" + Arrays.toString(exceptions) +
                    '}';
        }
    }
}
