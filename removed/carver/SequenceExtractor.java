/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testcarver.extraction;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.evosuite.testcase.TestCase;
import org.evosuite.utils.ResourceList;
import org.evosuite.utils.Utils;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to extract execution sequences.
 * 
 * @author Benjamin Friedrich (friedrich.benjamin@gmail.com)
 */
public final class SequenceExtractor 
{
	private static final Logger logger = LoggerFactory.getLogger(SequenceExtractor.class);

	private SequenceExtractor() {	}

	/**
	 * Determines if the given class implements a JUnit test
	 * 
	 * @param c  class to be considered
	 * @return true if class implements JUnit test, false otherwise
	 */
	private static boolean isJUnitTestClass(final Class<?> c) 
	{
		if (c.getSuperclass() != null &&  junit.framework.TestCase.class.equals(c.getSuperclass())) 
		{
			// Given class is JUnit 3.x test
			return true;
		} 
		else 
		{
			for (final Method method : c.getDeclaredMethods()) 
			{
				// if at least one method is annotated with @Test, the class is considered as JUnit test
				if (method.getAnnotation(org.junit.Test.class) != null) 
				{
					return true;
				}
			}

			// Nothing found -> no test
			return false;
		}
	}

	/**
	 * Get all names of classes to be considered.
	 * 
	 * @param  packageToBeConsidered  name of package to be considered
	 * @return names of all possible candidates
	 */
	private static Collection<String> getPossibleCandidates(final String packageToBeConsidered)
	{
		final Pattern pattern;
		if(packageToBeConsidered.trim().isEmpty())
		{
			pattern = Pattern.compile("^\\.+\\.class");
		}
		else
		{
			pattern = Pattern.compile(packageToBeConsidered.trim().replace('.', '/') + "/.*\\.class");
		}
		
		return ResourceList.getResources(pattern);
	}
	
	/**
	 * Extracts execution sequences from JUnit tests which are located in the given package with respect to the given target classes.
	 * 
	 * @param packageToBeConsidered  package to be scanned for JUnit tests
	 * @param targetClasses          classes for which sequences shall be extracted
	 * 
	 * @return carved sequences (TestCase)
	 */
	public static List<TestCase> extractSequences(final String packageToBeConsidered, final Class<?>... targetClasses) 
	{
		if (packageToBeConsidered == null) 
		{
			throw new NullPointerException("Name of the package to be considered must not be null");
		}

		if (targetClasses == null || targetClasses.length == 0) 
		{
			throw new IllegalArgumentException("No targets for sequence extraction specified");
		}

		final Collection<String>  classes         = getPossibleCandidates(packageToBeConsidered);
		final ArrayList<TestCase> carvedSequences = new ArrayList<TestCase>();

		CarvingTestRunner testRunner;
		Class<?> clazz;
		TestCase carvedSequence;
		for (final String className : classes) 
		{
			try 
			{
				clazz = Class.forName(Utils.getClassNameFromResourcePath(className));
//				clazz = Class.forName(className.replace(".class", "").replace('/', '.'), true, StaticTestCluster.classLoader);
				
				if (isJUnitTestClass(clazz)) 
				{
					try 
					{
						testRunner = new CarvingTestRunner(clazz, targetClasses);
						// TODO is test result interesting?
						testRunner.run(new RunNotifier());

						carvedSequence = testRunner.getCarvedTest();
						if (carvedSequence == null) 
						{
							logger.warn("For some reason, no carving took place for test class " + className);
						} 
						else 
						{
							carvedSequences.add(carvedSequence);
						}
					} 
					catch (final InitializationError e) 
					{
						logger.error("An error occurred while initializing CarvingTestRunner for test class " + className, e);
					}
				}
			} 
			catch(final Throwable e)
			{
				logger.error("Couldn't get class instance of class " + className, e);
			}
		}

		return carvedSequences;
	}
}