package org.evosuite.runtime;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.mock.java.io.MockFile;
import org.evosuite.mock.java.io.MockFileInputStream;
import org.evosuite.mock.java.io.MockFileOutputStream;
import org.evosuite.mock.java.io.MockRandomAccessFile;

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
		}
		
		return list;
	}

	/**
	 * Check if the given class has been mocked
	 * 
	 * @param className
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static boolean shouldBeMocked(String className) throws IllegalArgumentException{
		
		if(className==null || className.isEmpty()){
			throw new IllegalArgumentException("Empty className");
		}
		
		for(Class<?> mock : getList()){
			Class<?> target = mock.getSuperclass();
			
			if(target==null){
				continue;
			}
			
			if(className.equals(target.getCanonicalName())){
				return true;
			}
		}
		
		return false;
	}
}
