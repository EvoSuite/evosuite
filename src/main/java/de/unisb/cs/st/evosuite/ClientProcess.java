/**
 * 
 */
package de.unisb.cs.st.evosuite;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.SearchListener;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.utils.ExternalProcessUtilities;

/**
 * @author Gordon Fraser
 * @author Andrea Arcuri
 * 
 */
public class ClientProcess implements SearchListener {

	private final ExternalProcessUtilities util = new ExternalProcessUtilities();

	private GeneticAlgorithm ga;

	public void run() {
		System.out.println("* Connecting to master process on port "
		        + Properties.PROCESS_COMMUNICATION_PORT);
		if (!util.connectToMainProcess()) {
			System.err.println("* Could not connect to master process on port "
			        + Properties.PROCESS_COMMUNICATION_PORT);
			System.exit(1);
		}

		Object population_data = util.receiveInstruction();
		if (population_data == null) {
			// Starting a new search
			TestSuiteGenerator generator = new TestSuiteGenerator();
			ga = generator.setup();
			ga.addListener(this);
			generator.generateTestSuite(ga);
		} else {
			System.out.println("* Resuming search on new JVM");

			// Resume an interrupted search
			TestSuiteGenerator generator = new TestSuiteGenerator();
			ga = (GeneticAlgorithm) population_data;
			ga.addListener(this);
			generator.generateTestSuite(ga);
		}
		ga.removeListener(this);
		util.informSearchIsFinished(ga);
	}

	private boolean hasExceededResources() {
		if (TestCaseExecutor.getInstance().getNumStalledThreads() >= Properties.MAX_STALLED_THREADS) {
			System.out.println("* Too many stalled threads: "
			        + TestCaseExecutor.getInstance().getNumStalledThreads() + " / "
			        + Properties.MAX_STALLED_THREADS);
			return true;
		}

		Runtime runtime = Runtime.getRuntime();

		long freeMem = runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory();

		if (freeMem < Properties.MIN_FREE_MEM) {
			System.out.println("* Running out of memory, calling GC with memory left: "
			        + freeMem + " / " + runtime.maxMemory());
			System.gc();
			freeMem = runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory();

			if (freeMem < Properties.MIN_FREE_MEM) {
				System.out.println("* Running out of memory, giving up: " + freeMem
				        + " / " + runtime.maxMemory() + " - need "
				        + Properties.MIN_FREE_MEM);
				return true;
			} else {
				System.out.println("* Garbage collection recovered sufficient memory: "
				        + freeMem + " / " + runtime.maxMemory());
			}
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#searchStarted(de.unisb.cs.st.evosuite.ga.FitnessFunction)
	 */
	@Override
	public void searchStarted(GeneticAlgorithm algorithm) {
		if (ga == null)
			ga = algorithm;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#iteration(java.util.List)
	 */
	@Override
	public void iteration(GeneticAlgorithm algorithm) {
		if (ga == null)
			ga = algorithm;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#searchFinished(java.util.List)
	 */
	@Override
	public void searchFinished(GeneticAlgorithm algorithm) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#fitnessEvaluation(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public void fitnessEvaluation(Chromosome individual) {
		//System.out.println("Checking for restart");
		if (hasExceededResources()) {
			System.out.println("* Asking for JVM restart");
			ga.removeListener(this);
			util.askForRestart(ga);
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#modification(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		try {
			ClientProcess process = new ClientProcess();
			process.run();
		} catch (Throwable t) {
			System.err.println("Error when generating tests for: "
			        + Properties.TARGET_CLASS);
			t.printStackTrace();
			System.exit(1);
		}
	}
}
