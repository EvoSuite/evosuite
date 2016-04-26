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
package org.evosuite.continuous.persistency;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.evosuite.Properties;
import org.evosuite.continuous.project.ProjectStaticData;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.FileIOUtils;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.xsd.CUT;
import org.evosuite.xsd.CUTUtil;
import org.evosuite.xsd.Coverage;
import org.evosuite.xsd.Generation;
import org.evosuite.xsd.GenerationUtil;
import org.evosuite.xsd.Project;
import org.evosuite.xsd.ProjectUtil;
import org.evosuite.xsd.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Class used to store all CTG info on disk
 * 
 * @author arcuri
 *
 */
public class StorageManager {

	private static Logger logger = LoggerFactory.getLogger(StorageManager.class);

	private static final String TMP_PREFIX = "tmp_";

    private File tmpLogs = null;
	private File tmpReports = null;
	private File tmpTests = null;
	private File tmpPools = null;
	private File tmpSeeds = null;

	private boolean isStorageOk = false;

	private DecimalFormat df = null;

	public StorageManager() {
		this.isStorageOk = this.openForWriting();
		this.df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
		this.df.applyPattern("#0.00");
	}

	/**
	 * Open connection to Storage Manager
	 * Note: Here we just make sure we can write on disk
	 * 
	 * @return
	 */
	private boolean openForWriting() {

		File root = new File(Properties.CTG_DIR);
		if(root.exists()){
			if(root.isDirectory()){
				if(!root.canWrite()){					
					logger.error("Cannot write in "+root.getAbsolutePath());
					return false;
				}
			} else {
				//it exists but not a folder...
				boolean deleted = root.delete();
				if(!deleted){
					logger.error("Folder "+root+" is a file, and failed to delete it");
					return false;
				} else {
					if(!root.mkdirs()){
						logger.error("Failed to mkdir "+root.getAbsolutePath());
						return false;
					}
				}
			}
		} else {
			if(!root.mkdirs()){
				logger.error("Failed to mkdir "+root.getAbsolutePath());
				return false;
			}
		}

		File testsFolder = getBestTestFolder();
		if(!testsFolder.exists()){
			if(!testsFolder.mkdirs()){
				logger.error("Failed to mkdir "+testsFolder.getAbsolutePath());
				return false;
			}
		}

		File seedFolder = getSeedInFolder();
		if(!seedFolder.exists()){
			if(!seedFolder.mkdirs()){
				logger.error("Failed to mkdir "+seedFolder.getAbsolutePath());
			}
		}

		return true;		
	}

	public static File getBestTestFolder(){
		return getBestTestFolder(null);
	}

	public static File getBestTestFolder(File baseDir){
		String base = "";
		if(baseDir != null){
			base = baseDir.getAbsolutePath() + File.separator;
		}
		return new File(base + Properties.CTG_DIR +
				File.separator + Properties.CTG_BESTS_DIR_NAME);
	}

	public static File getSeedInFolder(){
		return new File(new File(Properties.CTG_DIR),"evosuite-"+Properties.CTG_SEEDS_DIR_NAME);
	}

