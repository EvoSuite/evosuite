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
package org.evosuite.symbolic.instrument;

import org.apache.commons.lang3.NotImplementedException;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.evosuite.dse.util.Assertions.check;
import static org.evosuite.dse.util.Assertions.notNull;
import static org.evosuite.symbolic.instrument.ConcolicConfig.*;
import static org.objectweb.asm.Opcodes.*;

/*
    This class is taken and adapted from the DSC tool developed by Christoph Csallner.
    Link at :
    http://ranger.uta.edu/~csallner/dsc/index.html
 */

/**
 * Main instrumentation class
 *
 * <p>
 * Before each user ByteCode instruction, add a call to one of our static
 * methods, that reflects the particular ByteCode. In a few cases noted below,
 * we add our callback after the user ByteCode, instead of before.
 *
 * @author csallner@uta.edu (Christoph Csallner)
 */
public final class ConcolicMethodAdapter extends GeneratorAdapter {

    private static final String THIS$0 = "this$0";

    private static final String INIT = "<init>"; //$NON-NLS-1$
    private static final String CLINIT = "<clinit>"; //$NON-NLS-1$

    private static final String METHOD_BEGIN = "METHOD_BEGIN"; //$NON-NLS-1$
    private static final String METHOD_BEGIN_PARAM = "METHOD_BEGIN_PARAM"; //$NON-NLS-1$
    private static final String METHOD_BEGIN_RECEIVER = "METHOD_BEGIN_RECEIVER"; //$NON-NLS-1$

    private static final String CALL_RESULT = "CALL_RESULT"; //$NON-NLS-1$
    private static final String METHOD_MAXS = "METHOD_MAXS"; //$NON-NLS-1$

    /**
     * InokveDynamic bootstrap methods owner classes
     * <p>
     * NOTE (ilebrero): We don't use class.getSimpleName() over LambdaMetaFactory and StringConcatFactory themselves
     * for retro compatibility with older JDK versions. Is there a more elegant way of doing this?
     */
    private static final String LAMBDA_METAFACTORY = "LambdaMetafactory"; //$NON-NLS-1$
    private static final String STRING_CONCAT_FACTORY = "StringConcatFactory"; //$NON-NLS-1$

    private final int access;
    private final String className;
    private final String methName;
    private final String methDescription;

    private int branchCounter = 1;

    private final OperandStack stack;
    private final Set<Label> exceptionHandlers = new HashSet<>();

    /**
     * Constructor
     */
    ConcolicMethodAdapter(MethodVisitor mv, int access, String className, String methName,
                          String desc) {
        super(Opcodes.ASM9, mv, access, methName, desc);

        this.access = access;
        this.className = notNull(className);
        this.methName = notNull(methName);
        this.methDescription = notNull(desc);

        this.stack = new OperandStack(mv);
    }

