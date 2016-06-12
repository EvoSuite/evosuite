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
package org.evosuite.continuous;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.continuous.job.JobDefinition;
import org.evosuite.continuous.job.JobExecutor;
import org.evosuite.continuous.job.JobScheduler;
import org.evosuite.continuous.persistency.StorageManager;
import org.evosuite.continuous.project.ProjectAnalyzer;
import org.evosuite.continuous.project.ProjectStaticData;
import org.evosuite.utils.FileIOUtils;
import org.evosuite.xsd.Project;
import org.evosuite.xsd.ProjectUtil;


/**
 * <p>
 * This is the main entry point for the new 
 * paradigm "Continuous Test Generation" (CTG).
 * This can be called directly from EvoSuite for
 * experiment reasons, but its usage for practitioners
 * will be based on build environments like Maven/Ant
 * and continuous integration environments like 
 * Jenkins/Hudson/Bamboo 
 * </p>
 * 
 * <p>
 * In a nutshell, the idea is for a project to keep track/store
 * the best test suite for each class in the project.
 * Each time CTG is run (and can be run continuously), we try
 * to generate test suites for classes we haven't addressed
 * yet, and we try to improve the current ones (by seeding).
 * Benefits are for example:
 * <ul>  
 * <li> No need to download EvoSuite. This is done automatically
 * with Maven </li>
 * <li> Smart allocation of search budget among classes with
 * different size/difficulty</li>
 * <li><b>Very</b> easily configurable to run 24/7, with reports on entire
 * project</li>
 * <li> Possibility to reuse test cases/data from one class to
 * help generation of test suites for new classes</li>
 * <li>Can automatically check from repository which classes
 * have been modified, and concentrate search on them</li>
 * </ul>
 * </p>
 * 
 * <p>
 * CTG can also be configured to "call home".
 * This will be useful to see how EvoSuite is used in practice.
 * </p>
 * 
 * 
 * <p>
 * TODO we should also have an option "regression" to run
 * current test suites, and see if any fails.
 * Even if do not want to explicitly do regression, we might still
 * have to run old test cases, as failing ones should be (re)moved and
 * labelled as "regression-failing"
 * 
 * 
 * <p>
 * TODO need also option to automatically commit to repository any new, better test
 * 
 * @author arcuri
 *
 */
public class ContinuousTestGeneration {


	/**
	 * Target folder/jar defining the SUT
	 */
	private final String target;
    
	/**
	 * Defines what classes in the target should be used by specifying 
	 * a common package prefix
	 */
	private final String prefix;
	
    /**
     *  The complete, used classpath
     */
    private final String projectClassPath;
	
    private CtgConfiguration configuration;

	/**
	 * An optional folder where to make a copy of the generated tests
	 */
	private final String exportFolder;

    /**
     * Specify which CUT to use. If {@code null} then use everything in target/prefix
     */
    private String[] cuts; 
    
    public ContinuousTestGeneration(String target, String projectClassPath, String prefix, CtgConfiguration conf, String[] cuts,
									String exportFolder) {
		super();		
		this.target = target;
		this.prefix = prefix;
		this.projectClassPath = projectClassPath;
		this.configuration = conf;
		this.cuts = cuts;
		this.exportFolder = exportFolder;
	}
	
    /**
     * Apply CTG, and return a string with some summary
     * 
     * @return
     */
	public String execute() {

		//init the local storage manager
		StorageManager storage = new StorageManager();
		if (!storage.isStorageOk()) {
			return "Failed to initialize local storage system";
		}

		if(Properties.CTG_DELETE_OLD_TMP_FOLDERS){
			storage.deleteAllOldTmpFolders();
		}

		if (!storage.createNewTmpFolders()) {
			return "Failed to create tmp folders";
		}

		//check project
		ProjectAnalyzer analyzer = new ProjectAnalyzer(target, prefix, cuts);
		ProjectStaticData data = analyzer.analyze();

		if (data.getTotalNumberOfTestableCUTs() == 0) {
			return "There is no class to test in the chosen project\n" +
					"Target: " + target + "\n" +
					"Prefix: '" + prefix + "'\n";
		}

		if(Properties.CTG_DEBUG_PORT != null && data.getTotalNumberOfTestableCUTs() != 1){
			throw new IllegalStateException("Cannot debug CTG when more than one CUT is selected");
		}

		if (Properties.CTG_TIME_PER_CLASS != null) {
			configuration = configuration.getWithChangedTime(Properties.CTG_TIME_PER_CLASS, data.getTotalNumberOfTestableCUTs());
		}

		JobScheduler scheduler = new JobScheduler(data, configuration);
		JobExecutor executor = new JobExecutor(storage, projectClassPath, configuration);

		//loop: define (partial) schedule
		while (scheduler.canExecuteMore()) {
			List<JobDefinition> jobs = scheduler.createNewSchedule();
			executor.executeJobs(jobs, configuration.getNumberOfUsableCores());
			executor.waitForJobs();
		}

		String description = storage.mergeAndCommitChanges(data, cuts);

		if(exportFolder != null){
			try {
				exportToFolder(".",exportFolder);
			} catch (IOException e) {
				return "Failed to export tests: "+e.getMessage();
			}
		}

		//call home
		if (configuration.callHome) {
			//TODO
		}

		return description;
	}

	public static File resolveExportFolder(String baseFolder, String exportFolder){

		Path exp = Paths.get(exportFolder);
		if(exp.isAbsolute()){
			return exp.toFile();
		} else {
			return Paths.get(baseFolder,exportFolder).toAbsolutePath().toFile();
		}
	}


	public static boolean exportToFolder(String baseFolder, String exportFolder) throws IOException {
		File basedir = new File(baseFolder);
		File evoFolder = StorageManager.getBestTestFolder(basedir);

		File[] children = evoFolder.listFiles();
		boolean isEmpty = children==null || children.length==0;

		if(isEmpty){
			return false;
		}

		File target = resolveExportFolder(baseFolder, exportFolder);


		//FileUtils.copyDirectory(evoFolder, target); //This did not overwrite old files!
		FileIOUtils.copyDirectoryAndOverwriteFilesIfNeeded(evoFolder,target);
		return true;
	}

	/**
     * Clean all persistent data (eg files on disk) that
     * CTG has created so far
     */
    public boolean clean(){
    		StorageManager storage = new StorageManager();
    		return storage.clean();
    }
    
    /**
     * Get info on the current test cases in the database
     * @return
     */
    public String info(){
    		
		Project project = StorageManager.getDatabaseProject(); 
		
		if(project==null){
			return "No info available";
		}
		
		//TODO all info
		
		StringBuilder sb = new StringBuilder();
		sb.append("Total number of classes in the project: "+
				ProjectUtil.getNumberTestableClasses(project)+"\n");
		sb.append("Number of classes in the project that are testable: "+
				ProjectUtil.getNumberTestableClasses(project)+"\n");
    		sb.append("Number of generated test suites: "+
    				ProjectUtil.getNumberGeneratedTestSuites(project)+"\n");
		sb.append("Overall coverage: "+
    				ProjectUtil.getOverallCoverage(project)+"\n");
    		
    		return sb.toString();
    }
}
