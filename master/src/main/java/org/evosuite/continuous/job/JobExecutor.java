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

import org.evosuite.Properties;
import org.evosuite.continuous.CtgConfiguration;
import org.evosuite.continuous.persistency.StorageManager;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

/**
 * Job executor will run EvoSuite on separate processes.
 * There will be no communication with these masters/clients, whose
 * visible side-effects would only be files written on local disk.
 * This does simplify the architecture a lot, especially considering we can
 * have several instances running in parallel.
 * Furthermore, writing to disk at each search has benefit that we can recover from
 * premature crashes, reboot of machine, etc.
 * This is particularly useful considering that CTG can be left running for hours, if not
 * even days.
 * Downside is not a big deal, as the searches in a schedule are anyway run independently.
 *
 *
 * <p>
 * Note: under no case ever two different jobs should access the same files at the same time, even
 * if just for reading. Piece-of-crap OS like Windows do put locks on files based
 * on processes accessing them... for multi-process applications running on same host,
 * that is a recipe for disaster...
 *
 * @author arcuri
 */
public class JobExecutor {

    private static final Logger logger = LoggerFactory.getLogger(JobExecutor.class);

    private volatile boolean executing;
    private long startTimeInMs;

    /**
     * This used to wait till all jobs are finished running
     */
    private volatile CountDownLatch latch;

    /**
     * Several threads read from this queue to execute jobs
     * on separated process
     */
    private BlockingQueue<JobDefinition> jobQueue;

    /**
     * keep track of all the jobs that have been executed so far.
     * Each job definition (value) is indexed by the CUT name (key).
     * This assumes in a schedule that the CUT names are unique, ie,
     * no more than one job should exist for the same CUT
     */
    private Map<String, JobDefinition> finishedJobs;

    protected final CtgConfiguration configuration;

    private final String projectClassPath;

    private final StorageManager storage;

    /**
     * Main constructor
     */
    public JobExecutor(StorageManager storage,
                       String projectClassPath, CtgConfiguration conf) throws IllegalArgumentException {

        this.storage = storage;
        if (!storage.isStorageOk()) {
            throw new IllegalArgumentException("Storage is not initialized");
        }

        this.configuration = conf;
        this.projectClassPath = projectClassPath;
    }

    protected long getRemainingTimeInMs() {
        long elapsed = System.currentTimeMillis() - startTimeInMs;
        long budgetInMs = configuration.timeInMinutes * 60 * 1000;
        long remaining = budgetInMs - elapsed;
        return remaining;
    }