    /**
     * Before first ByteCode of the method/constructor
     *
     * <p>
     * Issue one call per argument, excluding the "this" receiver for
     * non-constructor instance methods. Work left to right, starting with
     * receiver.
     *
     * <ol>
     * <li>
     * METHOD_BEGIN_RECEIVER(value) -- optional</li>
     * <li>
     * METHOD_BEGIN_PARAM(value, 0, 0)</li>
     * <li>
     * ...</li>
     * <li>
     * METHOD_BEGIN_PARAM(value, nrArgs-1, localsIndex)</li>
     * </ol>
     */
    @Override
    public void visitCode() {
        super.visitCode();

        /*
         * standard access flags may also fit into signed 16-bit. That would
         * allow using short and sipush. But Asm pseudo access flag
         * ACC_DEPRECATED is too large.
         */
        stack.pushInt(access);
        stack.pushStrings(className, methName, methDescription);
        insertCallback(METHOD_BEGIN, IGGG_V, false);

        final boolean needThis = !AccessFlags.isStatic(access)
                && !CLINIT.equals(methName);
        if (needThis && !INIT.equals(methName)) {
            mv.visitVarInsn(ALOAD, 0);
            insertCallback(METHOD_BEGIN_RECEIVER, L_V, false);
        }

        Type[] paramTypes = Type.getArgumentTypes(methDescription); // does not
        // include
        // "this"

        /*
         * Category-2 parameters require two slots in locals, so the index into
         * the locals increases by two.
         */
        int paramNr = 0;
        int calleeLocalsIndex = 0;
        if (needThis)
            calleeLocalsIndex += 1;

        for (Type type : paramTypes) {
            String dscMethParamSign = null;
            switch (type.getSort()) {
                case Type.BOOLEAN:
                    mv.visitVarInsn(ILOAD, calleeLocalsIndex);
                    dscMethParamSign = ZII_V;
                    break;
                case Type.BYTE:
                    mv.visitVarInsn(ILOAD, calleeLocalsIndex);
                    dscMethParamSign = BII_V;
                    break;
                case Type.CHAR:
                    mv.visitVarInsn(ILOAD, calleeLocalsIndex);
                    dscMethParamSign = CII_V;
                    break;
                case Type.SHORT:
                    mv.visitVarInsn(ILOAD, calleeLocalsIndex);
                    dscMethParamSign = SII_V;
                    break;
                case Type.INT:
                    mv.visitVarInsn(ILOAD, calleeLocalsIndex);
                    dscMethParamSign = III_V;
                    break;
                case Type.LONG:
                    mv.visitVarInsn(LLOAD, calleeLocalsIndex);
                    dscMethParamSign = JII_V;
                    break;
                case Type.DOUBLE:
                    mv.visitVarInsn(DLOAD, calleeLocalsIndex);
                    dscMethParamSign = DII_V;
                    break;
                case Type.FLOAT:
                    mv.visitVarInsn(FLOAD, calleeLocalsIndex);
                    dscMethParamSign = FII_V;
                    break;
                case Type.ARRAY:
                case Type.OBJECT:
                    mv.visitVarInsn(ALOAD, calleeLocalsIndex);
                    dscMethParamSign = LII_V;
                    break;
            }

            stack.pushInt(paramNr);
            stack.pushInt(calleeLocalsIndex);
            insertCallback(METHOD_BEGIN_PARAM, dscMethParamSign, false);

            paramNr += 1;
            calleeLocalsIndex += type.getSize();
        }
    }

