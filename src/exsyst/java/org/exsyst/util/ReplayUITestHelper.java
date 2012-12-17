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

import javax.swing.UIManager;

import org.exsyst.genetics.ChromosomeUIController;
import org.exsyst.genetics.UITestChromosome;
import org.uispec4j.UISpec4J;

import sun.awt.AWTAutoShutdown;

public final class ReplayUITestHelper {
	
	
	private ReplayUITestHelper(){
		
	}
	
	/**
	 * @param args
	 */
	public static void run(UITestChromosome test) {
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

		try {
			new ChromosomeUIController(test).call();
		} catch (Exception e) {
			System.out.println("Got exception in initialization random walk:");
			e.printStackTrace();
		}


		System.out.println("Finished!");
		AWTAutoShutdown.getInstance().notifyThreadFree(Thread.currentThread());
	}
}