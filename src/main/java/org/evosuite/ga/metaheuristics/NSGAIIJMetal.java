package org.evosuite.ga.metaheuristics;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NSGA-II implementation
 * 
 * @article{Deb:2002,
			author = {Deb, K. and Pratap, A. and Agarwal, S. and Meyarivan, T.},
			title = {{A Fast and Elitist Multiobjective Genetic Algorithm: NSGA-II}},
			journal = {Trans. Evol. Comp},
			issue_date = {April 2002},
			volume = {6},
			number = {2},
			month = apr,
			year = {2002},
			issn = {1089-778X},
			pages = {182--197},
			numpages = {16},
			url = {http://dx.doi.org/10.1109/4235.996017},
			doi = {10.1109/4235.996017},
			acmid = {2221582},
			publisher = {IEEE Press},
			address = {Piscataway, NJ, USA}}
 *
 * @author José Campos
 */
public class NSGAIIJMetal<T extends Chromosome> extends GeneticAlgorithm<T>
{
	private static final long serialVersionUID = 146182080947267628L;

	private static final Logger logger = LoggerFactory.getLogger(NSGAIIJMetal.class);

	/**
	 * Constructor
	 * @param factory a {@link org.evosuite.ga.ChromosomeFactory} object
	 */
	public NSGAIIJMetal(ChromosomeFactory<T> factory) {
		super(factory);
	}

	/** {@inheritDoc} */
	@Override
	protected void evolve()
	{
		// empty
	}

	/** {@inheritDoc} */
	@Override
	public void initializePopulation() {
		logger.info("executing initializePopulation function");

		notifySearchStarted();
		currentIteration = 0;

		// Create a random parent population P0
		this.generateInitialPopulation(Properties.POPULATION);

		this.notifyIteration();
	}

	/** {@inheritDoc} */
	@Override
	public void generateSolution() {
		logger.info("executing generateSolution function");

		if (population.isEmpty())
			initializePopulation();

		List<T> offspringPopulation;

		while (!isFinished())
		{
		    // Create the offSpring solutionSet
		    offspringPopulation = new ArrayList<T>(population.size());

		    Chromosome[] parents = new Chromosome[2];
		    for (int i = 0; i < (population.size() / 2); i++)
		    {
		        if (currentIteration < Properties.SEARCH_BUDGET)
		        {
		          //obtain parents
		          parents[0] = (Solution) selectionOperator.execute(population);
		          parents[1] = (Solution) selectionOperator.execute(population);

		          Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
		          mutationOperator.execute(offSpring[0]);
		          mutationOperator.execute(offSpring[1]);
		          problem_.evaluate(offSpring[0]);
		          problem_.evaluateConstraints(offSpring[0]);
		          problem_.evaluate(offSpring[1]);
		          problem_.evaluateConstraints(offSpring[1]);
		          offspringPopulation.add(offSpring[0]);
		          offspringPopulation.add(offSpring[1]);
		          currentIteration += 2;
		        }
		      }

		      // Create the solutionSet union of solutionSet and offSpring
		      union = ((SolutionSet) population).union(offspringPopulation);

		      // Ranking the union
		      Ranking ranking = new Ranking(union);

		      int remain = populationSize;
		      int index = 0;
		      SolutionSet front = null;
		      population.clear();

		      // Obtain the next front
		      front = ranking.getSubfront(index);

		      while ((remain > 0) && (remain >= front.size())) {
		        //Assign crowding distance to individuals
		        distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
		        //Add the individuals of this front
		        for (int k = 0; k < front.size(); k++) {
		          population.add(front.get(k));
		        } // for

		        //Decrement remain
		        remain = remain - front.size();

		        //Obtain the next front
		        index++;
		        if (remain > 0) {
		          front = ranking.getSubfront(index);
		        } // if        
		      } // while

		      // Remain is less than front(index).size, insert only the best one
		      if (remain > 0) {  // front contains individuals to insert                        
		        distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
		        front.sort(new CrowdingComparator());
		        for (int k = 0; k < remain; k++) {
		          population.add(front.get(k));
		        } // for

		        remain = 0;
		      }

		      currentIteration++;
		      this.notifyIteration();
		}

		/*for (T p : population) {
			for (FitnessFunction<T> ff : this.getFitnessFunctions()) {
				System.out.printf("%f,", p.getFitness(ff));
			}
			System.out.print("\n");
		}*/

		notifySearchFinished();
	}
}