	/**
	 * Create a new tmp folder for this CTG session
	 * 
	 * @return
	 */
	public boolean createNewTmpFolders() {

		if (!this.isStorageOk) {
			return false;
		}

		String time = DateFormatUtils.format(new Date(), "yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
		File tmp = null;

		if (Properties.CTG_GENERATION_DIR_PREFIX == null)
			tmp = new File(Properties.CTG_DIR + File.separator + TMP_PREFIX + time);
		else
			tmp = new File(Properties.CTG_DIR + File.separator + TMP_PREFIX + Properties.CTG_GENERATION_DIR_PREFIX + "_" + time);

		if (!tmp.mkdirs())
			return false;

		// if we created the "tmp" folder or already exists, then it should be fine to create new folders in it

		this.tmpLogs = new File(tmp.getAbsolutePath() + File.separator + Properties.CTG_TMP_LOGS_DIR_NAME);
		if (!this.tmpLogs.exists() && !this.tmpLogs.mkdirs()) {
			return false;
		}

		this.tmpReports = new File(tmp.getAbsolutePath() + File.separator + Properties.CTG_TMP_REPORTS_DIR_NAME);
		if (!this.tmpReports.exists() && !this.tmpReports.mkdirs()) {
			return false;
		}

		this.tmpTests = new File(tmp.getAbsolutePath() + File.separator + Properties.CTG_TMP_TESTS_DIR_NAME);
		if (!this.tmpTests.exists() && !this.tmpTests.mkdirs()) {
			return false;
		}

		this.tmpPools = new File(tmp.getAbsolutePath() + File.separator + Properties.CTG_TMP_POOLS_DIR_NAME);
		if (!this.tmpPools.exists() && !this.tmpPools.mkdirs()) {
			return false;
		}

		this.tmpSeeds = new File(tmp.getAbsolutePath() + File.separator + Properties.CTG_SEEDS_DIR_NAME);
		if (!this.tmpSeeds.exists() && !this.tmpSeeds.mkdirs()) {
			return false;
		}

		return true;
	}


	public void deleteAllOldTmpFolders(){

		File root = new File(Properties.CTG_DIR);
		for(File child : root.listFiles()){
			if(!child.isDirectory()){
				continue;
			}
			if(child.getName().startsWith(TMP_PREFIX)){
				try {
					FileUtils.deleteDirectory(child);
				} catch (IOException e) {
					logger.error("Failed to delete tmp folder "+child.getAbsolutePath());
				}
			}
		}
	}

	/**
	 * Delete all CTG files 
	 * @return
	 */
	public boolean clean(){
		try {
			FileUtils.deleteDirectory(new File(Properties.CTG_DIR));
		} catch (IOException e) {
			logger.error("Cannot delete folder "+Properties.CTG_DIR+": "+e,e);
			return false;
		}
		return true;
	}

	public static class TestsOnDisk{
		public final File testSuite;
		public final String cut;
		public final CsvJUnitData csvData;
		public final File serializedSuite;
		
		public TestsOnDisk(File testSuite, CsvJUnitData csvData, File serializedSuite) {
			super();
			this.testSuite = testSuite;
			this.csvData = csvData;
			this.cut = csvData.getTargetClass();
			this.serializedSuite = serializedSuite; //this might be null
		}

		public boolean isValid(){
			return testSuite!=null && testSuite.exists() &&
					cut!=null && !cut.isEmpty() &&
					csvData!=null && 
					cut.equals(csvData.getTargetClass()) &&
					(serializedSuite==null || serializedSuite.getName().endsWith(Properties.CTG_SEEDS_EXT))
					;
		}
	}
	
	/**
	 * Compare the results of this CTG run with what was in
	 * the database. Keep/update the best results. 
	 * 
	 * @param
	 * @return
	 */
	public String mergeAndCommitChanges(ProjectStaticData current, String[] cuts) throws NullPointerException{

		if(current == null){
			throw new NullPointerException("ProjectStaticData 'current' cannot be null");
		}
		
		Project db = StorageManager.getDatabaseProject();
		String info = "\n\n=== CTG run results ===\n";

		info += removeNoMoreExistentData(db, current);

		List<TestsOnDisk> suites = gatherGeneratedTestsOnDisk();
		info += "\nNew test suites: " + suites.size();

		// identify for which CUTs we failed to generate tests
		Set<String> missingCUTs = new LinkedHashSet<String>();

		db.setTotalNumberOfTestableClasses(BigInteger.valueOf(current.getTotalNumberOfTestableCUTs()));
		for (String cut : current.getClassNames()) {
		    if (!current.getClassInfo(cut).isTestable()) {
		        // if a class is not testable, we don't need to update any database
		        // of that class. and not even counting it as a missing class
		        continue ;
		    }

		    TestsOnDisk suite = suites.parallelStream().filter(s -> s.cut.equals(cut)).findFirst().orElse(null);
		    if (suite == null && current.getClassInfo(cut).isToTest()) {
                missingCUTs.add(cut);
            }

		    LoggingUtils.getEvoLogger().info("* Updating database to " + cut);
		    updateDatabase(cut, suite, db, current);
		}

		/*
         * Print out what class(es) EvoSuite failed to generate
         * test cases in this CTG run
         */

		if (!missingCUTs.isEmpty()) {
		    if (missingCUTs.size() == 1) {
		        info += "\n\nWARN: failed to generate tests for " + missingCUTs.iterator().next();
		    } else {
		        info += "\n\nMissing classes:";
		        for (String missingCUT : missingCUTs) {
		            info += "\n" + missingCUT;
		        }
		        String summary = "\n\nWARN: failed to generate tests for " + missingCUTs.size() + " classes out of " + current.getTotalNumberOfTestableCUTs();
                info += summary;
		    }
		}

		commitDatabase(db);
		return info;
	}

	/**
	 * Not only we need the generated JUnit files, but also the statistics
	 * on their execution.
	 * Note: in theory we could re-execute the test cases to extract/recalculate
	 * those statistics, but it would be pretty inefficient
	 * 
	 * @return  a List containing all info regarding generated tests in the last CTG run
	 */
	public List<TestsOnDisk> gatherGeneratedTestsOnDisk(){
		
		List<TestsOnDisk> list = new LinkedList<TestsOnDisk>();
		List<File> generatedTests = FileIOUtils.getRecursivelyAllFilesInAllSubfolders(tmpTests.getAbsolutePath(), ".java");
		List<File> generatedReports = FileIOUtils.getRecursivelyAllFilesInAllSubfolders(tmpReports.getAbsolutePath(), ".csv");
		List<File> generatedSerialized = FileIOUtils.getRecursivelyAllFilesInAllSubfolders(tmpSeeds.getAbsolutePath(), Properties.CTG_SEEDS_EXT);

		/*
		 * Key -> name of CUT
		 * Value -> data extracted from CSV file 
		 * 
		 * We use a map, otherwise we could have 2 inner loops going potentially on thousands
		 * of classes, ie, O(n^2) complexity
		 */
		Map<String,CsvJUnitData> reports = new LinkedHashMap<>();
		for(File file : generatedReports){
			CsvJUnitData data = CsvJUnitData.openFile(file);
			if(data==null){
				logger.warn("Cannot process "+file.getAbsolutePath());
			} else {
				reports.put(data.getTargetClass(), data);
			}
		}

		/*
		 * Key -> class name of CUT
		 * Value -> file location of serialized test suite
		 */
		Map<String,File> seeds = new LinkedHashMap<>();
		for(File file : generatedSerialized){
			//this assumes that seed files are in the form cutName.seed
			String cut = file.getName().substring(0 , file.getName().length() - (Properties.CTG_SEEDS_EXT.length() + 1));
			seeds.put(cut,file);
		}

		/*
		 * Try to extract info for each generated JUnit test suite
		 */
		for(File test : generatedTests){
			if (test.getAbsolutePath().contains(Properties.SCAFFOLDING_SUFFIX)) {
				continue ;
			}
			
			String testName = extractClassName(tmpTests,test);
			
			String cut = "";
			for(String className : reports.keySet()){
				/*
				 * This is tricky. We cannot be 100% what is going to be appended to the
				 * class name to form the test name, although the class name should still
				 * be a prefix. We need to check for the longest prefix as to avoid cases like
				 * 
				 * org.Foo
				 * org.Foo2
				 */
				if(testName.startsWith(className) && className.length() > cut.length()){
					cut = className;
				}
			}
			//String cut = testName.substring(0, testName.indexOf(junitSuffix)); //This does not work, eg cases like _N_suffix
						
			CsvJUnitData data = reports.get(cut);
			if(data==null){
				logger.warn("No CSV file for CUT "+cut+" with test suite at "+test.getAbsolutePath());
				continue;
			}

			File seed = seeds.get(cut);
			if(seed == null){
				logger.warn("No '"+Properties.CTG_SEEDS_EXT+"' file was generated for CUT "+cut);
				//do not skip, as this might happen if custom factory (ie no archive) was used for some experiments
			}

			TestsOnDisk info = new TestsOnDisk(test, data, seed);
			if(info.isValid()){
				list.add(info);
			} else {
				logger.warn("Invalid info for "+test.getAbsolutePath());
			}
		}
		
		return list; 
	}
	
	/**
	 * Example: </br>
	 * base   = /some/where/in/file/system  </br>
	 * target = /some/where/in/file/system/com/name/of/a/package/AClass.java  </br>
	 * </br>
	 * We want "com.name.of.a.package.AClass" as a result
	 * 
	 */
	protected String extractClassName(File base, File target){		
		int len = base.getAbsolutePath().length();
		String path = target.getAbsolutePath(); 
		String name = path.substring(len+1,path.length()-".java".length());
		
		/*
		 * Using File.separator seems to give problems in Windows, because "\\" is treated specially
		 * by the replaceAll method
		 */
		name = name.replaceAll("/",".");
		
		if(name.contains("\\")){
			name = name.replaceAll("\\\\",".");
		}
		
		return name;
	}
	
	
	private void commitDatabase(Project db) {

		StringWriter writer = null;
		try{
			writer = new StringWriter();
			JAXBContext context = JAXBContext.newInstance(Project.class);            
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); // TODO remove me!
			m.marshal(db, writer);
		} catch(Exception e){
			logger.error("Failed to create XML representation: "+e.getMessage(),e);
		}
		
		/*
		 * TODO: to be safe, we should first write to tmp file, delete original, and then
		 * rename the tmp
		 */
		File current = getProjectInfoFile();
		current.delete();
		try {
			FileUtils.write(current, writer.toString());
		} catch (IOException e) {
			logger.error("Failed to write to database: "+e.getMessage(),e);
		}
	}

