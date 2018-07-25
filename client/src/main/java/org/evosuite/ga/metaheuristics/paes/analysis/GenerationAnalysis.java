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
    private int archiveSize;
    private RETURN_OPTION returnoption;
    private long milliSeconds;
    private double currentFitness;
    private double candidateFitness;
    private int generation;

    public GenerationAnalysis(boolean accepted, int generation, int archiveSize, RETURN_OPTION returnoption,
                              double currentFitness, double candidateFitness ,long milliSeconds){
        this.accepted = accepted;
        this.archiveSize = archiveSize;
        this.returnoption = returnoption;
        this.milliSeconds = milliSeconds;
        this.generation = generation;
        this.currentFitness = currentFitness;
        this.candidateFitness = candidateFitness;
    }

    public String toJSON(){
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"accepted\":"+accepted);
        jsonBuilder.append(", \"generation\":"+this.generation);
        jsonBuilder.append(", \"archive_size\":"+this.archiveSize);
        jsonBuilder.append(", \"ReturnOption\":\"" + this.returnoption+"\"");
        jsonBuilder.append(", \"currentFitness\":"+ this.currentFitness);
        jsonBuilder.append(", \"candidateFitness\":"+this.candidateFitness);
        jsonBuilder.append(", \"time\" :"+this.milliSeconds);
        jsonBuilder.append("}");

        return "{" + "\"accepted\":"+accepted+", \"generation\":"+this.generation+", \"archive_size\":"+this.archiveSize+
                ", \"ReturnOption\":\"" + this.returnoption +"\", \"currentFitness\":"+ this.currentFitness +
                ", \"candidateFitness\":"+this.candidateFitness +", \"time\" :"+this.milliSeconds+"}";
    }

    public boolean isAccepted() {
        return accepted;
    }

    public int getArchiveSize() { return archiveSize; }

    public RETURN_OPTION getReturnoption() {
        return returnoption;
    }

    public long getMilliSeconds(){ return milliSeconds; }
}
