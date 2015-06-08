package org.evosuite.continuous.persistency;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import org.evosuite.utils.Utils;
import org.evosuite.xsd.CriterionCoverage;
import org.evosuite.xsd.ProjectInfo;
import org.evosuite.xsd.TestSuite;
import org.evosuite.xsd.TestSuiteCoverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to store all CTG info on disk
 * 
 * @author arcuri
 *
 */
public class StorageManager {

	private static Logger logger = LoggerFactory.getLogger(StorageManager.class);

    private File tmpLogs = null;
	private File tmpReports = null;
	private File tmpTests = null;
	private File tmpPools = null;
	private File tmpSeeds = null;

	private boolean isStorageOk = false;

	public StorageManager() {
		this.isStorageOk = this.openForWriting();
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

		File testsFolder = new File(Properties.CTG_BESTS_DIR);
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

	public File getSeedInFolder(){
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
			tmp = new File(Properties.CTG_DIR + File.separator + "tmp_" + time);
		else
			tmp = new File(Properties.CTG_DIR + File.separator + Properties.CTG_GENERATION_DIR_PREFIX + "_" + time);

		if (!tmp.mkdir())
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
	 * @param data
	 * @return
	 */
	public String mergeAndCommitChanges(ProjectStaticData current) throws NullPointerException{

		if(current == null){
			throw new NullPointerException("ProjectStaticData 'current' cannot be null");
		}
		
		ProjectInfo db = StorageManager.getDatabaseProjectInfo();
		String info = removeNoMoreExistentData(db, current);

		info += "\n\n=== CTG run results ===";
		
		/*
		 * Check what test cases have been actually generated
		 * in this CTG run
		 */
		List<TestsOnDisk> suites = gatherGeneratedTestsOnDisk();
		info += "\nNew test suites: "+suites.size();
		
		int better = 0;
		for(TestsOnDisk suite : suites){
			//TODO remove check, but only after seeding from previous CTG
			if(isBetterThanOldOne(suite,db)){
				updateDatabase(suite,db);
				better++;
			}
		}
		info += "\nBetter test suites: "+better;
		
		updateProjectStatistics(db,current);
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
		List<File> generatedTests = Utils.getAllFilesInSubFolder(tmpTests.getAbsolutePath(), ".java");
		List<File> generatedReports = Utils.getAllFilesInSubFolder(tmpReports.getAbsolutePath(), ".csv");
		List<File> generatedSerialized = Utils.getAllFilesInSubFolder(tmpSeeds.getAbsolutePath(), Properties.CTG_SEEDS_EXT);

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
			//this asssumes that seed files are in the form cutName.seed
			String cut = file.getName().substring(0 , file.getName().length() - Properties.CTG_SEEDS_EXT.length());
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
	
	
	private void commitDatabase(ProjectInfo db) {

		StringWriter writer = null;
		try{
			writer = new StringWriter();
			JAXBContext context = JAXBContext.newInstance(ProjectInfo.class);            
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
		File current = new File(Properties.CTG_PROJECT_INFO);
		current.delete();
		try {
			FileUtils.write(current, writer.toString());
		} catch (IOException e) {
			logger.error("Failed to write to database: "+e.getMessage(),e);
		}
	}

	private void updateProjectStatistics(ProjectInfo db, ProjectStaticData current) {

		db.setTotalNumberOfClasses(BigInteger.valueOf(current.getTotalNumberOfClasses()));
		int n = current.getTotalNumberOfTestableCUTs();
		db.setTotalNumberOfTestableClasses(BigInteger.valueOf(n));

		double coverage = 0d;
		for (TestSuite suite : db.getGeneratedTestSuites()) {
			TestSuiteCoverage suite_coverage = suite.getCoverageTestSuites().get( suite.getCoverageTestSuites().size() - 1 );

			double criterion_coverage = 0d;
			for (CriterionCoverage c : suite_coverage.getCoverage()) {
				criterion_coverage += c.getCoverageValue();
			}

			coverage += (criterion_coverage / (double) suite_coverage.getCoverage().size());
		}

		coverage = coverage / (double) n;
		db.setOverallCoverage(coverage);
	}

	/**
	 * Not only modify the state of <code>db</code>, but
	 * also copy/replace new test cases on file disk
	 * 
	 * @param ondisk
	 * @param db
	 */
	private void updateDatabase(TestsOnDisk ondisk, ProjectInfo db) {

		assert ondisk.isValid();

		CsvJUnitData csv = ondisk.csvData;

		String testName = extractClassName(tmpTests, ondisk.testSuite);

		TestSuite suite = null;
		Iterator<TestSuite> iter = db.getGeneratedTestSuites().iterator();
		while (iter.hasNext()) {
			TestSuite tmp = iter.next();
			if (tmp.getFullNameOfTargetClass().equals(csv.getTargetClass())) {
				suite = tmp;

				iter.remove();
				break;
			}
		}

		// first generation
		BigInteger totalEffort = BigInteger.ZERO;
		if (suite == null) {
			suite = new TestSuite();
			suite.setFullNameOfTestSuite(testName);
			suite.setFullNameOfTargetClass(csv.getTargetClass());
		} else {
			totalEffort = suite.getTotalEffortInSeconds();
		}

		TestSuiteCoverage new_coverage_test_suite = new TestSuiteCoverage();
		new_coverage_test_suite.setId(BigInteger.valueOf( suite.getCoverageTestSuites().size() ));
		new_coverage_test_suite.setFullPathOfTestSuite(ondisk.testSuite.getAbsolutePath());

		List<CriterionCoverage> coverageValues = new ArrayList<CriterionCoverage>();
		for (String criterion : csv.getCoverageValues().keySet()) {
			CriterionCoverage coverage = new CriterionCoverage();
			coverage.setCriterion(criterion);
			coverage.setCoverageValue(csv.getCoverage(criterion));

			coverageValues.add(coverage);
		}
		new_coverage_test_suite.getCoverage().addAll(coverageValues);

		new_coverage_test_suite.setNumberOfTests(BigInteger.valueOf(csv.getNumberOfTests()));
		new_coverage_test_suite.setTotalNumberOfStatements(BigInteger.valueOf(csv.getTotalNumberOfStatements()));

		BigInteger duration = new BigInteger(String.valueOf(csv.getDurationInSeconds()));
		suite.setTotalEffortInSeconds(totalEffort.add(duration));
		new_coverage_test_suite.setEffortInSeconds(duration);

		suite.getCoverageTestSuites().add(new_coverage_test_suite);
		db.getGeneratedTestSuites().add(suite);

		/*
		 * TODO to properly update failure data, we will first need
		 * to change how we output such info in EvoSuite (likely
		 * we will need something more than statistics.csv)
		 */

		/*
		 * So far we have modified only the content of db.
		 * Need also to update the actual test cases 
		 */
		removeTestSuite(testName);
		addTestSuite(ondisk.testSuite);

		File scaffolding = getScaffoldingIfExists(ondisk.testSuite);
		if (scaffolding != null) {
			addTestSuite(scaffolding);
		}

		if(ondisk.serializedSuite != null){
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
	private void addTestSuite(File newlyGeneratedTestSuite) {
		String testName = extractClassName(tmpTests,newlyGeneratedTestSuite);
		
		String path = testName.replace(".", File.separator) + ".java";
		File file = new File(Properties.CTG_BESTS_DIR + File.separator + path);
		file.delete(); //the following copy does not overwrite

		try {
			FileUtils.copyFile(newlyGeneratedTestSuite, file);
		} catch (IOException e) {
			logger.error("Failed to copy new generated test suite into the current best set: "+e.getMessage(),e);
		}
	}

	/*
		this not really reliable, as CUT could have changed. tracking compilation would be cumbersome (many edge
		cases), and not even so useful, as we would lose info each time of a "mvn clean"
	 */
	@Deprecated
	private boolean isBetterThanOldOne(TestsOnDisk suite, ProjectInfo db) {

		if(suite.csvData == null) {
			// no data available
			return false; 
		}
		
		TestSuite old = null;
		for(TestSuite tmp : db.getGeneratedTestSuites()){
			if(tmp.getFullNameOfTargetClass().equals(suite.cut)){
				old = tmp;
				break;
			}
		}
		
		if(old == null){
			// there is no old test suite, so accept new one
			return true;
		}

		File oldFile = getFileForTargetBestTest(old.getFullNameOfTestSuite());
		if(!oldFile.exists()){
			//this could happen if file was manually removed
			return true;
		}

		// first, check if the coverage of at least one criterion is better
		TestSuiteCoverage previousCoverage = old.getCoverageTestSuites().get( old.getCoverageTestSuites().size() - 1 );
		for (CriterionCoverage criterion : previousCoverage.getCoverage()) {
			if (!suite.csvData.hasCriterion(criterion.getCriterion())) {
				continue ;
			}
			double oldCov = criterion.getCoverageValue();
			double newCov = suite.csvData.getCoverage(criterion.getCriterion());
			double covDif = Math.abs(newCov - oldCov); 

			if (covDif > 0.0001) {
				/*
				 * this check is to avoid issues with double truncation 
				 */
				return newCov > oldCov;
			}
		}

		// ok, coverage seems the same, so look at failures
		int oldFail = previousCoverage.getFailures().size();
		int newFail = suite.csvData.getTotalNumberOfFailures();
		if (newFail != oldFail) {
			return newFail > oldFail;
		}

		// everything seems same, so look at size 
		int oldSize = previousCoverage.getTotalNumberOfStatements().intValue();
		int newSize = suite.csvData.getTotalNumberOfStatements();
		if (newSize != oldSize) {
			return newSize < oldSize;
		}

		// at last, look the number of test cases
		int oldNumTests = previousCoverage.getNumberOfTests().intValue();
		int newNumTests = suite.csvData.getNumberOfTests();

		return newNumTests <= oldNumTests; 
	}

	/**
	 * Some classes could had been removed/renamed.
	 * So just delete all info regarding them
	 * 
	 * @param
	 */
	private String removeNoMoreExistentData(ProjectInfo db,
			ProjectStaticData current) {

		int removed = 0;
		Iterator<TestSuite> iter = db.getGeneratedTestSuites().iterator();
		while(iter.hasNext()){
			TestSuite suite = iter.next();
			String cut = suite.getFullNameOfTargetClass();
			if(! current.containsClass(cut)){
				iter.remove();
				removeTestSuite(suite.getFullNameOfTestSuite());		
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
	private void removeTestSuite(String testName) {

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
		return new File(Properties.CTG_BESTS_DIR + File.separator + path);
	}

	/**
	 * Get current representation of the test cases in the database
	 * 
	 * @return
	 */
	public static ProjectInfo getDatabaseProjectInfo(){

		File current = new File(Properties.CTG_PROJECT_INFO);
		InputStream stream = null;
		if(!current.exists()){
			/*
			 * this will happen the first time CTG is run
			 */
			String empty = "/xsd/ctg_project_report_empty.xml";
			try {
				stream = StorageManager.class.getResourceAsStream(empty);
			} catch (Exception e) {
				throw new RuntimeException("Failed to read resource "+empty+" , "+e.getMessage());
			}
		} else {
			try {
				stream = new FileInputStream(current);
			} catch (FileNotFoundException e) {
				assert false; // this should never happen
				throw new RuntimeException("Bug in EvoSuite framework: "+e.getMessage());
			}
		}

		try{
			JAXBContext jaxbContext = JAXBContext.newInstance(ProjectInfo.class);
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(new StreamSource(StorageManager.class.getResourceAsStream("/xsd/ctg_project_report.xsd")));
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			jaxbUnmarshaller.setSchema(schema);
			ProjectInfo project = (ProjectInfo) jaxbUnmarshaller.unmarshal(stream);
			return project;
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
