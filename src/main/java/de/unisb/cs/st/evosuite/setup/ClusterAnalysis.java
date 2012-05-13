/**
 * 
 */
package de.unisb.cs.st.evosuite.setup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.utils.LoggingUtils;

/**
 * @author fraser
 * 
 */
public class ClusterAnalysis {

	private static Logger logger = LoggerFactory.getLogger(ClusterAnalysis.class);

	private static Map<String, Set<String>> inheritanceData = new HashMap<String, Set<String>>();

	private static Map<String, Set<String>> parameterData = new HashMap<String, Set<String>>();

	private static Map<String, Set<String>> generatorData = new HashMap<String, Set<String>>();

	private static Set<String> abstractClasses = new HashSet<String>();

	/**
	 * During runtime, we do not want to consider standard classes to safe some
	 * time, so we perform this analysis only once.
	 */
	public static void generateJDKCluster() {
		Collection<String> list = getAllResources();
		for (String name : list) {
			//			if (!name.startsWith("de/unisb")) {
			// We do not consider sun.* and apple.* and com.* 
			if (name.startsWith("java/") // || name.startsWith("sun") || name.startsWith("com/sun") 
			        || name.startsWith("javax/")) { // || name.startsWith("java/awt")) {
				try {
					ClassReader reader = new ClassReader(
					        name.replace("/", ".").replace(".class", ""));

					InheritanceClassAdapter cv = new InheritanceClassAdapter(null);
					reader.accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES
					        | ClassReader.SKIP_DEBUG);
				} catch (IOException e) {
					System.err.println("Error loading class: "
					        + name.replace("/", ".").replace(".class", ""));
				}
			}
		}

		// Need to filter down classes
		generatorData.remove("java.lang.Class");
		generatorData.remove("java.lang.Object");
		generatorData.remove("java.lang.String");
		generatorData.remove("java.lang.Comparable");

		inheritanceData.remove("java.lang.Object");
		inheritanceData.remove("java.io.Serializable");

		Set<String> writers = new HashSet<String>();
		writers.add("java.io.StringWriter");
		generatorData.put("java.io.Writer", writers);

		for (String targetClass : inheritanceData.keySet()) {
			String targetPrefix = targetClass.substring(0, targetClass.lastIndexOf("."));

			//if (!targetClass.startsWith("java.util"))
			//	continue;

			Set<String> subClasses = new HashSet<String>();
			for (String subClass : inheritanceData.get(targetClass)) {
				if (subClass.startsWith(targetPrefix))
					subClasses.add(subClass);
			}
			if (subClasses.isEmpty() && abstractClasses.contains(targetClass)) {
				subClasses.addAll(inheritanceData.get(targetClass));
			}
			inheritanceData.put(targetClass, subClasses);
		}

		for (String targetClass : generatorData.keySet()) {
			Set<String> generators = new HashSet<String>();
			if (!targetClass.contains(".")) {
				logger.warn("Does not contain: " + targetClass);
				continue;
			}
			String targetPrefix = targetClass.substring(0, targetClass.lastIndexOf("."));
			for (String generatorClass : generatorData.get(targetClass)) {
				if (generatorClass.contains("$"))
					continue;

				if (generatorClass.startsWith(targetPrefix))
					generators.add(generatorClass);
			}
			if (generators.isEmpty()) {
				for (String generatorClass : generatorData.get(targetClass)) {
					generators.add(generatorClass);
				}
			}
			generatorData.put(targetClass, generators);
		}

