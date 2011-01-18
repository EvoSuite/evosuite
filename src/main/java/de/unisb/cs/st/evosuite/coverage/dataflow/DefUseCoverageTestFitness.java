/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */


package de.unisb.cs.st.evosuite.coverage.dataflow;

import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.cfg.ControlFlowGraph;
import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageGoal;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageTestFitness;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.evosuite.testcase.Statement;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * Evaluate fitness of a single test case with respect to one def-use pair
 * 
 * @author 
 *
 */
public class DefUseCoverageTestFitness extends TestFitnessFunction {


	private CFGVertex def;
	private CFGVertex use;
	private BranchCoverageTestFitness defTestFitness;
	private BranchCoverageTestFitness useTestFitness;
	
	public DefUseCoverageTestFitness(CFGVertex def, CFGVertex use) {

		if(!def.getDUVariableName().equals(use.getDUVariableName()))
			throw new IllegalArgumentException("expect def and use to be for the same variable");
		
		this.def = def;
		this.use = use;
		this.defTestFitness = getTestFitness(def);
		this.useTestFitness = getTestFitness(use);
		
	}

	/**
	 * @param def2
	 * @return
	 */
	private BranchCoverageTestFitness getTestFitness(CFGVertex v) {

		BranchCoverageTestFitness r;
		
		if(v.branchID==-1) {
			r = new BranchCoverageTestFitness(new BranchCoverageGoal(v.className,v.className+"."+v.methodName));
		} else {
			ControlFlowGraph cfg = ExecutionTracer.getExecutionTracer().getCFG(v.className, v.methodName);
			int byteIdOfBranch = CFGMethodAdapter.branchCounterToBytecodeID.get(new Integer(v.branchID));
			r = new BranchCoverageTestFitness(new BranchCoverageGoal(v.branchID,byteIdOfBranch,v.branchExpressionValue,cfg,v.className,v.methodName));
		}
		return r;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestFitnessFunction#getFitness(de.unisb.cs.st.evosuite.testcase.TestChromosome, de.unisb.cs.st.evosuite.testcase.ExecutionResult)
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {

//		if(hasMoreThenOneCUTObject(individual))
//			return getMaxFitness();
		
		double defFitness = defTestFitness.getFitness(individual, result);
		int lastDef = getLastDef(result.trace);
		
		if((lastDef!=def.duID || defFitness != 0) && (!def.isStaticDU())) {
			return 1+normalize(defFitness);
		} else {
			
			if(lastDef == -1 && !def.isStaticDU()) {
//				System.out.println(result.trace.toString());
				throw new IllegalStateException("expect definition to be passed if its fitness is 0");
			}
			
			if(lastDef != def.duID && !def.isStaticDU())
				return 1+normalize(getMaxFitness());
			
			int defPos = getLastDefPos(result.trace);
			int usePos = getLastUsePos(result.trace);
			if(defPos>usePos && !def.isStaticDU())
				return normalize(getMaxFitness());
			
			double useFitness = useTestFitness.getFitness(individual, result);
			return normalize(useFitness);
		}
	}

//	/**
//	 * @param individual
//	 * @return
//	 */
//	private boolean hasMoreThenOneCUTObject(TestChromosome individual) {
//		
//		int c = 0;
//		
//		for(Statement s : individual.test.getStatements()) {
////			System.out.println("class "+def.className);
////			System.out.println(s.getReturnType().toString());
//			if(s.getReturnType().toString().equals("class "+def.className))
//				c++;
//		}
//		
////		System.out.println("#CCalls: "+c);
//		
//		return c > 1;
//	}

	/**
	 * @param trace
	 * @return
	 */
	private int getLastDef(ExecutionTrace trace) {
		int lastDef = -1;
//		System.out.println("#passedDefs: "+trace.passedDefs.size());
		if(trace.passedDefs.get(def.getDUVariableName()) == null)
			return -1;
		
		for(Integer defID : trace.passedDefs.get(def.getDUVariableName()).values()) {
//			System.out.println("saw "+defID);
			lastDef = defID;
		}
		
		return lastDef;
	}

	private int getLastDefPos(ExecutionTrace trace) {
		int lastPos = -1;
		if(trace.passedDefs.get(def.getDUVariableName()) == null)
			return -1;
		
		for(Integer defPos : trace.passedDefs.get(def.getDUVariableName()).keySet()) {
			lastPos = defPos;
		}
		
		return lastPos;
	}
	
	private int getLastUsePos(ExecutionTrace trace) {
		int lastPos = -1;
		if(trace.passedUses.get(def.getDUVariableName()) == null)
			return -1;
		
		for(Integer defPos : trace.passedUses.get(def.getDUVariableName()).keySet()) {
			lastPos = defPos;
		}
		
		return lastPos;
	}

	
	/**
	 * @return
	 */
	private double getMaxFitness() {

		return 200; // TODO
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#updateIndividual(de.unisb.cs.st.evosuite.ga.Chromosome, double)
	 */
	@Override
	protected void updateIndividual(Chromosome individual, double fitness) {
		
		individual.setFitness(fitness); // TODO ???

	}
	
	public String toString() {
		
		return "DUFitness for "+def.getDUVariableName()+" def "+def.duID+" in "+def.methodName+" branch "+def.branchID+"(l"+def.line_no+") use "+use.duID+" in "+use.methodName+" branch "+use.branchID+" (l"+use.line_no+")";
	}

}
