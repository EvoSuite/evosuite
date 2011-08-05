/**
 * 
 */
package de.unisb.cs.st.evosuite.setup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassReader;

/**
 * @author fraser
 * 
 */
public class ClusterAnalysis {

	private static Map<String, Set<String>> inheritanceData = new HashMap<String, Set<String>>();

	private static Map<String, Set<String>> parameterData = new HashMap<String, Set<String>>();

	private static Set<String> abstractClasses = new HashSet<String>();

	public static Collection<String> getResources() {
		final ArrayList<String> retval = new ArrayList<String>();
		String classPath = System.getProperty("java.class.path", ".");
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
			retval.addAll(ResourceList.getResources(element, pattern));
		}

		classPath = System.getProperty("sun.boot.class.path");
		classPathElements = classPath.split(":");
		for (final String element : classPathElements) {
			if (element.endsWith("classes.jar"))
				retval.addAll(ResourceList.getResources(element, pattern));
		}
		return retval;
	}

	public static void readAllClasses() {
		Collection<String> list = getResources();
		for (String name : list) {
			if (name.startsWith("java/lang") || name.startsWith("sun")
			        || name.startsWith("com/sun") || name.startsWith("javax/swing")
			        || name.startsWith("java/awt"))
				continue;

			try {
				ClassReader reader = new ClassReader(
				        name.replace("/", ".").replace(".class", ""));

				InheritanceClassAdapter cv = new InheritanceClassAdapter(null);
				reader.accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES
				        | ClassReader.SKIP_DEBUG);
			} catch (IOException e) {
				//System.err.println("Error loading class: "
				//   + name.replace("/", ".").replace(".class", ""));
				// TODO Auto-generated catch block
				//e.printStackTrace();
				// Ignore unloadable classes
			}
		}

	}

	public static void addSubclass(String superClass, String subClass) {
		if (!inheritanceData.containsKey(superClass))
			inheritanceData.put(superClass, new LinkedHashSet<String>());

		inheritanceData.get(superClass).add(subClass);
	}

	public static void addParameter(String className, String parameterClass) {
		if (!parameterData.containsKey(className))
			parameterData.put(className, new LinkedHashSet<String>());

		parameterData.get(className).add(parameterClass);
	}

	public static void addAbstract(String className) {
		abstractClasses.add(className);
	}

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
}
