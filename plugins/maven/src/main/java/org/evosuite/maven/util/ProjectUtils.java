package org.evosuite.maven.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;

/**
 * 
 * @author José Campos
 */
public class ProjectUtils {

	/**
	 * Get compile elements (i.e., classes under /target/classes)
	 * 
	 * @param project
	 * @return
	 */
	public static List<String> getCompileClasspathElements(MavenProject project) {
		List<String> compileClassPath = new ArrayList<String>();

		try {
			project.getCompileClasspathElements()
				.stream()
				// we only target what has been compiled to a folder
				.filter(element -> !element.endsWith(".jar"))
				.filter(element -> new File(element).exists())
				.forEach(element -> compileClassPath.add(element));
		} catch (DependencyResolutionRequiredException e) {
			e.printStackTrace();
		}

		return compileClassPath;
	}

	/**
	 * Get JUnit elements (i.e., classes under /target/test-classes) and compiled
	 * elements (i.e., classes under /target/classes)
	 * 
	 * @param project
	 * @return
	 */
	public static List<String> getTestClasspathElements(MavenProject project) {
		List<String> testClassPath = new ArrayList<String>();

		try {
			project.getTestClasspathElements()
				.stream()
				// we only target what has been compiled to a folder
				.filter(element -> !element.endsWith(".jar"))
				.filter(element -> new File(element).exists())
				.forEach(element -> testClassPath.add(element));
		} catch (DependencyResolutionRequiredException e) {
			e.printStackTrace();
		}

		return testClassPath;
	}

	/**
	 * Get runtime elements
	 * 
	 * @param project
	 * @return
	 */
	public static List<String> getRuntimeClasspathElements(MavenProject project) {
		List<String> runtimeClassPath = new ArrayList<String>();

		try {
			project.getRuntimeClasspathElements()
				.stream()
				.filter(element -> new File(element).exists())
				.forEach(element -> runtimeClassPath.add(element));
		} catch (DependencyResolutionRequiredException e) {
			e.printStackTrace();
		}

		return runtimeClassPath;
	}

	/**
	 * Get project's dependencies
	 * 
	 * @param project
	 * @return
	 */
	public static List<String> getDependencyPathElements(MavenProject project) {
		List<String> dependencyArtifacts = new ArrayList<String>();

		project.getDependencyArtifacts()
			.stream()
			.filter(element -> !element.isOptional())
			// FIXME do we really need to check the 'scope'?
			//.filter(element -> element.getScope().equals(scope))
			.filter(element -> element.getFile().exists())
			.filter(element -> !element.getGroupId().equals("org.evosuite"))
			.filter(element -> !element.getGroupId().equals("junit"))
			.forEach(element -> dependencyArtifacts.add(element.getFile().getAbsolutePath()));

		return dependencyArtifacts;
	}

	/**
	 * Convert a list of strings to a single string separated
	 * by File.pathSeparator (i.e., ':')
	 * 
	 * @param elements
	 * @return
	 */
	public static String toClasspathString(Collection<String> elements) {
		final StringBuilder str = new StringBuilder();

		elements.forEach(element ->
			str.append(str.length() == 0 ? element : File.pathSeparator + element)
		);

		return str.toString();
	}
}