	private static File getProjectInfoFile(){
		return new File(Properties.CTG_DIR + File.separator + Properties.CTG_PROJECT_INFO);
	}

	/**
	 * Not only modify the state of <code>db</code>, but
	 * also copy/replace new test cases on file disk
	 * 
	 * @param ondisk
	 * @param db
	 */
	private void updateDatabase(String targetClass, TestsOnDisk ondisk, Project db, ProjectStaticData current) {

  	    String testName = targetClass + Properties.JUNIT_SUFFIX; //extractClassName(tmpTests, ondisk.testSuite);

		// CUT data

		CUT cut = ProjectUtil.getCUT(db, targetClass);
		if (cut == null) {
		    // first generation
			cut = new CUT();
			cut.setFullNameOfTargetClass(targetClass);
			cut.setFullNameOfTestSuite(testName);

			db.getCut().add(cut);
		}

		// Generation data

		Generation generation = new Generation();
		generation.setId(BigInteger.valueOf(cut.getGeneration().size()));
		generation.setFailed(false); // by default
		generation.setModified(current.getClassInfo(targetClass).hasChanged());
		generation.setTimeBudgetInSeconds(BigInteger.valueOf(current.getClassInfo(targetClass).getTimeBudgetInSeconds()));
		generation.setMemoryInMB(BigInteger.valueOf(current.getClassInfo(targetClass).getMemoryInMB()));

		if (!current.getClassInfo(targetClass).isToTest()) {
            // if a class was not considered for testing purpose,
            // we still want to keep some information about it.
            // that information will be crucial to, for example,
            // determine how much time EvoSuite spent over all classes
		    cut.getGeneration().add(generation);
		    return ; // we do not have more information, so return
        }

		File std_err_CLIENT = new File(this.tmpLogs + File.separator + targetClass
            + File.separator + "std_err_CLIENT.log");
		assert std_err_CLIENT.exists();
	    File std_out_CLIENT = new File(this.tmpLogs + File.separator + targetClass
            + File.separator + "std_out_CLIENT.log");
	    assert std_out_CLIENT.exists();
	    File std_err_MASTER = new File(this.tmpLogs + File.separator + targetClass
            + File.separator + "std_err_MASTER.log");
	    assert std_err_MASTER.exists();
	    File std_out_MASTER = new File(this.tmpLogs + File.separator + targetClass
            + File.separator + "std_out_MASTER.log");
	    assert std_out_MASTER.exists();
		generation.setStdErrCLIENT(std_err_CLIENT.getAbsolutePath());
		generation.setStdOutCLIENT(std_out_CLIENT.getAbsolutePath());
		generation.setStdErrMASTER(std_err_MASTER.getAbsolutePath());
		generation.setStdOutMASTER(std_out_MASTER.getAbsolutePath());

		cut.getGeneration().add(generation);

		if (ondisk == null) {
		    // EvoSuite failed to generate any test case for 'targetClass'.
		    // was it supposed to happen?
		    if (current.getClassInfo(targetClass).isToTest()) {
		        // it should have generated test cases
		        generation.setFailed(true);

	            /*
	             * TODO to properly update failure data, we will first need
	             * to change how we output such info in EvoSuite (likely
	             * we will need something more than statistics.csv) 
	             */
		    }

		    return;
		}

        assert ondisk.isValid();
        CsvJUnitData csv = ondisk.csvData;

		if (!isBetterThanAnyExistingTestSuite(db, current, ondisk)) {
		    // if the new test suite is not better than any other
		    // test suite (manually written or generated), we don't
		    // accept the new test suite and we just keep information
		    // about EvoSuite execution.
		    return;
		}

		// Test Suite data

		TestSuite suite = new TestSuite();
		suite.setFullPathOfTestSuite(ondisk.testSuite.getAbsolutePath());
		suite.setNumberOfTests(BigInteger.valueOf(csv.getNumberOfTests()));
		suite.setTotalNumberOfStatements(BigInteger.valueOf(csv.getTotalNumberOfStatements()));
		suite.setTotalEffortInSeconds(BigInteger.valueOf(csv.getDurationInSeconds()));

		List<Coverage> coverageValues = new ArrayList<Coverage>();
		for (String criterion : csv.getCoverageVariables()) {
		    Coverage coverage = new Coverage();
		    coverage.setCriterion(criterion);
		    coverage.setCoverageValue(Double.parseDouble(this.df.format(csv.getCoverage(criterion))));
		    coverage.setCoverageBitString(csv.getCoverageBitString(criterion));

		    coverageValues.add(coverage);
		}

		suite.getCoverage().addAll(coverageValues);
		generation.setSuite(suite);

		/*
		 * So far we have modified only the content of db.
		 * Need also to update the actual test cases 
		 */
		removeBestTestSuite(testName);
		addBestTestSuite(ondisk.testSuite);

		File scaffolding = getScaffoldingIfExists(ondisk.testSuite);
		if (scaffolding != null) {
			addBestTestSuite(scaffolding);
		}

		if (ondisk.serializedSuite != null) {
			File target = new File(getSeedInFolder(), ondisk.serializedSuite.getName());
			target.delete();
			try {
				FileUtils.copyFile(ondisk.serializedSuite, target);
			} catch (IOException e) {
				logger.error("Failed to copy over a new generated serialized test suite: "+e.getMessage(),e);
			}
		}
	}

