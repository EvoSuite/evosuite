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
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import de.unisb.cs.st.evosuite.mutation.Mutateable;


public abstract class ControlFlowGraph<V extends Mutateable,E extends DefaultEdge> extends EvoSuiteGraph<V, E> {

	private static Logger logger = Logger.getLogger(ControlFlowGraph.class);

	protected String className;
	protected String methodName;
	
	private int diameter = -1;

	public ControlFlowGraph(DirectedGraph<V, E> cfg,
	        String className, String methodName) { 
		super(cfg);
		
		this.className = className;
		this.methodName = methodName;
	}
	
	public ControlFlowGraph(Class<E> cl, String className, String methodName) {
		super(cl);
		
		this.className = className;
		this.methodName = methodName;
	}
	
	public abstract BytecodeInstruction getBranch(int branchId); // TODO rename to getBranch .. maybe even return a Branch
	
	public abstract BytecodeInstruction getInstruction(int instructionId); // TODO rename to getInstruction
	
	public abstract boolean containsInstruction(BytecodeInstruction instruction);
	
	public void finalize() {
		computeDiameter();
		calculateMutationDistances();
		// TODO: call this! 
		// 	and sanity check with a flag whenever a call 
		//  to this method is assumed to have been made
	}
	
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
	
	public V getMutation(long id) {
		for (V v : vertexSet()) {
			if (v.isMutation()) {
				if (v.hasMutation(id)) {
					return v;
				}
			}
		}
		return null;
	}

	public List<Long> getMutations() {
		List<Long> ids = new ArrayList<Long>();
		for (V v : vertexSet()) {
			if (v.isMutation())
				ids.addAll(v.getMutationIds());
		}
		return ids;
	}

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

	public void computeDiameter() {
		FloydWarshall<V, E> f = new FloydWarshall<V, E>(
		        graph);
		diameter = (int) f.getDiameter();
	}

	public V determineEntryPoint() {
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

	public Set<V> determineExitPoints() {
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
