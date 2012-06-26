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
/**
 * 
 */
package org.evosuite;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.ga.SearchListener;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.testsuite.TestSuiteChromosome;


/**
 * @author gordon
 * 
 */
public class ProgressMonitor implements SearchListener, Serializable {

	private static final long serialVersionUID = -8518559681906649686L;

	protected Socket connection;
	protected ObjectOutputStream out;
	protected boolean connected = false;

	public ProgressMonitor() {
		connected = connectToMainProcess(Properties.PROGRESS_STATUS_PORT);
	}

	public boolean connectToMainProcess(int port) {

		try {
			connection = new Socket("127.0.0.1", port);
			out = new ObjectOutputStream(connection.getOutputStream());
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public void updateStatus(int percent, int coverage) {
		if (connected) {
			try {
				out.writeInt(percent);
				out.writeInt(coverage);
				out.flush();
			} catch (IOException e) {
				connected = false;
			}
		}
	}

	private StoppingCondition stoppingCondition = null;

	private long max = 1;

	private int currentCoverage = 0;

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#searchStarted(org.evosuite.ga.GeneticAlgorithm)
	 */
	@Override
	public void searchStarted(GeneticAlgorithm algorithm) {
		stoppingCondition = TestSuiteGenerator.getStoppingCondition();
		max = stoppingCondition.getLimit();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#iteration(org.evosuite.ga.GeneticAlgorithm)
	 */
	@Override
	public void iteration(GeneticAlgorithm algorithm) {
		long current = stoppingCondition.getCurrentValue();
		currentCoverage = (int) Math.floor(((TestSuiteChromosome) algorithm.getBestIndividual()).getCoverage() * 100);
		updateStatus((int) (100 * current / max), currentCoverage);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#searchFinished(org.evosuite.ga.GeneticAlgorithm)
	 */
	@Override
	public void searchFinished(GeneticAlgorithm algorithm) {
		System.out.println("");

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#fitnessEvaluation(org.evosuite.ga.Chromosome)
	 */
	@Override
	public void fitnessEvaluation(Chromosome individual) {
		long current = stoppingCondition.getCurrentValue();
		updateStatus((int) (100 * current / max), currentCoverage);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#modification(org.evosuite.ga.Chromosome)
	 */
	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub

	}

}
