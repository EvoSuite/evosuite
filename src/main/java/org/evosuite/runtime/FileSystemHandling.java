package org.evosuite.runtime;

/**
 * This class is used create files as test data
 * in the test cases.
 * 
 * <p>
 * The methods in this class are the only ones that are going
 * to be used in the generated JUnit files to manipulate
 * the virtual file system.
 * 
 * @author arcuri
 *
 */
public class FileSystemHandling {

	/**
	 * Append a string to the given file.
	 * If the file does not exist, it will be created.
	 * 
	 * @param filePath
	 * @param line
	 * @return 
	 */
	public static boolean appendStringToFile(EvoSuiteFile file, String line){
		
		if(line==null){
			return false;
		}
		
		return appendDataToFile(file,line.getBytes()); 
	}

	/**
	 * Append a byte array to the given file.
	 * If the file does not exist, it will be created.
	 * 
	 * @param filePath
	 * @param data
	 * @return
	 */
	public static boolean appendDataToFile(EvoSuiteFile file, byte[] data){
		return false; //TODO
	}
	
	
	public static boolean createFolder(EvoSuiteFile file){
		return false; //TODO
	}
	
	public static boolean setReadable(EvoSuiteFile file, boolean isReadable){
		return false; //TODO
	}

	public static boolean setWritable(EvoSuiteFile file, boolean isWritable){
		return false; //TODO
	}

	public static boolean setExecutable(EvoSuiteFile file, boolean isExecutable){
		return false; //TODO
	}
	
	public static boolean shouldThrowIOException(EvoSuiteFile file, boolean shouldThrow){
		return false; //TODO
	}
}
