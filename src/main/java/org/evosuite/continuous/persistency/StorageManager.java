package org.evosuite.continuous.persistency;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.evosuite.xsd.Project;
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

	public StorageManager(String rootFolderName) {
		super();
		this.rootFolderName = rootFolderName;
	}

	public StorageManager(){
		this(".continuous_evosuite");
	}

	/**
	 * Open connection to Storage Manager
	 * 
	 * @return
	 */
	public boolean open(){

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

	public Project getProjectInfo(){
		
		File current = new File(projectFileName);
		if(!current.exists()){
			return null;
		}
		
		try{
			JAXBContext jaxbContext = JAXBContext.newInstance(Project.class);
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(new StreamSource(
					new File(ClassLoader.getSystemResource("/xsd/ctg_project_report.xsd").toURI())));
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			jaxbUnmarshaller.setSchema(schema);
			Project project = (Project) jaxbUnmarshaller.unmarshal(current);
			return project;
		} catch(Exception e){
			logger.error("Error in reading "+current.getAbsolutePath()+". "+e,e);
			return null;
		}
	}
}