    /**
     * Do a separate search with EvoSuite for all jobs in the given list.
     * The executor tries a best effort to execute the jobs in the given order,
     * although no guarantee is provided (eg, there might be dependencies among jobs).
     *
     * @param jobs
     * @throws IllegalStateException if we are already executing some jobs
     */
    public synchronized void executeJobs(final List<JobDefinition> jobs, final int cores) throws IllegalStateException {
        if (executing) {
            throw new IllegalStateException("Already executing jobs");
        }

        logger.info("Going to execute " + jobs.size() + " jobs");

        initExecution(jobs);

        Thread mainThread = new Thread() {
            @Override
            public void run() {

                JobHandler[] handlers = JobHandler.getPool(cores, JobExecutor.this);
                for (JobHandler handler : handlers) {
                    handler.start();
                }

                long longestJob = -1L;

                try {
                    LoggingUtils.getEvoLogger().info("Going to execute " + jobs.size() + " jobs");

                    long minutes = configuration.timeInMinutes;
                    LocalDateTime endBy = LocalDateTime.now().plus(minutes, ChronoUnit.MINUTES);
                    LoggingUtils.getEvoLogger().info("Estimated completion time: " + minutes + " minutes, by " + endBy);

                    longestJob = execute(jobs);
                } catch (Exception e) {
                    logger.error("Error while trying to execute the " + jobs.size() + " jobs: " + e.getMessage(), e);
                } finally {
                    /*
                     * When we arrive here, in the worst case each handler is still executing a job,
                     * plus one in the queue.
                     * Note: this check is not precise
                     */
                    if (!this.isInterrupted() && longestJob > 0) {
                        try {
                            latch.await((longestJob * 2) + (60000), TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {
                            this.interrupt();
                        }
                    }

                    //be sure to release the latch
                    long stillNotFinished = latch.getCount();
                    for (int i = 0; i < stillNotFinished; i++) {
                        latch.countDown();
                    }

                    for (JobHandler handler : handlers) {
                        handler.stopExecution();
                    }

                    executing = false;
                }
            } //end of "run"
        };
        mainThread.start();
    }

    protected void initExecution(final List<JobDefinition> jobs) {
        executing = true;
        startTimeInMs = System.currentTimeMillis();
        latch = new CountDownLatch(jobs.size());

        /*
         * there is a good reason to have a blocking queue of size 1.
         * we want to put jobs on the queue only when we know there is going
         * to be a handler that can pull it.
         * this helps the scheduler, as we can wait longer before making the decision
         * of what job to schedule next
         */
        jobQueue = new ArrayBlockingQueue<>(1);
        finishedJobs = new ConcurrentHashMap<>();
    }

    protected long execute(List<JobDefinition> jobs) {

        long longestJob = -1L;

        //TODO handle memory
        Queue<JobDefinition> toExecute = new LinkedList<>(jobs);

        List<JobDefinition> postponed = new LinkedList<>();

        mainLoop:
        while (!toExecute.isEmpty() || !postponed.isEmpty()) {

            long remaining = getRemainingTimeInMs();
            if (remaining <= 0) {
                //time is over. do not submit any more job
                break mainLoop;
            }

            JobDefinition chosenJob = null;

            //postponed jobs have the priority
            if (!postponed.isEmpty()) {
                Iterator<JobDefinition> iterator = postponed.iterator();
                postponedLoop:
                while (iterator.hasNext()) {
                    JobDefinition job = iterator.next();
                    if (job.areDependenciesSatisfied(jobs, finishedJobs.keySet())) {
                        chosenJob = job;
                        iterator.remove();
                        break postponedLoop;
                    }
                }
            }

            if (chosenJob == null && toExecute.isEmpty()) {
                assert !postponed.isEmpty();
                /*
                 * tricky case: the are no more jobs in the queue, and there are
                 * jobs that have been postponed. But, at the moment, we shouldn't
                 * execute them due to missing dependencies.
                 *
                 * As the dependencies are just "optimizations" (eg, seeding), it is not
                 * wrong to execute any of those postponed jobs.
                 * There might be useful heuristics to pick up one in a smart way but,
                 * for now, we just choose the first (and so oldest)
                 */
                chosenJob = postponed.remove(0); //it is important to get the oldest jobs
            }

            if (chosenJob == null) {
                assert !toExecute.isEmpty();

                toExecuteLoop:
                while (!toExecute.isEmpty()) {
                    JobDefinition job = toExecute.poll();
                    if (job.areDependenciesSatisfied(jobs, finishedJobs.keySet())) {
                        chosenJob = job;
                        break toExecuteLoop;
                    } else {
                        postponed.add(job);
                    }
                }

                if (chosenJob == null) {
                    /*
                     * yet another tricky situation: we started with a list of jobs to execute,
                     * and none in the postponed list; but we cannot execute any of those
                     * jobs (this could happen if they all depend on jobs that are currently running).
                     * We should hence just choose one of them. Easiest thing, and most clean,
                     * to do is just to go back to beginning of the loop
                     */
                    assert !postponed.isEmpty() && toExecute.isEmpty();
                    continue mainLoop;
                }
            }

            assert chosenJob != null;
            longestJob = Math.max(longestJob, chosenJob.seconds * 1000);

            try {
                jobQueue.offer(chosenJob, remaining, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); //important for check later
                break mainLoop;
            }
        }

        return longestJob;
    }


    public JobDefinition pollJob() throws InterruptedException {
        return jobQueue.take();
    }

    public void doneWithJob(JobDefinition job) {
        finishedJobs.put(job.cut, job);
        latch.countDown();
        LoggingUtils.getEvoLogger().info("Completed job. Left: " + latch.getCount());
    }

    public void waitForJobs() {
        /*
         * Note: this method could be called a long while after the starting
         * of the execution. But likely not so important to handle such situation.
         *
         * Furthermore, due to crashes and phases that could end earlier (eg, minimization
         * and assertion generation), it is likely that jobs will finish before the expected time.
         */
        try {
            if (Properties.CTG_DEBUG_PORT != null) {
                //do not use timeout if we are debugging
                latch.await();
            } else {
                //add one extra minute just to be sure
                boolean elapsed = !latch.await(configuration.timeInMinutes + 1, TimeUnit.MINUTES);
                if (elapsed) {
                    logger.error("The jobs did not finish in time");
                }
            }
        } catch (InterruptedException e) {
        }
    }

    public String getProjectClassPath() {
        return projectClassPath;
    }

    public StorageManager getStorage() {
        return storage;
    }
}
