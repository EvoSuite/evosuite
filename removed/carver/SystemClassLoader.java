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
package org.evosuite.testcarver.instrument.classloader;

import org.evosuite.testcarver.capture.Capturer;
import org.evosuite.testcarver.configuration.Configuration;
import org.evosuite.testcarver.instrument.TransformerUtil;

public class SystemClassLoader extends ClassLoader
{
	private ClassLoader cl;
	
	public SystemClassLoader(final ClassLoader classLoader)
	{
		super(classLoader);
		
		Configuration.INSTANCE.initLogger();
	}
	
	
	@Override
    public Class<?> loadClass(final String name)  throws ClassNotFoundException
    {
		if(TransformerUtil.isClassConsideredForInstrumenetation(name.replace('.', '/')))
		{
			/*
			 * lazy class loading is done by intention here:
			 * we want to avoid loading all dependencies needed for instrumentation
			 * when system class loader is initiated (causes 
			 * "java.lang.IllegalStateException: recursive invocation")
			 */
			if(this.cl == null)
			{
				if(! Capturer.isCapturing())
				{
					final String classesToBeObserved = Configuration.INSTANCE.getProperty(Configuration.OBS_CLASSES);
					Capturer.startCapture(classesToBeObserved);
				}
				
				this.cl = new TransformingClassLoader(super.getParent());
			}
			
			return this.cl.loadClass(name);
		}
		else
		{
			return super.loadClass(name);
		}
    }
}