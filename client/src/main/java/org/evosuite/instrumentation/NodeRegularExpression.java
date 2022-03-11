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

package org.evosuite.instrumentation;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

/**
 * <p>NodeRegularExpression class.</p>
 *
 * @author Gordon Fraser
 */
public class NodeRegularExpression {

    /**
     * Constant <code>ICONST_0=3</code>
     */
    protected static final int ICONST_0 = 3;

    /**
     * Constant <code>ICONST_1=4</code>
     */
    protected static final int ICONST_1 = 4;

    /**
     * Constant <code>GOTO=167</code>
     */
    protected static final int GOTO = 167;

    /**
     * Constant <code>ACONST_NULL=1</code>
     */
    protected static final int ACONST_NULL = 1;

    /**
     * Constant <code>ACONST_M1=2</code>
     */
    protected static final int ACONST_M1 = 2;

    /**
     * Constant <code>ILOAD=21</code>
     */
    protected static final int ILOAD = 21;

    /**
     * Constant <code>ISTORE=54</code>
     */
    protected static final int ISTORE = 54;

    /**
     * Constant <code>IADD=96</code>
     */
    protected static final int IADD = 96;

    /**
     * Constant <code>ISUB=100</code>
     */
    protected static final int ISUB = 100;

    /**
     * Constant <code>IMUL=104</code>
     */
    protected static final int IMUL = 104;

    /**
     * Constant <code>IDIV=108</code>
     */
    protected static final int IDIV = 108;

    /**
     * Constant <code>IREM=112</code>
     */
    protected static final int IREM = 112;

    /**
     * Constant <code>INEG=116</code>
     */
    protected static final int INEG = 116;

    /**
     * Constant <code>IFEQ=153</code>
     */
    protected static final int IFEQ = 153;

    /**
     * Constant <code>IFNE=154</code>
     */
    protected static final int IFNE = 154;

    /**
     * Constant <code>IFLT=155</code>
     */
    protected static final int IFLT = 155;

    /**
     * Constant <code>IFGE=156</code>
     */
    protected static final int IFGE = 156;

    /**
     * Constant <code>IFGT=157</code>
     */
    protected static final int IFGT = 157;

    /**
     * Constant <code>IFLE=158</code>
     */
    protected static final int IFLE = 158;

    /**
     * Constant <code>IF_ICMPEQ=159</code>
     */
    protected static final int IF_ICMPEQ = 159;

    /**
     * Constant <code>IF_ICMPNE=160</code>
     */
    protected static final int IF_ICMPNE = 160;

    /**
     * Constant <code>IF_ICMPLT=161</code>
     */
    protected static final int IF_ICMPLT = 161;

    /**
     * Constant <code>IF_ICMPGE=162</code>
     */
    protected static final int IF_ICMPGE = 162;

    /**
     * Constant <code>IF_ICMPGT=163</code>
     */
    protected static final int IF_ICMPGT = 163;

    /**
     * Constant <code>IF_ICMPLE=164</code>
     */
    protected static final int IF_ICMPLE = 164;

    /**
     * Constant <code>IF_ACMPEQ=165</code>
     */
    protected static final int IF_ACMPEQ = 165;

    /**
     * Constant <code>IF_ACMPNE=166</code>
     */
    protected static final int IF_ACMPNE = 166;

    /**
     * Constant <code>IRETURN=172</code>
     */
    protected static final int IRETURN = 172;

    /**
     * Constant <code>GETSTATIC=178</code>
     */
    protected static final int GETSTATIC = 178;

    /**
     * Constant <code>PUTSTATIC=179</code>
     */
    protected static final int PUTSTATIC = 179;

    /**
     * Constant <code>GETFIELD=180</code>
     */
    protected static final int GETFIELD = 180;

    /**
     * Constant <code>PUTFIELD=181</code>
     */
    protected static final int PUTFIELD = 181;

    /**
     * Constant <code>INVOKEVIRTUAL=182</code>
     */
    protected static final int INVOKEVIRTUAL = 182;

    /**
     * Constant <code>INVOKESPECIAL=183</code>
     */
    protected static final int INVOKESPECIAL = 183;

    /**
     * Constant <code>INVOKESTATIC=184</code>
     */
    protected static final int INVOKESTATIC = 184;

    /**
     * Constant <code>INVOKEINTERFACE=185</code>
     */
    protected static final int INVOKEINTERFACE = 185;

    /**
     * Constant <code>NEWARRAY=188</code>
     */
    protected static final int NEWARRAY = 188;

    /**
     * Constant <code>INSTANCEOF=193</code>
     */
    protected static final int INSTANCEOF = 193;

    /**
     * Constant <code>IFNULL=198</code>
     */
    protected static final int IFNULL = 198;

