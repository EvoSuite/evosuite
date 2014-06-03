package org.evosuite.runtime;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.mock.java.io.MockFile;
import org.evosuite.mock.java.io.MockFileInputStream;
import org.evosuite.mock.java.io.MockFileOutputStream;
import org.evosuite.mock.java.io.MockFileReader;
import org.evosuite.mock.java.io.MockFileWriter;
import org.evosuite.mock.java.io.MockPrintStream;
import org.evosuite.mock.java.io.MockPrintWriter;
import org.evosuite.mock.java.io.MockRandomAccessFile;
import org.evosuite.mock.java.lang.MockException;
import org.evosuite.mock.java.lang.MockThrowable;
import org.evosuite.mock.java.util.MockDate;
import org.evosuite.mock.java.util.MockGregorianCalendar;
import org.evosuite.mock.java.util.MockRandom;
import org.evosuite.mock.java.util.logging.MockFileHandler;
import org.evosuite.mock.java.util.logging.MockLogRecord;
import org.evosuite.mock.javax.swing.MockJFileChooser;
import org.evosuite.mock.javax.swing.filechooser.MockFileSystemView;


/**
 * Class used to handle all the mock objects.
 * When a new mock is defined, it has to be statically added
 * to the source code of this class.
 * 
 * <p>
 * Recall that a mock M of class X has to extend X (ie 'class M extends X'),
 * and have the same constructors with same inputs, and same static methods.
 * Note: cannot use override for constructors and static methods.
 * 
 * @author arcuri
 *
 */
public class MockList {

	/**
	 * Return a list of all mock object classes used in EvoSuite.
	 * What is returned depend on which mock types are going to 
	 * be used in the search
	 * 
	 * @return a list of Class objects
	 */
	public static List<Class<?>> getList(){
		
		List<Class<?>>  list = new ArrayList<Class<?>>();
		
		if(Properties.VIRTUAL_FS){
			list.add(MockFile.class);
			list.add(MockFileInputStream.class);
			list.add(MockFileOutputStream.class);
			list.add(MockRandomAccessFile.class);
			list.add(MockFileReader.class);
			list.add(MockFileWriter.class);
			list.add(MockPrintStream.class);
			list.add(MockPrintWriter.class);
			list.add(MockFileHandler.class);
			list.add(MockJFileChooser.class);
			list.add(MockFileSystemView.class);
		}
		
		if(Properties.REPLACE_CALLS) {
			list.add(MockDate.class);
			list.add(MockRandom.class);
			list.add(MockGregorianCalendar.class);
			list.add(MockLogRecord.class);
			list.add(MockThrowable.class);
			list.add(MockException.class);
		}
		
		return list;
	}

	/**
	 * Check if the given class has been mocked
	 * 
	 * @param originalClass
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static boolean shouldBeMocked(String originalClass) throws IllegalArgumentException{		
		return getMockClass(originalClass) != null;
	}
	
	
	/**
	 * Check if the given class is among the mock classes
	 * 
	 * @param mockClass
	 * @return
	 */
	public static boolean isAMockClass(String mockClass) {
		if(mockClass == null){
			return false;
		}
		
		for(Class<?> mock : getList()){
			if(mock.getCanonicalName().equals(mockClass)){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Return the mock class for the given target
	 * 
	 * @param originalClass
	 * @return {@code null} if the target is not mocked
	 */
	public static Class<?> getMockClass(String originalClass) throws IllegalArgumentException{
		if(originalClass==null || originalClass.isEmpty()){
			throw new IllegalArgumentException("Empty className");
		}
		
		for(Class<?> mock : getList()){
			Class<?> target = mock.getSuperclass();
			
			if(target==null){
				continue;
			}
			
			if(originalClass.equals(target.getCanonicalName())){
				return mock;
			}
		}
		
		return null;
	}
}
