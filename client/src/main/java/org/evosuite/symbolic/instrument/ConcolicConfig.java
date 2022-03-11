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

import org.evosuite.dse.VM;

/*
    This class is taken and adapted from the DSC tool developed by Christoph Csallner.
    Link at :
    http://ranger.uta.edu/~csallner/dsc/index.html
 */

/**
 * The instrumenter parses all classes loaded by the JVM to run the
 * user program. The instrumenter inserts one method call before each
 * bytecode instruction it parses. This class contains the names of
 * the methods.
 *
 * @author csallner@uta.edu (Christoph Csallner)
 */
public final class ConcolicConfig {

    /**
     * Log each class and if we rewrite it (y/n)
     */
    public static final boolean LOG_CLASS_NAMES = false;

    /**
     * Class whose methods the instrumentation code will call
     */

    public static final String VM_FQ = VM.class.getName().replace('.', '/'); //$NON-NLS-1$

    public static final String VOID = "V"; //$NON-NLS-1$
    public static final String INT = "I"; //$NON-NLS-1$
    public static final String INT_ARR = "[I"; //$NON-NLS-1$
    public static final String BOOL = "Z"; //$NON-NLS-1$
    public static final String BYTE = "B"; //$NON-NLS-1$
    public static final String CHAR = "C"; //$NON-NLS-1$
    public static final String SHORT = "S"; //$NON-NLS-1$
    public static final String LONG = "J"; //$NON-NLS-1$
    public static final String FLOAT = "F"; //$NON-NLS-1$
    public static final String DOUBLE = "D"; //$NON-NLS-1$
    public static final String REF = "Ljava/lang/Object;"; //$NON-NLS-1$
    public static final String STR = "Ljava/lang/String;"; //$NON-NLS-1$
    public static final String CLASS = "Ljava/lang/Class;"; //$NON-NLS-1$


    public static final String V_V = "(" + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$

    public static final String I_V = "(" + INT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String II_V = "(" + INT + INT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String IIGG_V = "(" + INT + INT + STR + STR + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String III_V = "(" + INT + INT + INT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$

    public static final String IIR_V = "(" + INT + INT_ARR + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$

    public static final String Z_V = "(" + BOOL + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String ZII_V = "(" + BOOL + INT + INT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String IZ_V = "(" + INT + BOOL + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String IB_V = "(" + INT + BYTE + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String BII_V = "(" + BYTE + INT + INT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String IC_V = "(" + INT + CHAR + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String CII_V = "(" + CHAR + INT + INT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String IS_V = "(" + INT + SHORT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String SII_V = "(" + SHORT + INT + INT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$


    public static final String ZI_V = "(" + BOOL + INT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String BI_V = "(" + BYTE + INT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String CI_V = "(" + CHAR + INT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String SI_V = "(" + SHORT + INT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$

    public static final String J_V = "(" + LONG + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String JII_V = "(" + LONG + INT + INT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String IJ_V = "(" + INT + LONG + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$

    public static final String F_V = "(" + FLOAT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String IF_V = "(" + INT + FLOAT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String FI_V = "(" + INT + FLOAT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String FII_V = "(" + FLOAT + INT + INT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$

    public static final String D_V = "(" + DOUBLE + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String DII_V = "(" + DOUBLE + INT + INT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String ID_V = "(" + INT + DOUBLE + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$

    public static final String L_V = "(" + REF + ")" + VOID; //$NON-NLS-1$//$NON-NLS-2$
    public static final String LG_V = "(" + REF + STR + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String LI_V = "(" + REF + INT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String LIGG_V = "(" + REF + INT + STR + STR + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String LIL_V = "(" + REF + INT + REF + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String LILGG_V = "(" + REF + INT + REF + STR + STR + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String LII_V = "(" + REF + INT + INT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String IL_V = "(" + INT + REF + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String LL_V = "(" + REF + REF + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$

