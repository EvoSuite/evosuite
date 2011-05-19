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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

import de.unisb.cs.st.evosuite.mutation.Mutateable;

/**
 * Abstract base class for both forms of CFGs inside EvoSuite
 * 
 * One implementation of this is cfg.RawControlFlowGraph, 
 * which is also known as the complete CFG
 * The other implementation of this is cfg.ActualControlFlowGraph
 * which is also known as the minimal CFG
 * Look at the respective classes for more detailed information
 * 
 * The CFGs can be accessed via the CFGPool which holds for each
 * CUT and each of their methods a complete and a minimal CFG
 * 
 * CFGs are created by the CFGGenerator during the analysis of
 * the CFG's byteCode performed by the BytecodeAnalyzer 
 * 
 * @author Gordon Fraser, Andre Mis
 */
public abstract class ControlFlowGraph<V extends Mutateable> extends EvoSuiteGraph<V> {

	private static Logger logger = Logger.getLogger(ControlFlowGraph.class);

	protected String className;
	protected String methodName;
	
	private int diameter = -1;

	/**
	 * Creates a fresh and empty CFG for the given class and method
	 */
	protected ControlFlowGraph(String className, String methodName) {
		super();
		
		this.className = className;
		this.methodName = methodName;
	}

	/**
	 * Can be used to retrieve a Branch contained in this CFG identified by it's branchId
	 * 
	 *  If no such branch exists in this CFG, null is returned
	 */
	public abstract BytecodeInstruction getBranch(int branchId);

	/**
	 * Can be used to retrieve an instruction contained in this CFG identified by it's instructionId
	 * 
	 *  If no such instruction exists in this CFG, null is returned
	 */
	public abstract BytecodeInstruction getInstruction(int instructionId);
	
	/**
	 * Determines, whether a given instruction is contained in this CFG 
	 */
	public abstract boolean containsInstruction(BytecodeInstruction instruction);

	
	
	public void finalize() {
		computeDiameter();
		calculateMutationDistances();
		// TODO: call this! 
		// 	and sanity check with a flag whenever a call 
		//  to this method is assumed to have been made
	}
	
	/**
	 * For each node within this CFG that is known to contain a mutation
	 * the distance from each node of this CFG to the that mutated node 
	 * is computed using getDistance() and those result are then stored
	 * in that mutated node  
	 */
	private void calculateMutationDistances() {
		logger.trace("Calculating mutation distances");
		for (V m : vertexSet())
			if (m.isMutation())
				for (Long id : m.getMutationIds())
					for (V v : vertexSet()) {
						int distance = getDistance(v,m);
						if (distance >= 0)
							v.setDistance(id, distance);
						else
							v.setDistance(id, getDiameter());
					}
	}
	
	/**
	 * Returns the node of this CFG that contains the mutation identified
	 * by the given mutationId
	 * 
	 *   If no such node exists, null is returned
	 */
	public V getMutation(long mutationId) {
		for (V v : vertexSet())
			if (v.hasMutation(mutationId))
				return v;

		return null;
	}

	/**
	 * Returns a list of all mutationIds contained within this CFG
	 * 
	 *   TODO why isn't the return-type a set? where does the order come from?
	 */
	public List<Long> getMutations() {
		List<Long> ids = new ArrayList<Long>();
		for (V v : vertexSet()) {
			if (v.isMutation())
				ids.addAll(v.getMutationIds());
		}
		return ids;
	}

	/**
	 * Checks whether the mutation identified by the given mutationId
	 * is contained in this CFG 
	 */
	public boolean containsMutation(long id) {
		for (V v : vertexSet()) {
			if (v.isMutation() && v.hasMutation(id))
				return true;
		}
		return false;
	}

	public int getDiameter() {
		if(diameter == -1) // TODO: don't throw exception but rather just call computeDiameters() ?
			throw new IllegalStateException("diameter not computed yet. call computeDiameter() first");

		return diameter;
	}

	protected void computeDiameter() {
		FloydWarshall<V, DefaultEdge> f = new FloydWarshall<V, DefaultEdge>(
		        graph);
		diameter = (int) f.getDiameter();
	}

	protected V determineEntryPoint() {
		V r = null;

		for (V instruction : vertexSet())
			if (inDegreeOf(instruction) == 0) {
				if (r != null)
					throw new IllegalStateException(
							"expect CFG of a method to contain exactly one instruction with no parent");
				r = instruction;
			}
		if (r == null)
			throw new IllegalStateException(
					"expect CFG of a method to contain exactly one instruction with no parent");

		return r;
	}

	protected Set<V> determineExitPoints() {
		Set<V> r = new HashSet<V>();

		for (V instruction : vertexSet())
			if (outDegreeOf(instruction) == 0) {
				r.add(instruction);
			}
		if (r.isEmpty())
			throw new IllegalStateException(
					"expect CFG of a method to contain at least one instruction with no child");

		return r;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}
}
