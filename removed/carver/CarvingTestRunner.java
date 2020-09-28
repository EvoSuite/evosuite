/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.capture.Capturer;
import org.evosuite.testcarver.codegen.CaptureLogAnalyzer;
import org.evosuite.testcarver.testcase.EvoTestCaseCodeGenerator;
import org.evosuite.testcase.TestCase;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * JUnit test runner which carves the entire test execution.
 * 
 * @author Benjamin Friedrich (friedrich.benjamin@gmail.com)
 */
public final class CarvingTestRunner extends BlockJUnit4ClassRunner
{
	private final Class<?>[] targetClasses;
	private TestCase		 carvedTest;
	
	/**
	 * Constructor
	 * 
	 * @param  unitTestClass  JUnit test class
	 * @param  targetClasses  classes to be considered for test creation
	 * 
	 * @throws InitializationError
	 */
	public CarvingTestRunner(final Class<?> unitTestClass, final Class<?>...targetClasses) throws InitializationError 
	{
		super(unitTestClass);
		
		if(targetClasses == null || targetClasses.length == 0)
		{
			throw new IllegalArgumentException("No targets for sequence exttraction specified");
		}
		
		this.targetClasses = targetClasses;
	}
	
	/**
	 * Returns carved test
	 * 
	 * @return carved test or null if the test has not been executed
	 */
	public TestCase getCarvedTest()
	{
		return this.carvedTest;
	}
	

	
	@Override
	protected Statement withBeforeClasses(final Statement statement)
	{
		Capturer.startCapture();
		
		try
		{
			return super.withBeforeClasses(statement);
		}
		catch(final RuntimeException t)
		{
			this.stopCapture();
			throw t;
		}
	}
	
	@Override
	protected Statement withAfterClasses(final Statement statement)
	{
		try
		{
			final Statement afterClassesStmt = super.withAfterClasses(statement);
			return afterClassesStmt;
		}
		finally
		{
			this.stopCapture();
		}
	}
	
	/**
	 * Stops capture phase and creates carved TestCase
	 */
	private void stopCapture()
	{
		final CaptureLog log = Capturer.stopCapture();
		this.processLog(log);
		Capturer.clear();
	}
	
	/**
	 * Creates TestCase out of the captured log
	 * 
	 * @param log  log captured from test execution
	 */
	private void processLog(final CaptureLog log)
	{
		final CaptureLogAnalyzer       analyzer = new CaptureLogAnalyzer();
		final EvoTestCaseCodeGenerator codeGen  = new EvoTestCaseCodeGenerator();
		analyzer.analyze(log, codeGen, this.targetClasses);
		
		this.carvedTest = codeGen.getCode();	
		codeGen.clear();
	}
}
