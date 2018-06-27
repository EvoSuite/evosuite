package org.evosuite.ga.metaheuristics.paes.analysis;

import org.evosuite.ga.Chromosome;
import org.evosuite.statistics.RuntimeVariable;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by Sebastian on 06.06.2018.
 */
public class GenerationAnalysis<C extends Chromosome> {
    public enum RETURN_OPTION{
        CURRENT_DOMINATES_CANDIDATE,
        CANDIDATE_DOMINATES_CURRENT,
        CANDIDATE_DOMINATES_ARCHIVE,
        ARCHIVE_DOMINATES_CANDIDATE,
        ARCHIVE_DECIDES_CANDIDATE,
        ARCHIVE_DECIDES_CURRENT;

        public static RuntimeVariable getRuntimeVariableAvg(RETURN_OPTION option){
            switch (option){
                case CURRENT_DOMINATES_CANDIDATE:
                    return RuntimeVariable.AvgTimeCurrentDominatesCandidate;
                case CANDIDATE_DOMINATES_CURRENT:
                    return RuntimeVariable.AvgTimeCandidateDominatesCurrent;
                case CANDIDATE_DOMINATES_ARCHIVE:
                    return RuntimeVariable.AvgTimeCandidateDominatesArchive;
                case ARCHIVE_DOMINATES_CANDIDATE:
                    return RuntimeVariable.AvgTimeArchiveDominatesCandidate;
                case ARCHIVE_DECIDES_CANDIDATE:
                    return RuntimeVariable.AvgTimeArchiveDecidesCandidate;
                case ARCHIVE_DECIDES_CURRENT:
                    return RuntimeVariable.AvgTimeArchiveDecidesCurrent;
                default:
                    return null;
            }
        }

        public static RuntimeVariable getRuntimeVariableCount(RETURN_OPTION option){
            switch(option){
                case CURRENT_DOMINATES_CANDIDATE:
                    return RuntimeVariable.CountCurrentDominatesCandidate;
                case CANDIDATE_DOMINATES_CURRENT:
                    return RuntimeVariable.CountCandidateDominatesCurrent;
                case CANDIDATE_DOMINATES_ARCHIVE:
                    return RuntimeVariable.CountCandidateDominatesArchive;
                case ARCHIVE_DOMINATES_CANDIDATE:
                    return RuntimeVariable.CountArchiveDominatesCandidate;
                case ARCHIVE_DECIDES_CANDIDATE:
                    return RuntimeVariable.CountArchiveDecidesCandidate;
                case ARCHIVE_DECIDES_CURRENT:
                    return RuntimeVariable.CountArchiveDecidesCurrent;
                default:
                    return null;
            }
        }
    }

    private boolean accepted;
    private List<C> archivedChromosomes;
    private RETURN_OPTION returnoption;
    private long milliSeconds;

    public GenerationAnalysis(boolean accepted, List<C> archivedChromosomes, RETURN_OPTION returnoption, long milliSeconds){
        this.accepted = accepted;
        this.archivedChromosomes = new ArrayList<C>();
        this.archivedChromosomes.addAll(archivedChromosomes);
        this.returnoption = returnoption;
        this.milliSeconds = milliSeconds;
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

    public long getMilliSeconds(){ return milliSeconds; }
}
