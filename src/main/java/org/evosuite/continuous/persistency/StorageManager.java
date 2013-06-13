package org.evosuite.continuous.persistency;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.evosuite.continuous.project.ProjectStaticData;
import org.evosuite.xsd.ProjectInfo;
import org.evosuite.xsd.TestSuite;
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

	private final String rootFolderName;

	private final String projectFileName = "project_info.xml";

	private File tmpFolder;
	private File tmpLogs;
	private File tmpReports;
	private File tmpTests;

	public StorageManager(String rootFolderName) {
		super();
		this.rootFolderName = rootFolderName;
		this.tmpFolder = null;
	}

	public StorageManager(){
		this(".continuous_evosuite");
	}

	/**
	 * Open connection to Storage Manager
	 * 
	 * @return
	 */
	public boolean openForWriting(){

		/*
		 * Note: here we just make sure we can write on disk
		 */

		File root = new File(rootFolderName);
		if(root.exists()){
			if(root.isDirectory()){
				if(root.canWrite()){
					return true;
				} else {
					logger.error("Cannot write in "+root.getAbsolutePath());
					return false;
				}
			} else {
				//it exists but not a folder...
				boolean deleted = root.delete();
				if(!deleted){
					logger.error("Folder "+root+" is a file, and we cannot delete it");
					return false;
				} else {
					// same as "else" of !exist
				}
			}
		}

		boolean created = root.mkdir();
		if(!created){
			logger.error("Failed to mkdir "+root.getAbsolutePath());
		}

		return created;		
	}

	/**
	 * Create a new tmp folder for this CTG session
	 * 
	 * @return
	 */
	public boolean createNewTmpFolders(){
		String tmpPath = rootFolderName+"/tmp/";
		Date now = new Date();
		String time = DateFormatUtils.format(
				now, "yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
		File tmp = new File(tmpPath+"/tmp_"+time);
		boolean created = tmp.mkdirs();

		if(created){
			tmpFolder = tmp;
		} else {
			tmpFolder = null;
			return false;
		}

		//if we created the "tmp" folder, then it should be fine to create new folders in it

		tmpLogs = new File(tmpFolder.getAbsolutePath()+"/logs");
		tmpLogs.mkdirs();
		tmpReports = new File(tmpFolder.getAbsolutePath()+"/reports");
		tmpReports.mkdirs();
		tmpTests = new File(tmpFolder.getAbsolutePath()+"/tests");
		tmpTests.mkdirs();

		return true;
	}


	/**
	 * Delete all CTG files 
	 * @return
	 */
	public boolean clean(){
		try {
			FileUtils.deleteDirectory(new File(rootFolderName));
		} catch (IOException e) {
			logger.error("Cannot delete folder "+rootFolderName+": "+e,e);
			return false;
		}
		return true;
	}

	/**
	 * Compare the results of this CTG run with what was in
	 * the database. Keep/update the best results. 
	 * 
	 * @param data
	 * @return
	 */
	public String mergeAndCommitChanges(ProjectStaticData current){

		ProjectInfo db = getDatabaseProjectInfo();
		removeNoMoreExistentData(db,current);

		/*
		 * Check what test cases have been actually generated
		 * in this CTG run
		 */
		List<File> suites = null;//TODO

		for(File suite : suites){
			if(isBetterThanOldOne(suite,db)){
				updateDatabase(db,suite);
			}
		}

		updateProjectStatistics(db,current);
		commitDatabase(db);

		//TODO
		return null;
	}

	private void commitDatabase(ProjectInfo db) {

		StringWriter writer = null;
		try{
			writer = new StringWriter();
			JAXBContext context = JAXBContext.newInstance(ProjectInfo.class);            
			Marshaller m = context.createMarshaller();
			m.marshal(db, writer);
		} catch(Exception e){
			logger.error("Failed to create XML representation: "+e.getMessage(),e);
		}

		String xml = writer.toString();
		
		/*
		 * TODO: to be safe, we should first write to tmp file, delete original, and then
		 * rename the tmp
		 */
		File current = new File(rootFolderName + File.separator + projectFileName);
		current.delete();
		try {
			FileUtils.write(current, xml);
		} catch (IOException e) {
			logger.error("Failed to write to database: "+e.getMessage(),e);
		}
	}

	private void updateProjectStatistics(ProjectInfo db, ProjectStaticData current) {

		db.setTotalNumberOfClasses(BigInteger.valueOf(current.getTotalNumberOfClasses()));
		int n = current.getTotalNumberOfTestableCUTs();
		db.setTotalNumberOfTestableClasses(BigInteger.valueOf(n));

		double coverage = 0d;
		for(TestSuite suite : db.getGeneratedTestSuites()){
			coverage += suite.getBranchCoverage();
		}

		coverage = coverage / (double) n;
		db.setAverageBranchCoverage(coverage);
	}

	private void updateDatabase(ProjectInfo db, File suite) {
		// TODO Auto-generated method stub

	}

	private boolean isBetterThanOldOne(File suite, ProjectInfo db) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Some classes could had been removed/renamed.
	 * So just delete all info regarding them
	 * 
	 * @param data
	 */
	private void removeNoMoreExistentData(ProjectInfo db,
			ProjectStaticData current) {

		Iterator<TestSuite> iter = db.getGeneratedTestSuites().iterator();
		while(iter.hasNext()){
			TestSuite suite = iter.next();
			String cut = suite.getFullNameOfTargetClass();
			if(! current.containsClass(cut)){
				iter.remove();
			}
		}
	}


	/**
	 * Get current representation of the test cases in the database
	 * 
	 * @return
	 */
	public ProjectInfo getDatabaseProjectInfo(){

		File current = new File(rootFolderName + File.separator + projectFileName);
		if(!current.exists()){
			return null;
		}

		try{
			JAXBContext jaxbContext = JAXBContext.newInstance(ProjectInfo.class);
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(new StreamSource(
					new File(ClassLoader.getSystemResource("/xsd/ctg_project_report.xsd").toURI())));
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			jaxbUnmarshaller.setSchema(schema);
			ProjectInfo project = (ProjectInfo) jaxbUnmarshaller.unmarshal(current);
			return project;
		} catch(Exception e){
			logger.error("Error in reading "+current.getAbsolutePath()+". "+e,e);
			return null;
		}
	}

	public File getTmpFolder() {
		return tmpFolder;
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
}
