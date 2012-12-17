/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exsyst.util;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.Permission;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.UIManager;

import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.exsyst.UITestSuiteGenerator;
import org.exsyst.genetics.ChromosomeUIController;
import org.exsyst.genetics.UITestChromosome;
import org.uispec4j.UISpec4J;

import sun.awt.AWTAutoShutdown;

public class ReplayUITest {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.setProperty("uispec4j.test.library", "junit");

		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Really, really important, otherwise event loops get killed
		// randomly!
		AWTAutoShutdown.getInstance().notifyThreadBusy(Thread.currentThread());
		// Also important to do this very early (here), otherwise we might
		// end up with multiple event loops from loading classes
		UISpec4J.init();

		UITestSuiteGenerator.MainTrigger.USE_INSTRUMENTATION = false;
		
		String filename = null;

		if (args.length > 0) {
			filename = args[0];
		}

		if (filename == null) {
			filename = "solution.obj";
		}

		System.setSecurityManager(new SecurityManager() {
			@Override
			public void checkExit(int status) {
				StackTraceElement[] trace = new Exception().fillInStackTrace().getStackTrace();
				boolean afterExit = false;
				boolean isOK = false;

				for (StackTraceElement elem : trace) {
					String methodName = elem.getClassName() + "."
					        + elem.getMethodName();
					String mainMethod = "org.exsyst.util.ReplayUITest.main";

					if ((methodName.equals(mainMethod) && afterExit)
					        || (methodName.equals("javax.swing.JFrame.setDefaultCloseOperation"))) {
						isOK = true;
						break;
					}

					afterExit = methodName.equals("java.lang.System.exit");
				}

				if (!isOK) {
					throw new SecurityException();
				}
			}

			@Override
			public void checkPermission(Permission perm) {
				/* Allowed */
			}
		});
		
		List<UITestChromosome> tests = new LinkedList<UITestChromosome>();

		Object obj = readObjectFromFile(filename);
		
		if (obj instanceof AbstractTestSuiteChromosome<?>) {
			@SuppressWarnings("unchecked")
			AbstractTestSuiteChromosome<UITestChromosome> solution = (AbstractTestSuiteChromosome<UITestChromosome>) obj;
			tests.addAll(solution.getTestChromosomes());
		}
		else if (obj instanceof Collection<?>) {
			@SuppressWarnings("unchecked")
			Collection<UITestChromosome> collection = (Collection<UITestChromosome>) obj;
			tests.addAll(collection);
		}
		else {
			throw new Error("Unsupported object type " + obj.getClass());
		}

	
		int testIdx = 1;

		for (UITestChromosome test : tests) {
			System.out.println(String.format("Replaying test %d of %d...", testIdx,
			                                 tests.size()));

			try {
				new ChromosomeUIController(test).call();
			} catch (Exception e) {
				System.out.println("Got exception in initialization random walk:");
				e.printStackTrace();
			}

			testIdx++;
		}

		System.out.println("Finished!");
		AWTAutoShutdown.getInstance().notifyThreadFree(Thread.currentThread());
		System.exit(0);
	}

	public static Object readObjectFromFile(String filename) {
		try {
			ObjectInputStream oos = new ObjectInputStream(new FileInputStream(filename));
			Object result = oos.readObject();
			oos.close();
			return result;
		} catch (Throwable t) {
			System.out.println("Exception on reading object:");
			t.printStackTrace();
			return null;
		}
	}

}