    public static final String G_V = "(" + STR + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String GI_V = "(" + STR + INT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String GIGG_V = "(" + STR + INT + STR + STR + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String IG_V = "(" + INT + STR + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String GGG_V = "(" + STR + STR + STR + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String GGGII_V = "(" + STR + STR + STR + INT + INT + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$


    public static final String DGGG_V = "(" + DOUBLE + STR + STR + STR + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String FGGG_V = "(" + FLOAT + STR + STR + STR + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String JGGG_V = "(" + LONG + STR + STR + STR + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String IGGG_V = "(" + INT + STR + STR + STR + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String ZGGG_V = "(" + BOOL + STR + STR + STR + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String LGGG_V = "(" + REF + STR + STR + STR + ")" + VOID; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String IGGI_V = "(" + INT + STR + STR + INT + ")" + VOID;
    public static final String IIGGI_V = "(" + INT + INT + STR + STR + INT + ")" + VOID;
    public static final String LLGGI_V = "(" + REF + REF + STR + STR + INT + ")" + VOID;
    public static final String LGGI_V = "(" + REF + STR + STR + INT + ")" + VOID;

    public static final String CLASS_V = "(" + CLASS + ")" + VOID;

    //TODO: Move this to a better place eventually
    public static final String[] BYTECODE_NAME = new String[]{
            "NOP", //$NON-NLS-1$
            "ACONST_NULL", //$NON-NLS-1$
            "ICONST_M1", //$NON-NLS-1$
            "ICONST_0", //$NON-NLS-1$
            "ICONST_1", //$NON-NLS-1$
            "ICONST_2", //$NON-NLS-1$
            "ICONST_3", //$NON-NLS-1$
            "ICONST_4", //$NON-NLS-1$
            "ICONST_5", //$NON-NLS-1$
            "LCONST_0", //$NON-NLS-1$
            "LCONST_1", //$NON-NLS-1$
            "FCONST_0", //$NON-NLS-1$
            "FCONST_1", //$NON-NLS-1$
            "FCONST_2", //$NON-NLS-1$
            "DCONST_0", //$NON-NLS-1$
            "DCONST_1", //$NON-NLS-1$
            "BIPUSH", //$NON-NLS-1$
            "SIPUSH", //$NON-NLS-1$
            "LDC", //$NON-NLS-1$
            "LDC_W", //$NON-NLS-1$
            "LDC2_W", //$NON-NLS-1$
            "ILOAD", //$NON-NLS-1$
            "LLOAD", //$NON-NLS-1$
            "FLOAD", //$NON-NLS-1$
            "DLOAD", //$NON-NLS-1$
            "ALOAD", //$NON-NLS-1$
            "ILOAD_0", //$NON-NLS-1$
            "ILOAD_1", //$NON-NLS-1$
            "ILOAD_2", //$NON-NLS-1$
            "ILOAD_3", //$NON-NLS-1$
            "LLOAD_0", //$NON-NLS-1$
            "LLOAD_1", //$NON-NLS-1$
            "LLOAD_2", //$NON-NLS-1$
            "LLOAD_3", //$NON-NLS-1$
            "FLOAD_0", //$NON-NLS-1$
            "FLOAD_1", //$NON-NLS-1$
            "FLOAD_2", //$NON-NLS-1$
            "FLOAD_3", //$NON-NLS-1$
            "DLOAD_0", //$NON-NLS-1$
            "DLOAD_1", //$NON-NLS-1$
            "DLOAD_2", //$NON-NLS-1$
            "DLOAD_3", //$NON-NLS-1$
            "ALOAD_0", //$NON-NLS-1$
            "ALOAD_1", //$NON-NLS-1$
            "ALOAD_2", //$NON-NLS-1$
            "ALOAD_3", //$NON-NLS-1$
            "IALOAD", //$NON-NLS-1$
            "LALOAD", //$NON-NLS-1$
            "FALOAD", //$NON-NLS-1$
            "DALOAD", //$NON-NLS-1$
            "AALOAD", //$NON-NLS-1$
            "BALOAD", //$NON-NLS-1$
            "CALOAD", //$NON-NLS-1$
            "SALOAD", //$NON-NLS-1$
            "ISTORE", //$NON-NLS-1$
            "LSTORE", //$NON-NLS-1$
            "FSTORE", //$NON-NLS-1$
            "DSTORE", //$NON-NLS-1$
            "ASTORE", //$NON-NLS-1$
            "ISTORE_0", //$NON-NLS-1$
            "ISTORE_1", //$NON-NLS-1$
            "ISTORE_2", //$NON-NLS-1$
            "ISTORE_3", //$NON-NLS-1$
            "LSTORE_0", //$NON-NLS-1$
            "LSTORE_1", //$NON-NLS-1$
            "LSTORE_2", //$NON-NLS-1$
            "LSTORE_3", //$NON-NLS-1$
            "FSTORE_0", //$NON-NLS-1$
            "FSTORE_1", //$NON-NLS-1$
            "FSTORE_2", //$NON-NLS-1$
            "FSTORE_3", //$NON-NLS-1$
            "DSTORE_0", //$NON-NLS-1$
            "DSTORE_1", //$NON-NLS-1$
            "DSTORE_2", //$NON-NLS-1$
            "DSTORE_3", //$NON-NLS-1$
            "ASTORE_0", //$NON-NLS-1$
            "ASTORE_1", //$NON-NLS-1$
            "ASTORE_2", //$NON-NLS-1$
            "ASTORE_3", //$NON-NLS-1$
            "IASTORE", //$NON-NLS-1$
            "LASTORE", //$NON-NLS-1$
            "FASTORE", //$NON-NLS-1$
            "DASTORE", //$NON-NLS-1$
            "AASTORE", //$NON-NLS-1$
            "BASTORE", //$NON-NLS-1$
            "CASTORE", //$NON-NLS-1$
            "SASTORE", //$NON-NLS-1$
            "POP", //$NON-NLS-1$
            "POP2", //$NON-NLS-1$
            "DUP", //$NON-NLS-1$
            "DUP_X1", //$NON-NLS-1$
            "DUP_X2", //$NON-NLS-1$
            "DUP2", //$NON-NLS-1$
            "DUP2_X1", //$NON-NLS-1$
            "DUP2_X2", //$NON-NLS-1$
            "SWAP", //$NON-NLS-1$
            "IADD", //$NON-NLS-1$
            "LADD", //$NON-NLS-1$
            "FADD", //$NON-NLS-1$
            "DADD", //$NON-NLS-1$
            "ISUB", //$NON-NLS-1$
            "LSUB", //$NON-NLS-1$
            "FSUB", //$NON-NLS-1$
            "DSUB", //$NON-NLS-1$
            "IMUL", //$NON-NLS-1$
            "LMUL", //$NON-NLS-1$
            "FMUL", //$NON-NLS-1$
            "DMUL", //$NON-NLS-1$
            "IDIV", //$NON-NLS-1$
            "LDIV", //$NON-NLS-1$
            "FDIV", //$NON-NLS-1$
            "DDIV", //$NON-NLS-1$
            "IREM", //$NON-NLS-1$
            "LREM", //$NON-NLS-1$
            "FREM", //$NON-NLS-1$
            "DREM", //$NON-NLS-1$
            "INEG", //$NON-NLS-1$
            "LNEG", //$NON-NLS-1$
            "FNEG", //$NON-NLS-1$
            "DNEG", //$NON-NLS-1$
            "ISHL", //$NON-NLS-1$
            "LSHL", //$NON-NLS-1$
            "ISHR", //$NON-NLS-1$
            "LSHR", //$NON-NLS-1$
            "IUSHR", //$NON-NLS-1$
            "LUSHR", //$NON-NLS-1$
            "IAND", //$NON-NLS-1$
            "LAND", //$NON-NLS-1$
            "IOR", //$NON-NLS-1$
            "LOR", //$NON-NLS-1$
            "IXOR", //$NON-NLS-1$
            "LXOR", //$NON-NLS-1$
            "IINC", //$NON-NLS-1$
            "I2L", //$NON-NLS-1$
            "I2F", //$NON-NLS-1$
            "I2D", //$NON-NLS-1$
            "L2I", //$NON-NLS-1$
            "L2F", //$NON-NLS-1$
            "L2D", //$NON-NLS-1$
            "F2I", //$NON-NLS-1$
            "F2L", //$NON-NLS-1$
            "F2D", //$NON-NLS-1$
            "D2I", //$NON-NLS-1$
            "D2L", //$NON-NLS-1$
            "D2F", //$NON-NLS-1$
            "I2B", //$NON-NLS-1$
            "I2C", //$NON-NLS-1$
            "I2S", //$NON-NLS-1$
            "LCMP", //$NON-NLS-1$
            "FCMPL", //$NON-NLS-1$
            "FCMPG", //$NON-NLS-1$
            "DCMPL", //$NON-NLS-1$
            "DCMPG", //$NON-NLS-1$
            "IFEQ", //$NON-NLS-1$
            "IFNE", //$NON-NLS-1$
            "IFLT", //$NON-NLS-1$
            "IFGE", //$NON-NLS-1$
            "IFGT", //$NON-NLS-1$
            "IFLE", //$NON-NLS-1$
            "IF_ICMPEQ", //$NON-NLS-1$
            "IF_ICMPNE", //$NON-NLS-1$
            "IF_ICMPLT", //$NON-NLS-1$
            "IF_ICMPGE", //$NON-NLS-1$
            "IF_ICMPGT", //$NON-NLS-1$
            "IF_ICMPLE", //$NON-NLS-1$
            "IF_ACMPEQ", //$NON-NLS-1$
            "IF_ACMPNE", //$NON-NLS-1$
            "GOTO", //$NON-NLS-1$
            "JSR", //$NON-NLS-1$
            "RET", //$NON-NLS-1$
            "TABLESWITCH", //$NON-NLS-1$
            "LOOKUPSWITCH", //$NON-NLS-1$
            "IRETURN", //$NON-NLS-1$
            "LRETURN", //$NON-NLS-1$
            "FRETURN", //$NON-NLS-1$
            "DRETURN", //$NON-NLS-1$
            "ARETURN", //$NON-NLS-1$
            "RETURN", //$NON-NLS-1$
            "GETSTATIC", //$NON-NLS-1$
            "PUTSTATIC", //$NON-NLS-1$
            "GETFIELD", //$NON-NLS-1$
            "PUTFIELD", //$NON-NLS-1$
            "INVOKEVIRTUAL", //$NON-NLS-1$
            "INVOKESPECIAL", //$NON-NLS-1$
            "INVOKESTATIC", //$NON-NLS-1$
            "INVOKEINTERFACE", //$NON-NLS-1$
            "INVOKEDYNAMIC", //$NON-NLS-1$
            "NEW", //$NON-NLS-1$
            "NEWARRAY", //$NON-NLS-1$
            "ANEWARRAY", //$NON-NLS-1$
            "ARRAYLENGTH", //$NON-NLS-1$
            "ATHROW", //$NON-NLS-1$
            "CHECKCAST", //$NON-NLS-1$
            "INSTANCEOF", //$NON-NLS-1$
            "MONITORENTER", //$NON-NLS-1$
            "MONITOREXIT", //$NON-NLS-1$
            "WIDE", //$NON-NLS-1$
            "MULTIANEWARRAY", //$NON-NLS-1$
            "IFNULL", //$NON-NLS-1$
            "IFNONNULL", //$NON-NLS-1$
            "GOTO_W", //$NON-NLS-1$
            "JSR_W"}; //$NON-NLS-1$
}