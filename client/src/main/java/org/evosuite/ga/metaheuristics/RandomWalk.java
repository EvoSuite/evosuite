package org.evosuite.ga.metaheuristics;

import org.apache.commons.lang3.tuple.Pair;
import org.evosuite.Properties;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.ga.stoppingconditions.ZeroFitnessStoppingCondition;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class RandomWalk<T extends Chromosome<T>> extends GeneticAlgorithm<T> {
    private static final Logger logger = LoggerFactory.getLogger(RandomSearch.class);
    private final Map<Pair<FitnessFunction<?>, Integer>, Double> fitnessValueCache = new HashMap<>();

    private T theChromosome;

    /**
     * <p>
     * Constructor for RandomSearch.
     * </p>
     *
     * @param factory a {@link org.evosuite.ga.ChromosomeFactory} object.
     */
    public RandomWalk(ChromosomeFactory<T> factory) {
        super(factory);
    }

    private static final long serialVersionUID = -7685015421245920459L;

    /* (non-Javadoc)
     * @see org.evosuite.ga.GeneticAlgorithm#evolve()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    protected void evolve() {
        T newChromosome = theChromosome;
        newChromosome.mutate();
        getFitnessFunctions().forEach(f -> fitnessValueCache.put(Pair.of(f, currentIteration),
                f.getFitness(newChromosome)));
        currentIteration++;
    }

    @Override
    public T getBestIndividual() {
        return (T) new TestSuiteChromosome();
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.GeneticAlgorithm#initializePopulation()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializePopulation() {
        theChromosome = chromosomeFactory.getChromosome();
        T newChromosome = theChromosome;
        System.out.print("Computing fitness values\n");
        getFitnessFunctions().forEach(f -> fitnessValueCache.put(Pair.of(f, currentIteration),
                f.getFitness(newChromosome)));
        TestSuiteChromosome suite = new TestSuiteChromosome();
        suite.addTest((TestChromosome) newChromosome);
        notifyEvaluation((T) suite);
        population = Collections.emptyList();
        // calculateFitnessAndSortPopulation();
    }

    private double computeAC(List<Double> landscape) {
        int step = Properties.AC_STEP;
        double numerator = 0;
        double denominator = 0;
        double average = landscape.stream().mapToDouble(d -> d).average().orElse(0);
        for (int i = 0; i < landscape.size(); ++i) {
            if (i + step < landscape.size())
                numerator += (landscape.get(i) - average) * (landscape.get(i + step) - average);
            double cur = landscape.get(i) - average;
            denominator += cur * cur;
        }
        if (denominator == 0) return 1;
        return numerator / denominator;
    }

    private int computeND(List<Double> landscape) {
        Double fitness0 = landscape.get(0);
        for (int i = 1; i < landscape.size(); i++) {
            if (!landscape.get(i).equals(fitness0)) {
                return i;
            }
        }
        return landscape.size();
    }

    private Pair<Integer, List<Integer>> computeNV(List<Double> landscape) {
        int result = 0;
        for (int i = 1; i < landscape.size(); i++) {
            if (!landscape.get(i).equals(landscape.get(i - 1))) result++;
        }
        double epsilon = 0.001;
        List<Double> transformed = new ArrayList<>(landscape.size() - 1);
        for (int i = 1; i < landscape.size(); i++) {
            transformed.add(landscape.get(i) - landscape.get(i - 1));
        }
        return Pair.of(result, transformed.stream().map(d -> {
            if (d < -epsilon) return -1;
            else if (d > epsilon) return 1;
            else return 0;
        }).collect(Collectors.toList()));
    }

    private double computeIC(List<Integer> nvLandscape) {
        List<Pair<Integer, Integer>> tuples = new ArrayList<>(nvLandscape.size() - 1);
        for (int i = 0; i < nvLandscape.size() - 1; i++) {
            tuples.add(Pair.of(nvLandscape.get(i), nvLandscape.get(i + 1)));
        }
        double sum = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i != j) {
                    Pair<Integer, Integer> of = Pair.of(i, j);
                    long count = tuples.stream().filter(p -> p.equals(of)).count();
                    if (count > 0) {
                        double freq = (double) count / tuples.size();
                        double log = Math.log(freq) / Math.log(6);
                        sum += freq * log;
                        if (Double.isNaN(sum)) throw new IllegalStateException("IC is NaN now " + freq + " " + log);
                    }
                }
            }
        }
        return -sum;
    }

    private double computePIC(List<Integer> nvLandscape) {
        int mu = (int) nvLandscape.stream().filter(d -> d != 0).count();
        return ((double) mu) / nvLandscape.size();
    }

    private double computeDBI(List<Integer> nvLandscape) {
        List<Integer> collect = nvLandscape.stream().filter(i -> i != 0).collect(Collectors.toList());

        List<Pair<Integer, Integer>> tuples = new ArrayList<>();
        for (int i = 0; i < collect.size() - 1; i++) {
            tuples.add(Pair.of(collect.get(i), collect.get(i + 1)));
        }
        double sum = 0;
        for (int i = -1; i <= 1; i++) {
            int j = i;
            Pair<Integer, Integer> of = Pair.of(i, j);
            long count = tuples.stream().filter(p -> p.equals(of)).count();
            if (count > 0) {
                double freq = (double) count / tuples.size();
                double log = Math.log(freq) / Math.log(3);
                sum += freq * log;
            }
        }
        return -sum;
    }

    private void computeFitnessLandscapeMeasurements() throws IOException {
        List<BranchCoverageTestFitness> orderInBitString = new BranchCoverageFactory().getCoverageGoals();
        List<BranchCoverageTestFitness> orderInBitStringSorted = new ArrayList<>(orderInBitString);
        Collections.sort(orderInBitStringSorted);

        List<FitnessFunction<T>> fitnessFunctions = (List<FitnessFunction<T>>) getFitnessFunctions();

        String pathname = Properties.REPORT_DIR + File.separator + "fitnessMeasurements.csv";
        boolean exists = new File(pathname).exists();
        List<String> csv = new ArrayList<>(fitnessFunctions.size() + 1);
        if (!exists)
            csv.add("Id,TARGET_CLASS,AC,ND,NV,IC,PIC,DBI,Size,TT,HashCode,Random_Seed,BitStringOffset," +
                    "BitStringOffset2");
        int id = 0;
        for (FitnessFunction<T> fitnessFunction : fitnessFunctions) {
            List<Double> landscape = new ArrayList<>();
            for (int i = 0; fitnessValueCache.containsKey(Pair.of(fitnessFunction, i)); ++i) {
                landscape.add(fitnessValueCache.get(Pair.of(fitnessFunction, i)));
            }
            double AC = computeAC(landscape);
            int nd = computeND(landscape);
            Pair<Integer, List<Integer>> nvResult = computeNV(landscape);
            int nv = nvResult.getLeft();
            List<Integer> nvLandscape = nvResult.getRight();
            if (nvLandscape.size() == 0) {
                logger.warn(String.format("%d: zero size nvLandscape, actual landscape = %s ", id,
                        landscape.toString()));
            }
            double ic = computeIC(nvLandscape);
            double pic = computePIC(nvLandscape);
            double dbi = computeDBI(nvLandscape);
            csv.add(String.format("%d,%s,%.5f,%d,%d,%.5f,%.5f,%.5f,%d,%s,%d,%d,%d,%d",
                    id++,
                    Properties.TARGET_CLASS,
                    AC,
                    nd,
                    nv,
                    ic,
                    pic,
                    dbi,
                    landscape.size(),
                    Properties.TT,
                    fitnessFunction.hashCode(),
                    Properties.RANDOM_SEED,
                    orderInBitString.contains(fitnessFunction) ? orderInBitString.indexOf(fitnessFunction) : -1,
                    orderInBitStringSorted.contains(fitnessFunction) ? orderInBitStringSorted.indexOf(fitnessFunction) : -1));
        }
        try (FileWriter writer = new FileWriter(pathname, true)) {
            if (exists) writer.write("\n");
            writer.write(String.join("\n", csv));
        }
    }

    void writeBranchInfo() throws IOException {
        String pathname = Properties.REPORT_DIR + File.separator + "BranchInfo.csv";
        boolean exists = new File(pathname).exists();
        List<String> csv = new ArrayList<>(fitnessFunctions.size() + 1);
        if (!exists)
            csv.add("ClassName,MethodName,Index,InstructionId,ActualBranchId,LineNumber,InsturctionType,isSwitch," +
                    "targetCaseValue");
        for (int i = 0; i < fitnessFunctions.size(); i++) {
            FitnessFunction<T> fitnessFunction = fitnessFunctions.get(i);
            if (fitnessFunction instanceof BranchCoverageTestFitness) {
                Branch branch = ((BranchCoverageTestFitness) fitnessFunction).getBranch();
                BranchCoverageGoal branchGoal = ((BranchCoverageTestFitness) fitnessFunction).getBranchGoal();
                int instructionId;
                int actualBranchId;
                String instructionType;
                Integer targetCaseValue;
                boolean isSwitch;
                if (branch != null) {
                    BytecodeInstruction instruction = branch.getInstruction();
                    instructionId = instruction.getInstructionId();
                    actualBranchId = branch.getActualBranchId();
                    instructionType = instruction.getInstructionType();
                    isSwitch = branch.isSwitchCaseBranch();
                    if (isSwitch) {
                        Integer targetCaseValue1 = branch.getTargetCaseValue();
                        targetCaseValue = targetCaseValue1 != null ? targetCaseValue1 : -1;
                    } else {
                        targetCaseValue = -1;
                    }
                } else {
                    instructionId = -1;
                    actualBranchId = -1;
                    instructionType = "METHODENTER";
                    isSwitch = false;
                    targetCaseValue = -1;
                }
                int lineNumber = branchGoal.getLineNumber();

                csv.add(String.format("%s,%s,%d,%d,%d,%d,%s,%s,%d", branchGoal.getClassName(),
                        branchGoal.getMethodName(), i, instructionId, actualBranchId, lineNumber, instructionType,
                        isSwitch, targetCaseValue));
            }
        }
        try (FileWriter writer = new FileWriter(pathname, true)) {
            if (exists) writer.write("\n");
            writer.write(String.join("\n", csv));
        }
    }



    /* (non-Javadoc)
     * @see org.evosuite.ga.GeneticAlgorithm#generateSolution()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateSolution() {
        notifySearchStarted();
        if (population.isEmpty()) initializePopulation();

        currentIteration = 0;
        while (!isFinished() && currentIteration < Properties.SEARCH_BUDGET) {
            evolve();
            notifyIteration();
        }
        // updateBestIndividualFromArchive();
        try {
            computeFitnessLandscapeMeasurements();
            writeBranchInfo();
        } catch (IOException e) {
            logger.warn("Could not write measurement String");
        }
        // notifySearchFinished();
    }

    @Override
    public boolean isFinished() {
        return stoppingConditions.stream().filter(s -> !(s instanceof ZeroFitnessStoppingCondition)).allMatch(StoppingCondition::isFinished);
    }
}