	private File getScaffoldingIfExists(File testSuite) throws IllegalArgumentException{

		String java = ".java";
		
		if(testSuite==null || !testSuite.exists() || !testSuite.getName().endsWith(java)){
			throw new IllegalArgumentException("Invalid test suite: "+testSuite);
		}
		
		String name = testSuite.getName();
		String scaffoldingName = name.substring(0, name.length() - java.length()); //remove .java at the end
		scaffoldingName += "_"+Properties.SCAFFOLDING_SUFFIX;
		scaffoldingName += java;
		
		File scaffolding = new File(testSuite.getParentFile().getAbsolutePath() + File.separator + scaffoldingName);
		if(scaffolding.exists()){
			return scaffolding;
		} else {		
			return null;
		}
	}
	
	/**
	 * From the test suites generated in the last CTG run, add the given
	 * one to the current best set 
	 *   
	 * @param newlyGeneratedTestSuite
	 */
	private void addBestTestSuite(File newlyGeneratedTestSuite) {
		String testName = extractClassName(tmpTests,newlyGeneratedTestSuite);
		
		String path = testName.replace(".", File.separator) + ".java";
		File file = new File(getBestTestFolder() + File.separator + path);
		file.delete(); //the following copy does not overwrite

		try {
			FileUtils.copyFile(newlyGeneratedTestSuite, file);
		} catch (IOException e) {
			logger.error("Failed to copy new generated test suite into the current best set: "+e.getMessage(),e);
		}
	}

