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
package com.examples.with.different.packagename.agent;

import org.evosuite.runtime.agent.InstrumentingAgent_exceptionsIntTest;

public class ExceptionHolder extends Exception{

	private static final long serialVersionUID = -2460331308621553548L;

	
	public static class StaticPublicException extends Exception{
		private static final long serialVersionUID = -3176736235485114939L;		
	}
	
	
	public Throwable getMockedThrowable(){
		return new Throwable();
	}
	
	
	public NullPointerException getNonMockedNPE(){
		return InstrumentingAgent_exceptionsIntTest.getNPE();
	}
	
	
	public StackTraceElement[] getTracesWhenCast(){		
		Exception exception = (Exception) getNonMockedNPE();		
		return exception.getStackTrace();
	}
	
	
	public StackTraceElement[] getTraces(){
		return getNonMockedNPE().getStackTrace();
	}
}
