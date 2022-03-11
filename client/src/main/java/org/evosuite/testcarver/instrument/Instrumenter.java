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
package org.evosuite.testcarver.instrument;

import org.evosuite.PackageInfo;
import org.evosuite.Properties;
import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.capture.CaptureUtil;
import org.evosuite.testcarver.capture.FieldRegistry;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.TraceClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public final class Instrumenter {
    private int captureId;

    public static final int CAPTURE_ID_JAVA_UTIL_DATE = Integer.MIN_VALUE;
    public static final int CAPTURE_ID_JAVA_UTIL_CALENDAR = Integer.MIN_VALUE + 1;
    public static final int CAPTURE_ID_JAVA_TEXT_DATEFORMAT = Integer.MIN_VALUE + 2;
    public static final int CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT = Integer.MIN_VALUE + 3;


    public static final String WRAP_NAME_PREFIX = "_sw_prototype_original_";

    private static final Logger logger = LoggerFactory.getLogger(Instrumenter.class);

    public Instrumenter() {
        this.captureId = CAPTURE_ID_JAVA_UTIL_DATE + 4;
    }


    public void instrument(final String className, final ClassNode cn) {
        if (!TransformerUtil.isClassConsideredForInstrumentation(className)) {
            logger.debug("Class {} has not been instrumented because its name is on the blacklist", className);
            return;
        }

        try {
            this.transformClassNode(cn, className);
        } catch (final Throwable t) {
            logger.error("An error occurred while instrumenting class {} -> returning unmodified version", className, t);
        }

    }

    public byte[] instrument(final String className, final byte[] classfileBuffer) throws IllegalClassFormatException {
        logger.debug("Start instrumenting class {}", className);


        final ClassReader cr = new ClassReader(classfileBuffer);
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        final ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.SKIP_DEBUG);

        if (!TransformerUtil.isClassConsideredForInstrumentation(className)) {
            logger.debug("Class {} has not been instrumented because its name is on the blacklist", className);
            return classfileBuffer;
        }

        try {
            this.transformClassNode(cn, className);

            cn.accept(cw);

            return cw.toByteArray();

        } catch (final Throwable t) {
            logger.error("An error occurred while instrumenting class {} -> returning unmodified version", className, t);
            return classfileBuffer;
        }
    }

    @SuppressWarnings("unchecked")
    private void addFieldRegistryRegisterCall(final MethodNode methodNode) {
        AbstractInsnNode ins = null;
        ListIterator<AbstractInsnNode> iter = methodNode.instructions.iterator();

        int numInvokeSpecials = 0; // number of invokespecial calls before actual constructor call

        while (iter.hasNext()) {
            ins = iter.next();

            if (ins instanceof MethodInsnNode) {
                MethodInsnNode mins = (MethodInsnNode) ins;
                if (ins.getOpcode() == Opcodes.INVOKESPECIAL) {
                    if (mins.name.startsWith("<init>")) {
                        if (numInvokeSpecials == 0) {
                            break;
                        } else {
                            numInvokeSpecials--;
                        }
                    }
                }
            } else if (ins instanceof TypeInsnNode) {
                TypeInsnNode typeIns = (TypeInsnNode) ins;
                if (typeIns.getOpcode() == Opcodes.NEW || typeIns.getOpcode() == Opcodes.NEWARRAY) {
                    numInvokeSpecials++;
                }
            }
        }


        final InsnList instructions = new InsnList();

        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                PackageInfo.getNameWithSlash(FieldRegistry.class),
                "register",
                "(Ljava/lang/Object;)V"));

        methodNode.instructions.insert(ins, instructions);
    }

    @SuppressWarnings("unchecked")
    public void transformClassNode(ClassNode cn, final String internalClassName) {
        if (!TransformerUtil.isClassConsideredForInstrumentation(internalClassName)) {
            logger.debug("Class {} has not been instrumented because its name is on the blacklist", internalClassName);
            return;
        }

        // consider only public and protected classes which are not interfaces
        if ((cn.access & Opcodes.ACC_INTERFACE) != 0) {
            return;
        }

        // No private
        if ((cn.access & Opcodes.ACC_PRIVATE) != 0) {
            // TODO: Why is this not detecting $DummyIntegrator?
            logger.debug("Ignoring private class {}", cn.name);
            return;
        }

        String packageName = internalClassName.replace('/', '.');
        if (packageName.contains("."))
            packageName = packageName.substring(0, packageName.lastIndexOf('.'));


        // ASM has some problem with the access of inner classes
        // so we check if the inner class name is the current class name
        // and if so, check if the inner class is actually accessible
        List<InnerClassNode> in = cn.innerClasses;
        for (InnerClassNode inc : in) {
            if (cn.name.equals(inc.name)) {
                logger.info("ASM Bug: Inner class equals class.");
                if ((inc.access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED) {
                    if (!Properties.CLASS_PREFIX.equals(packageName)) {
                        return;
                    }
                }
                if ((inc.access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE) {
                    return;
                }
                logger.debug("Can use inner class {}", inc.name);
            }
        }
        logger.info("Checking package {} for class {}", packageName, cn.name);

        // Protected/default only if in same package
        if ((cn.access & Opcodes.ACC_PUBLIC) == 0) {
            if (!Properties.CLASS_PREFIX.equals(packageName)) {
                logger.info("Not using protected/default class because package name does not match");
                return;
            } else {
                logger.info("Using protected/default class because package name matches");
            }
        }
		/*
		if(	(cn.access & Opcodes.ACC_PUBLIC) == 0 && (cn.access & Opcodes.ACC_PROTECTED) == 0)
		{
			return;
		}
		*/

        final ArrayList<MethodNode> wrappedMethods = new ArrayList<>();
        MethodNode methodNode;

        for (final MethodNode node : cn.methods) {
            methodNode = node;

            // consider only public methods which are not abstract or native
            if (!TransformerUtil.isPrivate(methodNode.access) &&
                    !TransformerUtil.isAbstract(methodNode.access) &&
                    !TransformerUtil.isNative(methodNode.access) &&
                    !methodNode.name.equals("<clinit>")) {
                if (!TransformerUtil.isPublic(methodNode.access)) {
                    //if(!Properties.CLASS_PREFIX.equals(packageName)) {
                    transformWrapperCalls(methodNode);
                    continue;
                    //}
                }
                if (methodNode.name.equals("<init>")) {
                    if (TransformerUtil.isAbstract(cn.access)) {
                        // We cannot invoke constructors of abstract classes directly
                        continue;
                    }
                    this.addFieldRegistryRegisterCall(methodNode);
                }

                this.instrumentPUTXXXFieldAccesses(cn, internalClassName, methodNode);
                this.instrumentGETXXXFieldAccesses(cn, internalClassName, methodNode);

                this.instrumentMethod(cn, internalClassName, methodNode, wrappedMethods);
            } else {
                transformWrapperCalls(methodNode);
            }
        }

        final int numWM = wrappedMethods.size();
        cn.methods.addAll(wrappedMethods);

        TraceClassVisitor tcv = new TraceClassVisitor(new PrintWriter(System.err));
        cn.accept(tcv);
    }


    private void instrumentGETXXXFieldAccesses(final ClassNode cn, final String internalClassName, final MethodNode methodNode) {
        final InsnList instructions = methodNode.instructions;

        AbstractInsnNode ins = null;
        FieldInsnNode fieldIns = null;

        for (int i = 0; i < instructions.size(); i++) {
            ins = instructions.get(i);
            if (ins instanceof FieldInsnNode) {
                fieldIns = (FieldInsnNode) ins;

                /*
                 * Is field referencing outermost instance? if yes, ignore it
                 * http://tns-www.lcs.mit.edu/manuals/java-1.1.1/guide/innerclasses/spec/innerclasses.doc10.html
                 */
                if (fieldIns.name.endsWith("$0")) {
                    continue;
                }

                final int opcode = ins.getOpcode();
                if (opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC) {
                    final InsnList il = new InsnList();

                    if (opcode == Opcodes.GETFIELD) {
                        Type fieldType = Type.getType(fieldIns.desc);
                        if (fieldType.getSize() == 1) {
                            instructions.insertBefore(fieldIns, new InsnNode(Opcodes.DUP));
                            il.add(new InsnNode(Opcodes.SWAP));
                        } else if (fieldType.getSize() == 2) {
                            instructions.insertBefore(fieldIns, new InsnNode(Opcodes.DUP));
                            // v
                            // GETFIELD
                            // v, w
                            il.add(new InsnNode(Opcodes.DUP2_X1));
                            // w, v, w
                            il.add(new InsnNode(Opcodes.POP2));
                            // w, v
                            // -> Call
                            // w
                        }
                    } else
                        il.add(new InsnNode(Opcodes.ACONST_NULL));

                    il.add(new LdcInsnNode(this.captureId));
                    il.add(new LdcInsnNode(fieldIns.owner));
                    il.add(new LdcInsnNode(fieldIns.name));
                    il.add(new LdcInsnNode(fieldIns.desc));

                    il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            PackageInfo.getNameWithSlash(org.evosuite.testcarver.capture.FieldRegistry.class),
                            "notifyReadAccess",
                            "(Ljava/lang/Object;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"));

                    i += il.size();

                    instructions.insert(fieldIns, il);
                    this.captureId++;
                }
            }
        }
    }


    private void instrumentPUTXXXFieldAccesses(final ClassNode cn, final String internalClassName, final MethodNode methodNode) {
        final InsnList instructions = methodNode.instructions;

        AbstractInsnNode ins = null;
        FieldInsnNode fieldIns = null;

        // needed get right receiver var in case of PUTFIELD

        for (int i = 0; i < instructions.size(); i++) {
            ins = instructions.get(i);
            if (ins instanceof FieldInsnNode) {
                fieldIns = (FieldInsnNode) ins;

                /*
                 * Is field referencing outermost instance? if yes, ignore it
                 * http://tns-www.lcs.mit.edu/manuals/java-1.1.1/guide/innerclasses/spec/innerclasses.doc10.html
                 */
                if (fieldIns.name.endsWith("$0")) {
                    continue;
                }


                final int opcode = ins.getOpcode();
                if (opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC) {
                    // construction of
                    //   Capturer.capture(final Object receiver, final String methodName, final Object[] methodParams)
                    // call
                    final InsnList il = new InsnList();

                    if (opcode == Opcodes.PUTFIELD) {
                        Type fieldType = Type.getType(fieldIns.desc);
                        if (fieldType.getSize() == 1) {
                            instructions.insertBefore(fieldIns, new InsnNode(Opcodes.DUP2));
                            il.add(new InsnNode(Opcodes.POP));
                        } else if (fieldType.getSize() == 2) {
                            InsnList uglyList = new InsnList();
                            // v, w
                            uglyList.add(new InsnNode(Opcodes.DUP2_X1));
                            // w, v, w
                            uglyList.add(new InsnNode(Opcodes.POP2));
                            // w, v
                            uglyList.add(new InsnNode(Opcodes.DUP));
                            // w, v, v
                            uglyList.add(new InsnNode(Opcodes.DUP2_X2));
                            // v, v, w, v, v
                            uglyList.add(new InsnNode(Opcodes.POP2));
                            // v, v, w
                            instructions.insertBefore(fieldIns, uglyList);
                            // PUTFIELD
                            // v
                        }
                    } else
                        il.add(new InsnNode(Opcodes.ACONST_NULL));

                    il.add(new LdcInsnNode(this.captureId));
                    il.add(new LdcInsnNode(fieldIns.owner));
                    il.add(new LdcInsnNode(fieldIns.name));
                    il.add(new LdcInsnNode(fieldIns.desc));

                    il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            PackageInfo.getNameWithSlash(FieldRegistry.class),
                            "notifyModification",
                            "(Ljava/lang/Object;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"));

                    // PUTFIELDRegistry.notifyModification also adds corresponding GETFIELD capture instructions
                    this.captureId++;
                    i += il.size();

                    instructions.insert(fieldIns, il);
                    this.captureId++;
                }
            }
        }
    }


    private void instrumentMethod(final ClassNode cn, final String internalClassName, final MethodNode methodNode, final List<MethodNode> wrappedMethods) {
        wrappedMethods.add(this.wrapMethod(cn, internalClassName, methodNode));
        this.captureId++;
    }


    private InsnList addCaptureCall(final boolean isStatic, final String internalClassName, final String methodName, final String methodDesc, final Type[] argTypes) {
        // construction of
        //   Capturer.capture(final Object receiver, final String methodName, final Object[] methodParams)
        // call
        final InsnList il = new InsnList();

        il.add(new LdcInsnNode(this.captureId));

        // --- load receiver argument
        int varIndex;
        if (isStatic) {
            // static method invocation
            il.add(new LdcInsnNode(internalClassName));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    PackageInfo.getNameWithSlash(CaptureUtil.class),
                    "loadClass",
                    "(Ljava/lang/String;)Ljava/lang/Class;"));


            varIndex = 0;
        } else {
            // non-static method call
            il.add(new VarInsnNode(Opcodes.ALOAD, 0));
            varIndex = 1;
        }

        // --- load method name argument

        il.add(new LdcInsnNode(methodName));

        // --- load method description argument

        il.add(new LdcInsnNode(methodDesc));

        // --- load methodParams arguments

        // load methodParams length
        // TODO ICONST_1 to ICONST_5 would be more efficient
        il.add(new IntInsnNode(Opcodes.BIPUSH, argTypes.length));

        // create array object
        il.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));

        // fill the array

        for (int i = 0; i < argTypes.length; i++) {
            il.add(new InsnNode(Opcodes.DUP));

            // TODO ICONST_1 to ICONST_5 would be more efficient
            il.add(new IntInsnNode(Opcodes.BIPUSH, i));

            //check for primitives
            this.loadAndConvertToObject(il, argTypes[i], varIndex++);
            il.add(new InsnNode(Opcodes.AASTORE));

            // long/double take two registers
            if (argTypes[i].equals(Type.LONG_TYPE) || argTypes[i].equals(Type.DOUBLE_TYPE)) {
                varIndex++;
            }
        }

        // --- construct Capture.capture() call

        il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                PackageInfo.getNameWithSlash(org.evosuite.testcarver.capture.Capturer.class),
                "capture",
                "(ILjava/lang/Object;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V"));

        return il;
    }


    private void addCaptureEnableStatement(final String className, final MethodNode mn, final InsnList il, final int returnValueVar) {
        il.add(new LdcInsnNode(this.captureId));


        if (TransformerUtil.isStatic(mn.access)) {
            // static method

            il.add(new LdcInsnNode(className));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    PackageInfo.getNameWithSlash(CaptureUtil.class),
                    "loadClass",
                    "(Ljava/lang/String;)Ljava/lang/Class;"));
        } else {
            // non-static method

            il.add(new VarInsnNode(Opcodes.ALOAD, 0));
        }


        final Type returnType = Type.getReturnType(mn.desc);
        if (returnType.equals(Type.VOID_TYPE)) {
            // load return value for VOID methods
            il.add(new FieldInsnNode(Opcodes.GETSTATIC, PackageInfo.getNameWithSlash(CaptureLog.class),
                    "RETURN_TYPE_VOID",
                    Type.getDescriptor(Object.class)));
        } else {
            // load return value as object
            il.add(new VarInsnNode(Opcodes.ALOAD, returnValueVar));
        }

        il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                PackageInfo.getNameWithSlash(org.evosuite.testcarver.capture.Capturer.class),
                "enable",
                "(ILjava/lang/Object;Ljava/lang/Object;)V"));
    }


    /**
     * public int myMethod(int i)
     * {
     * try
     * {
     * return _sw_prototype_original_myMethod(i)
     * }
     * finally
     * {
     * Capturer.enable();
     * }
     * }
     *
     * @param classNode
     * @param className
     * @param methodNode
     */
    @SuppressWarnings("unchecked")
    private MethodNode wrapMethod(final ClassNode classNode, final String className, final MethodNode methodNode) {
        methodNode.maxStack += 4;

        // create wrapper for original method
        final MethodNode wrappingMethodNode = new MethodNode(methodNode.access,
                methodNode.name,
                methodNode.desc,
                methodNode.signature,
                methodNode.exceptions.toArray(new String[methodNode.exceptions.size()]));
        wrappingMethodNode.maxStack = methodNode.maxStack;

        // assign annotations to wrapping method
        wrappingMethodNode.visibleAnnotations = methodNode.visibleAnnotations;
        wrappingMethodNode.visibleParameterAnnotations = methodNode.visibleParameterAnnotations;

        // remove annotations from wrapped method to avoid wrong behavior controlled by annotations
        methodNode.visibleAnnotations = null;
        methodNode.visibleParameterAnnotations = null;

        // rename original method
        methodNode.access = TransformerUtil.modifyVisibility(methodNode.access, Opcodes.ACC_PRIVATE);

        final LabelNode l0 = new LabelNode();
        final LabelNode l1 = new LabelNode();
        final LabelNode l2 = new LabelNode();

        final InsnList wInstructions = wrappingMethodNode.instructions;

        if ("<init>".equals(methodNode.name)) {
            // wrap a constructor

            methodNode.name = WRAP_NAME_PREFIX + "init" + WRAP_NAME_PREFIX;

            // move call to other constructors to new method
            AbstractInsnNode ins = null;
            ListIterator<AbstractInsnNode> iter = methodNode.instructions.iterator();

            int numInvokeSpecials = 0; // number of invokespecial calls before actual constructor call

            while (iter.hasNext()) {
                ins = iter.next();
                iter.remove();
                wInstructions.add(ins);

                if (ins instanceof MethodInsnNode) {
                    MethodInsnNode mins = (MethodInsnNode) ins;
                    if (ins.getOpcode() == Opcodes.INVOKESPECIAL) {
                        if (mins.name.startsWith("<init>")) {
                            if (numInvokeSpecials == 0) {
                                break;
                            } else {
                                numInvokeSpecials--;
                            }
                        }
                    }
                } else if (ins instanceof TypeInsnNode) {
                    TypeInsnNode typeIns = (TypeInsnNode) ins;
                    if (typeIns.getOpcode() == Opcodes.NEW || typeIns.getOpcode() == Opcodes.NEWARRAY) {
                        numInvokeSpecials++;
                    }
                }
            }
        } else {
            methodNode.name = WRAP_NAME_PREFIX + methodNode.name;
        }


        int varReturnValue = 0;

        final Type returnType = Type.getReturnType(methodNode.desc);

        if (returnType.equals(Type.VOID_TYPE)) {
            wrappingMethodNode.tryCatchBlocks.add(new TryCatchBlockNode(l0, l1, l1, "java/lang/Throwable"));

        } else {

            wrappingMethodNode.tryCatchBlocks.add(new TryCatchBlockNode(l0, l1, l2, "java/lang/Throwable"));

            //--- create "Object returnValue = null;"

            if (!TransformerUtil.isStatic(methodNode.access)) {
                // load "this"
                varReturnValue++;
            }

            // consider method arguments to find right variable index
            final Type[] argTypes = Type.getArgumentTypes(methodNode.desc);
            for (final Type argType : argTypes) {
                varReturnValue++;

                // long/double take two registers
                if (argType.equals(Type.LONG_TYPE) || argType.equals(Type.DOUBLE_TYPE)) {
                    varReturnValue++;
                }
            }

            // push NULL on the stack and initialize variable for return value for it
            wInstructions.add(new InsnNode(Opcodes.ACONST_NULL));
            wInstructions.add(new VarInsnNode(Opcodes.ASTORE, varReturnValue));
        }

        int var = 0;

        // --- L0
        wInstructions.add(l0);

        wInstructions.add(this.addCaptureCall(TransformerUtil.isStatic(methodNode.access), className, wrappingMethodNode.name, wrappingMethodNode.desc, Type.getArgumentTypes(methodNode.desc)));

        // --- construct call to wrapped methode

        if (!TransformerUtil.isStatic(methodNode.access)) {
            // load "this" to call method
            wInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            var++;
        }


        final Type[] argTypes = Type.getArgumentTypes(methodNode.desc);
        for (final Type argType : argTypes) {
            this.addLoadInsn(wInstructions, argType, var++);

            // long/double take two registers
            if (argType.equals(Type.LONG_TYPE) || argType.equals(Type.DOUBLE_TYPE)) {
                var++;
            }
        }


        if (TransformerUtil.isStatic(methodNode.access)) {
            wInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    classNode.name,
                    methodNode.name,
                    methodNode.desc));
        } else {
            wInstructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                    classNode.name,
                    methodNode.name,
                    methodNode.desc));
        }

        var++;

        if (returnType.equals(Type.VOID_TYPE)) {
            wInstructions.add(new JumpInsnNode(Opcodes.GOTO, l2));

            // --- L1

            wInstructions.add(l1);

            wInstructions.add(new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"}));

            wInstructions.add(new VarInsnNode(Opcodes.ASTORE, --var));

            this.addCaptureEnableStatement(className, methodNode, wInstructions, -1);

            wInstructions.add(new VarInsnNode(Opcodes.ALOAD, var));
            wInstructions.add(new InsnNode(Opcodes.ATHROW));

            // FIXME <--- DUPLICATE CODE

            // --- L2

            wInstructions.add(l2);
            wInstructions.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));

            this.addCaptureEnableStatement(className, methodNode, wInstructions, -1);

            wInstructions.add(new InsnNode(Opcodes.RETURN));
        } else {
            // construct store of the wrapped method call's result

            this.addBoxingStmt(wInstructions, returnType);

            wInstructions.add(new VarInsnNode(Opcodes.ASTORE, varReturnValue));
            wInstructions.add(new VarInsnNode(Opcodes.ALOAD, varReturnValue));

            this.addUnBoxingStmt(wInstructions, returnType);

            final int storeOpcode = returnType.getOpcode(Opcodes.ISTORE);
            wInstructions.add(new VarInsnNode(storeOpcode, ++var)); // might be only var

            // --- L1

            wInstructions.add(l1);

            this.addCaptureEnableStatement(className, methodNode, wInstructions, varReturnValue);

            // construct load of the wrapped method call's result
            int loadOpcode = returnType.getOpcode(Opcodes.ILOAD);
            wInstructions.add(new VarInsnNode(loadOpcode, var));

            // construct return of the wrapped method call's result
            this.addReturnInsn(wInstructions, returnType);

            //---- L2

            wInstructions.add(l2);

            wInstructions.add(new FrameNode(Opcodes.F_FULL, 2, new Object[]{className, this.getInternalName(returnType)}, 1, new Object[]{"java/lang/Throwable"}));
            wInstructions.add(new VarInsnNode(Opcodes.ASTORE, --var));

            this.addCaptureEnableStatement(className, methodNode, wInstructions, varReturnValue);

            wInstructions.add(new VarInsnNode(Opcodes.ALOAD, var));
            wInstructions.add(new InsnNode(Opcodes.ATHROW));
        }
        transformWrapperCalls(methodNode);
        return wrappingMethodNode;
    }

    @SuppressWarnings("unchecked")
    private void transformWrapperCalls(MethodNode mn) {
        Iterator<AbstractInsnNode> iterator = mn.instructions.iterator();
        List<Class<?>> wrapperClasses = getWrapperClasses();

        while (iterator.hasNext()) {
            AbstractInsnNode insn = iterator.next();
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
                if (methodInsnNode.name.equals("<init>")) {
                    String ownerName = methodInsnNode.owner.replace('/', '.');
                    for (Class<?> wrapperClass : wrapperClasses) {
                        if (wrapperClass.getName().equals(ownerName)) {
                            logger.debug("Replacing call " + methodInsnNode.name);
                            methodInsnNode.owner = "org/evosuite/testcarver/wrapper/" + methodInsnNode.owner;
                            break;
                        }
                    }
                } else {
                    String ownerName = methodInsnNode.owner.replace('/', '.');
                    for (Class<?> wrapperClass : wrapperClasses) {
                        if (wrapperClass.getName().equals(ownerName)) {
                            if (methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC) {
                                logger.debug("Replacing call " + methodInsnNode.name);
                                methodInsnNode.owner = PackageInfo.getEvoSuitePackageWithSlash() + "/testcarver/wrapper/" + methodInsnNode.owner;
                            }
                            Type[] parameterTypes = Type.getArgumentTypes(methodInsnNode.desc);
                            try {
                                Class<?>[] parameterClasses = new Class<?>[parameterTypes.length];
                                int pos = 0;
                                for (Type parameter : parameterTypes) {
                                    switch (parameter.getSort()) {
                                        case Type.OBJECT:
                                            parameterClasses[pos++] = Class.forName(parameter.getClassName());
                                            break;
                                        case Type.BOOLEAN:
                                            parameterClasses[pos++] = boolean.class;
                                            break;
                                        case Type.BYTE:
                                            parameterClasses[pos++] = byte.class;
                                            break;
                                        case Type.CHAR:
                                            parameterClasses[pos++] = char.class;
                                            break;
                                        case Type.DOUBLE:
                                            parameterClasses[pos++] = double.class;
                                            break;
                                        case Type.FLOAT:
                                            parameterClasses[pos++] = float.class;
                                            break;
                                        case Type.INT:
                                            parameterClasses[pos++] = int.class;
                                            break;
                                        case Type.LONG:
                                            parameterClasses[pos++] = long.class;
                                            break;
                                        case Type.SHORT:
                                            parameterClasses[pos++] = short.class;
                                            break;
                                    }
                                }
                                Method method = wrapperClass.getMethod(methodInsnNode.name, parameterClasses);
                                if (Modifier.isFinal(method.getModifiers())) {
                                    if (methodInsnNode.getOpcode() != Opcodes.INVOKESTATIC) {
                                        methodInsnNode.setOpcode(Opcodes.INVOKESTATIC);
                                        Type[] args = Type.getArgumentTypes(methodInsnNode.desc);
                                        Type returnType = Type.getReturnType(methodInsnNode.desc);
                                        Type[] newargs = new Type[args.length + 1];
                                        newargs[0] = Type.getObjectType(methodInsnNode.owner);
                                        for (int i = 0; i < args.length; i++)
                                            newargs[i + 1] = args[i];
                                        methodInsnNode.desc = Type.getMethodDescriptor(returnType, newargs);
                                        methodInsnNode.owner = PackageInfo.getEvoSuitePackageWithSlash() + "/testcarver/wrapper/" + methodInsnNode.owner;
                                    } else {
                                        methodInsnNode.name += "_final";
                                    }
                                    logger.debug("Method is final: " + methodInsnNode.owner + "." + methodInsnNode.name);
                                } else {
                                    logger.debug("Method is not final: " + methodInsnNode.owner + "." + methodInsnNode.name);
                                }
                            } catch (Exception e) {
                                logger.warn("Error while instrumenting: " + e);
                            }

                            break;
                        }
                    }
                    //				} else if(methodInsnNode.name.equals("getTime")) {
                    //					if(methodInsnNode.owner.equals("java/util/Calendar")) {
                    //						logger.debug("Replacing call "+methodInsnNode.name);
                    //						methodInsnNode.owner = "org/evosuite/testcarver/wrapper/java/util/Calendar";
                    //						methodInsnNode.name = "getTime";
                    //						methodInsnNode.desc = "(Ljava/util/Calendar;)Ljava/util/Date;";
                    //						methodInsnNode.setOpcode(Opcodes.INVOKESTATIC);
                    //					}
                    //				}
                }
            } else if (insn.getOpcode() == Opcodes.NEW || insn.getOpcode() == Opcodes.CHECKCAST) {
                TypeInsnNode typeInsnNode = (TypeInsnNode) insn;
                Type generatedType = Type.getObjectType(typeInsnNode.desc);
                String name = generatedType.getInternalName().replace('/', '.');
                logger.debug("Checking for replacement of " + name);
                for (Class<?> wrapperClass : wrapperClasses) {
                    if (wrapperClass.getName().equals(name)) {
                        logger.debug("Replacing new " + name);
                        typeInsnNode.desc = PackageInfo.getEvoSuitePackageWithSlash() + "/testcarver/wrapper/" + generatedType.getInternalName();
                        break;
                    }
                }

            }
        }
    }

    private void addReturnInsn(final InsnList il, final Type type) {
        if (type.equals(Type.BOOLEAN_TYPE)) {
            il.add(new InsnNode(Opcodes.IRETURN));
        } else if (type.equals(Type.CHAR_TYPE)) {
            il.add(new InsnNode(Opcodes.IRETURN));
        } else if (type.equals(Type.BYTE_TYPE)) {
            il.add(new InsnNode(Opcodes.IRETURN));
        } else if (type.equals(Type.SHORT_TYPE)) {
            il.add(new InsnNode(Opcodes.IRETURN));
        } else if (type.equals(Type.INT_TYPE)) {
            il.add(new InsnNode(Opcodes.IRETURN));
        } else if (type.equals(Type.FLOAT_TYPE)) {
            il.add(new InsnNode(Opcodes.FRETURN));
        } else if (type.equals(Type.LONG_TYPE)) {
            il.add(new InsnNode(Opcodes.LRETURN));
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            il.add(new InsnNode(Opcodes.DRETURN));
        } else {
            il.add(new InsnNode(Opcodes.ARETURN));
        }
    }

    private void addLoadInsn(final InsnList il, final Type type, final int argLocation) {
        if (type.equals(Type.BOOLEAN_TYPE)) {
            il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
        } else if (type.equals(Type.CHAR_TYPE)) {
            il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
        } else if (type.equals(Type.BYTE_TYPE)) {
            il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
        } else if (type.equals(Type.SHORT_TYPE)) {
            il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
        } else if (type.equals(Type.INT_TYPE)) {
            il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
        } else if (type.equals(Type.FLOAT_TYPE)) {
            il.add(new VarInsnNode(Opcodes.FLOAD, argLocation));
        } else if (type.equals(Type.LONG_TYPE)) {
            il.add(new VarInsnNode(Opcodes.LLOAD, argLocation));
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            il.add(new VarInsnNode(Opcodes.DLOAD, argLocation));
        } else {
            il.add(new VarInsnNode(Opcodes.ALOAD, argLocation));
        }
    }


    private String getInternalName(final Type type) {
        if (type.equals(Type.BOOLEAN_TYPE)) {
            return "java/lang/Boolean";
        } else if (type.equals(Type.CHAR_TYPE)) {
            return "java/lang/Character";
        } else if (type.equals(Type.BYTE_TYPE)) {
            return "java/lang/Byte";
        } else if (type.equals(Type.SHORT_TYPE)) {
            return "java/lang/Short";
        } else if (type.equals(Type.INT_TYPE)) {
            return "java/lang/Integer";
        } else if (type.equals(Type.FLOAT_TYPE)) {
            return "java/lang/Float";
        } else if (type.equals(Type.LONG_TYPE)) {
            return "java/lang/Long";
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            return "java/lang/Double";
        } else {
            return type.getInternalName();
        }
    }


    private void loadAndConvertToObject(final InsnList il, final Type type, final int argLocation) {
        if (type.equals(Type.BOOLEAN_TYPE)) {
            il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean",
                    "valueOf", "(Z)Ljava/lang/Boolean;", false));
        } else if (type.equals(Type.CHAR_TYPE)) {
            il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Character",
                    "valueOf", "(C)Ljava/lang/Character;", false));
        } else if (type.equals(Type.BYTE_TYPE)) {
            il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Byte",
                    "valueOf", "(B)Ljava/lang/Byte;", false));
        } else if (type.equals(Type.SHORT_TYPE)) {
            il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Short",
                    "valueOf", "(S)Ljava/lang/Short;", false));
        } else if (type.equals(Type.INT_TYPE)) {
            il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer",
                    "valueOf", "(I)Ljava/lang/Integer;", false));
        } else if (type.equals(Type.FLOAT_TYPE)) {
            il.add(new VarInsnNode(Opcodes.FLOAD, argLocation));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float",
                    "valueOf", "(F)Ljava/lang/Float;", false));
        } else if (type.equals(Type.LONG_TYPE)) {
            il.add(new VarInsnNode(Opcodes.LLOAD, argLocation));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long",
                    "valueOf", "(J)Ljava/lang/Long;", false));
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            il.add(new VarInsnNode(Opcodes.DLOAD, argLocation));
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double",
                    "valueOf", "(D)Ljava/lang/Double;", false));
        } else {
            il.add(new VarInsnNode(Opcodes.ALOAD, argLocation));
        }
    }


    private void addBoxingStmt(final InsnList il, final Type type) {
        if (type.equals(Type.BOOLEAN_TYPE)) {
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean",
                    "valueOf", "(Z)Ljava/lang/Boolean;", false));
        } else if (type.equals(Type.CHAR_TYPE)) {
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Character",
                    "valueOf", "(C)Ljava/lang/Character;", false));
        } else if (type.equals(Type.BYTE_TYPE)) {
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Byte",
                    "valueOf", "(B)Ljava/lang/Byte;", false));
        } else if (type.equals(Type.SHORT_TYPE)) {
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Short",
                    "valueOf", "(S)Ljava/lang/Short;", false));
        } else if (type.equals(Type.INT_TYPE)) {
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer",
                    "valueOf", "(I)Ljava/lang/Integer;", false));
        } else if (type.equals(Type.FLOAT_TYPE)) {
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float",
                    "valueOf", "(F)Ljava/lang/Float;", false));
        } else if (type.equals(Type.LONG_TYPE)) {
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long",
                    "valueOf", "(J)Ljava/lang/Long;", false));
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double",
                    "valueOf", "(D)Ljava/lang/Double;", false));
        }
    }


    private void addUnBoxingStmt(final InsnList il, final Type type) {
        if (type.equals(Type.BOOLEAN_TYPE)) {
            il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean",
                    "booleanValue", "()Z", false));
        } else if (type.equals(Type.CHAR_TYPE)) {
            il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Character",
                    "charValue", "()C", false));
        } else if (type.equals(Type.BYTE_TYPE)) {
            il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Byte",
                    "byteValue", "()B", false));
        } else if (type.equals(Type.SHORT_TYPE)) {
            il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Short",
                    "shortValue", "()S", false));
        } else if (type.equals(Type.INT_TYPE)) {
            il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer",
                    "intValue", "()I", false));
        } else if (type.equals(Type.FLOAT_TYPE)) {
            il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Float",
                    "floatValue", "()F", false));
        } else if (type.equals(Type.LONG_TYPE)) {
            il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Long",
                    "longValue", "()J", false));
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Double",
                    "doubleValue", "()D", false));
        }
    }

    private List<Class<?>> getWrapperClasses() {
        return Arrays.asList(new Class<?>[]{java.util.Date.class,
                java.util.Calendar.class,
                java.text.DateFormat.class,
                java.text.SimpleDateFormat.class});
    }
}
