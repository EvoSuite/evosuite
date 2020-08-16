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
package org.evosuite.testcarver.agent;

import java.lang.instrument.Instrumentation;

import org.evosuite.testcarver.capture.Capturer;
import org.evosuite.testcarver.configuration.Configuration;
import org.evosuite.testcarver.exception.CapturerException;
import org.evosuite.testcarver.instrument.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



//TODO param for deleting modified bin?
public final class Agent 
{
	// TODO just for convenience during development -> set system properties via command line later on
	private static final Logger LOG = LoggerFactory.getLogger(Agent.class);
	
	private Agent() {}
	
	/**
	 * @param args
	 */
	public static void premain(final String agentArgs, final Instrumentation inst) 
	{
		Configuration.INSTANCE.initLogger();
		
		LOG.debug("starting agent with with args={}", agentArgs);
		try
		{
			try
			{
				// start Capturer if not active yet
				// NOTE: Stopping the capture and saving the corresponding logs is handled in the ShutdownHook
				//       which is automatically initialized in the Capturer

				if(! Capturer.isCapturing())
				{
					Capturer.startCapture(agentArgs);
				}
			}
			catch(CapturerException e)
			{
				LOG.error(e.getMessage(), e);
				throw new Error(e);
			}
			
			final Transformer trans = new Transformer(agentArgs.split("\\s+"));
			
			// install our class transformer which performs the instrumentation
			inst.addTransformer(trans);
		}
		catch(Throwable t)
		{
			LOG.error("an errorr occurred while executing agent (premain)", t);
		}


	}
}
