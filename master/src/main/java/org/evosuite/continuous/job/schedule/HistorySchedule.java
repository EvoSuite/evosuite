/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.continuous.job.schedule;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.evosuite.continuous.job.JobDefinition;
import org.evosuite.continuous.job.JobScheduler;
import org.evosuite.continuous.project.ProjectStaticData;
import org.evosuite.continuous.project.ProjectStaticData.ClassInfo;
import org.evosuite.utils.LoggingUtils;

/**
 * <p>
 * HistorySchedule class.
 * </p>
 * 
 * @author José Campos
 */
public class HistorySchedule
    extends OneTimeSchedule
{
    private static double NEW_CLASS = 2.0;

    private static double OLD_CLASS = 1.0;

    public static final int COMMIT_IMPROVEMENT = 3;

    public HistorySchedule(JobScheduler scheduler)
    {
        super(scheduler);
    }

    @Override
    protected List<JobDefinition> createScheduleOnce()
    {
        ProjectStaticData data = this.scheduler.getProjectData();

        int maximumBudgetPerCore = 60 * this.scheduler.getConfiguration().timeInMinutes;

        // the total budget we need to choose how to allocate
        int totalBudget = maximumBudgetPerCore * this.scheduler.getConfiguration().getNumberOfUsableCores();
        LoggingUtils.getEvoLogger().info("totalBudget: " + totalBudget);

        // a part of the budget is fixed, as each CUT needs a minimum of it
        int minTime = 60 * this.scheduler.getConfiguration().minMinutesPerJob * data.getTotalNumberOfTestableCUTs();

        // this is what left from the minimum allocation, and that now we can
        // choose how best to allocate
        int extraTime = totalBudget - minTime;
        LoggingUtils.getEvoLogger().info("extraTime: " + extraTime);

        // check how much time we can give extra for each branch in a CUT
        double timePerBranch = (double) extraTime / (double) data.getTotalNumberOfBranches();

        // left
        int totalLeftOver = 0;

        List<JobDefinition> jobs = new LinkedList<JobDefinition>();
        for (ClassInfo c_info : data.getClassInfos())
        {
            if (!c_info.isTestable())
                continue;

            double budget =
                60.0 * scheduler.getConfiguration().minMinutesPerJob + (c_info.numberOfBranches * timePerBranch);

            if (c_info.hasChanged())
            {
                budget *= HistorySchedule.NEW_CLASS;
                LoggingUtils.getEvoLogger().info("[M] " + c_info.getClassName()); // M - modified
            }
            else
            {
                if (c_info.isToTest())
                {
                	budget *= HistorySchedule.OLD_CLASS;
                    LoggingUtils.getEvoLogger().info("[O] " + c_info.getClassName()); // O - original/old
                }
                else
                {
                	LoggingUtils.getEvoLogger().info("[NI] " + c_info.getClassName()); // NI - not improved
                    continue;
                }
            }

            if (budget > maximumBudgetPerCore)
            {
                /*
                 * Need to guarantee that no job has more than maximumBudgetPerCore regardless of number of cores
                 */
                totalLeftOver += (budget - maximumBudgetPerCore);
                budget = maximumBudgetPerCore;
            }

            jobs.add(new JobDefinition((int) budget, this.scheduler.getConfiguration().getConstantMemoryPerJob(),
                                       c_info.getClassName(), 0, null, null));
        }

        /*
         * we still have some more budget to allocate
         */
        if (totalLeftOver > 0) {
            LoggingUtils.getEvoLogger().info("Distributing left budget (" + totalLeftOver + ")");
            distributeExtraBudgetEvenly(jobs, totalLeftOver, maximumBudgetPerCore);
        }

        /*
         * using scheduling theory, there could be different best orders to maximize CPU usage. Here, at least for the
         * time being, for simplicity we just try to execute the most expensive jobs as soon as possible
         */
        Collections.sort(jobs, new Comparator<JobDefinition>()
        {
            @Override
            public int compare(JobDefinition a, JobDefinition b)
            {
                /*
                 * the job with takes most time will be "lower". recall that sorting is in ascending order
                 */
                return b.seconds - a.seconds;
            }
        });

        int budgetUsed = 0;

        List<JobDefinition> jobsUnderBudget = new LinkedList<JobDefinition>();
        for (int i = 0; i < jobs.size(); i++)
        {
            JobDefinition job = jobs.get(i);

            if (budgetUsed <= totalBudget)
            {
                LoggingUtils.getEvoLogger().info("+ Added class: " + job.cut + ", budget: " + job.seconds);
                jobsUnderBudget.add(job);
                budgetUsed += job.seconds;
            }
            else
                LoggingUtils.getEvoLogger().info("- Ignored class: " + job.cut + ", budget: " + job.seconds);
        }

        return jobsUnderBudget;
    }
}
