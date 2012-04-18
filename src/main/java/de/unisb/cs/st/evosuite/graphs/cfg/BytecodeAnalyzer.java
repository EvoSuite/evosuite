/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.graphs.cfg;

import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;

/**
 * This class analyzes the byteCode from a method in the CUT and generates it's
 * CFG using a cfg.CFGGenerator
 * 
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

	public BytecodeAnalyzer() {
		super(new SourceInterpreter());
	}

	// main interface

	/**
	 * Analyzes the method corresponding to the given Strings and initializes
	 * the CFGGenerator and thus the BytecodeInstructionPool for the given
	 * method in the given class.
	 */
	public CFGFrame analyze(String owner, String method, MethodNode node)
	        throws AnalyzerException {

		cfgGenerator = new CFGGenerator(owner, method, node);
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
	 */
	public CFGGenerator retrieveCFGGenerator() {
		if (cfgGenerator == null)
			throw new IllegalStateException(
			        "you have to call analyze() first before retrieving the CFGGenerator");
		return cfgGenerator;
	}

	// inherited from asm.Analyzer

	/**
	 * Called for each non-exceptional cfg edge
	 */
	@Override
	protected void newControlFlowEdge(int src, int dst) {

		cfgGenerator.registerControlFlowEdge(src, dst, getFrames(), false);
	}

	/**
	 * We also need to keep track of exceptional edges - they are also branches
	 */
	@Override
	protected boolean newControlFlowExceptionEdge(int src, int dst) {
		// TODO: Make use of information that this is an exception edge?
		cfgGenerator.registerControlFlowEdge(src, dst, getFrames(), true);

		return true;
	}

	@Override
	protected Frame newFrame(int nLocals, int nStack) {
		return new CFGFrame(nLocals, nStack);
	}

	@Override
	protected Frame newFrame(Frame src) {
		return new CFGFrame(src);
	}
}
