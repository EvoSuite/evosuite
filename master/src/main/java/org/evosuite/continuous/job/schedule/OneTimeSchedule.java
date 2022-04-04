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
import org.evosuite.utils.LoggingUtils;

import java.util.List;

/**
 * A schedule that can only be called once.
 * In other words, the entire schedule is calculated
 *
 * @author arcuri
 */
public abstract class OneTimeSchedule extends ScheduleType {

    private boolean called = false;

    public OneTimeSchedule(JobScheduler scheduler) {
        super(scheduler);
    }

    @Override
    public final boolean canExecuteMore() {
        return !called;
    }

    @Override
    public final List<JobDefinition> createNewSchedule() {
        if (called) {
            throw new IllegalStateException("Schedule has already been created");
        }

        called = true;

        if (!enoughBudgetForAll()) {
            LoggingUtils.getEvoLogger().info("There is no enough time budget to generate test cases for all classes in the project");
            return createScheduleForWhenNotEnoughBudget();
        }

        return createScheduleOnce();
    }

    protected abstract List<JobDefinition> createScheduleOnce();

    protected void distributeExtraBudgetEvenly(List<JobDefinition> jobs,
                                               int totalLeftOver, int maximumBudgetPerCore) {

        int counter = 0;
        for (JobDefinition job : jobs) {
            assert job.seconds <= maximumBudgetPerCore;
            if (job.seconds < maximumBudgetPerCore) {
                counter++;
            }
        }

        if (totalLeftOver < counter || counter == 0) {
            /*
             * no point in adding complex code to handle so little budget left.
             * here we lost at most only one second per job...
             *
             *  furthermore, it could happen that budget is left, but
             *  all jobs have maximum. this happens if there are more
             *  cores than CUTs
             */
            return;
        }

        int extraPerJob = totalLeftOver / counter;

        for (int i = 0; i < jobs.size(); i++) {
            JobDefinition job = jobs.get(i);

            int toAdd = Math.min(extraPerJob, (maximumBudgetPerCore - job.seconds));

            if (toAdd > 0) {
                totalLeftOver -= toAdd;
                jobs.set(i, job.getByAddingBudget(toAdd));
            }
        }

        if (totalLeftOver > 0 && totalLeftOver >= counter) {
            //recursion
            distributeExtraBudgetEvenly(jobs, totalLeftOver, maximumBudgetPerCore);
        }
    }
}
