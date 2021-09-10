/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.continuous.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Definition of a "job", ie a run of EvoSuite on a CUT.
 *
 * <p>
 * Note: this class is/should be immutable
 *
 * @author arcuri
 */
public class JobDefinition {

    private static final Logger logger = LoggerFactory.getLogger(JobDefinition.class);

    /**
     * counter used to create unique ids in a thread-safe manner
     */
    private static final AtomicInteger counter = new AtomicInteger(0);

    /**
     * A unique, human-readable identifier for this job
     */
    public final int jobID;

    /**
     * define for how long this job should be run
     */
    public final int seconds;

    /**
     * define how much memory this job can allocate
     */
    public final int memoryInMB;

    /**
     * full qualifying name of the class under test (CUT)
     */
    public final String cut;

    /**
     * the configuration id, identify which parameter settings were used
     */
    public final int configurationId;

    /**
     * the name of all classes this CUT depends on, and that would be good to
     * have generated test cases before starting this job. This is a union of
     * all the the types of dependency (eg, input and parent)
     */
    public final Set<String> dependentOnClasses;

    /**
     * All dependent classes used as input for this CUT
     */
    public final Set<String> inputClasses;

    /**
     * All dependent classes in the parent hierarchy
     */
    public final Set<String> parentClasses;

    /**
     * Main constructor
     *
     * @param seconds
     * @param memoryInMB
     * @param cut
     * @param configurationId
     */
    public JobDefinition(int seconds, int memoryInMB, String cut, int configurationId,
                         Set<String> inputDependencies, Set<String> parentDependencies) {
        super();
        this.jobID = counter.getAndIncrement();
        this.seconds = seconds;
        this.memoryInMB = memoryInMB;
        this.cut = cut;
        this.configurationId = configurationId;

        HashSet<String> union = new HashSet<>();

        if (inputDependencies != null && inputDependencies.size() > 0) {
            this.inputClasses = Collections.unmodifiableSet(new HashSet<>(
                    inputDependencies));
            union.addAll(inputClasses);
        } else {
            this.inputClasses = null;
        }

        if (parentDependencies != null && parentDependencies.size() > 0) {
            this.parentClasses = Collections.unmodifiableSet(new HashSet<>(
                    parentDependencies));
            union.addAll(parentClasses);
        } else {
            this.parentClasses = null;
        }

        if (union.size() == 0) {
            this.dependentOnClasses = null;
        } else {
            this.dependentOnClasses = Collections.unmodifiableSet(new HashSet<>(
                    union));
        }
    }

    /**
     * Create a copy of this job, and add the input and parent dependencies to
     * the set of CUT dependencies
     *
     * <p>
     * It is OK to have one of the sets null, but not both
     *
     * @param input
     * @return
     */
    public JobDefinition getByAddingDependencies(Set<String> inputs, Set<String> parents)
            throws IllegalArgumentException {

        if (inputs == null && parents == null) {
            throw new IllegalArgumentException("Both sets are null");
        }

        if (inputs != null && inputs.contains(cut)) {
            throw new IllegalArgumentException("'inputs' contains reference to this job");
        }

        if (parents != null && parents.contains(cut)) {
            throw new IllegalArgumentException("'parents' contains reference to this job");
        }

        if (inputClasses != null) {

            logger.debug("Adding " + inputClasses.size() + "input dependecies in job " + jobID);

            if (inputs == null) {
                inputs = inputClasses;
            } else {
                inputs.addAll(inputClasses);
            }
        }
        if (parentClasses != null) {
            if (parents == null) {
                parents = parentClasses;
            } else {
                parents.addAll(parentClasses);
            }
        }

        return new JobDefinition(seconds, memoryInMB, cut, configurationId, inputs,
                parents);
    }

    /**
     * Create a copy of this job by adding extra seconds
     *
     * @param input
     * @return
     */
    public JobDefinition getByAddingBudget(int moreSeconds)
            throws IllegalArgumentException {

        if (moreSeconds <= 0) {
            throw new IllegalArgumentException("Invalid extra seconds: " + moreSeconds);
        }

        return new JobDefinition(seconds + moreSeconds, memoryInMB, cut,
                configurationId, inputClasses, parentClasses);
    }


    /**
     * Does the execution of this job depend on the other?
     *
     * @param other
     * @return
     */
    public boolean dependOn(JobDefinition other) {
        return dependentOnClasses != null && dependentOnClasses.contains(other.cut);
    }

    /**
     * The number of classes this job depends on and
     * should be executed before this job
     *
     * @return
     */
    public int getNumberOfDependencies() {
        if (dependentOnClasses == null) {
            return 0;
        } else {
            return dependentOnClasses.size();
        }
    }

    /**
     * Check if all jobs this one depends on are finished
     *
     * @param job
     * @return
     */
    public boolean areDependenciesSatisfied(List<JobDefinition> schedule, Set<String> done) {

        if (dependentOnClasses == null) {
            return true; // no dependencies to satisfy
        }

        for (String name : dependentOnClasses) {
            /*
             * It could happen that a schedule is not complete, in the sense that
             * we do not create jobs for each single CUT in the project.
             * If A depends on B, but we have no job for B, then no point in postponing
             * a job for A
             */
            if (!inTheSchedule(schedule, name)) {
                continue;
            }
            if (!done.contains(name)) {
                return false;
            }
        }
        return true;
    }

    private boolean inTheSchedule(List<JobDefinition> jobs, String cut) {
        for (JobDefinition job : jobs) {
            if (job.cut.equals(cut)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + configurationId;
        result = prime * result + ((cut == null) ? 0 : cut.hashCode());
        result = prime * result
                + ((dependentOnClasses == null) ? 0 : dependentOnClasses.hashCode());
        result = prime * result + jobID;
        result = prime * result + memoryInMB;
        result = prime * result + seconds;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JobDefinition other = (JobDefinition) obj;
        if (configurationId != other.configurationId)
            return false;
        if (cut == null) {
            if (other.cut != null)
                return false;
        } else if (!cut.equals(other.cut))
            return false;
        if (dependentOnClasses == null) {
            if (other.dependentOnClasses != null)
                return false;
        } else if (!dependentOnClasses.equals(other.dependentOnClasses))
            return false;
        if (jobID != other.jobID)
            return false;
        if (memoryInMB != other.memoryInMB)
            return false;
        return seconds == other.seconds;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "job " + jobID + ", target " + cut +
                ", number of dependencies " + getNumberOfDependencies();
    }

}
