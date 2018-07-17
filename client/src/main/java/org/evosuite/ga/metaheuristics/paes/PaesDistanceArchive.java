package org.evosuite.ga.metaheuristics.paes;

import org.evosuite.ga.archive.Archive;
import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.paes.Grid.DummyChromosome;
import org.evosuite.ga.metaheuristics.paes.Grid.DummyFitnessFunction;
import org.evosuite.ga.metaheuristics.paes.Grid.GridNode;
import org.evosuite.ga.operators.ranking.CrowdingDistance;
import org.evosuite.testcase.TestFitnessFunction;

import java.util.*;

/**
 * Created by Sebastian on 04.07.2018.
 */
public class PaesDistanceArchive<C extends Chromosome> implements PaesArchiveInterface<C> {
    private List<C> archivedChromosomes = new ArrayList<C>();
    private CrowdingDistance<C> distance = new CrowdingDistance<C>();
    private List<FitnessFunction<C>> fitnessFunctions;
    private static boolean FAVOUR_CANDIDATE = Properties.FAVOUR_CANDIDATE;
    private static int MAX_SIZE = Properties.POPULATION;

    public PaesDistanceArchive(List<FitnessFunction<C>> fitnessFunctions){
        this.fitnessFunctions = fitnessFunctions;
    }

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
            this.distance.crowdingDistanceAssignment(withC,this.fitnessFunctions);
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

    @Override
    public List<C> getChromosomes() {
        return archivedChromosomes;
    }

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

    public boolean decide(C candidate, C current, List<FitnessFunction<C>> uncovered) {
        List<C> withCandidateCurrent = new ArrayList<>(archivedChromosomes);
        withCandidateCurrent.add(candidate);
        withCandidateCurrent.add(current);
        this.distance.crowdingDistanceAssignment(withCandidateCurrent, uncovered);
        if(candidate.getDistance() == current.getDistance())
            return FAVOUR_CANDIDATE;
        return candidate.getDistance() > current.getDistance();
    }

    @Override
    public void removeDominated(C c) {
        List<FitnessFunction<?>> ffs = new ArrayList<>(this.fitnessFunctions);
        List<C> dominated = new ArrayList<C>();
        for(C c1 : archivedChromosomes)
            if(c.dominates(c1, ffs))
                dominated.add(c1);
        this.archivedChromosomes.removeAll(dominated);
    }

    @Override
    public void updateFitnessFunctions(Set<FitnessFunction<?>> fitnessFunctions) {
    }

    public static void main(String[] args){
        DummyFitnessFunction ff1 = new DummyFitnessFunction();
        DummyFitnessFunction ff2 = new DummyFitnessFunction();
        DummyFitnessFunction ff3 = new DummyFitnessFunction();
        Map<FitnessFunction<?>,Double> upperBounds = new LinkedHashMap<>();
        Map<FitnessFunction<?>,Double> lowerBounds = new LinkedHashMap<>();
        upperBounds.put(ff1,1.0);
        upperBounds.put(ff2,1.0);
        upperBounds.put(ff3,1.0);
        lowerBounds.put(ff1,0.0);
        lowerBounds.put(ff2,0.0);
        lowerBounds.put(ff3,0.0);
        DummyChromosome c1 = new DummyChromosome();
        DummyChromosome c2 = new DummyChromosome();
        DummyChromosome c3 = new DummyChromosome();
        DummyChromosome current = new DummyChromosome();
        DummyChromosome candidate = new DummyChromosome();
        c1.setFitness(ff1, 1);
        c1.setFitness(ff2, 0);
        c1.setFitness(ff3, 1);
        c2.setFitness(ff1, 0);
        c2.setFitness(ff2, 1);
        c2.setFitness(ff3, 0);
        current.setFitness(ff1, 0.5);
        current.setFitness(ff2, 0.4);
        current.setFitness(ff3, 0.3);
        candidate.setFitness(ff1,0.55);
        candidate.setFitness(ff2, 0.35);
        candidate.setFitness(ff3, 0.3);
        List<FitnessFunction<DummyChromosome>> ffs = new ArrayList<>();
        ffs.add(ff1);
        ffs.add(ff2);
        ffs.add(ff3);
        PaesDistanceArchive<DummyChromosome> archive = new PaesDistanceArchive<DummyChromosome>(ffs);
        archive.add(c1);
        archive.add(c2);
        System.out.println(archive.decide(current, candidate, ffs));
    }
}
