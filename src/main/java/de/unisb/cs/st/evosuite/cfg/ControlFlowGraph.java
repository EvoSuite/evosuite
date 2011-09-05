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

package de.unisb.cs.st.evosuite.cfg;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for both forms of CFGs inside EvoSuite
 * 
 * One implementation of this is cfg.RawControlFlowGraph, which is also known as
 * the complete CFG The other implementation of this is
 * cfg.ActualControlFlowGraph which is also known as the minimal CFG Look at the
 * respective classes for more detailed information
 * 
 * The CFGs can be accessed via the CFGPool which holds for each CUT and each of
 * their methods a complete and a minimal CFG
 * 
 * CFGs are created by the CFGGenerator during the analysis of the CUTs'
 * byteCode performed by the BytecodeAnalyzer
 * 
 * @author Gordon Fraser, Andre Mis
 */
public abstract class ControlFlowGraph<V> extends
		EvoSuiteGraph<V, ControlFlowEdge> {

	private static Logger logger = LoggerFactory
			.getLogger(ControlFlowGraph.class);

	protected String className;
	protected String methodName;

	private int diameter = -1;

	/**
	 * Creates a fresh and empty CFG for the given class and method
	 */
	protected ControlFlowGraph(String className, String methodName) {
		super(ControlFlowEdge.class);

		if (className == null || methodName == null)
			throw new IllegalArgumentException("null given");

		this.className = className;
		this.methodName = methodName;
	}

	/**
	 * Creates a CFG determined by the given jGraph for the given class and
	 * method
	 */
	protected ControlFlowGraph(String className, String methodName,
			DefaultDirectedGraph<V, ControlFlowEdge> jGraph) {
		super(jGraph, ControlFlowEdge.class);

		if (className == null || methodName == null)
			throw new IllegalArgumentException("null given");

		this.className = className;
		this.methodName = methodName;
	}

	public boolean leadsToNode(ControlFlowEdge e, V b) {

		Set<V> handled = new HashSet<V>();

		Queue<V> queue = new LinkedList<V>();
		queue.add(getEdgeTarget(e));
		while (!queue.isEmpty()) {
			V current = queue.poll();
			if (handled.contains(current))
				continue;
			handled.add(current);

			for (V next : getChildren(current))
				if (next.equals(b))
					return true;
				else
					queue.add(next);
		}

		return false;
	}

	// /**
	// * Can be used to retrieve a Branch contained in this CFG identified by
	// it's
	// * branchId
	// *
	// * If no such branch exists in this CFG, null is returned
	// */
	// public abstract BytecodeInstruction getBranch(int branchId);

	/**
	 * Can be used to retrieve an instruction contained in this CFG identified
	 * by it's instructionId
	 * 
	 * If no such instruction exists in this CFG, null is returned
	 */
	public abstract BytecodeInstruction getInstruction(int instructionId);

	/**
	 * Determines, whether a given instruction is contained in this CFG
	 */
	public abstract boolean containsInstruction(BytecodeInstruction instruction);

	/**
	 * Computes the diameter of this CFG and the mutation distances
	 * 
	 * Since both takes some time this is not automatically done on each CFG
	 * 
	 * CFGPool will automatically call this immediately after the instantiation
	 * of an ActualControlFlowGraph, but not after the creation of a
	 * RawControlFlowGraph
	 */
	public void finalise() {
		computeDiameter();
		// TODO: call this!
		// and sanity check with a flag whenever a call
		// to this method is assumed to have been made
	}

	/**
	 * Returns the Diameter of this CFG
	 * 
	 * If the diameter of this graph was not computed previously it is computed
	 * first
	 */
	public int getDiameter() {
		if (diameter == -1) {
			logger
					.debug("diameter not computed yet. calling computeDiameter() first!");
			computeDiameter();
		}

		return diameter;
	}

	protected void computeDiameter() {
		// The diameter is just an upper bound for the approach level
		// Let's try to use something that's easier to compute than
		// FLoydWarshall
		diameter = this.edgeCount();
		/*
		 * FloydWarshall<V, ControlFlowEdge> f = new FloydWarshall<V,
		 * ControlFlowEdge>(graph); diameter = (int) f.getDiameter();
		 */
	}

	protected V determineEntryPoint() {
		Set<V> candidates = determineEntryPoints();

		if (candidates.size() > 1)
			throw new IllegalStateException(
					"expect CFG of a method to contain at most one instruction with no parent in "
							+ methodName);

		for (V instruction : candidates)
			return instruction;

		// there was a back loop to the first instruction within this CFG, so no
		// candidate
		// TODO for now return null and handle in super class
		// RawControlFlowGraph separately by overriding this method

		// can also happen in empty methods
		return null;
	}

	/**
	 * In some cases there can be isolated nodes within a CFG. For example in an
	 * completely empty try-catch-finally. Since these nodes are not reachable
	 * but cause trouble when determining the entry point of a CFG they get
	 * removed.
	 * 
	 * @return
	 */
	public int removeIsolatedNodes() {
		Set<V> candidates = determineEntryPoints();

		int removed = 0;
		if (candidates.size() > 1) {

			for (V instruction : candidates) {
				if (outDegreeOf(instruction) == 0) {
					if (graph.removeVertex(instruction))
						removed++;
				}
			}

		}
		return removed;
	}

	@Override
	protected Set<V> determineExitPoints() {
		Set<V> r = new HashSet<V>();

		for (V instruction : vertexSet())
			if (outDegreeOf(instruction) == 0) {
				r.add(instruction);
			}

		return r;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	@Override
	public String getName() {
		return getCFGType() + " " + methodName;
	}

	@Override
	protected String dotSubFolder() {
		return toFileString(className) + "/" + getCFGType() + "/";
	}

	public abstract String getCFGType();
}
