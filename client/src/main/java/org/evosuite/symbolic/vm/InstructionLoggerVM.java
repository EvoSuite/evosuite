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
package org.evosuite.symbolic.vm;

import org.evosuite.Properties;
import org.evosuite.dse.AbstractVM;
import org.evosuite.symbolic.instrument.ConcolicConfig;
import org.evosuite.symbolic.vm.instructionlogger.IInstructionLogger;
import org.evosuite.symbolic.vm.instructionlogger.InstructionLoggerFactory;

/*
    This class is taken and adapted from the DSC tool developed by Christoph Csallner.
    Link at :
    http://ranger.uta.edu/~csallner/dsc/index.html
 */

/**
 * Log the name of a ByteCode instruction and any of its parameters
 *
 * @author csallner@uta.edu (Christoph Csallner)
 */
public class InstructionLoggerVM extends AbstractVM {

    private final IInstructionLogger instructionLogger;

    /**
     * Constructor
     */
    public InstructionLoggerVM() {
        this.instructionLogger = InstructionLoggerFactory.getInstance().getInstructionLogger(Properties.BYTECODE_LOGGING_MODE);
    }

    @Override
    public void SRC_LINE_NUMBER(int lineNr) {
        instructionLogger.log("\t\t\t\t\tsrc line: ");
        instructionLogger.logln(lineNr);
    }

