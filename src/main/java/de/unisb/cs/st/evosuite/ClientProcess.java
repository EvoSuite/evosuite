/**
 * 
 */
package de.unisb.cs.st.evosuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.coverage.FitnessLogger;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.SearchListener;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.testcase.TestCluster;
import de.unisb.cs.st.evosuite.utils.ExternalProcessUtilities;
import de.unisb.cs.st.evosuite.utils.LoggingUtils;

/**
 * @author Gordon Fraser
 * @author Andrea Arcuri
 * 
 */
public class ClientProcess implements SearchListener {

	private static final boolean logLevelSet = LoggingUtils.checkAndSetLogLevel();
	
	private static Logger logger = LoggerFactory.getLogger(ClientProcess.class);
	
	private final ExternalProcessUtilities util = new ExternalProcessUtilities();

	private GeneticAlgorithm ga;

	public static GeneticAlgorithm  geneticAlgorithmStatus;
	
	public void run() {
		
		System.out.println("* Connecting to master process on port "
		        + Properties.PROCESS_COMMUNICATION_PORT);
		if (!util.connectToMainProcess()) {
			System.err.println("* Could not connect to master process on port "
			        + Properties.PROCESS_COMMUNICATION_PORT);
			System.exit(1);
		}

		TestSuiteGenerator generator = null;
		Object population_data = util.receiveInstruction();
		if (population_data == null) {
			// Starting a new search
			generator = new TestSuiteGenerator();
			// FIXXME: This needs fixing. Like this, it breaks mutation testing
			ga = null; //generator.setup();
			//ga.addListener(this);
			generator.generateTestSuite(ga);
		} else {
			System.out.println("* Resuming search on new JVM");

			// Resume an interrupted search
			generator = new TestSuiteGenerator();
			ga = (GeneticAlgorithm) population_data;
			ga.addListener(this);
			generator.generateTestSuite(ga);
		}
		/*
		 * TODO: RE-FACTOR: add/remove listeners on ga has no effect, as this ga reference is not used
		 * inside generateTestSuite, and anyway listener here does not do anything (ga si always
		 * != null)
		 */
		if (ga != null) {
			ga.removeListener(this);

			ga = generator.getEmployedGeneticAlgorithm();
		} else {
			//FIXME: this is a dirty hack. this code needs to be refactored
			ga = generator.getEmployedGeneticAlgorithm();
		}
		
		if(Properties.CLIENT_ON_THREAD){
			/*
			 * FIXME:
			 * this is done when the client is run on same JVM, to avoid
			 * problems of serializing ga
			 */
			geneticAlgorithmStatus = ga;
		}
		util.informSearchIsFinished(ga);
	}

	private boolean hasExceededResources() {
		if (TestCaseExecutor.getInstance().getNumStalledThreads() >= Properties.MAX_STALLED_THREADS) {
			LoggingUtils.getEvoLogger().info("* Too many stalled threads: "
			        + TestCaseExecutor.getInstance().getNumStalledThreads() + " / "
			        + Properties.MAX_STALLED_THREADS);
			return true;
		}

		Runtime runtime = Runtime.getRuntime();

		long freeMem = runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory();

		if (freeMem < Properties.MIN_FREE_MEM) {
			LoggingUtils.getEvoLogger().info("* Running out of memory, calling GC with memory left: "
			        + freeMem + " / " + runtime.maxMemory());
			System.gc();
			freeMem = runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory();

			if (freeMem < Properties.MIN_FREE_MEM) {
				LoggingUtils.getEvoLogger().info("* Running out of memory, giving up: " + freeMem
				        + " / " + runtime.maxMemory() + " - need "
				        + Properties.MIN_FREE_MEM);
				return true;
			} else {
				LoggingUtils.getEvoLogger().info("* Garbage collection recovered sufficient memory: "
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
			LoggingUtils.getEvoLogger().info("* Asking for JVM restart");
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
			LoggingUtils.getEvoLogger().info("* Starting client");
			ClientProcess process = new ClientProcess();
			process.run();
			if(!Properties.CLIENT_ON_THREAD){
				System.exit(0);
			}
		} catch (Throwable t) {
			logger.error("Error when generating tests for: "
			        + Properties.TARGET_CLASS, t);
			t.printStackTrace();
			
			//sleep 1 sec to be more sure that the above log is recorded
			try { Thread.sleep(1000);} 
			catch (InterruptedException e) {}
			
			if(!Properties.CLIENT_ON_THREAD){
				System.exit(1);
			}
		}
	}
}
