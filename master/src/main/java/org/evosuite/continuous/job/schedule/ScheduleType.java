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
package org.evosuite.continuous.job.schedule;

import org.evosuite.continuous.job.JobDefinition;
import org.evosuite.continuous.job.JobScheduler;
import org.evosuite.continuous.project.ProjectStaticData;
import org.evosuite.continuous.project.ProjectStaticData.ClassInfo;

import java.util.LinkedList;
import java.util.List;

/**
 * Root class for the different kinds of schedule.
 *
 * <p>
 * TODO: memory based on number of branches, as we can/should use larger population size, longer
 * test cases, larger test suites, etc. These latter are an initial proof-of-concept of a more
 * general optimized tuning.
 *
 * <p>
 * TODO: handle schedule that is specific for the SVN/Git changes (ie only recently modified classes used for search).
 * Would be interesting to analyze how often there are commits (how many classes are modified) in open source / industrial projects.
 * If in last commit there were added/modified a set X of classes, not only we might
 * want to focus on those, but also on the others that take them as input.
 * Furthermore, we can look at the history: if CTG has been run for weeks/months,
 * and then new classes are added, those should have much higher priority than old,
 * heavily tested classes
 *
 * <p>
 * TODO: we should also consider what version of EvoSuite has been used.
 * If a new version of EvoSuite is introduced during CTG, it would make sense
 * to re-generate test cases for all CUTs, as new version might achieve higher
 * coverage
 *
 * @author arcuri
 */
public abstract class ScheduleType {

    protected final JobScheduler scheduler;

    protected ScheduleType(JobScheduler scheduler) {
        this.scheduler = scheduler;
    }


    protected boolean enoughBudgetForAll() {
        int totalBudget = 60 * scheduler.getConfiguration().timeInMinutes * scheduler.getConfiguration().getNumberOfUsableCores();
        int maximumNumberOfJobs = totalBudget / (60 * scheduler.getConfiguration().minMinutesPerJob);
        return maximumNumberOfJobs >= scheduler.getProjectData().getTotalNumberOfTestableCUTs();
    }

    /**
     * Create a new partial/complete schedule if there is still search budget left
     *
     * @return
     * @throws IllegalStateException
     */
    public abstract List<JobDefinition> createNewSchedule() throws IllegalStateException;

    /**
     * <p>
     * When we get a schedule, the scheduler might decide to do not use the entire
     * budget. Reason? It might decide to generate some test cases first, and then
     * use those as seeding for a new round of execution.
     * </p>
     *
     * <p>
     * Once the budget is finished, this schedule cannot be reused. A new
     * instance needs to be created.
     * </p>
     *
     * @return
     */
    public abstract boolean canExecuteMore();

    /**
     * if there is not enough search budget, then try
     * to target as many CUTs as possible
     *
     * @return
     */
    protected List<JobDefinition> createScheduleForWhenNotEnoughBudget() {

        ProjectStaticData data = scheduler.getProjectData();
        int totalBudget = 60 * scheduler.getConfiguration().timeInMinutes * scheduler.getConfiguration().getNumberOfUsableCores();

        List<JobDefinition> jobs = new LinkedList<>();

        /*
         * TODO: when starting schedule, check how many classes we do already have test cases from previous runs.
         * If from previous run we have some classes, then prioritize the others
         */

        //not enough budget
        for (ClassInfo info : data.getClassInfos()) {
            if (!info.isTestable()) {
                continue;
            }
            JobDefinition job = new JobDefinition(
                    60 * scheduler.getConfiguration().minMinutesPerJob, scheduler.getConfiguration().getConstantMemoryPerJob(),
                    info.getClassName(), 0, null, null);
            jobs.add(job);

            totalBudget -= (60 * scheduler.getConfiguration().minMinutesPerJob);

            if (totalBudget <= 0) {
                break;
            }
        }
        return jobs;
    }
}
