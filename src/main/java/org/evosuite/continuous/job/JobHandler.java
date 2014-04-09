package org.evosuite.continuous.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.continuous.persistency.StorageManager;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.ReportGenerator.RuntimeVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class that actually execute the job as a separated process
 * 
 * @author arcuri
 * 
 */
public class JobHandler extends Thread {

	private static Logger logger = LoggerFactory.getLogger(JobHandler.class);

	private final JobExecutor executor;

	/**
	 * Main constructor
	 * 
	 * @param queue
	 */
	public JobHandler(JobExecutor executor) {
		super();
		this.executor = executor;
	}

	/**
	 * Return a pool of handlers, all sharing same queue and latch
	 * 
	 * @param n
	 * @return
	 */
	public static JobHandler[] getPool(int n, JobExecutor executor) {
		JobHandler[] jobs = new JobHandler[n];
		for (int i = 0; i < jobs.length; i++) {
			jobs[i] = new JobHandler(executor);
		}
		return jobs;
	}

	public void stopExecution() {
		this.interrupt();
	}

	@Override
	public void run() {
		while (!this.isInterrupted()) {
			JobDefinition job = null;

			try {
				job = executor.pollJob();
			} catch (InterruptedException e) {
				break;
			}

			String command = getCommandString(job);

			Process process = null;

			try {
				String baseDir = System.getProperty("user.dir");
				File dir = new File(baseDir);

				String[] parsedCommand = parseCommand(command);

				ProcessBuilder builder = new ProcessBuilder(parsedCommand);
				builder.directory(dir);
				builder.redirectErrorStream(true);

				LoggingUtils.getEvoLogger().info("Going to start job for: " + job.cut);
				logger.debug("Base directory: " + baseDir);
				logger.debug("Command: " + command);

				process = builder.start();

				int exitCode = process.waitFor(); //no need to have timeout here, as it is handled by the scheduler/executor				

				if (exitCode != 0) {
					handleProcessError(job, process);
				}

			} catch (IOException e) {
				logger.error("Failed to start new job: " + e.getMessage(), e);
			} catch (InterruptedException e) {
				this.interrupt();
				if (process != null) {
					process.destroy();
				}
			} finally {
				/*
				 * if there were problems with this job, still
				 * be sure to decrease the job counter
				 */
				executor.doneWithJob(job);
			}
		}
	}

	private String[] parseCommand(String command) {
		List<String> list = new ArrayList<String>();
		for (String token : command.split(" ")) {
			String entry = token.trim();
			if (!entry.isEmpty()) {
				list.add(entry);
			}
		}
		String[] parsedCommand = list.toArray(new String[0]);
		return parsedCommand;
	}

	/**
	 * Print process console output if it died, as its logs on disks might not
	 * have been generated yet
	 * 
	 * @param job
	 * @param process
	 * @throws IOException
	 */
	private void handleProcessError(JobDefinition job, Process process)
	        throws IOException {

		StringBuffer sb = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(
		        process.getInputStream()));

		int data = 0;
		while (data != -1 && !isInterrupted()) {
			data = in.read();
			if (data != -1) {
				sb.append((char) data);
			}
		}

