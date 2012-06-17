/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.SearchListener;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;

/**
 * EvoSuite can run out of resources: eg out of memory, or too many threads that are stalled and
 * cannot be killed. 
 * 
 * There can be several ways to handle these cases. The simplest is to to just stop the search.
 * Note: stopping the search when EvoSuite is close to run of memory is important because, if it does
 * actually run out of memory, when it will not be able to write down the results obtained so far!
 * 
 *
 */
public class ResourceController implements SearchListener, StoppingCondition {

	private static Logger logger = LoggerFactory.getLogger(ResourceController.class);
	
	private GeneticAlgorithm ga;
	private boolean stopComputation;
	
	private boolean hasExceededResources() {
		
		if (TestCaseExecutor.getInstance().getNumStalledThreads() >= Properties.MAX_STALLED_THREADS) {
			logger.info("* Too many stalled threads: "
			                                         + TestCaseExecutor.getInstance().getNumStalledThreads()
			                                         + " / "
			                                         + Properties.MAX_STALLED_THREADS);
			return true;
		}

		Runtime runtime = Runtime.getRuntime();

		long freeMem = runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory();

		if (freeMem < Properties.MIN_FREE_MEM) {
			logger.trace("* Running out of memory, calling GC with memory left: "
			                                         + freeMem
			                                         + " / "
			                                         + runtime.maxMemory());
			System.gc();
			freeMem = runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory();

			if (freeMem < Properties.MIN_FREE_MEM) {
				logger.info("* Running out of memory, giving up: "
				                                         + freeMem + " / "
				                                         + runtime.maxMemory()
				                                         + " - need "
				                                         + Properties.MIN_FREE_MEM);
				return true;
			} else {
				logger.trace("* Garbage collection recovered sufficient memory: "
				                                         + freeMem
				                                         + " / "
				                                         + runtime.maxMemory());
			}
		}

		return false;
	}

	@Override
	public void searchStarted(GeneticAlgorithm algorithm) {
		ga = algorithm;
	}

	@Override
	public void iteration(GeneticAlgorithm algorithm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void searchFinished(GeneticAlgorithm algorithm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fitnessEvaluation(Chromosome individual) {
		if (hasExceededResources()) {
			/*
			 * TODO: for now, we just stop the search. in case of running out of memory, other options could
			 * be to reduce the population size 
			 */
			stopComputation = true;
			ga.addStoppingCondition(this);
			logger.warn("Shutting down the search do to running out of computational resources");
		}		
	}

	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forceCurrentValue(long value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getCurrentValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isFinished() {
		return stopComputation;
	}

	@Override
	public void reset() {
		stopComputation = false;
	}

	@Override
	public void setLimit(long limit) {
		// TODO Auto-generated method stub
		
	}
	
}
