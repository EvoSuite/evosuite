/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.javaagent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

import de.unisb.cs.st.evosuite.mutation.HOM.HOMFileTransformer;
import de.unisb.cs.st.javalanche.mutation.javaagent.classFileTransfomer.DistanceTransformer;

/**
 * @author Gordon Fraser
 *
 */
public class TestSuitePreMain {

	/**
	 * Decides which transformers to add depending on the {@link RunMode}.
	 * 
	 */
	public static void premain(String agentArguments,
			Instrumentation instrumentation) {
		System.out.println("EvoSuite rocks!");
		
		if(agentArguments.equals("generate")) {
			System.out.println("Creating tests");
//			addClassFileTransformer(instrumentation, new HOMFileTransformer());
			addClassFileTransformer(instrumentation, new CoverageInstrumentation());
		} else 	if(agentArguments.equals("assert")) {
			System.out.println("Creating assertions");
			
			addClassFileTransformer(instrumentation, new HOMFileTransformer());
			//addClassFileTransformer(instrumentation, new CoverageInstrumentation());

		} else if (agentArguments.equals("scan")) {
			System.out.println("Scanning project for classes");
			addClassFileTransformer(instrumentation,
					new DistanceTransformer());
			//addClassFileTransformer(instrumentation,
			//		new ScanProjectTransformer());			
		} else if (agentArguments.equals("tasks")) {
			// Do nothing?
		}
	}
	
	private static void addClassFileTransformer(
			Instrumentation instrumentation, ClassFileTransformer clt) {
		instrumentation.addTransformer(clt);
	}
}
