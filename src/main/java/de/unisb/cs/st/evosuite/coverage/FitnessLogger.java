/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.SearchListener;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;

/**
 * @author Gordon Fraser
 * 
 */
public class FitnessLogger implements SearchListener {

	private static Logger logger = LoggerFactory.getLogger(FitnessLogger.class);

	private final List<Integer> evaluations_history = new ArrayList<Integer>();

	private final List<Integer> statements_history = new ArrayList<Integer>();

	private final List<Double> fitness_history = new ArrayList<Double>();

	private final List<Integer> size_history = new ArrayList<Integer>();

	private String name = null;

	private int evaluations = 0;

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#searchStarted(de.unisb.cs.st.evosuite.ga.FitnessFunction)
	 */
	@Override
	public void searchStarted(GeneticAlgorithm algorithm) {
		evaluations = 0;
		evaluations_history.clear();
		statements_history.clear();
		fitness_history.clear();
		size_history.clear();
		File dir = new File(Properties.REPORT_DIR + "/goals/");
		dir.mkdir();
		name = Properties.REPORT_DIR
		        + "/goals/"
		        + algorithm.getFitnessFunction().toString().replace(" ", "_").replace(":",
		                                                                              "-").replace("(",
		                                                                                           "").replace(")",
		                                                                                                       "");
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#iteration(java.util.List)
	 */
	@Override
	public void iteration(GeneticAlgorithm algorithm) {
		if (algorithm.getPopulation().isEmpty())
			return;

		evaluations_history.add(evaluations);
		statements_history.add(MaxStatementsStoppingCondition.getNumExecutedStatements());
		fitness_history.add(algorithm.getBestIndividual().getFitness());
		size_history.add(algorithm.getBestIndividual().size());
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#searchFinished(java.util.List)
	 */
	@Override
	public void searchFinished(GeneticAlgorithm algorithm) {
		if (name == null)
			return;

		File f = new File(name);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
			out.write("Iteration,Evaluations,Statements,Fitness,Size\n");
			for (int i = 0; i < fitness_history.size(); i++) {
				out.write(i + ",");
				out.write(evaluations_history.get(i) + ",");
				out.write(statements_history.get(i) + ",");
				out.write(fitness_history.get(i) + ",");
				out.write(size_history.get(i) + "\n");
			}
			out.close();
		} catch (IOException e) {
			logger.error("Could not open csv file: " + e);
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#fitnessEvaluation(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public void fitnessEvaluation(Chromosome individual) {
		evaluations++;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#modification(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub

	}

}