    /**
     * Constant <code>IFNONNULL=199</code>
     */
    protected static final int IFNONNULL = 199;

    /**
     * Constant <code>ALOAD=new int[] { 25, 42, 43, 44, 45 }</code>
     */
    protected static final int[] ALOAD = new int[]{25, 42, 43, 44, 45};

    /**
     * Constant <code>IF=new int[] { 153, 154, 155, 156, 157, 158, 159, 160,
     * 161, 162, 163, 164 }</code>
     */
    protected static final int[] IF = new int[]{153, 154, 155, 156, 157, 158, 159, 160,
            161, 162, 163, 164};

    /**
     * Constant <code>BOOL=new int[] { ICONST_0, ICONST_1 }</code>
     */
    protected static final int[] BOOL = new int[]{ICONST_0, ICONST_1};

    //public static NodeRegularExpression IFELSE = new NodeRegularExpression(new int[] {
    //        160, 4, 167, 3 });

    /**
     * Constant <code>IFELSE</code>
     */
    public static NodeRegularExpression IFELSE = new NodeRegularExpression(new int[][]{
            IF, BOOL, {GOTO}, BOOL});

    /**
     * Constant <code>NESTED_STOREFLAG</code>
     */
    public static NodeRegularExpression NESTED_STOREFLAG = new NodeRegularExpression(
            new int[][]{IF, IF, BOOL, {ISTORE}});

    /**
     * Constant <code>STOREFLAG</code>
     */
    public static NodeRegularExpression STOREFLAG = new NodeRegularExpression(
            new int[][]{IF, BOOL, {ISTORE}});

    /**
     * Constant <code>STOREFLAG2</code>
     */
    public static NodeRegularExpression STOREFLAG2 = new NodeRegularExpression(
            new int[][]{IF, BOOL, {PUTSTATIC}});

    /**
     * Constant <code>STOREFLAG3</code>
     */
    public static NodeRegularExpression STOREFLAG3 = new NodeRegularExpression(
            new int[][]{IF, ALOAD, BOOL, {PUTFIELD}});

    /**
     * Constant <code>STOREFLAG4</code>
     */
    public static NodeRegularExpression STOREFLAG4 = new NodeRegularExpression(
            new int[][]{IF, BOOL, {IRETURN}});

    public final int[][] pattern;

    /**
     * <p>Constructor for NodeRegularExpression.</p>
     *
     * @param opcodes an array of int.
     */
    public NodeRegularExpression(int[] opcodes) {
        this.pattern = new int[opcodes.length][];
        for (int i = 0; i < opcodes.length; i++) {
            this.pattern[i] = new int[]{opcodes[i]};
        }
    }

    /**
     * <p>Constructor for NodeRegularExpression.</p>
     *
     * @param opcodes an array of int.
     */
    public NodeRegularExpression(int[][] opcodes) {
        this.pattern = opcodes;
    }

    /**
     * <p>matches</p>
     *
     * @param instructions a {@link org.objectweb.asm.tree.InsnList} object.
     * @return a boolean.
     */
    public boolean matches(InsnList instructions) {
        int match = 0;

        AbstractInsnNode node = instructions.getFirst();
        while (node != instructions.getLast()) {
            if (node.getType() == AbstractInsnNode.FRAME
                    || node.getType() == AbstractInsnNode.LABEL
                    || node.getType() == AbstractInsnNode.LINE) {
                node = node.getNext();
                continue;
            } else {
                boolean found = false;
                for (int opcode : pattern[match]) {
                    if (node.getOpcode() == opcode) {
                        match++;
                        found = true;
                        break;
                    }
                }
                if (!found)
                    match = 0;
            }
            if (match == pattern.length)
                return true;

            node = node.getNext();
        }

        return false;
    }

    /**
     * <p>getNextMatch</p>
     *
     * @param start        a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     * @param instructions a {@link org.objectweb.asm.tree.InsnList} object.
     * @return a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     */
    public AbstractInsnNode getNextMatch(AbstractInsnNode start, InsnList instructions) {
        int match = 0;

        AbstractInsnNode node = start;
        AbstractInsnNode startNode = start;
        while (node != instructions.getLast()) {
            if (node.getType() == AbstractInsnNode.FRAME
                    || node.getType() == AbstractInsnNode.LABEL
                    || node.getType() == AbstractInsnNode.LINE) {
                node = node.getNext();
                continue;
            } else {
                boolean found = false;
                for (int opcode : pattern[match]) {
                    if (node.getOpcode() == opcode) {
                        if (match == 0)
                            startNode = node;
                        match++;
                        found = true;
                        break;
                    }
                }
                if (!found)
                    match = 0;
            }
            if (match == pattern.length) {
                return startNode;
            }

            node = node.getNext();
        }

        return null;

    }

}
