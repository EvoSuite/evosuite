package org.evosuite.ga.metaheuristics.paes.analysis;

import org.evosuite.ga.Chromosome;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by Sebastian on 06.06.2018.
 */
public class GenerationAnalysis<C extends Chromosome> {
    public static enum RETURN_OPTION{
        CURRENT_DOMINATES_CANDIDATE,
        CANDIDATE_DOMINATES_CURRENT,
        CANDIDATE_DOMINATES_ARCHIVE,
        ARCHIVE_DOMINATES_CANDIDATE,
        ARCHIVE_DECIDES_CANDIDATE,
        ARCHIVE_DECIDES_CURRENT
    }

    private boolean accepted;
    private List<C> archivedChromosomes;
    private RETURN_OPTION returnoption;

    public GenerationAnalysis(boolean accepted, List<C> archivedChromosomes, RETURN_OPTION returnoption){
        this.accepted = accepted;
        this.archivedChromosomes = new ArrayList<C>();
        this.archivedChromosomes.addAll(archivedChromosomes);
        this.returnoption = returnoption;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public List<C> getArchivedChromosomes() {
        return archivedChromosomes;
    }

    public RETURN_OPTION getReturnoption() {
        return returnoption;
    }
}