    /**
     * Insert call to our method directly before the corresponding user
     * instruction
     */
    @Override
    public void visitInsn(int opcode) {
        String instructionDescriptor;

        /* Divide instructions: Pass second argument (top of stack) to listeners */
        switch (opcode) {
            case IDIV:
            case IREM:
                mv.visitInsn(DUP);
                instructionDescriptor = I_V;
                break;
            case LDIV:
            case LREM:
                mv.visitInsn(DUP2);
                instructionDescriptor = J_V;
                break;
            case FDIV:
            case FREM:
                mv.visitInsn(DUP);
                instructionDescriptor = F_V;
                break;
            case DDIV:
            case DREM:
                mv.visitInsn(DUP2);
                instructionDescriptor = D_V;
                break;

            /*
             * ..., arrayref, index ==> ..., value
             */
            case IALOAD: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc6.html#iaload
            case LALOAD:
            case DALOAD: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc3.html#daload
            case FALOAD:
            case AALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD:
                mv.visitInsn(DUP2);
                mv.visitLdcInsn(className);
                mv.visitLdcInsn(methName);
                instructionDescriptor = LIGG_V;
                break;

            /*
             * ..., arrayref, index, value ==> ...
             */
            case IASTORE: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc6.html#iastore
            case FASTORE:
            case BASTORE:
            case CASTORE:
            case SASTORE:
                /* ..., arrayref, index, value */
                stack.c1b1a1__c1b1a1c1();
                /* ..., arrayref, index, value, arrayref */
                stack.c1b1a1__c1b1a1c1();
                /* ..., arrayref, index, value, arrayref, index */
                mv.visitLdcInsn(className);
                mv.visitLdcInsn(methName);
                instructionDescriptor = LIGG_V;
                break;

            case LASTORE:
            case DASTORE:
                /* ..., arrayref, index, value */
                stack.c1b1a2__c1b1a2c1();
                /* ..., arrayref, index, value, arrayref */
                stack.c1b2a1__c1b2a1c1();
                /* ..., arrayref, index, value, arrayref, index */
                mv.visitLdcInsn(className);
                mv.visitLdcInsn(methName);
                instructionDescriptor = LIGG_V;
                break;

            case AASTORE:
                /* ..., arrayref, index, value */
                stack.c1b1a1__c1b1a1c1();
                /* ..., arrayref, index, value, arrayref */
                stack.c1b1a1__c1b1a1c1();
                /* ..., arrayref, index, value, arrayref, index */
                stack.c1b1a1__c1b1a1c1();
                /* ..., arrayref, index, value, arrayref, index, value */
                mv.visitLdcInsn(className);
                mv.visitLdcInsn(methName);
                instructionDescriptor = LILGG_V;
                break;

            case ATHROW:
            case ARRAYLENGTH: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html#ARRAYLENGTH
                mv.visitInsn(DUP);
                instructionDescriptor = L_V;
                break;

            default:
                instructionDescriptor = V_V;
        }

        insertCallback(BYTECODE_NAME[opcode], instructionDescriptor, false);

        super.visitInsn(opcode); // user code ByteCode instruction
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {

        // The use of branchCounter is inlined so that branchIds match
        // what EvoSuite produces
        //
        switch (opcode) {
            case IFEQ: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc6.html#ifcond
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
                mv.visitInsn(DUP);
                mv.visitLdcInsn(className);
                mv.visitLdcInsn(methName);
                mv.visitLdcInsn(branchCounter++);
                insertCallback(BYTECODE_NAME[opcode], IGGI_V, false);
                break;

            case IF_ICMPEQ: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc6.html#if_icmpcond
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
                mv.visitInsn(DUP2);
                mv.visitLdcInsn(className);
                mv.visitLdcInsn(methName);
                mv.visitLdcInsn(branchCounter++);
                insertCallback(BYTECODE_NAME[opcode], IIGGI_V, false);
                break;

            case IF_ACMPEQ: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc6.html#if_acmpcond
            case IF_ACMPNE:
                mv.visitInsn(DUP2);
                mv.visitLdcInsn(className);
                mv.visitLdcInsn(methName);
                mv.visitLdcInsn(branchCounter++);
                insertCallback(BYTECODE_NAME[opcode], LLGGI_V, false);
                break;

            case IFNULL: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc6.html#ifnull
            case IFNONNULL:
                mv.visitInsn(DUP);
                mv.visitLdcInsn(className);
                mv.visitLdcInsn(methName);
                mv.visitLdcInsn(branchCounter++);
                insertCallback(BYTECODE_NAME[opcode], LGGI_V, false);
                break;

            case GOTO: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc5.html#goto
            case JSR:
            case 200: // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc5.html#goto_w
            case 201:
                insertCallback(BYTECODE_NAME[opcode], V_V, false);
                break;

            default:
                check(false, BYTECODE_NAME[opcode] + " is not a jump instruction.");
        }

        /* All our code is added now to the basic block, before the jump */

        super.visitJumpInsn(opcode, label); // user's jump instruction
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        stack.pushInt(line);
        insertCallback("SRC_LINE_NUMBER", I_V, false); //$NON-NLS-1$

        super.visitLineNumber(line, start);
    }

    /**
     * Pseudo-instruction, inserted directly before the corresponding target
     * instruction.
     *
     * <p>
     * Our instrumentation code does not change the shape of the instrumented
     * method's control flow graph. So hopefully we do not need to modify any
     * label and trust that ASM will recompute the concrete offsets correctly
     * for us.
     */
    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);

        if (!exceptionHandlers.contains(label)) {
            insertCallback("BB_BEGIN", V_V, false); //$NON-NLS-1$
            return;
        }

        /* Exception handler basic block */

