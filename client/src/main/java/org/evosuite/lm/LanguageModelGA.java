package org.evosuite.lm;

import org.evosuite.testcase.ValueMinimizer;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by mat on 02/04/2014.
 */
public class LanguageModelGA extends LanguageModelSearch {

    private static final int TOURNAMENT_SIZE = 7;
    private static final double MUTATION_RATE = 0.15;
    private static final double CROSSOVER_RATE = 0.8;
    private static final double ELITIST_RATE = 0.3;
    private static final double REFILL_LEVEL = 1;

    private HashSet<Chromosome> population;

    private static int POPULATION_SIZE = 20;

    private static Logger logger = LoggerFactory.getLogger(LanguageModelGA.class);


    public LanguageModelGA(ConstantValue constantValue, ValueMinimizer.Minimization objective){
        super(objective, constantValue);

        population = new HashSet<>();
        setupPopulation();
    }

    /**
     * Seed the population with one instance of the original string, plus
     * a population of random individuals.
     */
    private void setupPopulation(){
        Chromosome originalValue = new Chromosome(startPoint);
        population.add(originalValue);

        while(population.size() < POPULATION_SIZE - 1){
            population.add(mutate(originalValue));
            //population.add(mutate(originalValue));
        }
    }

    private Chromosome select(){
        return select(population, true);
    }

    private Chromosome select(HashSet<Chromosome> pool){
        return select(pool, true);
    }

    private Chromosome select(HashSet<Chromosome> pool, boolean best){
        ArrayList<Chromosome> sample = new ArrayList<>();
        //Chose a random sample from the population (duplicates are handled by using a HashSet):
        while(sample.size() < TOURNAMENT_SIZE && sample.size() < pool.size()){
            sample.add(Randomness.choice(pool));
        }

        assert !sample.isEmpty();

        if(best) {
            Collections.sort(sample, Collections.reverseOrder(this));
            return sample.get(0);
        }else{
            Collections.sort(sample,this);
            return sample.get(0);
        }

    }

    private HashSet<Chromosome> generation(HashSet<Chromosome> newPopulation){
        int numMutants = 0;
        int numCrossovers = 0;
        int numElites = 0;

        for(int crossover =0; crossover < population.size(); crossover++){
            if(Randomness.nextDouble() < CROSSOVER_RATE){
                Chromosome parent1 = select();
                Chromosome parent2 = select();
                if(parent1 != parent2) {
                    if(newPopulation.addAll(crossover(parent1, parent2))) {
                        numCrossovers++;
                    }
                }
            }

        }



        int numElitesToSelect = (int)Math.round(ELITIST_RATE * population.size());
        //copy some elites over:

        ArrayList<Chromosome> rankedPopulation = new ArrayList<Chromosome>(population.size());
        rankedPopulation.addAll(population);
        Collections.sort(rankedPopulation,Collections.reverseOrder(this));



        for(Chromosome elite : rankedPopulation) {
            if (numElitesToSelect > 0) {
                if(newPopulation.add(elite)) {
                    numElites++;
                    numElitesToSelect--;
                }
            } else {
                break;
            }
        }

        //mutation
        for(int mutant = 0; mutant < population.size(); mutant++) {
            if(Randomness.nextDouble() < MUTATION_RATE) {
                if(newPopulation.add(mutate(select(newPopulation))))
                    numMutants++;
            }
        }




        //cull the weakest individuals to keep pop size under control:
        if(newPopulation.size() > POPULATION_SIZE){
            logger.debug("Removing some weak individuals from old population");
        }
        int numRemoved = 0;
        while(newPopulation.size() > POPULATION_SIZE){
            Chromosome item = select(newPopulation, false);
            assert item != null;
            assert newPopulation.contains(item);
            newPopulation.remove(item);
            numRemoved++;
        }

        int numAdded = 0;
        //it's also possible that we haven't got enough individuals
        //if that's the case, randomly pull some from the previous generation
        if(newPopulation.size() < POPULATION_SIZE * REFILL_LEVEL){
            HashSet<Chromosome> oldPopulation = new HashSet<>(population);
            logger.debug("Pulling individuals from old population (size = {})", newPopulation.size());

            while(newPopulation.size() < POPULATION_SIZE * REFILL_LEVEL && !oldPopulation.isEmpty()) {
                Chromosome individual = Randomness.choice(oldPopulation);
                oldPopulation.remove(individual);
                if(newPopulation.add(individual)) {
                    numAdded++;
                }
            }

            //have we done enough, or do we need to add more individuals?
            if(newPopulation.size() < POPULATION_SIZE * REFILL_LEVEL){
                logger.debug("Adding random mutants to keep pop size up (current size is {})", newPopulation.size());
            }

            while(newPopulation.size() < POPULATION_SIZE * REFILL_LEVEL && !population.isEmpty()){
                newPopulation.add(mutate(Randomness.choice(population)));
            }

        }


        logger.debug("Finished a GA generation. Created {} mutants, performed {} crossovers and preserved {} elites on a population of {} (-{},+{}) New pop: {}",
                numMutants,
                numCrossovers,
                numElites,
                population.size(),
                numRemoved,
                numAdded,
                newPopulation.size());

        return newPopulation;

    }

    protected Chromosome getBest(HashSet<Chromosome> population, Chromosome best){

        for(Chromosome individual : population){
            try {
                if (individual.compareTo(best) > 0) {
                    best = individual;
                }
            }catch(EvaluationBudgetExpendedException e){
                logger.debug("Evaluation limit hit by GA; will select best from evaluated population.");
                break;
            }
        }
        return best;
    }
    
    @Override
    public String optimise(){


        resetEvaluationCounter();

        Chromosome best = getBest(population, null);

        for(int generation = 0; generation < GENERATIONS && !isBudgetExpended(); generation++){
            HashSet<Chromosome> newPopulation = new HashSet<Chromosome>();

            try{
                generation(newPopulation);


                if(newPopulation.isEmpty()){
                    assert isBudgetExpended();
                    //If we successfully ran a generation (didn't run out of evals) but didn't get any individuals,
                    // we've had problems.
                    throw new RuntimeException("LM GA: New population is empty");
                }

            }catch(EvaluationBudgetExpendedException e){
                //For now, we just ignore the exception. At this stage, the generation failed, but there might be chromosomes
                // that made it into the new population.
                //At the next iteration of the loop, we'll see if any of those were any better than the current best individual.
                logger.debug("Couldn't finish a generation, ran out of evaluations.");
            }


            logger.debug("LM GA: Generation {} of {}. Population size is {} (previously {}). Best fitness is [{}], fitness: {}",
                    generation,
                    GENERATIONS,
                    newPopulation.size(),
                    population.size(),
                    best.getValue(),
                    best.getFitness());

            /*for(Chromosome c : population){
                logger.debug("Gen {} Chromosome [{}] Fitness {}",
                        generation,
                        c.getValue(),
                        c.getFitness());
            }*/


            population = newPopulation;

            best = getBest(population, best);

        }
        return best.getValue();
    }

    protected List<? extends Chromosome> crossover(Chromosome parent1, Chromosome parent2) {

        String p1 = parent1.getValue();
        String p2 = parent2.getValue();

        int splitPoint = Randomness.nextInt(0,p1.length());


        String c1 = p1.substring(0,splitPoint) + p2.substring(splitPoint);
        String c2 = p2.substring(0,splitPoint) + p1.substring(splitPoint);



        Chromosome child1 = new Chromosome(c1);
        Chromosome child2 = new Chromosome(c2);


        return Arrays.asList(child1, child2);
    }


}

