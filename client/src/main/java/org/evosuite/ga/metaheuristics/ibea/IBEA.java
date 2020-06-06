package org.evosuite.ga.metaheuristics.ibea;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.comparators.DominanceComparator;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

public class IBEA<T extends Chromosome> extends GeneticAlgorithm<T> {


    private static final long serialVersionUID = 146182080947267628L;

    private static final Logger logger = LoggerFactory.getLogger(IBEA.class);

    private List<List<Double>> indicatorValues_;

    private double maxIndicatorValue_;

    private double[] indicatorFitnessValues = new double[Properties.POPULATION*2];


    /**
     * Constructor
     *
     * @param factory a {@link org.evosuite.ga.ChromosomeFactory} object
     */
    public IBEA(ChromosomeFactory<T> factory) {
        super(factory);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void evolve() {
        // Create the offSpring population
        List<T> offSpringGeneration = new ArrayList<T>(Properties.POPULATION);
        int populationSize = this.population.size();
        int intendedPopulationSize = getNeededPopulationSize();
        //TODO : check if we need to add elitism and add the best two parents first

        while (offSpringGeneration.size()+populationSize < intendedPopulationSize) {

            T parent1 = this.selectionFunction.select(population);
            T parent2 = this.selectionFunction.select(population);
            T offspring1 = (T) parent1.clone();
            T offspring2 = (T) parent2.clone();

            try {
                // 1. make the crossover
                if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
                    crossoverFunction.crossOver(offspring1, offspring2);
                }

                if (Randomness.nextDouble() <= Properties.MUTATION_RATE) {
                    notifyMutation(offspring1);
                    offspring1.mutate();
                    notifyMutation(offspring2);
                    offspring2.mutate();
                }

                if (offspring1.isChanged()) {
                    offspring1.updateAge(currentIteration);
                }
                if (offspring2.isChanged()) {
                    offspring2.updateAge(currentIteration);
                }

                // Evaluate
                for (final FitnessFunction<T> ff : this.getFitnessFunctions()) {
                    ff.getFitness(offspring1);
                    notifyEvaluation(offspring1);
                    ff.getFitness(offspring2);
                    notifyEvaluation(offspring2);
                }

                offSpringGeneration.add(offspring1);
                offSpringGeneration.add(offspring2);
            } catch (ConstructionFailedException e) {
                logger.info("CrossOver/Mutation failed.");
                continue;
            }
        }

        this.population.addAll(offSpringGeneration);
        int currentSize = this.population.size();
        while(currentSize > intendedPopulationSize){
            this.population.remove(--currentSize);
        }
    }