		logger.warn("Job ended with erroneous exit code: " + job.cut
		        + "\nProcess console output:\n" + sb.toString());
	}

	private String getCommandString(JobDefinition job) {

		String cmd = "java ";
		String classpath = System.getProperty("java.class.path");
		cmd += " -cp " + classpath + File.pathSeparator + executor.getProjectClassPath();
		/* 
		 * FIXME for seeding, need to setup classpath of generated test suites
		 * - first the currently generated
		 * - then the old ones
		 * 
		 * if same test suites happen twice (ie in current and old), then we it would be 
		 * complicated to use both (we would need to change their name) 
		 */

		/*
		 *  it is important to set it before calling EvoSuite, as it has to be read by Master before loading properties.
		 *  Note: the Client will get it automatically from Master
		 */
		cmd += " -D" + LoggingUtils.USE_DIFFERENT_LOGGING_XML_PARAMETER
		        + "=logback-ctg.xml";

		StorageManager storage = executor.getStorage();
		File logs = storage.getTmpLogs();
		//cmd += " -Devosuite.log.folder=" + logs.getAbsolutePath() + "/job" + job.jobID;
		cmd += " -Devosuite.log.folder=" + logs.getAbsolutePath() + "/" + job.cut;

		if (Properties.LOG_LEVEL != null && !Properties.LOG_LEVEL.isEmpty()) {
			cmd += " -Dlog.level=" + Properties.LOG_LEVEL;
		}

		/*
		 * TODO: this will likely need better handling
		 */
		int masterMB = 250;
		int clientMB = job.memoryInMB - masterMB;

		cmd += (" -Xmx" + masterMB) + "m";
		cmd += " " + org.evosuite.EvoSuite.class.getName();
		cmd += " -mem " + clientMB;
		cmd += " -class " + job.cut;
		cmd += " -projectCP "+executor.getProjectClassPath();

		//needs to be called twice, after the Java command
		if (Properties.LOG_LEVEL != null && !Properties.LOG_LEVEL.isEmpty()) {
			cmd += " -Dlog.level=" + Properties.LOG_LEVEL;
		}

		if (Properties.LOG_TARGET != null && !Properties.LOG_TARGET.isEmpty()) {
			cmd += " -Dlog.target=" + Properties.LOG_TARGET;
		}

		/*
		 * TODO for now we ignore the job configuration (ie special parameter settings)
		 */
		//cmd += " -D<TODO>="+job.configurationId; 

		/*
		 * TODO we should check on whether the dependent CUTs have been
		 * generated in this CTG run, or should rather look at previous runs.
		 * This could happen for at least 2 reasons:
		 * - under budget, and we could not run jobs for all CUTs
		 * - job for dependency crashed, but we have test cases from previous CTG run
		 * 
		 * 
		 * Regardless of whether dependencies for seeding were calculated, we might
		 * still want to use seeding based on previous CTG runs, if any test suite
		 * is available for the CUT 
		 */

		cmd += " " + getPoolInfo(job);

		//TODO not just input pool, but also hierarchy

		cmd += timeSetUp(job.seconds);

		File reports = storage.getTmpReports();
		File tests = storage.getTmpTests();

		//TODO check if it works on Windows... likely not	
		//cmd += " -Dreport_dir=" + reports.getAbsolutePath() + "/job" + job.jobID;
		cmd += " -Dreport_dir=" + reports.getAbsolutePath() + "/" + job.cut;
		cmd += " -Dtest_dir=" + tests.getAbsolutePath();

		//cmd += " -Derror_branches=true"; 
		cmd += " -criterion exception";
		cmd += " -Dtest_factory=" + Properties.TEST_FACTORY;
		cmd += " -Dseed_clone=" + Properties.SEED_CLONE;
		cmd += " -Dseed_dir=" + storage.getTmpSeeds().getAbsolutePath();

		cmd += " " + getOutputVariables();

		cmd += " -Djunit_suffix=" + StorageManager.junitSuffix;

		cmd += " -Denable_asserts_for_evosuite=" + Properties.ENABLE_ASSERTS_FOR_EVOSUITE;
		String confId = Properties.CONFIGURATION_ID;
		if (confId != null && !confId.isEmpty()) {
			cmd += " -Dconfiguration_id=" + confId;
		} else {
			cmd += " -Dconfiguration_id=default";
		}

		if (Properties.RANDOM_SEED != null) {
			cmd += " -Drandom_seed=" + Properties.RANDOM_SEED;
		}

		cmd += " -Dprint_to_system=" + Properties.PRINT_TO_SYSTEM;

		cmd += " -Dp_object_pool=" + Properties.P_OBJECT_POOL;

		/*
		 * these 2 options should always be 'true'.
		 * Here we take them as parameter, just because for experiments
		 * we might skip those phases if we do not analyze their results
		 */
		cmd += " -Dminimize=" + Properties.MINIMIZE;
		cmd += " -Dassertions=" + Properties.ASSERTIONS;

		cmd += " -Dmax_size=" + Properties.MAX_SIZE;

		cmd += " -Dsecondary_objectives=totallength  -Dtimeout=5000  ";
		cmd += " -Dhtml=false -Dlog_timeout=false  -Dplot=false -Djunit_tests=true -Dtest_comments=false -Dshow_progress=false";
		cmd += " -Dsave_all_data=false  -Dinline=false";

		/*
		 * for (de)serialization of classes with static fields, inner classes, etc,
		 * we must have this options set to true
		 */
		cmd += " -Dreset_static_fields=true -Dreplace_calls=true";

		if (!Properties.CTG_HISTORY_FILE.isEmpty())
            cmd += " -Dctg_history_file=" + Properties.CTG_HISTORY_FILE;

		return cmd;
	}

	private String getPoolInfo(JobDefinition job) {

		StorageManager storage = executor.getStorage();
		File poolFolder = storage.getTmpPools();

		String extension = ".pool";
		String cmd = "";
		cmd += " -Dwrite_pool=" + poolFolder.getAbsolutePath() + File.separator + job.cut
		        + extension;

		if (job.inputClasses != null && job.inputClasses.size() > 0) {

			String[] dep = job.inputClasses.toArray(new String[0]);

			double poolP = 0.5;
			if (Properties.P_OBJECT_POOL > 0) { //TODO need refactoring, ie a Double initialized with null
				poolP = Properties.P_OBJECT_POOL;
			}

			cmd += " -Dp_object_pool=" + poolP;
			cmd += " -Dobject_pools=";

			cmd += poolFolder.getAbsolutePath() + File.separator + dep[0] + extension;

			for (int i = 1; i < dep.length; i++) {
				cmd += File.pathSeparator + poolFolder.getAbsolutePath() + File.separator
				        + dep[i] + extension;
			}
		}

		return cmd;
	}

	private String getOutputVariables() {
		String cmd = " -Doutput_variables=";
		cmd += "TARGET_CLASS,configuration_id,";
		cmd += "ctg_min_time_per_job,ctg_schedule,search_budget,p_object_pool,";
		cmd += RuntimeVariable.Covered_Branches + ",";
		cmd += RuntimeVariable.Total_Branches + ",";
		cmd += RuntimeVariable.BranchCoverage + ",";
		cmd += RuntimeVariable.NumberOfInputPoolObjects + ",";
		cmd += RuntimeVariable.Minimized_Size + ",";
		cmd += RuntimeVariable.NumberOfGeneratedTestCases + ",";
		cmd += RuntimeVariable.Statements_Executed + ",";
		cmd += RuntimeVariable.Total_Time + ",";
		cmd += RuntimeVariable.Implicit_MethodExceptions + ",";
		cmd += RuntimeVariable.Random_Seed + ",";
		cmd += RuntimeVariable.Explicit_MethodExceptions;

		if (Properties.CTG_TIME_PER_CLASS != null) {
			cmd += ",ctg_time_per_class";
			cmd += " -Dctg_time_per_class=" + Properties.CTG_TIME_PER_CLASS;
		}

		/*
		 * Master/Client will not use these variables.
		 * But here we include them just to be sure that they will end
		 * up in the generated CSV files
		 */
		cmd += " -Dctg_schedule=" + Properties.CTG_SCHEDULE;
		cmd += " -Dctg_min_time_per_job=" + Properties.CTG_MIN_TIME_PER_JOB;

		return cmd;
	}

	private String timeSetUp(int seconds) {

		//do we have enough time for this job?
		int remaining = (int) executor.getRemainingTimeInMs() / 1000;

		if (seconds > remaining) {
			seconds = remaining;
		}

		int minSecondsPerJob = 60 * executor.configuration.minMinutesPerJob;

		if (seconds < minSecondsPerJob) {
			//even if we do not have enough time, we go for the minimum
			seconds = minSecondsPerJob;
		}

		/*
		 * We have at least 4 phases:
		 * - init (eg dependency analysis)
		 * - search
		 * - minimization
		 * - assertion generation
		 * 
		 * Plus extra time that might be needed 
		 * 
		 * How to best divide the budget among them?
		 * 
		 * For now we just do something very basic
		 */

		final int PHASES = 5;
		
		int initialization = seconds / PHASES;
		int minimization = seconds / PHASES;
		int assertions = seconds / PHASES;
		int extra = seconds / PHASES;

		final int MAJOR_DELTA = 120;
		final int MINOR_DELTA = 60;
		
		if (seconds > PHASES * MAJOR_DELTA) {
			initialization = MAJOR_DELTA;
			minimization = MAJOR_DELTA;
			assertions = MAJOR_DELTA;
			extra = MAJOR_DELTA;
		} else if (seconds > PHASES * MINOR_DELTA) {
			initialization = MINOR_DELTA;
			minimization = MINOR_DELTA;
			assertions = MINOR_DELTA;
			extra = MINOR_DELTA;
		}

		int search = seconds - (initialization + minimization + assertions + extra);

		String cmd = " -Dsearch_budget=" + search;
		cmd += " -Dglobal_timeout=" + search;
		cmd += " -Dstopping_condition=" + StoppingCondition.MAXTIME;
		cmd += " -Dinitialization_timeout=" + initialization;
		cmd += " -Dminimization_timeout=" + minimization;
		cmd += " -Dassertion_timeout=" + assertions;
		cmd += " -Dextra_timeout=" + extra;

		return cmd;
	}
}
