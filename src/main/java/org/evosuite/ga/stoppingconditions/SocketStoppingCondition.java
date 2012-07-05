/**
 * 
 */
package org.evosuite.ga.stoppingconditions;

import java.io.IOException;
import java.net.ServerSocket;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.utils.LoggingUtils;

/**
 * <p>SocketStoppingCondition class.</p>
 *
 * @author Gordon Fraser
 */
public class SocketStoppingCondition implements StoppingCondition {

	private static boolean interrupted = false;

	/**
	 * <p>accept</p>
	 */
	public void accept() {
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					ServerSocket serverSocket = new ServerSocket(Properties.STOPPING_PORT);
					serverSocket.accept();
					LoggingUtils.getEvoLogger().info("* Stopping request received");
					synchronized (this) {
						interrupted = true;
					}

				} catch (IOException e) {
					LoggingUtils.getEvoLogger().warn("Failed to create socket on port "
					                                         + Properties.STOPPING_PORT);
				}

			}
		};
		t.start();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#searchStarted(org.evosuite.ga.GeneticAlgorithm)
	 */
	/** {@inheritDoc} */
	@Override
	public void searchStarted(GeneticAlgorithm algorithm) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#iteration(org.evosuite.ga.GeneticAlgorithm)
	 */
	/** {@inheritDoc} */
	@Override
	public void iteration(GeneticAlgorithm algorithm) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#searchFinished(org.evosuite.ga.GeneticAlgorithm)
	 */
	/** {@inheritDoc} */
	@Override
	public void searchFinished(GeneticAlgorithm algorithm) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#fitnessEvaluation(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public void fitnessEvaluation(Chromosome individual) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#modification(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.stoppingconditions.StoppingCondition#forceCurrentValue(long)
	 */
	/** {@inheritDoc} */
	@Override
	public void forceCurrentValue(long value) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.stoppingconditions.StoppingCondition#getCurrentValue()
	 */
	/** {@inheritDoc} */
	@Override
	public long getCurrentValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.stoppingconditions.StoppingCondition#getLimit()
	 */
	/** {@inheritDoc} */
	@Override
	public long getLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.stoppingconditions.StoppingCondition#isFinished()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isFinished() {
		return interrupted;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.stoppingconditions.StoppingCondition#reset()
	 */
	/** {@inheritDoc} */
	@Override
	public void reset() {
		interrupted = false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.stoppingconditions.StoppingCondition#setLimit(long)
	 */
	/** {@inheritDoc} */
	@Override
	public void setLimit(long limit) {
		// TODO Auto-generated method stub

	}

}
