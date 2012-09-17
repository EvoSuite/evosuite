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

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.UIManager;

import org.evosuite.Properties;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TimeoutHandler;
import org.exsyst.ui.genetics.ChromosomeUIController;
import org.exsyst.ui.genetics.UITestChromosome;
import org.uispec4j.UISpec4J;

import sun.awt.AWTAutoShutdown;

public final class ReplayUITestHelper {
	
	
	private ReplayUITestHelper(){
		
	}
	
	/**
	 * @param args
	 */
	public static void run(final UITestChromosome test) 
	{
		final TimeoutHandler<ExecutionResult> handler = new TimeoutHandler<ExecutionResult>();
		final ChromosomeUIController callable = new ChromosomeUIController(test);
		final ExecutorService executor = Executors.newSingleThreadExecutor(TestCaseExecutor.getInstance());

		try 
		{
			System.setProperty("uispec4j.test.library", "junit");
			
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			
			// Really, really important, otherwise event loops get killed
			// randomly!
			AWTAutoShutdown.getInstance().notifyThreadBusy(Thread.currentThread());
			// Also important to do this very early (here), otherwise we might
			// end up with multiple event loops from loading classes
			UISpec4J.init();
	
			handler.execute(callable, executor, Integer.MAX_VALUE, Properties.CPU_TIMEOUT);
		}
		catch(final Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			executor.shutdown();
			try 
			{
				System.out.println("waiting for termination");
				
				waitForEmptyAWTEventQueue();
				
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
				System.out.println("terminated");
			} 
			catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			AWTAutoShutdown.getInstance().notifyThreadFree(Thread.currentThread());
		}
	}
	
	
	public static void waitForEmptyAWTEventQueue()
	{
		try 
		{
			EventQueue.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					// Waiting for EventQueue to be empty
				}
			});
		} 
		catch (final Exception e) {
		}

		
		final EventQueue evtQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
		while(evtQueue.peekEvent() != null)
		{
			System.out.println("EMPTYING EVENT QUEUE");
			try {
				evtQueue.getNextEvent();
			} catch (InterruptedException e) {
			}
		}

		// FIXME how to recognize end of foregoing executions???
		try {
			Thread.sleep(10000l);
		} catch (InterruptedException e) {
		}
	}
}