		// Write data to XML file
		try {
			FileOutputStream stream = new FileOutputStream(
			        new File("JDK_inheritance.xml"));
			XStream xstream = new XStream();
			xstream.toXML(inheritanceData, stream);
			stream = new FileOutputStream(new File("JDK_parameter.xml"));
			xstream.toXML(parameterData, stream);
			stream = new FileOutputStream(new File("JDK_generator.xml"));
			xstream.toXML(generatorData, stream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	public static void readJDKData() {
		XStream xstream = new XStream();
		InputStream inheritance = ClusterAnalysis.class.getResourceAsStream("/JDK_inheritance.xml");
		if (inheritance != null)
			inheritanceData = (Map<String, Set<String>>) xstream.fromXML(inheritance);
		InputStream parameter = ClusterAnalysis.class.getResourceAsStream("/JDK_parameter.xml");
		if (parameter != null)
			parameterData = (Map<String, Set<String>>) xstream.fromXML(parameter);

		InputStream generator = ClusterAnalysis.class.getResourceAsStream("/JDK_generator.xml");
		if (generator != null)
			generatorData = (Map<String, Set<String>>) xstream.fromXML(generator);
		logger.debug("Read JDK data: " + inheritanceData.size());
	}

	public static Collection<String> getResources() {
		return getResources(Properties.CP);
	}

	public static Collection<String> getAllResources() {
		Collection<String> retval = getResources(System.getProperty("java.class.path",
		                                                            "."));
		retval.addAll(getResources(System.getProperty("sun.boot.class.path")));
		return retval;
	}

	private static Collection<String> getResources(String classPath) {
		final ArrayList<String> retval = new ArrayList<String>();
		//String classPath = System.getProperty("java.class.path", ".");
		//String classPath = Properties.CP;
		String[] classPathElements = classPath.split(":");
		Pattern pattern = Pattern.compile(".*\\.class$");

		for (final String element : classPathElements) {
			if (element.contains("evosuite-0.1-SNAPSHOT-dependencies.jar"))
				continue;
			if (element.endsWith("jpf-annotations.jar"))
				continue;
			if (element.endsWith("jpf-classes.jar"))
				continue;
			if (element.contains("evosuite"))
				continue;
			logger.info("Getting resources from " + element);
			retval.addAll(ResourceList.getResources(element, pattern));
		}

		//		classPathElements = classPath.split(":");
		//		for (final String element : classPathElements) {
		//			if (element.endsWith("classes.jar"))
		//				retval.addAll(ResourceList.getResources(element, pattern));
		//		}
		return retval;
	}

	public static void readAllClasses() {
		if (!generatorData.isEmpty()) {
			logger.info("Have already read data");
			return;
		}

		readJDKData();

		Collection<String> list = getResources();
		LoggingUtils.getEvoLogger().info("* Analyzing classpath to satisfy runtime dependencies, found "
		        + list.size() + " classes");
		for (String name : list) {
			if (name.startsWith("java/lang") || name.startsWith("sun")
			        || name.startsWith("com/sun") || name.startsWith("javax/swing")
			        || name.startsWith("java/awt"))
				continue;
			logger.warn("Analyzing class " + name);
			try {
				ClassReader reader = new ClassReader(
				        name.replace("/", ".").replace(".class", ""));

				InheritanceClassAdapter cv = new InheritanceClassAdapter(null);
				reader.accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES
				        | ClassReader.SKIP_DEBUG);
			} catch (IOException e) {
				System.err.println("Error loading class: "
				        + name.replace("/", ".").replace(".class", ""));
				// TODO Auto-generated catch block
				//e.printStackTrace();
				// Ignore unloadable classes
			}
		}
		// System.out.println("Finished analyzing " + list.size() + " classes");
	}

	public static void addSubclass(String superClass, String subClass) {
		if (!inheritanceData.containsKey(superClass))
			inheritanceData.put(superClass, new LinkedHashSet<String>());

		inheritanceData.get(superClass).add(subClass);
		// System.out.println("Subclass relation: " + superClass + " <- " + subClass);
	}

	public static void addParameter(String className, String parameterClass) {
		if (!parameterData.containsKey(className))
			parameterData.put(className, new LinkedHashSet<String>());

		parameterData.get(className).add(parameterClass);
	}

	public static void addGenerator(String className, String targetName) {

		if (targetName.matches(".*\\$\\d+$"))
			return;
		if (className.matches(".*\\$\\d+$"))
			return;
		if (!targetName.contains("$") && className.contains("$"))
			return;

		if (!generatorData.containsKey(targetName))
			generatorData.put(targetName, new LinkedHashSet<String>());

		logger.info("New generator for " + targetName + ": " + className);
		generatorData.get(targetName).add(className);
	}

	public static void addAbstract(String className) {
		abstractClasses.add(className);
	}

	/**
	 * Calculate package distance between two classnames
	 * 
	 * @param className1
	 * @param className2
	 * @return
	 */
	private static int getDistance(String className1, String className2) {
		String[] package1 = className1.split(".");
		String[] package2 = className2.split(".");
		int distance = 0;
		int same = 0;
		int num = 0;
		while (num < package1.length && num < package2.length
		        && package1[num].equals(package2[num]))
			same++;

		if (package1.length > same)
			distance += package1.length - same;

		if (package2.length > same)
			distance += package2.length - same;

		return distance;
	}

	/**
	 * Determine the closest concrete implementation of this type
	 * 
	 * @param className
	 * @return
	 */
	public static String getFirstSubclass(final String className) {
		List<String> subClasses = new ArrayList<String>(getSubclasses(className));
		Comparator<String> packageSorter = new Comparator<String>() {

			@Override
			public int compare(String arg0, String arg1) {
				return getDistance(arg0, className) - getDistance(arg1, className);
			}
		};

		Collections.sort(subClasses, packageSorter);

		System.out.println("Sorted classes related to " + className);
		for (String subClass : subClasses) {
			System.out.println(subClass + ": " + getDistance(subClass, className));
		}

		return subClasses.get(0);

	}

	/**
	 * Determine all direct and indirect subclasses
	 * 
	 * @param className
	 * @return
	 */
	public static Set<String> getSubclasses(String className) {
		Set<String> subclasses = new HashSet<String>();
		Queue<String> queue = new LinkedList<String>();

		queue.add(className);
		while (!queue.isEmpty()) {
			String name = queue.poll();
			if (subclasses.contains(name))
				continue;

			subclasses.add(name);
			if (inheritanceData.containsKey(name)) {
				for (String subclass : inheritanceData.get(name)) {
					if (!subclasses.contains(subclass)) {
						// subclasses.add(subclass);
						queue.add(subclass);
					}
				}
			}

		}
		return subclasses;
	}

	/**
	 * Determine set of classes that this class depends on
	 * 
	 * @param className
	 * @return
	 */
	public static Set<String> getDependencies(String className) {
		Set<String> dependencies = new LinkedHashSet<String>();
		Queue<String> queue = new LinkedList<String>();

		queue.add(className);

		while (!queue.isEmpty()) {
			String name = queue.poll();
			if (name.contains("$"))
				name = name.substring(0, name.indexOf("$"));
			if (dependencies.contains(name))
				continue;

			if (parameterData.containsKey(name)) {
				for (String param : parameterData.get(name)) {
					if (!dependencies.contains(param) && !queue.contains(param))
						queue.add(param);
				}
			}

			if (inheritanceData.containsKey(name) && !name.startsWith("java.lang")) {

				for (String param : inheritanceData.get(name)) {
					if (!dependencies.contains(param) && !queue.contains(param))
						queue.add(param);
				}
			}

			dependencies.add(name);
		}

		return dependencies;
	}

	/**
	 * Determine set of classes that can generate a certain type
	 * 
	 * @param className
	 * @return
	 */
	public static Set<String> getGenerators(String className) {
		Set<String> generatorClasses = new HashSet<String>();
		logger.info("Getting generators for " + className);
		if (generatorData.containsKey(className)) {
			generatorClasses.addAll(generatorData.get(className));
		} else
			logger.info("Has no direct generators " + className);

		if (generatorClasses.isEmpty()) {
			for (String subClass : getSubclasses(className)) {
				logger.info("Checking subclass " + subClass);
				if (generatorData.containsKey(subClass))
					generatorClasses.addAll(generatorData.get(subClass));
				else
					logger.info("Has no generators " + subClass);
			}
		}

		logger.info("Done checking generators for " + className + ": "
		        + generatorClasses.size());

		return generatorClasses;
	}

	public static void main(String[] args) {
		generateJDKCluster();
	}
}
