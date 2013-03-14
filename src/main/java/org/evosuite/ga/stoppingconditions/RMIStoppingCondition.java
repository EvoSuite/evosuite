/**
 * 
 */
package org.evosuite.ga.stoppingconditions;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.GeneticAlgorithm;

/**
 * @author Gordon Fraser
 * 
 */
public class RMIStoppingCondition implements StoppingCondition {

	private static RMIStoppingCondition instance = null;

	private boolean isStopped = false;

	private RMIStoppingCondition() {

	}

	public static RMIStoppingCondition getInstance() {
		if (instance == null)
			instance = new RMIStoppingCondition();

		return instance;
	}

	public void stop() {
		isStopped = true;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#searchStarted(org.evosuite.ga.GeneticAlgorithm)
	 */
	@Override
	public void searchStarted(GeneticAlgorithm<?> algorithm) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#iteration(org.evosuite.ga.GeneticAlgorithm)
	 */
	@Override
	public void iteration(GeneticAlgorithm<?> algorithm) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#searchFinished(org.evosuite.ga.GeneticAlgorithm)
	 */
	@Override
	public void searchFinished(GeneticAlgorithm<?> algorithm) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#fitnessEvaluation(org.evosuite.ga.Chromosome)
	 */
	@Override
	public void fitnessEvaluation(Chromosome individual) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#modification(org.evosuite.ga.Chromosome)
	 */
	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.stoppingconditions.StoppingCondition#forceCurrentValue(long)
	 */
	@Override
	public void forceCurrentValue(long value) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.stoppingconditions.StoppingCondition#getCurrentValue()
	 */
	@Override
	public long getCurrentValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.stoppingconditions.StoppingCondition#getLimit()
	 */
	@Override
	public long getLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.stoppingconditions.StoppingCondition#isFinished()
	 */
	@Override
	public boolean isFinished() {
		return isStopped;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.stoppingconditions.StoppingCondition#reset()
	 */
	@Override
	public void reset() {
		isStopped = false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.stoppingconditions.StoppingCondition#setLimit(long)
	 */
	@Override
	public void setLimit(long limit) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RMIStoppingCondition";
	}
}
