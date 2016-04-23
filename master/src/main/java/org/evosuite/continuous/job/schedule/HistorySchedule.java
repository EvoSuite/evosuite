/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
import org.evosuite.utils.LoggingUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * HistorySchedule class.
 * </p>
 * 
 * @author José Campos
 */
public class HistorySchedule extends OneTimeSchedule {

  private static double MODIFIED = 2.0;

  private static double NOT_MODIFIED = 1.0;

  public static final int COMMIT_IMPROVEMENT = 3;

  public HistorySchedule(JobScheduler scheduler) {
    super(scheduler);
  }

  @Override
  protected List<JobDefinition> createScheduleOnce() {

    ProjectStaticData data = this.scheduler.getProjectData();

    int maximumBudgetPerCore = 60 * this.scheduler.getConfiguration().timeInMinutes;

    // the total budget we need to choose how to allocate
    int totalBudget =
        maximumBudgetPerCore * this.scheduler.getConfiguration().getNumberOfUsableCores();

    // a part of the budget is fixed, as each CUT needs a minimum of it
    int minTime = 60 * this.scheduler.getConfiguration().minMinutesPerJob
        * data.getTotalNumberOfTestableCUTs();

    // this is what left from the minimum allocation, and that now we can
    // choose how best to allocate
    int extraTime = totalBudget - minTime;

    // check how much time we can give extra for each branch in a CUT
    int number_of_branches = data.getTotalNumberOfBranches();
    double timePerBranch =
        number_of_branches == 0.0 ? 0.0 : (double) extraTime / (double) number_of_branches;

    List<ClassInfo> classesInfo = new ArrayList<ClassInfo>(data.getClassInfos());

    // classes that have been changed first
    Collections.sort(classesInfo, new Comparator<ClassInfo>() {
      @Override
      public int compare(ClassInfo a, ClassInfo b) {
        if (a.hasChanged() && !b.hasChanged()) {
          return -1;
        } else if (!a.hasChanged() && b.hasChanged()) {
          return 1;
        }

        // otherwise, get the most difficult classes first
        return Integer.compare(b.numberOfBranches, a.numberOfBranches);
      }
    });

    int totalLeftOver = 0;
    int totalBudgetUsed = 0;
    List<JobDefinition> jobs = new LinkedList<JobDefinition>();

    for (ClassInfo c_info : classesInfo) {
      if (!c_info.isTestable()) {
        continue;
      }
      if (!c_info.hasChanged() && !c_info.isToTest()) {
        LoggingUtils.getEvoLogger().info("- Skipping class " + c_info.getClassName()
            + " because it does not seem to be worth it");
        continue;
      }

      double budget = 60.0 * scheduler.getConfiguration().minMinutesPerJob
          + (c_info.numberOfBranches * timePerBranch);

      // classes that have been modified could get more time than 'normal' classes
      budget *= c_info.hasChanged() ? HistorySchedule.MODIFIED : HistorySchedule.NOT_MODIFIED;

      if (budget > maximumBudgetPerCore) {
        /*
         * Need to guarantee that no job has more than maximumBudgetPerCore regardless of number of
         * cores
         */
        totalLeftOver += (budget - maximumBudgetPerCore);
        budget = maximumBudgetPerCore;
      }

      if ((totalBudgetUsed + budget) <= totalBudget) {
        totalBudgetUsed += budget;

        LoggingUtils.getEvoLogger()
            .info("+ Going to generate test cases for " + c_info.getClassName()
                + " using a time budget of " + budget + " seconds. Status of it ["
                + (c_info.hasChanged() ? "modified" : "not modified") + "]");

        jobs.add(new JobDefinition((int) budget,
            this.scheduler.getConfiguration().getConstantMemoryPerJob(), c_info.getClassName(), 0,
            null, null));
      } else {
        LoggingUtils.getEvoLogger()
            .info("- There is not enough time budget to test " + c_info.getClassName()
                + ". Status of it [" + (c_info.hasChanged() ? "modified" : "not modified") + "]");

        // mark this CUT as not tested. this is useful to later on distinguish
        // classes that EvoSuite failed to generate test cases and classes that
        // we didn't actually create any job for
        c_info.isToTest(false);
      }
    }

    totalLeftOver += (totalBudget - totalBudgetUsed);

    /*
     * do we still have some more budget to allocate? and is it less
     * than totalBudget? (because if it is equal to totalBudget, means
     * that we have skipped all classes, and therefore there is not point
     * of distributing left over budget as all classes will be skipped)
     */
    if (totalLeftOver > 0 && totalLeftOver < totalBudget) {
      LoggingUtils.getEvoLogger().info("Distributing left budget (" + totalLeftOver + ")");
      distributeExtraBudgetEvenly(jobs, totalLeftOver, maximumBudgetPerCore);
    }

    return jobs;
  }
}
