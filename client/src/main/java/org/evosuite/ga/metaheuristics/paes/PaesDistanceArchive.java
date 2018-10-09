package org.evosuite.ga.metaheuristics.paes;

import org.evosuite.ga.archive.Archive;
import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.operators.ranking.CrowdingDistance;
import org.evosuite.testcase.TestFitnessFunction;

import java.util.*;

/**
 *
 *
 * Created by Sebastian on 04.07.2018.
 */
public class PaesDistanceArchive<C extends Chromosome> implements PaesArchiveInterface<C> {
    private List<C> archivedChromosomes = new ArrayList<C>();
    private CrowdingDistance<C> distance = new CrowdingDistance<C>();
    private Set<FitnessFunction<C>> fitnessFunctions;
    private static boolean FAVOUR_CANDIDATE = Properties.FAVOUR_CANDIDATE;
    private static int MAX_SIZE = Properties.POPULATION;

    /**
     * Constructor for a PAES archive using the distance crowding measure to decide about
     * PAES acceptance
     *
     * @param fitnessFunctions
     */
    public PaesDistanceArchive(Set<FitnessFunction<C>> fitnessFunctions){
        this.fitnessFunctions = fitnessFunctions;
    }

    public PaesDistanceArchive(List<FitnessFunction<C>> fitnessFunctions){
        this.fitnessFunctions = new HashSet<>(fitnessFunctions);
    }

    /**
     * {@InheritDoc}
     */
    @Override
    public boolean add(C c) {

        List<FitnessFunction<?>> ffs = new ArrayList<>(fitnessFunctions);
        for(C c1 : archivedChromosomes)
             if(c1.dominates(c, ffs))
                return false;
        this.removeDominated(c);
        if(archivedChromosomes.size() >= MAX_SIZE){
            List<C> withC = new ArrayList<>(archivedChromosomes);
            withC.add(c);
            this.distance.crowdingDistanceAssignment(withC,new ArrayList<FitnessFunction<C>>(this.fitnessFunctions));
            C min = withC.get(0);
            for(C c1 : withC){
                if(c1.getDistance() < min.getDistance())
                    min = c1;
            }
            withC.remove(min);
            this.archivedChromosomes = withC;
            return min != c;
        }else {
            archivedChromosomes.add(c);
            return true;
        }
    }


    /**
     * {@InheritDoc}
     */
    @Override
    public List<C> getChromosomes() {
        return archivedChromosomes;
    }

    /**
     * {@InheritDoc}
     */
    @Override
    public boolean decide(C candidate, C current) {
        List<FitnessFunction<C>> uncovered = new ArrayList<>();
        Set<TestFitnessFunction> uncoveredTargets = Archive.getArchiveInstance().getUncoveredTargets();
        for(TestFitnessFunction tff: uncoveredTargets)
            uncovered.add((FitnessFunction<C>)tff);
        List<C> withCandidateCurrent = new ArrayList<>(archivedChromosomes);
        withCandidateCurrent.add(candidate);
        withCandidateCurrent.add(current);
        this.distance.crowdingDistanceAssignment(withCandidateCurrent, uncovered);
        if(candidate.getDistance() == current.getDistance())
            return FAVOUR_CANDIDATE;
        return candidate.getDistance() > current.getDistance();
    }

    /**
     * Decides between candidate and current for uncovered targets.
     * Does not check for Pareto Optionals
     *
     * @param candidate candidate chromosome for acceptance decision
     * @param current current chromosome for acceptance decision
     * @param uncovered remaining uncovered targets for the decision
     * @return
     */
    public boolean decide(C candidate, C current, List<FitnessFunction<C>> uncovered) {
        List<C> withCandidateCurrent = new ArrayList<>(archivedChromosomes);
        withCandidateCurrent.add(candidate);
        withCandidateCurrent.add(current);
        this.distance.crowdingDistanceAssignment(withCandidateCurrent, uncovered);
        if(candidate.getDistance() == current.getDistance())
            return FAVOUR_CANDIDATE;
        return candidate.getDistance() > current.getDistance();
    }

    /**
     * {@InheritDoc}
     */
    @Override
    public void removeDominated(C c) {
        List<FitnessFunction<?>> ffs = new ArrayList<>(this.fitnessFunctions);
        List<C> dominated = new ArrayList<C>();
        for(C c1 : archivedChromosomes)
            if(c.dominates(c1, ffs))
                dominated.add(c1);
        this.archivedChromosomes.removeAll(dominated);
    }

    /**
     * {@InheritDoc}
     */
    @Override
    public void updateFitnessFunctions(Set<FitnessFunction<C>> fitnessFunctions) {
        this.fitnessFunctions = fitnessFunctions;
    }
}