	/**
	 * Before accepting a new generated test suite, this function
	 * checks if it improves coverage of any existing test suite.
	 * The coverage of any existing test suite can be obtained
	 * using mvn evosuite:coverage, which creates a evosuite-report/statistics.csv
	 * file with code coverage. This function first verifies whether
	 * it a new class (or a class that has been modified). Note that
	 * by default and to be compatible with all Schedules, we consider
	 * that a class has always been modified, unless HistorySchedule
	 * says different. Then it checks if the new generated test suite
	 * has better coverage (or if it covers different goals). If yes,
	 * it returns true (and the generated test suite is accepted),
	 * false otherwise.
	 * 
	 * @param db
	 * @param current
	 * @param suite
	 * @return true is the generated test suite is better (in terms of
	 * coverage) than any existing test suite, false otherwise
	 */
	private boolean isBetterThanAnyExistingTestSuite(Project db, ProjectStaticData current, TestsOnDisk suite) {

		if (suite.csvData == null) {
			// no data available
			return false; 
		}

		// first check if the class under test has been changed or if
		// is a new class. if yes, accept the generated TestSuite
		// (even without checking if the coverage has decreased)
		// note: by default a class has been changed
		if (current.getClassInfo(suite.cut).hasChanged()) {
			return true;
		}

		// load evosuite-report/statistics.csv which contains
		// the coverage of each existing test suite

		String statistics = Properties.REPORT_DIR + File.separator + "statistics.csv";
		File statistics_file = new File(statistics);
		if (!statistics_file.exists()) {
			// this could happen if file was manually removed
			// or if is a project without test cases. before giving
		    // up, let's check if it's better than any previous generated
		    // test suite
		    return isBetterThanPreviousGeneration(db, current, suite);
		}

		List<String[]> rows = null;
		try {
			CSVReader reader = new CSVReader(new FileReader(statistics_file));
			rows = reader.readAll();
			reader.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
			return true;
		}

        // select the row of the Class Under Test
        List<String[]> rowCUT = new ArrayList<String[]>();
        rowCUT.add(rows.get(0)); // add header (i.e., column names)
        for (String[] row : rows) {
        	if (ArrayUtil.contains(row, suite.cut)) {
        		rowCUT.add(row);
        		break ;
        	}
        }

        if (rowCUT.size() == 1) {
        	// this could happen if the data of the Class Under
        	// Test was manually removed, or if during the execution
        	// of measureCoverage option something wrong happened.
            // if so, try to compare with a previous generated one
            return isBetterThanPreviousGeneration(db, current, suite);
        }

        // is the OverallCoverage higher?
        double existingOverallCoverage = 0.0;
        double generatedOverallCoverage = 0.0;

        for (String variable : suite.csvData.getCoverageVariables()) {
        	String coverageVariable = CsvJUnitData.getValue(rowCUT, variable);
        	if (coverageVariable == null) {
        		continue ;
        	}

        	generatedOverallCoverage += suite.csvData.getCoverage(variable);
        	existingOverallCoverage += Double.valueOf(coverageVariable);
        }

        // average
        generatedOverallCoverage /= suite.csvData.getNumberOfCoverageValues();
        existingOverallCoverage /= suite.csvData.getNumberOfCoverageValues();

        double covDif = generatedOverallCoverage - existingOverallCoverage; 
        // this check is to avoid issues with double truncation
        if (covDif > 0.0001) {
            return true;
        }

        // coverage seems to be either the same or lower. does the generated
        // test suite cover different goals? we accept the generate TestSuite
        // if it covers at least one goal not covered by the previous test suite
        for (String variable : suite.csvData.getCoverageBitStringVariables()) {
        	String existingCoverage = CsvJUnitData.getValue(rowCUT, variable);
        	if (existingCoverage == null) {
        		continue ;
        	}

        	String generatedCoverage = suite.csvData.getCoverageBitString(variable);
        	if (generatedCoverage.length() != existingCoverage.length()) {
                // accept the new suite, as we can't compare both BitStrings
                return true;
            }

        	for (int i = 0; i < generatedCoverage.length(); i++) {
        		if (existingCoverage.charAt(i) == '0' && generatedCoverage.charAt(i) == '1') {
        			return true;
        		}
        	}
        }

		return false;
	}

