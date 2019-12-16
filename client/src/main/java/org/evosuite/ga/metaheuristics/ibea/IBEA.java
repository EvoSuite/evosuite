package org.evosuite.ga.metaheuristics.ibea;

import org.evosuite.Properties;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.comparators.DominanceComparator;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.operators.selection.SelectionFunction;
import org.evosuite.ga.operators.selection.TournamentSelection;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.BudgetConsumptionMonitor;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IBEA <T extends Chromosome> extends GeneticAlgorithm<T> {

    /**
     * logger factory
     **/
    private static final Logger logger = LoggerFactory.getLogger(IBEA.class);

    /**
     * Keep track of overall suite fitness and coverage
     */
    protected TestSuiteFitnessFunction suiteFitness;

    /**
     * Object used to keep track of the execution time needed to reach the maximum coverage
     */
    protected BudgetConsumptionMonitor budgetMonitor;

    /**
     * To keep track when the overall search started
     */
    protected long startGlobalSearch = 0;

    /**
     * IBEA Selection function
     */
    protected SelectionFunction<T> selectionFunction = new TournamentSelection<T>();

    protected List<T> archive = null;

    private List<List<Double>> indicatorValues_;

    private double maxIndicatorValue_;

    private boolean isModificatedIBEA = false;

    /**
     * Constructor
     */
    public IBEA(ChromosomeFactory<T> factory) {
        super(factory);
        if (ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.BRANCH)) {
            suiteFitness = new BranchCoverageSuiteFitness();
        }
        startGlobalSearch = System.currentTimeMillis();
        budgetMonitor = new BudgetConsumptionMonitor();

    }

    @Override
    protected void evolve() {

        List<T> offSpringGeneration = new ArrayList<T>(Properties.POPULATION);
        //TODO : check if we need to add elitism and add the best two parents first

        while(offSpringGeneration.size()<Properties.POPULATION) {

            T parent1 = this.selectionFunction.select(population);
            T parent2 = this.selectionFunction.select(population);
            T offspring1 = (T) parent1.clone();
            T offspring2 = (T) parent2.clone();

            try {
                // 1. make the crossover
                if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
                    crossoverFunction.crossOver(offspring1, offspring2);
                }

                //Jemtal Ibea only mutate the first one
                notifyMutation(offspring1);
                offspring1.mutate();

                if (offspring1.isChanged()) {
                    offspring1.updateAge(currentIteration);
                    //this.calculateFitness(offspring1);
                    offSpringGeneration.add(offspring1);
                }

            } catch (ConstructionFailedException e) {
                logger.info("CrossOver/Mutation failed.");
                continue;
            }
        }
        this.population.clear();
        this.population = offSpringGeneration;

        // calculate fitness for all test cases in the current (new) population
        calculateFitness();
    }

    private void removeWorst(List<T> archive) {

        // Find the worst;
        double worst      = archive.get(0).getFitness();
        int    worstIndex = 0;
        double kappa = 0.05;

        for (int i = 1; i < archive.size(); i++) {
            if (archive.get(i).getFitness() > worst) {
                worst = archive.get(i).getFitness();
                worstIndex = i;
            }
        }

        // Update the population
        for (int i = 0; i < archive.size(); i++) {
            if (i!=worstIndex) {
                double fitness = archive.get(i).getFitness();
                Double indicationValue = new Double(Math.exp((- indicatorValues_.get(worstIndex).get(i)/maxIndicatorValue_) / kappa));
                fitness -= indicationValue.isNaN()?0.0:indicationValue.doubleValue();

                archive.get(i).setFitness(this.getFitnessFunction(),fitness);
            }
        }

        // remove worst from the indicatorValues list
        indicatorValues_.remove(worstIndex); // Remove its own list
        Iterator<List<Double>> it = indicatorValues_.iterator();
        while (it.hasNext()){
            it.next().remove(worstIndex);

        }
        // remove the worst individual from the population
        archive.remove(worstIndex);
    } // removeWorst

    @Override
    public void initializePopulation() {
        logger.info("executing initializePopulation function");
        notifySearchStarted();

        currentIteration = 0;

        // Set up initial population
        //TODO : check adding to fitness function --> this.fitnessFunctions
        generateInitialPopulation(Properties.POPULATION);

        // Determine fitness
        calculateFitnessAndSortPopulation();

        this.notifyIteration();
    }

    @Override
    public void generateSolution() {

        if (population.isEmpty()){
            initializePopulation();
            this.archive = new ArrayList<>();
        }

        while (!isFinished()){
            // Create the union of parents and offSpring
            List<T> union = new ArrayList<T>();
            union.addAll(this.population);
            union.addAll(archive);
            this.archive = union;

            calculateFitness(archive);

            while (archive.size() > Properties.POPULATION) {
                removeWorst(archive);
            }

            evolve();
        }

//        this.getRankingFunction().computeRankingAssignment(this.archive);
//        this.getRankingFunction().getSubfront(0);
    }

    private void calculateFitness(List<T> archive) {
        //count the number of Goals to get the total number of goals

        updateFitnessFunctionsAndValues();

        //because of WTS
        int numberOfObjectives = 1;

        double [] maximumValues = new double[numberOfObjectives];
        double [] minimumValues = new double[numberOfObjectives];

        for (int i = 0; i < numberOfObjectives ; i++) {
            maximumValues[i] = - Double.MAX_VALUE; // i.e., the minus maximum value
            minimumValues[i] =   Double.MAX_VALUE; // i.e., the maximum value
        }

        for (int pos = 0; pos < archive.size(); pos++) {
            for (int obj = 0; obj < numberOfObjectives; obj++) {
                double value = archive.get(pos).getFitness();
                if (value > maximumValues[obj])
                    maximumValues[obj] = value;
                if (value < minimumValues[obj])
                    minimumValues[obj] = value;
            }
        }

        computeIndicatorValuesHD(archive,maximumValues,minimumValues);
        for (int pos =0; pos < archive.size(); pos++) {
            fitness(archive,pos);
        }
    }

    public void computeIndicatorValuesHD(List<T> archive,
                                         double [] maximumValues,
                                         double [] minimumValues) {
        T A, B;
        // Initialize the structures
        indicatorValues_ = new ArrayList<List<Double>>();
        maxIndicatorValue_ = - Double.MAX_VALUE;

        for (int j = 0; j < archive.size(); j++) {
            A =  (T)  archive.get(j).clone();


            List<Double> aux = new ArrayList<Double>();
            for (int i = 0; i < archive.size(); i++) {
                B =  (T)  archive.get(i).clone();

                int flag = (new DominanceComparator<T>()).compare(A, B);

                double value = 0.0;
                if (flag == -1) {
                    value = - calcHypervolumeIndicator(A, B, 1, maximumValues, minimumValues);
                } else if(flag == 1) { // Hadi : TODO : wgat if they are equal ( flag =0 )
                    value = calcHypervolumeIndicator(B, A, 1, maximumValues, minimumValues);
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

    public void fitness(List<T> archive ,int pos) {
        double fitness = 0.0;
        double kappa   = 0.05;

        for (int i = 0; i < archive.size(); i++) {
            if (i!=pos) {
                Double calculatedValue = new Double(Math.exp((-1 * indicatorValues_.get(i).get(pos)/maxIndicatorValue_) / kappa));
                fitness += (calculatedValue.isNaN())?0.0:calculatedValue;
            }
        }
        archive.get(pos).setFitness(this.getFitnessFunction(),fitness);
    }

    /**
     * calculates the hypervolume of that portion of the objective space that
     * is dominated by individual a but not by individual b
     */
    double calcHypervolumeIndicator(T p_ind_a,
                                    T p_ind_b,
                                    int d,
                                    double maximumValues [],
                                    double minimumValues []) {
        double a, b, r, max;
        double volume = 0;
        double rho = 2.0;

        r = rho * (maximumValues[d-1] - minimumValues[d-1]);
        max = minimumValues[d-1] + r;


        a = p_ind_a.getFitness();
        if (p_ind_b == null){
            b = max;
        } else {
            b = p_ind_b.getFitness();
        }


        if (d == 1)
        {
            if (a < b){
                volume = (b - a) / r;
            }
            else {
                volume = 0;
            }
        }
        else
        {
            if (a < b)
            {
                volume = calcHypervolumeIndicator(p_ind_a, null, d - 1, maximumValues, minimumValues) *
                        (b - a) / r;
                volume += calcHypervolumeIndicator(p_ind_a, p_ind_b, d - 1, maximumValues, minimumValues) *
                        (max - b) / r;
            }
            else
            {
                volume = calcHypervolumeIndicator(p_ind_a, p_ind_b, d - 1, maximumValues, minimumValues) *
                        (max - a) / r;
            }
        }

        return (volume);
    }


    public boolean isModificatedIBEA() {
        return isModificatedIBEA;
    }

    public void setModificatedIBEA(boolean modificatedIBEA) {
        isModificatedIBEA = modificatedIBEA;
    }
}