    @Override
    public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, boolean value) {
        CALLER_STACK_PARAM(nr, calleeLocalsIndex, (Object) value);
    }

    @Override
    public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, byte value) {
        CALLER_STACK_PARAM(nr, calleeLocalsIndex, (Object) value);
    }

    @Override
    public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, char value) {
        CALLER_STACK_PARAM(nr, calleeLocalsIndex, (Object) value);
    }

    @Override
    public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, double value) {
        CALLER_STACK_PARAM(nr, calleeLocalsIndex, (Object) value);
    }

    @Override
    public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, float value) {
        CALLER_STACK_PARAM(nr, calleeLocalsIndex, (Object) value);
    }

    @Override
    public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, int value) {
        CALLER_STACK_PARAM(nr, calleeLocalsIndex, (Object) value);
    }

    @Override
    public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, long value) {
        CALLER_STACK_PARAM(nr, calleeLocalsIndex, (Object) value);
    }

    @Override
    public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, short value) {
        CALLER_STACK_PARAM(nr, calleeLocalsIndex, (Object) value);
    }

    @Override
    public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, Object value) {
        instructionLogger.log("callerStackParam ");
        instructionLogger.log(nr);
        // TODO: why theres a null value coming here? should that happend?
        if (value != null) {
            instructionLogger.logln(" ", value.toString());
        } else {
            instructionLogger.logln(" ", "null");
        }
    }


    @Override
    public void METHOD_BEGIN(int access, String className, String methName, String methDesc) {
        // FIXME: print modifiers (static, public, etc.)
        instructionLogger.log("-------------------", "enter method ");
        instructionLogger.log(className, " ");
        instructionLogger.log(methName, " ");
        instructionLogger.log(methDesc);
        instructionLogger.logln();
    }

    @Override
    public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, boolean value) {
        METHOD_BEGIN_PARAM(nr, calleeLocalsIndex, (Object) value);
    }

    @Override
    public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, byte value) {
        METHOD_BEGIN_PARAM(nr, calleeLocalsIndex, (Object) value);
    }

    @Override
    public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, char value) {
        METHOD_BEGIN_PARAM(nr, calleeLocalsIndex, (Object) value);
    }

    @Override
    public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, double value) {
        METHOD_BEGIN_PARAM(nr, calleeLocalsIndex, (Object) value);
    }

    @Override
    public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, float value) {
        METHOD_BEGIN_PARAM(nr, calleeLocalsIndex, (Object) value);
    }

    @Override
    public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, int value) {
        METHOD_BEGIN_PARAM(nr, calleeLocalsIndex, (Object) value);
    }

    @Override
    public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, long value) {
        METHOD_BEGIN_PARAM(nr, calleeLocalsIndex, (Object) value);
    }

    @Override
    public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, short value) {
        METHOD_BEGIN_PARAM(nr, calleeLocalsIndex, (Object) value);
    }

    @Override
    public void METHOD_BEGIN_PARAM(int nr, int calleeLocalsIndex, Object value) {
        instructionLogger.log("methodBeginParam ");
        instructionLogger.log(nr);
        instructionLogger.log(" ");
        instructionLogger.log(calleeLocalsIndex);
        if (value != null) {
            instructionLogger.logln(" ", value.toString());
        } else {
            instructionLogger.logln(" ", "null");
        }
    }

    @Override
    public void METHOD_BEGIN_RECEIVER(Object value) {
        instructionLogger.log("methodBeginReceiver ");
        if (value != null) {
            instructionLogger.logln(" ", value.toString());
        } else {
            instructionLogger.logln(" ", "null");
        }
    }


    @Override
    public void CALL_RESULT(String owner, String name, String desc) {
        CALL_RESULT("void");
    }

    @Override
    public void CALL_RESULT(boolean res, String owner, String name, String desc) {
        CALL_RESULT(Boolean.valueOf(res));
    }

    @Override
    public void CALL_RESULT(int res, String owner, String name, String desc) {
        CALL_RESULT(Integer.valueOf(res));
    }

    @Override
    public void CALL_RESULT(long res, String owner, String name, String desc) {
        CALL_RESULT(Long.valueOf(res));
    }

    @Override
    public void CALL_RESULT(double res, String owner, String name, String desc) {
        CALL_RESULT(Double.valueOf(res));
    }

    @Override
    public void CALL_RESULT(float res, String owner, String name, String desc) {
        CALL_RESULT(Float.valueOf(res));
    }

    @Override
    public void CALL_RESULT(Object res, String owner, String name, String desc) {
        CALL_RESULT(res);
    }

    protected void CALL_RESULT(Object res) {
        instructionLogger.log("\t ==> ");

        if (res == null) {
            instructionLogger.logln("null");
        } else {
            instructionLogger.logln(res.toString());
        }
    }

    @Override
    public void BB_BEGIN() {
        instructionLogger.logln("---------- basic block");
    }

    @Override
    public void HANDLER_BEGIN(int access, String className, String methName,
                              String methDesc) {
        instructionLogger.log("---------- handler block in ");
        instructionLogger.logln(className, ".", methName);
    }

    /*
     * Some 200 JVM ByteCode instructions
     */

    @Override
    public void NOP() {
        logInsn(0);
    }

    @Override
    public void ACONST_NULL() {
        logInsn(1);
    }

    @Override
    public void ICONST_M1() {
        logInsn(2);
    }

    @Override
    public void ICONST_0() {
        logInsn(3);
    }

    @Override
    public void ICONST_1() {
        logInsn(4);
    }

    @Override
    public void ICONST_2() {
        logInsn(5);
    }

    @Override
    public void ICONST_3() {
        logInsn(6);
    }

    @Override
    public void ICONST_4() {
        logInsn(7);
    }

    @Override
    public void ICONST_5() {
        logInsn(8);
    }

    @Override
    public void LCONST_0() {
        logInsn(9);
    }

    @Override
    public void LCONST_1() {
        logInsn(10);
    }

    @Override
    public void FCONST_0() {
        logInsn(11);
    }

    @Override
    public void FCONST_1() {
        logInsn(12);
    }

    @Override
    public void FCONST_2() {
        logInsn(13);
    }

    @Override
    public void DCONST_0() {
        logInsn(14);
    }

    @Override
    public void DCONST_1() {
        logInsn(15);
    }

    @Override
    public void BIPUSH(int value) {
        logInsn(16, value);
    }

    @Override
    public void SIPUSH(int value) {
        logInsn(17, value);
    }

    @Override
    public void LDC(Class<?> x) {
        logInsn(18, x);
    }

    @Override
    public void LDC(String x) {
        logInsn(18, x);
    }

    @Override
    public void LDC(int x) {
        logInsn(18, x);
    }

    @Override
    public void LDC(float x) {
        logInsn(18, Float.valueOf(x));
    }

    @Override
    public void LDC2_W(long x) {
        logInsn(20, Long.valueOf(x));
    }

    @Override
    public void LDC2_W(double x) {
        logInsn(20, Double.valueOf(x));
    }

    @Override
    public void ILOAD(int i) {
        logInsn(21, i);
    }

    @Override
    public void LLOAD(int i) {
        logInsn(22, i);
    }

    @Override
    public void FLOAD(int i) {
        logInsn(23, i);
    }

    @Override
    public void DLOAD(int i) {
        logInsn(24, i);
    }

    @Override
    public void ALOAD(int i) {
        logInsn(25, i);
    }

    @Override
    public void IALOAD(Object receiver, int index, String className, String methodName) {
        logInsn(46, receiver);
    } // visitInsn

    @Override
    public void LALOAD(Object receiver, int index, String className, String methodName) {
        logInsn(47, receiver);
    }

    @Override
    public void FALOAD(Object receiver, int index, String className, String methodName) {
        logInsn(48, receiver);
    }

    @Override
    public void DALOAD(Object receiver, int index, String className, String methodName) {
        logInsn(49, receiver);
    }

    @Override
    public void AALOAD(Object receiver, int index, String className, String methodName) {
        logInsn(50, receiver);
    }

    @Override
    public void BALOAD(Object receiver, int index, String className, String methodName) {
        logInsn(51, receiver);
    }

    @Override
    public void CALOAD(Object receiver, int index, String className, String methodName) {
        logInsn(52, receiver);
    }

    @Override
    public void SALOAD(Object receiver, int index, String className, String methodName) {
        logInsn(53, receiver);
    }

    @Override
    public void ISTORE(int i) {
        logInsn(54, i);
    }

    @Override
    public void LSTORE(int i) {
        logInsn(55, i);
    }

    @Override
    public void FSTORE(int i) {
        logInsn(56, i);
    }

    @Override
    public void DSTORE(int i) {
        logInsn(57, i);
    }

    @Override
    public void ASTORE(int i) {
        logInsn(58, i);
    }

    @Override
    public void IASTORE(Object arr, int index, String className, String methodName) {
        logInsn(79);
    }

    @Override
    public void LASTORE(Object arr, int index, String className, String methodName) {
        logInsn(80);
    }

    @Override
    public void FASTORE(Object arr, int index, String className, String methodName) {
        logInsn(81);
    }

    @Override
    public void DASTORE(Object arr, int index, String className, String methodName) {
        logInsn(82);
    }

    @Override
    public void AASTORE(Object arr, int index, Object ref, String className, String methodName) {
        logInsn(83);
    }

    @Override
    public void BASTORE(Object arr, int index, String className, String methodName) {
        logInsn(84);
    }

    @Override
    public void CASTORE(Object arr, int index, String className, String methodName) {
        logInsn(85);
    }

    @Override
    public void SASTORE(Object arr, int index, String className, String methodName) {
        logInsn(86);
    }

    @Override
    public void POP() {
        logInsn(87);
    }

    @Override
    public void POP2() {
        logInsn(88);
    }

    @Override
    public void DUP() {
        logInsn(89);
    }

    @Override
    public void DUP_X1() {
        logInsn(90);
    }

    @Override
    public void DUP_X2() {
        logInsn(91);
    }

    @Override
    public void DUP2() {
        logInsn(92);
    }

    @Override
    public void DUP2_X1() {
        logInsn(93);
    }

    @Override
    public void DUP2_X2() {
        logInsn(94);
    }

    @Override
    public void SWAP() {
        logInsn(95);
    }

    @Override
    public void IADD() {
        logInsn(96);
    }

    @Override
    public void LADD() {
        logInsn(97);
    }

    @Override
    public void FADD() {
        logInsn(98);
    }

    @Override
    public void DADD() {
        logInsn(99);
    }

    @Override
    public void ISUB() {
        logInsn(100);
    }

    @Override
    public void LSUB() {
        logInsn(101);
    }

    @Override
    public void FSUB() {
        logInsn(102);
    }

    @Override
    public void DSUB() {
        logInsn(103);
    }

    @Override
    public void IMUL() {
        logInsn(104);
    }

    @Override
    public void LMUL() {
        logInsn(105);
    }

    @Override
    public void FMUL() {
        logInsn(106);
    }

    @Override
    public void DMUL() {
        logInsn(107);
    }

    @Override
    public void IDIV(int rhs) {
        logInsn(108, rhs);
    }

    @Override
    public void LDIV(long rhs) {
        logInsn(109, Long.valueOf(rhs));
    }

    @Override
    public void FDIV(float rhs) {
        logInsn(110, Float.valueOf(rhs));
    }

    @Override
    public void DDIV(double rhs) {
        logInsn(111, Double.valueOf(rhs));
    }

    @Override
    public void IREM(int rhs) {
        logInsn(112, rhs);
    }

    @Override
    public void LREM(long rhs) {
        logInsn(113, Long.valueOf(rhs));
    }

    @Override
    public void FREM(float rhs) {
        logInsn(114, Float.valueOf(rhs));
    }

    @Override
    public void DREM(double rhs) {
        logInsn(115, Double.valueOf(rhs));
    }

    @Override
    public void INEG() {
        logInsn(116);
    }

    @Override
    public void LNEG() {
        logInsn(117);
    }

    @Override
    public void FNEG() {
        logInsn(118);
    }

    @Override
    public void DNEG() {
        logInsn(119);
    }

    @Override
    public void ISHL() {
        logInsn(120);
    }

    @Override
    public void LSHL() {
        logInsn(121);
    }

    @Override
    public void ISHR() {
        logInsn(122);
    }

    @Override
    public void LSHR() {
        logInsn(123);
    }

    @Override
    public void IUSHR() {
        logInsn(124);
    }

    @Override
    public void LUSHR() {
        logInsn(125);
    }

    @Override
    public void IAND() {
        logInsn(126);
    }

    @Override
    public void LAND() {
        logInsn(127);
    }

    @Override
    public void IOR() {
        logInsn(128);
    }

    @Override
    public void LOR() {
        logInsn(129);
    }

    @Override
    public void IXOR() {
        logInsn(130);
    }

    @Override
    public void LXOR() {
        logInsn(131);
    }

    @Override
    public void IINC(int i, int value) {
        logInsn(132, i, value);
    }

    @Override
    public void I2L() {
        logInsn(133);
    }

    @Override
    public void I2F() {
        logInsn(134);
    }

    @Override
    public void I2D() {
        logInsn(135);
    }

    @Override
    public void L2I() {
        logInsn(136);
    }

    @Override
    public void L2F() {
        logInsn(137);
    }

    @Override
    public void L2D() {
        logInsn(138);
    }

    @Override
    public void F2I() {
        logInsn(139);
    }

    @Override
    public void F2L() {
        logInsn(140);
    }

    @Override
    public void F2D() {
        logInsn(141);
    }

    @Override
    public void D2I() {
        logInsn(142);
    }

    @Override
    public void D2L() {
        logInsn(143);
    }

    @Override
    public void D2F() {
        logInsn(144);
    }

    @Override
    public void I2B() {
        logInsn(145);
    }

    @Override
    public void I2C() {
        logInsn(146);
    }

    @Override
    public void I2S() {
        logInsn(147);
    }

    @Override
    public void LCMP() {
        logInsn(148);
    }

    @Override
    public void FCMPL() {
        logInsn(149);
    }

    @Override
    public void FCMPG() {
        logInsn(150);
    }

    @Override
    public void DCMPL() {
        logInsn(151);
    }

    @Override
    public void DCMPG() {
        logInsn(152);
    }

    @Override
    public void IFEQ(String className, String methNane, int branchIndex, int p) {
        logInsn(153, p);
    }

    @Override
    public void IFNE(String className, String methNane, int branchIndex, int p) {
        logInsn(154, p);
    }

    @Override
    public void IFLT(String className, String methNane, int branchIndex, int p) {
        logInsn(155, p);
    }

    @Override
    public void IFGE(String className, String methNane, int branchIndex, int p) {
        logInsn(156, p);
    }

    @Override
    public void IFGT(String className, String methNane, int branchIndex, int p) {
        logInsn(157, p);
    }

    @Override
    public void IFLE(String className, String methNane, int branchIndex, int p) {
        logInsn(158, p);
    }

    @Override
    public void IF_ICMPEQ(String className, String methNane, int branchIndex, int left, int right) {
        logInsn(159, left, right);
    }

    @Override
    public void IF_ICMPNE(String className, String methNane, int branchIndex, int left, int right) {
        logInsn(160, left, right);
    }

    @Override
    public void IF_ICMPLT(String className, String methNane, int branchIndex, int left, int right) {
        logInsn(161, left, right);
    }

    @Override
    public void IF_ICMPGE(String className, String methNane, int branchIndex, int left, int right) {
        logInsn(162, left, right);
    }

    @Override
    public void IF_ICMPGT(String className, String methNane, int branchIndex, int left, int right) {
        logInsn(163, left, right);
    }

    @Override
    public void IF_ICMPLE(String className, String methNane, int branchIndex, int left, int right) {
        logInsn(164, left, right);
    }

    @Override
    public void IF_ACMPEQ(String className, String methNane, int branchIndex, Object left, Object right) {
        logInsn(165, left, right);
    }

    @Override
    public void IF_ACMPNE(String className, String methNane, int branchIndex, Object left, Object right) {
        logInsn(166, left, right);
    }

    @Override
    public void GOTO() {
        logInsn(167);
    }

    @Override
    public void JSR() {
        logInsn(168);
    }

    @Override
    public void RET() {
        logInsn(169);
    } // visitVarInsn

    @Override
    public void TABLESWITCH(String className, String methName, int branchIndex, int target, int min, int max) {
        logInsn(170, target);
    } // visiTableSwitchInsn

    @Override
    public void LOOKUPSWITCH(String className, String methName,
                             int branchIndex, int target, int[] goals) {
        logInsn(171, target);
    } // visitLookupSwitch

    protected void exit() {
        instructionLogger.logln("-------------------- exit method");
    }

    @Override
    public void IRETURN() {
        logInsn(172);
        exit();
    }

    @Override
    public void LRETURN() {
        logInsn(173);
        exit();
    }

    @Override
    public void FRETURN() {
        logInsn(174);
        exit();
    }

    @Override
    public void DRETURN() {
        logInsn(175);
        exit();
    }

    @Override
    public void ARETURN() {
        logInsn(176);
        exit();
    }

    @Override
    public void RETURN() {
        logInsn(177);
        exit();
    }

    @Override
    public void GETSTATIC(String owner, String name, String desc) {
        logInsn(178, owner, name, desc);
    }

    @Override
    public void PUTSTATIC(String owner, String name, String desc) {
        logInsn(179, owner, name, desc);
    }

    @Override
    public void GETFIELD(Object receiver, String owner, String name, String desc) {
        logInsn(180, receiver, owner, name, desc);
    }

    @Override
    public void PUTFIELD(Object receiver, String owner, String name, String desc) {
        logInsn(181, receiver, owner, name, desc);
    }


    @Override
    public void INVOKESTATIC(String owner, String name, String desc) {
        logInsn(184, owner, name, desc);
    }


    @Override
    public void INVOKEVIRTUAL(Object receiver, String owner, String name, String desc) {
        logInsn(182, owner, name, desc, receiver);
    }


    @Override
    public void INVOKESPECIAL(String owner, String name, String desc) {
        logInsn(183, owner, name, desc);
    }

    @Override
    public void INVOKESPECIAL(Object receiver, String owner, String name, String desc) {
        logInsn(183, owner, name, desc, receiver);
    }

    @Override
    public void INVOKEINTERFACE(Object receiver, String owner, String name, String desc) {
        logInsn(185, owner, name, desc, receiver);
    }

    @Override
    public void INVOKEDYNAMIC(Object instance, String desc) {
        logInsn(186, desc, instance);
    }

    @Override
    public void NEW(String typeName) {
        logInsn(187, typeName);
    }

    @Override
    public void NEWARRAY(int length, Class<?> componentType, String className, String methodName) {
        logInsn(188, componentType, "[", length);
    }

    @Override
    public void ANEWARRAY(int length, String componentTypeName, String className, String typeName) {
        logInsn(189, typeName, "[", length);
    }

    @Override
    public void ARRAYLENGTH(Object reference) {
        logInsn(190);
    }

    @Override
    public void ATHROW(Throwable throwable) {
        logInsn(191);
    }

    @Override
    public void CHECKCAST(Object reference, String typeName) {
        logInsn(192, typeName);
    }

    @Override
    public void INSTANCEOF(Object reference, String typeName) {
        logInsn(193, typeName);
    }

    @Override
    public void MONITORENTER() {
        logInsn(194);
    }

    @Override
    public void MONITOREXIT() {
        logInsn(195);
    }

    @Override
    public void WIDE() {
        logInsn(196);
    } // NOT VISITED

    @Override
    public void MULTIANEWARRAY(String arrayTypeDesc, int nrDimensions, String className, String methodName) {
        logInsn(197, arrayTypeDesc);
    }

    @Override
    public void IFNULL(String className, String methNane, int branchIndex,
                       Object p) {
        logInsn(198, p);
    }

    @Override
    public void IFNONNULL(String className, String methNane, int branchIndex,
                          Object p) {
        logInsn(199, p);
    }

    @Override
    public void GOTO_W() {
        logInsn(200);
    }

    @Override
    public void JSR_W() {
        logInsn(201);
    }

    @Override
    public void cleanUp() {
        instructionLogger.cleanUp();
    }

    protected void logInsn(int opcode) {
        instructionLogger.logln(ConcolicConfig.BYTECODE_NAME[opcode]);
//        logLoopIterations();
    }

    protected void logInsn(int opcode, int p) {
        logInsn(opcode, Integer.valueOf(p));
    }

    protected void logInsn(int opcode, Object p) {
        instructionLogger.log(ConcolicConfig.BYTECODE_NAME[opcode], " ");
        if (p != null) {
            instructionLogger.logln(" ", p.toString());
        } else {
            instructionLogger.logln(" ", "null");
        }
//        instructionLogger.log(p.toString());
        instructionLogger.logln();
//        logLoopIterations();
    }

    protected void logInsn(int opcode, Object p1, Object p2) {
        instructionLogger.log(ConcolicConfig.BYTECODE_NAME[opcode], " ");
        if (p1 != null) {
            instructionLogger.logln(p1.toString(), " ");
        } else {
            instructionLogger.logln("null", " ");
        }

        if (p2 != null) {
            instructionLogger.logln(p2.toString());
        } else {
            instructionLogger.logln("null");
        }

//        instructionLogger.log(p1.toString(), " ");
//        instructionLogger.log(p2.toString());
        instructionLogger.logln();
//        logLoopIterations();
    }

    protected void logInsn(int opcode, Object p1, Object p2, Object p3) {
        instructionLogger.log(ConcolicConfig.BYTECODE_NAME[opcode], " ");
        if (p1 != null) {
            instructionLogger.logln(p1.toString(), " ");
        } else {
            instructionLogger.logln("null", " ");
        }
        if (p2 != null) {
            instructionLogger.logln(p2.toString(), " ");
        } else {
            instructionLogger.logln("null", " ");
        }
        if (p3 != null) {
            instructionLogger.logln(p3.toString());
        } else {
            instructionLogger.logln("null");
        }
        instructionLogger.logln();
//        logLoopIterations();
    }

    protected void logInsn(int opcode, Object p1, Object p2, Object p3, Object p4) {
        instructionLogger.log(ConcolicConfig.BYTECODE_NAME[opcode], " ");
        if (p1 != null) {
            instructionLogger.logln(p1.toString(), " ");
        } else {
            instructionLogger.logln("null", " ");
        }
        if (p2 != null) {
            instructionLogger.logln(p2.toString(), " ");
        } else {
            instructionLogger.logln("null", " ");
        }
        if (p3 != null) {
            instructionLogger.logln(p3.toString(), " ");
        } else {
            instructionLogger.logln("null", " ");
        }
        if (p4 != null) {
            instructionLogger.logln(p4.toString());
        } else {
            instructionLogger.logln("null");
        }
        instructionLogger.logln();
//        logLoopIterations();
    }

    protected void logInsn(int opcode, int p1, int p2) {
        logInsn(opcode, Integer.valueOf(p1), Integer.valueOf(p2));
    }

// TODO (ilebrero): we need to add loop counting first
//    protected void logLoopIterations() {
//        if (!conf.ENABLE_STATIC_ANALYSIS || !conf.LOG_LOOP_ITERATION_COUNTS)
//            return;
//
//        final TObjectIntHashMap<Loop> counts = topFrame().iterationCounts;
//        for (Loop loop : counts.keys(new Loop[counts.size()])) {
//            instructionLogger.logln("\t" + counts.get(loop) + " " + loop.getHead());
//            for (Stmt stmt : loop.getLoopStatements())
//                instructionLogger.logln("\t\t" + stmt);
//        }
//    }
}