	/**
	 * Before accepting the new test suite this function verifies
	 * whether it is better (in terms of coverage) than a previous
	 * test generation. It first checks whether it is a class that
	 * has been modified. By default we consider that a class has
	 * always been changed. Only HistorySchedule will change that
	 * behavior. So, for all Schedules except History we accept
	 * the new generated test suite. For HistorySchedule, and if a
	 * class has not been changed it then checks if the new test
	 * suite improves the coverage of the previous one.
	 * 
	 * @param db
	 * @param current
	 * @param suite
	 * @return true if the generated test suite is better (in terms of
	 * coverage) than a previous generated test suite, false otherwise
	 */
	private boolean isBetterThanPreviousGeneration(Project db, ProjectStaticData current, TestsOnDisk suite) {

  	    if (suite.csvData == null) {
          // no data available
            return false; 
        }

        // first check if the class under test has been changed or if
        // is a new class. if yes, accept the generated TestSuite
        // (even without checking if the coverage has increased/decreased)
        // note: by default we consider that a class has been changed,
  	    // only HistorySchedule change this behavior
        if (current.getClassInfo(suite.cut).hasChanged()) {
            return true;
        }

        CUT cut = ProjectUtil.getCUT(db, suite.cut);
        Generation latestSuccessfulGeneration = CUTUtil.getLatestSuccessfulGeneration(cut);
        if (latestSuccessfulGeneration == null) {
            return true;
        }
        TestSuite previousTestSuite = latestSuccessfulGeneration.getSuite();

		File oldFile = getFileForTargetBestTest(cut.getFullNameOfTestSuite());
		if (!oldFile.exists()) {
			// this could happen if file was manually removed
			return true;
		}

		// is the OverallCoverage higher?
        double previousOverallCoverage = GenerationUtil.getOverallCoverage(latestSuccessfulGeneration);
        double generatedOverallCoverage = 0.0;

		// first, check if the coverage of at least one criterion is better
		for (Coverage coverage : previousTestSuite.getCoverage()) {
		    if (!suite.csvData.hasCoverage(coverage.getCriterion())) {
		        continue ;
            }
            generatedOverallCoverage += suite.csvData.getCoverage(coverage.getCriterion());
		}
		generatedOverallCoverage /= suite.csvData.getNumberOfCoverageValues();

		double covDif = generatedOverallCoverage - previousOverallCoverage; 
        if (covDif > 0.01) {
            // this check is to avoid issues with double truncation
            // by default, the coverage values in the project_info.xml
            // just has two decimal digits
            return true;
        }

		// seems we got the same coverage or lower, what about goals covered?
        // if the new test generation is covering other goals, accept it, as
        // developers could be interested on that particular goal(s)
        for (Coverage coverage : previousTestSuite.getCoverage()) {
            if (!suite.csvData.hasCoverage(coverage.getCriterion())) {
                continue ;
            }

            String generatedCoverage = suite.csvData.getCoverageBitString(coverage.getCriterion());
            String previousCoverage = coverage.getCoverageBitString();
            if (generatedCoverage.length() != previousCoverage.length()) {
                // accept the new suite, as we can't compare both BitStrings
                return true;
            }

            for (int i = 0; i < generatedCoverage.length(); i++) {
                if (previousCoverage.charAt(i) == '0' && generatedCoverage.charAt(i) == '1') {
                    return true;
                }
            }
        }

        if (covDif < 0.0) {
            // a negative difference means that the previous coverage
            // was higher, therefore discard the new test suite
            return false;
        }

        // if we got same coverage, look at size 
		int oldSize = previousTestSuite.getTotalNumberOfStatements().intValue();
		int newSize = suite.csvData.getTotalNumberOfStatements();
		if (newSize != oldSize) {
			return newSize < oldSize;
		}

		// same number of statements, look the number of test cases
		int oldNumTests = previousTestSuite.getNumberOfTests().intValue();
		int newNumTests = suite.csvData.getNumberOfTests();

		return newNumTests < oldNumTests; 
	}

