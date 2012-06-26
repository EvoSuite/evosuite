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
package org.exsyst.ui.util;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import javax.swing.UIManager;

import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.exsyst.ui.genetics.ChromosomeUIController;
import org.exsyst.ui.genetics.UITestChromosome;

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

		String filename = null;

		if (args.length > 0) {
			filename = args[0];
		}

		if (filename == null) {
			filename = "solution.obj";
		}

		Object obj = readObjectFromFile(filename);
		@SuppressWarnings("unchecked")
		AbstractTestSuiteChromosome<UITestChromosome> solution = (AbstractTestSuiteChromosome<UITestChromosome>) obj;

		int testIdx = 1;

		for (UITestChromosome test : solution.getTestChromosomes()) {
			System.out.println(String.format("Replaying test %d of %d...", testIdx,
			                                 solution.getTestChromosomes().size()));

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
