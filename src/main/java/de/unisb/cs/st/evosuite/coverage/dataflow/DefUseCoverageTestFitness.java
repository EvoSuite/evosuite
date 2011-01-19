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

import java.util.Map;

import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.cfg.ControlFlowGraph;
import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageGoal;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageTestFitness;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * Evaluate fitness of a single test case with respect to one def-use pair
 * 
 * @author 
 *
 */
public class DefUseCoverageTestFitness extends TestFitnessFunction {


	private Definition def;
	private Use use;
	private BranchCoverageTestFitness defTestFitness;
	private BranchCoverageTestFitness useTestFitness;
	
	public DefUseCoverageTestFitness(Definition def, Use use) {

		if(!def.getDUVariableName().equals(use.getDUVariableName()))
			throw new IllegalArgumentException("expect def and use to be for the same variable");
		
		this.def = def;
		this.use = use;
		this.defTestFitness = getTestFitness(def.getCFGVertex());
		this.useTestFitness = getTestFitness(use.getCFGVertex());
		
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
			ControlFlowGraph cfg = CFGMethodAdapter.getCFG(v.className, v.methodName);
			int byteIdOfBranch = BranchPool.getBytecodeIDFor(v.branchID);
			r = new BranchCoverageTestFitness(new BranchCoverageGoal(v.branchID,byteIdOfBranch,v.branchExpressionValue,cfg,v.className,v.methodName));
		}
		return r;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestFitnessFunction#getFitness(de.unisb.cs.st.evosuite.testcase.TestChromosome, de.unisb.cs.st.evosuite.testcase.ExecutionResult)
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {

		if(traceCoversGoal(result.trace)) {
//			System.out.println("i got covered: "+toString());
//			System.out.println(result.test.toCode());
			result.test.addCoveredGoal(this);
			return 0;
		}
		
		double defFitness = defTestFitness.getFitness(individual, result);
		int lastDef = getLastDef(result.trace);
		
		CFGVertex defVertex = def.getCFGVertex();
		
		if(defFitness != 0 && !(defVertex.isStaticDU())) {
			return 1+normalize(defFitness);
		} else {

			if(lastDef == -1 && !defVertex.isStaticDU()) {
//				System.out.println(result.trace.toString());
				throw new IllegalStateException("expect definition to be passed if its fitness is 0");
			}
			
			if(lastDef != defVertex.duID && !defVertex.isStaticDU())
				return 1+normalize(getMaxFitness());
			
			int defPos = getLastDefPos(result.trace);
			int usePos = getLastUsePos(result.trace);
			
			if(defPos>usePos && !defVertex.isStaticDU())
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

	private boolean traceCoversGoal(ExecutionTrace trace) {
		
		int defPos = getLastDefPos(trace);
		int usePos = getLastUsePos(trace);
		
		// not both of them reached yet
		if(defPos == -1 || usePos == -1)
			return false;
		// def came after use
		if(defPos>usePos)
			return false;
		
		// the use for this DUVar is the one of this goal
		int useID = trace.passedUses.get(use.getDUVariableName()).get(usePos);
		if(useID!=use.getDUID())
			return false;
		
		int activeDef = getActiveDefFor(trace, usePos);
		
		if(activeDef == def.getDUID())
			return true;
		
		return false;
	}
	
	private int getActiveDefFor(ExecutionTrace trace, int usePos) {

		Map<Integer,Integer>defMap = trace.passedDefs.get(def.getDUVariableName());
		if(defMap == null)
			return -1;
		
		int lastDef = -1;
		
		for(Integer defPos : defMap.keySet()) {
			if(defPos>usePos)
				continue;
			lastDef = defMap.get(defPos);
		}
		
		return lastDef;
	}


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
		
		return "DUFitness for "+def.getDUVariableName()+" def "+def.getDUID()+" in "+def.getMethodName()+" branch "+def.getBranchID()+"(l"+def.getLineNumber()+") use "+use.getDUID()+" in "+use.getMethodName()+" branch "+use.getBranchID()+" (l"+use.getLineNumber()+")";
	}

}