	/**
	 * Some classes could had been removed/renamed.
	 * So just delete all info regarding them
	 * 
	 * @param
	 */
	private String removeNoMoreExistentData(Project db,
			ProjectStaticData current) {

		int removed = 0;
		Iterator<CUT> iter = db.getCut().iterator();
		while(iter.hasNext()){
			CUT cut = iter.next();
			String cutName = cut.getFullNameOfTargetClass();
			if(! current.containsClass(cutName)){
				iter.remove();
				removeBestTestSuite(cut.getFullNameOfTestSuite());		
				removed++;
			}
			
		}
		
		return "Removed test suites: "+removed; 
	}

	/**
	 * Remove the given test suite
	 * 
	 * @param
	 */
	private void removeBestTestSuite(String testName) {

		File file = getFileForTargetBestTest(testName);
		
		if(!file.exists()){
			logger.debug("Nothing to delete, as following file does not exist: "+file.getAbsolutePath());
		} else {
			boolean deleted = file.delete();
			if(!deleted){
				logger.warn("Failed to delete "+file.getAbsolutePath());
			}
		}
	}

	private File getFileForTargetBestTest(String testName) {
		String path = testName.replace(".", File.separator);
		path += ".java";
		return new File(getBestTestFolder() + File.separator + path);
	}

