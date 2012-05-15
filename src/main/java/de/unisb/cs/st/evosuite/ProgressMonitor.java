/**
 * 
 */
package de.unisb.cs.st.evosuite;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.SearchListener;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

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
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#searchStarted(de.unisb.cs.st.evosuite.ga.GeneticAlgorithm)
	 */
	@Override
	public void searchStarted(GeneticAlgorithm algorithm) {
		stoppingCondition = TestSuiteGenerator.getStoppingCondition();
		max = stoppingCondition.getLimit();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#iteration(de.unisb.cs.st.evosuite.ga.GeneticAlgorithm)
	 */
	@Override
	public void iteration(GeneticAlgorithm algorithm) {
		long current = stoppingCondition.getCurrentValue();
		currentCoverage = (int) Math.floor(((TestSuiteChromosome) algorithm.getBestIndividual()).getCoverage() * 100);
		updateStatus((int) (100 * current / max), currentCoverage);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#searchFinished(de.unisb.cs.st.evosuite.ga.GeneticAlgorithm)
	 */
	@Override
	public void searchFinished(GeneticAlgorithm algorithm) {
		System.out.println("");

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#fitnessEvaluation(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public void fitnessEvaluation(Chromosome individual) {
		long current = stoppingCondition.getCurrentValue();
		updateStatus((int) (100 * current / max), currentCoverage);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#modification(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub

	}

}
