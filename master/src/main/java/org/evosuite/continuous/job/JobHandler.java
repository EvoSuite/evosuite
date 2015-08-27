package org.evosuite.continuous.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.continuous.persistency.StorageManager;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.utils.LoggingUtils;
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

	private Process latestProcess;
	
	/**
	 * Main constructor
	 * 
	 * @param queue
	 */
	public JobHandler(JobExecutor executor) {
		super();
		this.executor = executor;
	}

	public void setUpShutdownHook(){
		/*
		 * Just to be sure, in case this thread-class is
		 * not properly stopped in case of problems
		 */
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override public void run(){
				if(latestProcess!=null){
					latestProcess.destroy();
				}
			}
		});
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
			jobs[i].setUpShutdownHook();
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

			List<String> commands = getCommandString(job);

			Process process = null;

			try {
				String baseDir = System.getProperty("user.dir");
				File dir = new File(baseDir);

				// String[] parsedCommand = parseCommand(command);
				String[] parsedCommand = new String[commands.size()];
				commands.toArray(parsedCommand);

				ProcessBuilder builder = new ProcessBuilder(parsedCommand);
				builder.directory(dir);
				builder.redirectErrorStream(true);

				LoggingUtils.getEvoLogger().info("Going to start job for: " + job.cut);
				logger.debug("Base directory: " + baseDir);
				logger.debug("Commands: " + Arrays.asList(parsedCommand));

				process = builder.start();
				latestProcess = process;
				
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

	private List<String> getCommandString(JobDefinition job) {

		List<String> commands = new ArrayList<>();
		commands.add("java");		
		String classpath = System.getProperty("java.class.path");
		commands.add("-cp");
		commands.add(classpath + File.pathSeparator + executor.getProjectClassPath());
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
		commands.add("-D" + LoggingUtils.USE_DIFFERENT_LOGGING_XML_PARAMETER + "=logback-ctg.xml");

		StorageManager storage = executor.getStorage();
		File logs = storage.getTmpLogs();
		commands.add("-Devosuite.log.folder=" + logs.getAbsolutePath() + File.separator + job.cut);

		if (Properties.LOG_LEVEL != null && !Properties.LOG_LEVEL.isEmpty()) {
			commands.add("-Dlog.level=" + Properties.LOG_LEVEL);
		}

		/*
		 * TODO: this will likely need better handling
		 */
		int masterMB = 250;
		int clientMB = job.memoryInMB - masterMB;

		commands.add("-Xmx" + masterMB + "m");
		
		commands.add(org.evosuite.EvoSuite.class.getName());
		
		commands.add("-mem");
		commands.add(""+clientMB);
		commands.add("-class");
		commands.add(job.cut);
		commands.add("-projectCP");
		commands.add(executor.getProjectClassPath());

		//needs to be called twice, after the Java command
		if (Properties.LOG_LEVEL != null && !Properties.LOG_LEVEL.isEmpty()) {
			commands.add("-Dlog.level=" + Properties.LOG_LEVEL);
		}

		if (Properties.LOG_TARGET != null && !Properties.LOG_TARGET.isEmpty()) {
			commands.add("-Dlog.target=" + Properties.LOG_TARGET);
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

		commands.addAll(getPoolInfo(job));

		//TODO not just input pool, but also hierarchy

		commands.addAll(timeSetUp(job.seconds));

		File reports = storage.getTmpReports();
		File tests = storage.getTmpTests();
		File seedOut = storage.getTmpSeeds();
		File seedIn = storage.getSeedInFolder();

		commands.add("-Dreport_dir=" + reports.getAbsolutePath() + File.separator + job.cut);
		commands.add("-Dtest_dir=" + tests.getAbsolutePath());

		String seedsFileName = job.cut + "." + Properties.CTG_SEEDS_EXT;
		commands.add("-Dctg_seeds_file_out=" + seedOut.getAbsolutePath() + File.separator +seedsFileName);
		commands.add("-Dctg_seeds_file_in=" + seedIn.getAbsolutePath() + File.separator +seedsFileName);

		commands.addAll(getOutputVariables());
		if (Properties.ANALYSIS_CRITERIA.isEmpty()) {
			commands.add("-Danalysis_criteria=" +
					Properties.Criterion.LINE + "," + Properties.Criterion.STATEMENT + "," +
					Properties.Criterion.BRANCH + "," + Properties.Criterion.ONLYBRANCH + "," + Properties.Criterion.CBRANCH + "," + Properties.Criterion.IBRANCH + "," +
					Properties.Criterion.EXCEPTION + "," +
					Properties.Criterion.WEAKMUTATION + "," + Properties.Criterion.ONLYMUTATION + "," + Properties.Criterion.MUTATION + "," +
					Properties.Criterion.OUTPUT + "," +
					Properties.Criterion.METHOD + "," + Properties.Criterion.METHODTRACE + "," + Properties.Criterion.METHODNOEXCEPTION);
		} else {
			commands.add("-Danalysis_criteria=" + Properties.ANALYSIS_CRITERIA);
		}
		commands.add("-Dcriterion=" + Arrays.toString(Properties.CRITERION).
												replace("[", "").
												replace("]", "").
												replaceAll(", ", ":"));

		commands.add("-Djunit_suffix=" + Properties.JUNIT_SUFFIX);

		commands.add("-Denable_asserts_for_evosuite=" + Properties.ENABLE_ASSERTS_FOR_EVOSUITE);
		String confId = Properties.CONFIGURATION_ID;
		if (confId != null && !confId.isEmpty()) {
			commands.add("-Dconfiguration_id=" + confId);
		} else {
			commands.add("-Dconfiguration_id=default");
		}

		if (Properties.RANDOM_SEED != null) {
			commands.add("-Drandom_seed=" + Properties.RANDOM_SEED);
		}

		commands.add("-Dprint_to_system=" + Properties.PRINT_TO_SYSTEM);

		commands.add("-Dp_object_pool=" + Properties.P_OBJECT_POOL);

		/*
		 * these 4 options should always be 'true'.
		 * Here we take them as parameter, just because for experiments
		 * we might skip those phases if we do not analyze their results
		 */
		commands.add("-Dminimize=" + Properties.MINIMIZE);
		commands.add("-Dassertions=" + Properties.ASSERTIONS);
		commands.add("-Djunit_tests=" + Properties.JUNIT_TESTS);
		commands.add("-Djunit_check=" + Properties.JUNIT_CHECK);

		commands.add("-Dmax_size=" + Properties.MAX_SIZE);

		commands.add("-Dhtml=false");
		commands.add("-Dlog_timeout=false");
		commands.add("-Dplot=false");
		commands.add("-Dtest_comments=false");
		commands.add("-Dshow_progress=false");
		commands.add("-Dsave_all_data=false");
		commands.add("-Dcoverage=" + Properties.COVERAGE);

		/*
		 * for (de)serialization of classes with static fields, inner classes, etc,
		 * we must have this options set to true
		 */
		commands.add("-Dreset_static_fields=true");
		commands.add("-Dreplace_calls=true");

		if (Properties.CTG_HISTORY_FILE != null) {
			commands.add("-Dctg_history_file=" + Properties.CTG_HISTORY_FILE);
		}

		return commands;
	}

	private List<String> getPoolInfo(JobDefinition job) {

		List<String> commands = new ArrayList<String>();
		StorageManager storage = executor.getStorage();
		File poolFolder = storage.getTmpPools();

		String extension = ".pool";
		commands.add("-Dwrite_pool=" + poolFolder.getAbsolutePath() + File.separator + job.cut
		        + extension);

		if (job.inputClasses != null && job.inputClasses.size() > 0) {

			String[] dep = job.inputClasses.toArray(new String[0]);

			double poolP = 0.5;
			if (Properties.P_OBJECT_POOL > 0) { //TODO need refactoring, ie a Double initialized with null
				poolP = Properties.P_OBJECT_POOL;
			}

			commands.add("-Dp_object_pool=" + poolP);
			String cmd = "-Dobject_pools=";
			
			cmd += poolFolder.getAbsolutePath() + File.separator + dep[0] + extension;

			for (int i = 1; i < dep.length; i++) {
				cmd += File.pathSeparator + poolFolder.getAbsolutePath() + File.separator
				        + dep[i] + extension;
			}
			commands.add(cmd);
		}

		return commands;
	}

	private List<String> getOutputVariables() {
		List<String> commands = new ArrayList<String>();

		if (Properties.OUTPUT_VARIABLES == null) {
			// add some default output variables
			StringBuilder cmd = new StringBuilder();
			cmd.append("TARGET_CLASS,configuration_id,criterion");
			cmd.append("," + "ctg_min_time_per_job,ctg_schedule,search_budget,p_object_pool");
			if (Properties.CTG_TIME_PER_CLASS != null) {
				cmd.append(",ctg_time_per_class");
			}
			//cmd.append("," + RuntimeVariable.NumberOfInputPoolObjects);
			cmd.append("," + RuntimeVariable.Size);
			cmd.append("," + RuntimeVariable.Length);
			cmd.append("," + RuntimeVariable.Statements_Executed);
			cmd.append("," + RuntimeVariable.Total_Time);
			cmd.append("," + RuntimeVariable.Random_Seed);
			cmd.append("," + RuntimeVariable.Explicit_MethodExceptions + "," +  RuntimeVariable.Explicit_TypeExceptions);
			cmd.append("," + RuntimeVariable.Implicit_MethodExceptions + "," +  RuntimeVariable.Implicit_TypeExceptions);

			// coverage/score
			cmd.append("," + RuntimeVariable.LineCoverage + "," + RuntimeVariable.StatementCoverage + "," +
					RuntimeVariable.BranchCoverage + "," + RuntimeVariable.OnlyBranchCoverage + "," + RuntimeVariable.CBranchCoverage + "," + RuntimeVariable.IBranchCoverage + "," +
					RuntimeVariable.ExceptionCoverage + "," + RuntimeVariable.WeakMutationScore + "," + RuntimeVariable.OnlyMutationScore + "," + RuntimeVariable.MutationScore + "," +
					RuntimeVariable.OutputCoverage + "," + RuntimeVariable.InputCoverage + "," +
					RuntimeVariable.MethodCoverage + "," + RuntimeVariable.MethodTraceCoverage + "," + RuntimeVariable.MethodNoExceptionCoverage);

			// coverage bit string
			cmd.append("," + RuntimeVariable.LineCoverageBitString + "," + RuntimeVariable.StatementCoverageBitString + "," +
					RuntimeVariable.BranchCoverageBitString + "," + RuntimeVariable.OnlyBranchCoverageBitString + "," + RuntimeVariable.CBranchCoverageBitString + "," + RuntimeVariable.IBranchCoverageBitString + "," +
					RuntimeVariable.ExceptionCoverageBitString + "," + RuntimeVariable.WeakMutationCoverageBitString + "," + RuntimeVariable.OnlyMutationCoverageBitString + "," + RuntimeVariable.MutationCoverageBitString + "," +
					RuntimeVariable.OutputCoverageBitString + "," + RuntimeVariable.InputCoverageBitString + "," +
					RuntimeVariable.MethodCoverageBitString + "," + RuntimeVariable.MethodTraceCoverageBitString + "," + RuntimeVariable.MethodNoExceptionCoverageBitString);

			commands.add("-Doutput_variables=" + cmd.toString());
		} else {
			commands.add("-Doutput_variables=" + Properties.OUTPUT_VARIABLES);
		}

		if (Properties.CTG_TIME_PER_CLASS != null) {
			commands.add("-Dctg_time_per_class=" + Properties.CTG_TIME_PER_CLASS);
		}

		commands.add("-startedByCtg");
		/*
		 * Master/Client will not use these variables.
		 * But here we include them just to be sure that they will end
		 * up in the generated CSV files
		 */
		commands.add("-Dctg_schedule=" + Properties.CTG_SCHEDULE);
		commands.add("-Dctg_min_time_per_job=" + Properties.CTG_MIN_TIME_PER_JOB);

		return commands;
	}

	private List<String> timeSetUp(int seconds) {

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

		final int PHASES = 6; //not including "search"

		//the main phase is "search", which should take at least 50% of the budget
		int halfTime = seconds / 2;

		int initialization = halfTime / PHASES;
		int minimization = halfTime / PHASES;
		int assertions = halfTime / PHASES;
		int extra = halfTime / PHASES;
        int junit = halfTime / PHASES;
		int write = halfTime / PHASES;

		final int MAJOR_DELTA = 120;
		final int MINOR_DELTA = 60;
		
		if (halfTime > PHASES * MAJOR_DELTA) {
			initialization = MAJOR_DELTA;
			minimization = MAJOR_DELTA;
			assertions = MAJOR_DELTA;
			extra = MAJOR_DELTA;
            junit = MAJOR_DELTA;
			write = MAJOR_DELTA;
		} else if (halfTime > PHASES * MINOR_DELTA) {
			initialization = MINOR_DELTA;
			minimization = MINOR_DELTA;
			assertions = MINOR_DELTA;
			extra = MINOR_DELTA;
            junit = MINOR_DELTA;
			write = MINOR_DELTA;
		}

		int search = seconds - (initialization + minimization + assertions + extra + junit + write);

		List<String> commands = new ArrayList<>();
		commands.add("-Dsearch_budget=" + search);
		commands.add("-Dglobal_timeout=" + search);
		commands.add("-Dstopping_condition=" + StoppingCondition.MAXTIME);
		commands.add("-Dinitialization_timeout=" + initialization);
		commands.add("-Dminimization_timeout=" + minimization);
		commands.add("-Dassertion_timeout=" + assertions);
        commands.add("-Dextra_timeout=" + extra);
        commands.add("-Djunit_check_timeout=" + junit);
		commands.add("-Dwrite_junit_timeout=" + write);

		return commands;
	}
}