	/**
	 * Get current representation of the test cases in the database
	 * 
	 * @return
	 */
	public static Project getDatabaseProject() {

		File current = getProjectInfoFile();
		InputStream stream = null;
		if(!current.exists()){
			stream = getDefaultXmlStream();
			return getProject(current, stream);
		} else {
			try {
				stream = getCurrentXmlStream(current);
				return getProject(current, stream);
			} catch(Exception e){
				//this could happen if it was an old file, and EvoSuite did not have a proper backward compatibility
				stream = getDefaultXmlStream();
				return getProject(current, stream);
			}
		}


	}

	private static InputStream getCurrentXmlStream(File current) {
		InputStream stream;
		try {
            stream = new FileInputStream(current);
        } catch (FileNotFoundException e) {
            assert false; // this should never happen
            throw new RuntimeException("Bug in EvoSuite framework: "+e.getMessage());
        }
		return stream;
	}

	private static InputStream getDefaultXmlStream() {
		InputStream stream;/*
         * this will happen the first time CTG is run
         */
		String empty = "/xsd/ctg_project_report_empty.xml";
		try {
            stream = StorageManager.class.getResourceAsStream(empty);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read resource "+empty+" , "+e.getMessage());
        }
		return stream;
	}

	private static Project getProject(File current, InputStream stream) {
		try{
			JAXBContext jaxbContext = JAXBContext.newInstance(Project.class);
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(new StreamSource(StorageManager.class.getResourceAsStream("/xsd/ctg_project_report.xsd")));
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			jaxbUnmarshaller.setSchema(schema);
			return (Project) jaxbUnmarshaller.unmarshal(stream);
		} catch(Exception e){
			String msg = "Error in reading "+current.getAbsolutePath()+" , "+e;
			logger.error(msg,e);
			throw new RuntimeException(msg);
		}
	}

	public File getTmpLogs() {
		return tmpLogs;
	}

	public File getTmpReports() {
		return tmpReports;
	}

	public File getTmpTests() {
		return tmpTests;
	}

	public File getTmpPools() {
		return tmpPools;
	}

	public File getTmpSeeds() {
		return tmpSeeds;
	}

	public boolean isStorageOk() {
		return this.isStorageOk;
	}
}
