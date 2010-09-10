/**
 * 
 */
package de.unisb.cs.st.evosuite.setup;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import de.unisb.cs.st.evosuite.Properties;

/**
 * @author Gordon Fraser
 *
 */
public class ScanProject {

	static String prefix;

	private static List<Class<? >> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<? >> classes = new ArrayList<Class<? >>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
            	try {
            		classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            	} catch(IllegalAccessError e) {
            		System.out.println("Cannot access class "+packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
            	} catch(NoClassDefFoundError e) {
            		System.out.println("Cannot find class "+packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
            	}
            }
        }
        return classes;
    }
	
	private static Class<?>[] getClasses(String packageName)
	throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class<? >> classes = new ArrayList<Class<? >>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new Class[classes.size()]);
	}
	
	/**
	 * Entry point - generate task files
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Scanning project for test suite generation.");
		//MutationProperties.checkProperty(MutationProperties.PROJECT_PREFIX_KEY);
		prefix = Properties.PROJECT_PREFIX;
		System.out.println("Project prefix: "+prefix);
		try {
			getClasses(prefix);
			//TestDetector.scanForTests(prefix);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
