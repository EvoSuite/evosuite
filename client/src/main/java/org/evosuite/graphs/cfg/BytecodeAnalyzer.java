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
package org.evosuite.graphs.cfg;

import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;

/**
 * This class analyzes the byteCode from a method in the CUT and generates it's
 * CFG using a cfg.CFGGenerator
 * <p>
 * This is done using the ASM library, extending from it's asm.Analyzer and
 * redirecting the calls to newControlFlowEdge() to an instance of
 * cfg.CFGGenerator which in turn builds up a graph representation of the CFG,
 * which later is used to build a "smaller" CFG containing not
 * BytecodeInstructions but BasicBlocks of BytecodeInstructions which are always
 * executed successively
 *
 * @author Gordon Fraser, Andre Mis
 */
public class BytecodeAnalyzer extends Analyzer {

    CFGGenerator cfgGenerator;

    /**
     * <p>
     * Constructor for BytecodeAnalyzer.
     * </p>
     */
    public BytecodeAnalyzer() {
        super(new SourceInterpreter());
    }

    // main interface

    /**
     * Analyzes the method corresponding to the given Strings and initializes
     * the CFGGenerator and thus the BytecodeInstructionPool for the given
     * method in the given class.
     *
     * @param owner  a {@link java.lang.String} object.
     * @param method a {@link java.lang.String} object.
     * @param node   a {@link org.objectweb.asm.tree.MethodNode} object.
     * @return a {@link org.evosuite.graphs.cfg.CFGFrame} object.
     * @throws org.objectweb.asm.tree.analysis.AnalyzerException if any.
     */
    public CFGFrame analyze(ClassLoader classLoader, String owner, String method,
                            MethodNode node) throws AnalyzerException {

        cfgGenerator = new CFGGenerator(classLoader, owner, method, node);
        this.analyze(owner, node);

        Frame[] frames = getFrames();

        if (frames.length == 0)
            return null;

        return (CFGFrame) getFrames()[0];
    }

    /**
     * After running analyze() this method yields the filled CFGGenerator for
     * further processing of the gathered information from analyze() within the
     * ByteCode representation of EvoSuite
     *
     * @return a {@link org.evosuite.graphs.cfg.CFGGenerator} object.
     */
    public CFGGenerator retrieveCFGGenerator() {
        if (cfgGenerator == null)
            throw new IllegalStateException(
                    "you have to call analyze() first before retrieving the CFGGenerator");
        return cfgGenerator;
    }

    // inherited from asm.Analyzer

    /**
     * {@inheritDoc}
     * <p>
     * Called for each non-exceptional cfg edge
     */
    @Override
    protected void newControlFlowEdge(int src, int dst) {

        cfgGenerator.registerControlFlowEdge(src, dst, getFrames(), false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * We also need to keep track of exceptional edges - they are also branches
     */
    @Override
    protected boolean newControlFlowExceptionEdge(int src, int dst) {
        // TODO: Make use of information that this is an exception edge?
        cfgGenerator.registerControlFlowEdge(src, dst, getFrames(), true);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Frame newFrame(int nLocals, int nStack) {
        return new CFGFrame(nLocals, nStack);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Frame newFrame(Frame src) {
        return new CFGFrame(src);
    }
}