        stack.pushInt(access);
        stack.pushStrings(className, methName, methDescription);
        insertCallback("HANDLER_BEGIN", IGGG_V, false); //$NON-NLS-1$
    }

    /**
     * <ul>
     * <li>
     * BIPUSH: push one byte from instruction stream to operand stack</li>
     * <li>
     * SIPUSH: push two bytes from instruction stream to operand stack</li>
     * <li>
     * NEWARRAY</li>
     * </ul>
     */
    @Override
    public void visitIntInsn(int opcode, int operand) {
        switch (opcode) {
            case BIPUSH: // @see
                // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc1.html#bipush
            case SIPUSH: // @see
                // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc13.html#sipush
                super.visitIntInsn(opcode, operand); // user ByteCode instruction
                mv.visitInsn(DUP);
                insertCallback(BYTECODE_NAME[opcode], I_V, false);
                return;

            case NEWARRAY: // @see
                // http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc10.html#newarray
                mv.visitInsn(DUP); // duplicate array length
                mv.visitIntInsn(BIPUSH, operand); // push array componenet type
                mv.visitLdcInsn(className);
                mv.visitLdcInsn(methName);
                insertCallback(BYTECODE_NAME[opcode], IIGG_V, false);
                super.visitIntInsn(opcode, operand); // user ByteCode instruction
                return;

            default:
                check(false);
        }
    }

    /**
     * <ul>
     * <li>
     * LDC, (LDC_W) -- push category one constant from constant pool</li>
     * <li>
     * LDC2_W -- push category two constant from constant pool</li>
     * </ul>
     * <p>
     * Insert call to our method after user ByteCode instruction, allows us to
     * use the result of LDC.
     *
     * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc8.html#ldc">ldc doc</a>
     * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc8.html#ldc2_w">ldc2 doc</a>
     */
    @Override
    public void visitLdcInsn(Object constant) {
        super.visitLdcInsn(constant); // Put our callback after LDC instruction,
        // so we can send result

        notNull(constant);

        if (constant instanceof Integer) {
            mv.visitInsn(DUP);
            insertCallback(BYTECODE_NAME[LDC], I_V, false);
        } else if (constant instanceof Float) {
            mv.visitInsn(DUP);
            insertCallback(BYTECODE_NAME[LDC], F_V, false);
        } else if (constant instanceof String) {
            mv.visitInsn(DUP);
            insertCallback(BYTECODE_NAME[LDC], G_V, false);
        } else if (constant instanceof Type) {
            mv.visitInsn(DUP);
            insertCallback(BYTECODE_NAME[LDC], CLASS_V, false); //$NON-NLS-1$
            // } else if (constant instanceof Handle) { // TODO: how are we supposed to handle this?
            // } else if (constant instanceof ConstantDynamic) { // TODO: complete if intended JDK11 support.
        } else { /* LDC2_W */
            mv.visitInsn(DUP2);

            if (constant instanceof Long)
                insertCallback(BYTECODE_NAME[20], J_V, false);
            else if (constant instanceof Double)
                insertCallback(BYTECODE_NAME[20], D_V, false);
            else
                check(false);
        }
    }

    /**
     * ILOAD, ISTORE, ILOAD_0, ILOAD_1, etc.
     *
     * <p>
     * These may follow a WIDE instruction.
     */
    @Override
    public void visitVarInsn(int opcode, int var) {
        check(var >= 0);

        if ((ILOAD <= opcode) && (opcode <= ALOAD) || (ISTORE <= opcode)
                && (opcode <= ASTORE)) {
            /*
             * Bytecode has an explicit parameter, e.g., ILOAD 33. In this case,
             * we need to push value 33 onto the user's operand stack.
             */
            stack.pushInt(var); // push local variable index
            insertCallback(BYTECODE_NAME[opcode], I_V, false);
        } else {
            /*
             * Bytecode does not have an explicit parameter, the parameter is
             * hard-coded into the ByteCode, e.g., ILOAD_2
             */
            insertCallback(BYTECODE_NAME[opcode], V_V, false);
        }

        super.visitVarInsn(opcode, var); // user ByteCode instruction
    }

    /**
     * GETFIELD, PUTFIELD, GETSTATIC, PUTSTATIC
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc5.html#getfield
     * http://java.sun.com/docs/books/jvms/second_edition/html
     * /Instructions2.doc11.html#putfield
     */
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {

        if (!name.equals(THIS$0)) {
            String signInvoke = GGG_V;
            Type fieldType = Type.getType(desc);

            /*
             * ..., objectref ==> ..., value
             */
            if (opcode == GETFIELD) { // top of stack == receiver instance
                mv.visitInsn(DUP);
                signInvoke = LGGG_V;
            }

            /*
             * ..., objectref, value ==> ...
             */
            if (opcode == PUTFIELD) { // top-1 == receiver instance
                signInvoke = LGGG_V;
                if (fieldType.getSize() == 1)
                    stack.b1a1__b1a1b1();
                else if (fieldType.getSize() == 2)
                    stack.b1a2__b1a2b1();
                else
                    check(false);
            }

            stack.pushStrings(owner, name, desc);
            insertCallback(BYTECODE_NAME[opcode], signInvoke, false);

        }
        super.visitFieldInsn(opcode, owner, name, desc); // user ByteCode
        // instruction
    }

    /**
     * INVOKEDYNAMIC
     *
     * @param name
     * @param methodDesc
     * @param bsm
     * @param bsmArgs
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.invokedynamic">
     * invokedynamic doc
     * </a>
     * <p>
     * TODO: Add support for other InvokeDynamic based features.
     * 		 (e.g. milling coin project, local variable type inference, dynalink, etc...)
     */
    @Override
    public void visitInvokeDynamicInsn(String name, String methodDesc, Handle bsm, Object... bsmArgs) {
        super.visitInvokeDynamicInsn(name, methodDesc, bsm, bsmArgs); // user ByteCode instruction

        mv.visitInsn(DUP); // newly created indy result

        String callbackMethodDesc;

        /**
         * These two seems to be the only cases of invokedynamic uses in JDK 9.
         * Dynalink and other JVM invokedynamic features are still not being
         * used in java. If we want to support dynamic languages at some point (scala, jruby, etc.) we should
         * use a more general implementation of indy.
         */
        if (ownerIsLambdaMetafactory(bsm)) {
            // Lambda case, is either a method reference, lambda usage or SAM method conversion
            push(((Handle) bsmArgs[1]).getOwner());
            callbackMethodDesc = LG_V;
        } else if (ownerIsStringConcatFactory(bsm)) {
            // String concatenation case
            push(bsm.getOwner());

            /**
             * String concatenation recipe, all parameter are already pushed to the symbolic stack
             * @see  <a href="https://docs.oracle.com/javase/9/docs/api/java/lang/invoke/StringConcatFactory.html">ldc doc</a>
             */
            push((String) bsmArgs[0]);
            callbackMethodDesc = GGG_V;
        } else {
            throw new NotImplementedException("Invokedynamic's bootstrap method not supported yet: " + bsm.getOwner());
        }

        insertCallback(BYTECODE_NAME[INVOKEDYNAMIC], callbackMethodDesc, false);
    }


    /**
     * Invoke a method/constructor/class initializer:
     * <ol>
     * <li>
     * INVOKE_X -- signature and receiver (!) of invoked method</li>
     * <li>
     * CALLER_STACK_PARAM -- last parameter</li>
     * <li>
     * CALLER_STACK_PARAM -- next-to-last parameter</li>
     * <li>
     * make call</li>
     * <li>
     * CALL_RESULT</li>
     * </ol>
     *
     * <p>
     * Add callback before any invocation. Besides all method calls (static,
     * instance, private, public, native, etc.) and all constructor calls, this
     * also works for the special case of the (implicit) super call that the
     * Java compiler adds to the beginning of each constructor:
     * "InvokeSpecial MySuperType <init>"
     *
     * <p>
     * Although the Java compiler does not allow any statement before this super
     * call, we can still add a callback at the JVM level. It seems that the
     * Java compiler (and language) is overly restrictive here.
     *
     * <p>
     * TODO: Pass receiver for more than two parameters.
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Concepts.doc.html
     * #16411
     */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        Type[] argTypes = Type.getArgumentTypes(descriptor); // does not include
        // "this"

        String signInvoke;

        if (opcode == INVOKEVIRTUAL || opcode == INVOKEINTERFACE
                || (opcode == INVOKESPECIAL && !INIT.equals(name))) {

            // pop arguments
            Map<Integer, Integer> to = popArguments(argTypes);

            // duplicate receiver
            dup();// callee

            // push arguments
            Type ownerType = Type.getObjectType(owner);
            for (int i = 0; i < argTypes.length; i++) {
                loadLocal(to.get(i));
                swap(ownerType, argTypes[i]);
            }

            /* INVOKE_X with receiver */

            // signInvoke = LGGG_V;
            signInvoke = LGGG_V;
            stack.pushStrings(owner, name, descriptor);
            insertCallback(BYTECODE_NAME[opcode], signInvoke, false);

        } else {

            /* INVOKE_X with no receiver */

            signInvoke = GGG_V;
            stack.pushStrings(owner, name, descriptor);
            insertCallback(BYTECODE_NAME[opcode], signInvoke, false);
        }

        /*
         * CALLER_STACK_PARAM Pass non-receiver-parameters one at a time, right
         * to left, downwards the operand stack
         */
        final boolean needThis = opcode == INVOKEVIRTUAL || opcode == INVOKEINTERFACE
                || opcode == INVOKESPECIAL;
        passCallerStackParams(argTypes, needThis);

        /* User's actual invoke instruction */

        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

        /* CALL_RESULT */

        Type returnType = Type.getReturnType(descriptor);
        if (returnType.getSort() != Type.VOID) {
            int instruction = returnType.getSize() == 2 ? DUP2 : DUP;
            mv.visitInsn(instruction);
        }

        String signResult = null;
        switch (returnType.getSort()) {
            case Type.VOID:
                signResult = GGG_V;
                break;
            case Type.DOUBLE:
                signResult = DGGG_V;
                break;
            case Type.FLOAT:
                signResult = FGGG_V;
                break;
            case Type.LONG:
                signResult = JGGG_V;
                break;
            case Type.OBJECT:
                signResult = LGGG_V;
                break;
            case Type.ARRAY:
                signResult = LGGG_V;
                break;
            case Type.BOOLEAN:
                signResult = ZGGG_V;
                break;
            case Type.SHORT:
            case Type.BYTE:
            case Type.CHAR:
            case Type.INT:
                signResult = IGGG_V;
                break;
            default:
                check(false);
        }

        stack.pushStrings(owner, name, descriptor);
        insertCallback(CALL_RESULT, signResult, false);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        this.visitMethodInsn(opcode, owner, name, desc, opcode == INVOKEINTERFACE);
    }

    /**
     * IINC
     *
     * <p>
     * Increment i-th local (int) variable by constant (int) value.
     *
     * <p>
     * May follow a WIDE instruction.
     */
    @Override
    public void visitIincInsn(int i, int value) {
        stack.pushInt(i); // push local variable index
        stack.pushInt(value); // push increment value
        insertCallback(BYTECODE_NAME[132], II_V, false);
        super.visitIincInsn(i, value); // user ByteCode instruction
    }

    /**
     * MULTIANEWARRAY
     *
     * <pre>
     * boolean[] b1 = new boolean[1]; // NEWARRAY T_BOOLEAN
     * Boolean[] B1 = new Boolean[1]; // ANEWARRAY java/lang/Boolean
     * boolean[][] b2 = new boolean[1][2]; // MULTIANEWARRAY [[Z 2
     * Boolean[][] B2 = new Boolean[1][2]; // MULTIANEWARRAY [[Ljava/lang/Boolean; 2
     * </pre>
     */
    @Override
    public void visitMultiANewArrayInsn(String arrayTypeDesc, int nrDimensions) {

        // FIXME: Also pass concrete dimension ints
        mv.visitLdcInsn(arrayTypeDesc); // push name of nrDimensions-dimensional
        // array type
        mv.visitIntInsn(SIPUSH, nrDimensions); // push number of dimensions
        // (unsigned byte)
        mv.visitLdcInsn(className);
        mv.visitLdcInsn(methName);
        insertCallback(BYTECODE_NAME[MULTIANEWARRAY], GIGG_V, false);

        super.visitMultiANewArrayInsn(arrayTypeDesc, nrDimensions); // user
        // ByteCode
        // instruction
    }

    /**
     * TABLESWITCH
     */
    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {


        int currentBranchIndex = branchCounter++;

        mv.visitInsn(DUP); // pass concrete int value
        stack.pushInt(min);
        stack.pushInt(max);
        // push branch className, methName and index
        mv.visitLdcInsn(className);
        mv.visitLdcInsn(methName);
        mv.visitLdcInsn(currentBranchIndex);
        String IIIGGI_V = "(" + INT + INT + INT + STR + STR + INT + ")" + VOID;
        insertCallback(BYTECODE_NAME[TABLESWITCH], IIIGGI_V, false);

        super.visitTableSwitchInsn(min, max, dflt, labels); // user ByteCode
        // instruction
    }

    /**
     * LOOKUPSWITCH
     *
     * <p>
     * TODO: Optimize this. Do we really need to create a new array every time
     * we execute this switch statement?
     */
    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {


        int currentBranchIndex = branchCounter++;

        mv.visitInsn(DUP); // pass concrete int value
        stack.pushInt(keys.length); // pass keys as a new array
        mv.visitIntInsn(NEWARRAY, 10);

        for (int i = 0; i < keys.length; i++) {
            mv.visitInsn(DUP);
            stack.pushInt(i);
            stack.pushInt(keys[i]);
            mv.visitInsn(IASTORE); // write the i-th case target into the new
            // array
        }

        // push branch className, methName and index

        mv.visitLdcInsn(className);
        mv.visitLdcInsn(methName);
        mv.visitLdcInsn(currentBranchIndex);

        String IRGGI_V = "(" + INT + INT_ARR + STR + STR + INT + ")" + VOID;

        insertCallback(BYTECODE_NAME[LOOKUPSWITCH], IRGGI_V, false);

        super.visitLookupSwitchInsn(dflt, keys, labels); // user ByteCode
        // instruction
    }

    /**
     * CHECKCAST, INSTANCEOF, NEW, ANEWARRAY
     */
    @Override
    public void visitTypeInsn(int opcode, String type) {

        switch (opcode) {
            case CHECKCAST:
            case INSTANCEOF:
                mv.visitInsn(DUP); // duplicate reference to be cast
                mv.visitLdcInsn(type); // pass name of type to be cast to
                insertCallback(BYTECODE_NAME[opcode], LG_V, false);
                break;

            case NEW:
                mv.visitLdcInsn(type);
                insertCallback(BYTECODE_NAME[opcode], G_V, false);
                break;

            case ANEWARRAY:
                mv.visitInsn(DUP); // duplicate array length
                mv.visitLdcInsn(type); // name of component type
                mv.visitLdcInsn(className);
                mv.visitLdcInsn(methName);
                insertCallback(BYTECODE_NAME[opcode], IGGG_V, false);
                break;

            default:
                check(false);
        }

        super.visitTypeInsn(opcode, type); // user ByteCode instruction
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        exceptionHandlers.add(handler);
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        stack.pushStrings(className, methName, methDescription);
        stack.pushInt(maxStack);
        stack.pushInt(maxLocals);
        insertCallback(METHOD_MAXS, GGGII_V, false); //$NON-NLS-1$
        super.visitMaxs(maxStack, maxLocals);
    }

    /**
     * Insert a callback to org.evosuite.dse.VM.methodName(methodDesc)
     */
    private void insertCallback(String methodName, String methodDesc, boolean isInterface) {
        mv.visitMethodInsn(INVOKESTATIC, VM_FQ, methodName, methodDesc, isInterface);
    }

    private void passCallerStackParams(Type[] argTypes, boolean needThis) {

        if (argTypes == null || argTypes.length == 0)
            return;

        Map<Integer, Integer> to = popArguments(argTypes);

        // restore all arguments for user invocation
        for (int i = 0; i < to.size(); i++)
            loadLocal(to.get(i));

        // push arguments copy and paramNr and calleLocalsIndex
        int calleeLocalsIndex = calculateCalleeLocalsIndex(argTypes, needThis);

        for (int i = argTypes.length - 1; i >= 0; i--) {
            int localVarIndex = to.get(i);
            loadLocal(localVarIndex);
            Type argType = argTypes[i];
            stack.passCallerStackParam(argType, i, calleeLocalsIndex);
            calleeLocalsIndex -= argType.getSize();
        }

    }

    private Map<Integer, Integer> popArguments(Type[] argTypes) {
        Map<Integer, Integer> to = new HashMap<>();
        for (int i = argTypes.length - 1; i >= 0; i--) {
            int loc = newLocal(argTypes[i]);
            storeLocal(loc);
            to.put(i, loc);
        }
        return to;
    }

    private int calculateCalleeLocalsIndex(Type[] argTypes, boolean needThis) {
        int calleeLocalsIndex = 0;
        for (Type type : argTypes)
            calleeLocalsIndex += type.getSize();
        if (needThis)
            calleeLocalsIndex += 1;
        return calleeLocalsIndex;
    }

    private boolean ownerIsLambdaMetafactory(Handle bsm) {
        return bsm.getOwner().contains(LAMBDA_METAFACTORY);
    }

    private boolean ownerIsStringConcatFactory(Handle bsm) {
        return bsm.getOwner().contains(STRING_CONCAT_FACTORY);
    }
}