    private void updateFitnessFunctionsAndValues(List<T> archive) {
        for (T object : archive) {
            for (final FitnessFunction<T> ff : this.getFitnessFunctions()) {
                ff.getFitness(object);
                notifyEvaluation(object);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializePopulation() {
        logger.info("executing initializePopulation function");

        notifySearchStarted();

        // Create a random parent population P0
        this.generateInitialPopulation(Properties.POPULATION);

        currentIteration = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateSolution() {
        logger.info("executing generateSolution function");

        initializePopulation();
        updateFitnessFunctionsAndValues(this.population);
        evolve();

        boolean finished = isFinished();
        while (!finished) {

            this.population = applyModification(this.population);

            calculateFitness(this.population);
            while (population.size() > Properties.POPULATION) {
                removeWorst(population);
            }

            currentIteration++;
            this.notifyIteration();

            evolve();
            finished = isFinished();
        }

        sortPopulationOnPareto();

        this.writeIndividuals(this.population);
        logger.warn("IS finished print the fucking next" + finished);
        notifySearchFinished();
        return;
    }

    private void sortPopulationOnPareto(){
        this.rankingFunction.computeRankingAssignment(this.population, new LinkedHashSet<FitnessFunction<T>>(this.getFitnessFunctions()));

        List<T> front = null;
        population.clear();
        front = this.rankingFunction.getSubfront(0);
        population.addAll(front);
        int numberOfSubfronts = this.rankingFunction.getNumberOfSubfronts();
        int idx= 1;
        while (idx < numberOfSubfronts){
            front = this.rankingFunction.getSubfront(idx);
            if(front == null ){
                break;
            }
            population.addAll(front);
            idx++;
        }
    }

    @Override
    public T getBestIndividual() {

        if (population.isEmpty()) {
            return this.chromosomeFactory.getChromosome();
        }

        this.rankingFunction.computeRankingAssignment(this.population, new LinkedHashSet<FitnessFunction<T>>(this.getFitnessFunctions()));
        // Assume population is sorted
        return (T)  this.getRankingFunction().getSubfront(0).get(0);
    }

    private void removeWorst(List<T> archive) {
        // Find the worst;
        //double worst = archive.get(0).getFitness();
        double worst = indicatorFitnessValues[0] ;

        int worstIndex = 0;
        double kappa = 0.05;

        for (int i = 1; i < archive.size(); i++) {
            if (indicatorFitnessValues[i]  > worst) {
                worst =indicatorFitnessValues[i] ;
                worstIndex = i;
            }
        }


        // Update the population
        for (int i = 0; i < archive.size(); i++) {
            if (i != worstIndex) {
                double fitness = indicatorFitnessValues[i];
                Double indicationValue = new Double(Math.exp((-indicatorValues_.get(worstIndex).get(i) / maxIndicatorValue_) / kappa));
                fitness -= indicationValue.isNaN() ? 0.0 : indicationValue.doubleValue();

                //archive.get(i).setFitness(this.getFitnessFunction(), fitness);
                indicatorFitnessValues[i] = fitness;
            }
        }

        // remove worst from the indicatorValues list
        indicatorValues_.remove(worstIndex);
        for (List<Double> anIndicatorValues_ : indicatorValues_) {
            anIndicatorValues_.remove(worstIndex);
        }
        indicatorFitnessValues[worstIndex] = 0;
        archive.remove(worstIndex);

    } // removeWorst

    private void calculateFitness(List<T> archive) {


        FitnessFunction<T>[] ffArray =  this.getFitnessFunctions().toArray(new FitnessFunction[0]);

        //because of WTS
        int numberOfObjectives = ffArray.length;

        double[] maximumValues = new double[numberOfObjectives];
        double[] minimumValues = new double[numberOfObjectives];

        for (int i = 0; i < numberOfObjectives; i++) {
            maximumValues[i] = -Double.MAX_VALUE; // i.e., the minus maximum value
            minimumValues[i] = Double.MAX_VALUE; // i.e., the maximum value
        }

        for (int pos = 0; pos < archive.size(); pos++) {
            for (int obj = 0; obj < numberOfObjectives; obj++) {
                double value = archive.get(pos).getFitness(ffArray[obj]);
                if (value > maximumValues[obj])
                    maximumValues[obj] = value;
                if (value < minimumValues[obj])
                    minimumValues[obj] = value;
            }
        }

        computeIndicatorValuesHD(archive, maximumValues, minimumValues);
        for (int pos = 0; pos < archive.size(); pos++) {
            fitness(archive, pos);
        }
    }

    public void computeIndicatorValuesHD(List<T> archive,
                                         double[] maximumValues,
                                         double[] minimumValues) {
        T A, B;
        // Initialize the structures
        indicatorValues_ = new ArrayList<List<Double>>();
        maxIndicatorValue_ = -Double.MAX_VALUE;

        for (int j = 0; j < archive.size(); j++) {
            A = (T) archive.get(j).clone();

            List<Double> aux = new ArrayList<Double>();
            for (int i = 0; i < archive.size(); i++) {
                B = (T) archive.get(i).clone();

                int flag = (new DominanceComparator<T>(this.getFitnessFunction())).compare(A, B);

                double value = 0.0;
                if (flag == -1) {
                    value = -calcHypervolumeIndicator(A,indicatorFitnessValues[j], B, indicatorFitnessValues[i],1, maximumValues, minimumValues);
                } else if (flag == 1) { // Hadi : TODO : wgat if they are equal ( flag =0 )
                    value = calcHypervolumeIndicator(B, indicatorFitnessValues[i], A,  indicatorFitnessValues[j],1, maximumValues, minimumValues);
                }
                //double value = epsilon.epsilon(matrixA,matrixB,problem_.getNumberOfObjectives());

                //Update the max value of the indicator
                if (Math.abs(value) > maxIndicatorValue_)
                    maxIndicatorValue_ = Math.abs(value);
                aux.add(value);
            }
            indicatorValues_.add(aux);
        }
    }

    public void fitness(List<T> archive, int pos) {
        double fitness = 0.0;
        double kappa = 0.05;

        for (int i = 0; i < archive.size(); i++) {
            if (i != pos) {
                Double calculatedValue = new Double(Math.exp((-1 * indicatorValues_.get(i).get(pos) / maxIndicatorValue_) / kappa));
                fitness += (calculatedValue.isNaN()) ? 0.0 : calculatedValue;
            }
        }
        //archive.get(pos).setFitness(this.getFitnessFunction(), fitness);
        indicatorFitnessValues[pos]=fitness;
    }

    /**
     * calculates the hypervolume of that portion of the objective space that
     * is dominated by individual a but not by individual b
     */
    double calcHypervolumeIndicator(T p_ind_a,
                                    double aFitness,
                                    T p_ind_b,
                                    double bFitness,
                                    int d,
                                    double maximumValues[],
                                    double minimumValues[]) {
        double a, b, r, max;
        double volume = 0;
        double rho = 2.0;

        r = rho * (maximumValues[d - 1] - minimumValues[d - 1]);
        max = minimumValues[d - 1] + r;


        a = aFitness;
        if (p_ind_b == null) {
            b = max;
        } else {
            b = bFitness;
        }


        if (d == 1) {
            if (a < b) {
                volume = (b - a) / r;
            } else {
                volume = 0;
            }
        } else {
            if (a < b) {
                volume = calcHypervolumeIndicator(p_ind_a, aFitness, null,bFitness, d - 1, maximumValues, minimumValues) *
                        (b - a) / r;
                volume += calcHypervolumeIndicator(p_ind_a,aFitness, p_ind_b,bFitness, d - 1, maximumValues, minimumValues) *
                        (max - b) / r;
            } else {
                volume = calcHypervolumeIndicator(p_ind_a, aFitness, p_ind_b, bFitness,d - 1, maximumValues, minimumValues) *
                        (max - a) / r;
            }
        }

        return (volume);
    }


    public List applyModification(List<T> union) {
        return union;
    }

    private int getNeededPopulationSize() {
        return Properties.POPULATION*2;
    }
}

