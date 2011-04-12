/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
import de.unisb.cs.st.evosuite.ga.Randomness;
import de.unisb.cs.st.evosuite.ga.SearchListener;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;

/**
 * @author Gordon Fraser
 * 
 */
public class BloatListener implements SearchListener {

	private List<Double> length_history = new ArrayList<Double>();

	private List<Double> fitness_history = new ArrayList<Double>();

	private List<Integer> best_length_history = new ArrayList<Integer>();

	private List<Double> best_fitness_history = new ArrayList<Double>();

	private List<Integer> statement_history = new ArrayList<Integer>();

	private List<Double> budget_history = new ArrayList<Double>();

	private final int statement_budget;

	public List<Double> getLengthHistory() {
		return length_history;
	}

	public BloatListener(int statement_budget) {
		this.statement_budget = statement_budget;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.ga.SearchListener#fitnessEvaluation(de.unisb.cs.st.ga.
	 * Chromosome)
	 */
	@Override
	public void fitnessEvaluation(Chromosome individual) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unisb.cs.st.ga.SearchListener#iteration(java.util.List)
	 */
	@Override
	public void iteration(List<Chromosome> population) {
		double avg_length = 0.0;
		int min_length = Integer.MAX_VALUE;
		int max_length = 0;

		double avg_fitness = 0.0;

		for (Chromosome c : population) {
			avg_length += c.size();
			avg_fitness += c.getFitness();

			if (c.size() > max_length)
				max_length = c.size();
			if (c.size() < min_length)
				min_length = c.size();
		}
		length_history.add(avg_length / population.size());
		fitness_history.add(avg_fitness / population.size());
		best_length_history.add(population.get(0).size());
		best_fitness_history.add(population.get(0).getFitness());
		int statements = MaxStatementsStoppingCondition
		        .getNumExecutedStatements();
		statement_history.add(statements);
		budget_history.add((double) statements / statement_budget);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unisb.cs.st.ga.SearchListener#searchFinished(java.util.List)
	 */
	@Override
	public void searchFinished(List<Chromosome> population) {
		iteration(population);
		try {
			String xover = "RPX";
			if (Properties.getPropertyOrDefault("crossover_function",
			        "SinglePointRelative").equals("SinglePoint"))
				xover = "TPX";
			int num_branch = Properties.getPropertyOrDefault("branch", 0); // TODO:
																		   // read
																		   // from
																		   // fitness
																		   // function
			String filename = Properties.getPropertyOrDefault("report_dir",
			        "report")
			        + "/"
			        + Properties.TARGET_CLASS
			        + "_branch_"
			        + num_branch
			        + "_"
			        + Randomness.getInstance().getSeed()
			        + "_";
			filename += xover
			        + "_"
			        + Properties.getPropertyOrDefault("check_rank_length",
			                "true")
			        + "_"
			        + Properties.getPropertyOrDefault("check_parents_length",
			                "true")
			        + "_"
			        + Properties.getPropertyOrDefault("check_best_length",
			                "true")
			        + "_"
			        + Properties.getPropertyOrDefault("check_max_length",
			                "true") + "_" + Properties.CHROMOSOME_LENGTH
			        + ".csv";

			FileWriter writer = new FileWriter(filename, false);
			BufferedWriter w = new BufferedWriter(writer);
			w.write("Generation,Statements,Budget,AvgLength,AvgFitness,BestLength,BestFitness\n");
			for (int num = 0; num < length_history.size(); num++) {
				w.write(num + "," + statement_history.get(num) + ","
				        + budget_history.get(num) + ","
				        + length_history.get(num) + ","
				        + fitness_history.get(num) + ","
				        + best_length_history.get(num) + ","
				        + best_fitness_history.get(num));
				w.write("\n");
			}
			w.close();
		} catch (IOException e) {

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unisb.cs.st.ga.SearchListener#searchStarted(de.unisb.cs.st.ga.
	 * FitnessFunction)
	 */
	@Override
	public void searchStarted(FitnessFunction objective) {
		length_history = new ArrayList<Double>();
		fitness_history = new ArrayList<Double>();
		best_length_history = new ArrayList<Integer>();
		best_fitness_history = new ArrayList<Double>();
		statement_history = new ArrayList<Integer>();
		budget_history = new ArrayList<Double>();
	}

	// Keep track of how many rejections, and who rejected

	// Keep track of average population length

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.ga.SearchListener#mutation(de.unisb.cs.st.evosuite
	 * .ga.Chromosome)
	 */
	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub

	}
}
