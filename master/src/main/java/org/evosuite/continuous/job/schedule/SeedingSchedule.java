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
import org.evosuite.continuous.project.ProjectGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Choose a precise order in which the CUTs will be targeted.
 * Test cases for a CUT can be used for seeding in the search
 * of the following CUTs in the schedule
 *
 * @author arcuri
 */
public class SeedingSchedule extends OneTimeSchedule {

    private static final Logger logger = LoggerFactory.getLogger(SeedingSchedule.class);

    protected final OneTimeSchedule base;

    public SeedingSchedule(JobScheduler scheduler) {
        this(scheduler, new SimpleSchedule(scheduler));
    }

    protected SeedingSchedule(JobScheduler scheduler, OneTimeSchedule base) {
        super(scheduler);
        this.base = base;
    }


    @Override
    protected List<JobDefinition> createScheduleOnce() {
        List<JobDefinition> jobs = base.createScheduleOnce();

        if (logger.isDebugEnabled()) {
            logger.debug("Base schedule: " + jobs);
        }

        return addDepenciesAndSort(jobs);
    }

    @Override
    protected List<JobDefinition> createScheduleForWhenNotEnoughBudget() {
        /*
         * even if we do not have enough budget to target all CUTs, we
         * still want to use seeding.
         */
        List<JobDefinition> jobs = super.createScheduleForWhenNotEnoughBudget();
        if (logger.isDebugEnabled()) {
            logger.debug("Base, reduced schedule: " + jobs);
        }
        return addDepenciesAndSort(jobs);
    }


    protected List<JobDefinition> addDepenciesAndSort(List<JobDefinition> jobs) {
        jobs = addDependenciesForSeeding(jobs);

        if (logger.isDebugEnabled()) {
            logger.debug("Schedule after adding dependencies: " + jobs);
        }

        jobs = getSortedToSatisfyDependencies(jobs);

        if (logger.isDebugEnabled()) {
            logger.debug("Final schedule after sorting: " + jobs);
        }

        return jobs;
    }

    /**
     * Try (best effort) to sort the jobs in a way in which dependent jobs
     * are executed first. Try to maintain the relative order of the input list.
     * Note: even if sorting is not precise, still the job executor will be able to handle it
     *
     * @param jobs A sorted copy of the input list
     */
    protected static List<JobDefinition> getSortedToSatisfyDependencies(List<JobDefinition> jobs) {

        Queue<JobDefinition> toAssign = new LinkedList<>(jobs);
        List<JobDefinition> postponed = new LinkedList<>();

        List<JobDefinition> out = new ArrayList<>(jobs.size());
        Set<String> assigned = new HashSet<>();

        /*
         * Note: the code here is similar to what done in JobExecutor
         */

        mainLoop:
        while (!toAssign.isEmpty() || !postponed.isEmpty()) {

            JobDefinition chosenJob = null;

            //postponed jobs have the priority
            if (!postponed.isEmpty()) {
                Iterator<JobDefinition> iterator = postponed.iterator();
                postponedLoop:
                while (iterator.hasNext()) {
                    JobDefinition job = iterator.next();
                    if (job.areDependenciesSatisfied(jobs, assigned)) {
                        chosenJob = job;
                        iterator.remove();
                        break postponedLoop;
                    }
                }
            }

            if (chosenJob == null && toAssign.isEmpty()) {
                /*
                 * nothing satisfied in 'postponed', and nothing left in 'toAssign'.
                 * so just pick up one (the oldest)
                 */
                assert !postponed.isEmpty();
                chosenJob = postponed.remove(0);
            }

            if (chosenJob == null) {
                assert !toAssign.isEmpty();

                toExecuteLoop:
                while (!toAssign.isEmpty()) {
                    JobDefinition job = toAssign.poll();
                    if (job.areDependenciesSatisfied(jobs, assigned)) {
                        chosenJob = job;
                        break toExecuteLoop;
                    } else {
                        postponed.add(job);
                    }
                }

                if (chosenJob == null) {
                    assert !postponed.isEmpty() && toAssign.isEmpty();
                    continue mainLoop;
                }
            }

            out.add(chosenJob);
            assigned.add(chosenJob.cut);
        }

        return out;
    }


    /**
     * For each input job, identify all the others jobs we want to generate
     * test cases first.
     * The scheduler will use this information to first try to generate
     * the test cases for the "dependency" jobs.
     *
     * <p>
     * There can be different strategies to define a dependency:
     * - ancestor, non-interface classes
     * - classes used as input objects
     * - subtypes of classes used as input objects
     *
     * @param jobs
     * @return a copy given input list, but with new jobs objects
     */
    protected List<JobDefinition> addDependenciesForSeeding(List<JobDefinition> jobs) {

        List<JobDefinition> list = new ArrayList<>(jobs.size());

        for (JobDefinition job : jobs) {
            Set<String> inputs = calculateInputClasses(job);
            Set<String> parents = calculateAncestors(job);

            list.add(job.getByAddingDependencies(inputs, parents));
        }

        return list;
    }

    /**
     * If CUT A takes as input an object B, then to cover A we might need B
     * set in a specific way. Using the test cases generated for B can give
     * us a pool of interesting instances of B.
     *
     * @param job
     * @param dep
     */
    private Set<String> calculateInputClasses(JobDefinition job) {

        Set<String> dep = new LinkedHashSet<>();

        ProjectGraph graph = scheduler.getProjectData().getProjectGraph();
        for (String input : graph.getCUTsDirectlyUsedAsInput(job.cut, true)) {
            if (graph.isInterface(input)) {
                continue;
            }
            dep.add(input);
        }

        return dep;
    }

    /**
     * The motivation for adding ancestors is as follows:
     * consider CUT A extends B. If B is not an interface,
     * then likely it will have an internal state.
     * Test cases for B might bring it to some interesting/hard
     * to reach configurations.
     * If the methods in A rely on those states in the upper class,
     * then such test cases from B "might" be helpful.
     * But, to do so, the seeded test cases need to change the concrete class.
     * For example, if we have:
     *
     * <p> <code>B foo = new B(); </br>
     * foo.doSomething(x,y); </code>
     *
     * <p> then we should transform it into:
     *
     * <p> <code>B foo = new A(); </br>
     * foo.doSomething(x,y); </code>
     *
     * <p> There is a potentially tricky case.
     * Consider for example if B is abstract and,
     * when test cases were generated for it, a subclass
     * C was chosen instead of A. This would mean the test
     * case for B would look like:
     *
     * <p> <code>B foo = new C(); </br>
     * foo.doSomething(x,y); </code>
     *
     * <p> However, it is actually not a big deal, as it is safe
     * to modify <code>new C()</code> with <code>new A()</code>.
     *
     * <p> Note: if the test cases for B are in the
     * form <code>C foo = new C();</code>, then that would rather
     * be a bug/problem in how EvoSuite generated test cases for B
     *
     * @param job
     */
    private Set<String> calculateAncestors(JobDefinition job) {

        Set<String> dep = new LinkedHashSet<>();

        ProjectGraph graph = scheduler.getProjectData().getProjectGraph();

        if (graph.isInterface(job.cut)) {
            /*
             * even if an interface has code, it will have no class state (ie fields).
             * so, no point in looking at its ancestors
             */
            return dep;
        }

        for (String parent : graph.getAllCUTsParents(job.cut)) {
            if (graph.isInterface(parent)) {
                continue;
            }
            dep.add(parent);
        }
        return dep;
    }

}
