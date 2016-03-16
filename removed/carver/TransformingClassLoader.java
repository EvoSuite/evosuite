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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.evosuite.testcarver.capture.FieldRegistry;
import org.evosuite.testcarver.instrument.Instrumenter;
import org.evosuite.testcarver.instrument.TransformerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TransformingClassLoader extends ClassLoader
{
	private final Instrumenter instrumenter;
	
	private static final Logger logger = LoggerFactory.getLogger(TransformingClassLoader.class);
	
	public TransformingClassLoader(final ClassLoader classLoader)
	{
		super(classLoader);

		this.instrumenter = new Instrumenter();
	}
	
		
	@Override
    public Class<?> loadClass(final String name) 
    {
    	logger.debug("start loading and transforming class {}", name);
    	
    	try
    	{
    		// is necessary because this class loader does not know about class already loaded
    		// by another class loader -> so it will try to load all dependencies again
    		final String className = name.replace('.', '/');
    		if(! TransformerUtil.isClassConsideredForInstrumenetation(className))
    		{
    			return super.loadClass(name);
    		}
    		
    		final String classAsPath = className + ".class";

    		final InputStream in= ClassLoader.getSystemClassLoader().getResourceAsStream(classAsPath);
        	if(in == null)
        	{
        		logger.warn("Could not find resource {} in classpath -> no instrumentation applied", classAsPath);
        		return super.loadClass(className);
        	}
    		
    		
            final ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
            
            final byte[] buffer = new byte[1024];
            int numBytes;
            while((numBytes = in.read(buffer)) != -1)
            {
            	bout.write(buffer, 0, numBytes);
            }
            byte[] classBytes = bout.toByteArray();
            
            classBytes = this.instrumenter.instrument(className, classBytes);
            logger.debug("instrumentation of {} has been successful", name);
            
            
            final Class<?> c = defineClass(name, classBytes , 0, classBytes.length);
            
            // this is needed as it is not possible to get the right CLASS (!!!) instance
            // which leads to errors when fields are accessed via reflections. That's why
            // this step can't be done in <clinit>.
            FieldRegistry.register(c);
            
            return c;
    	}
    	catch(final Exception e)
    	{
    		logger.error("an error occurred while loading and transforming {} -> returning null", new Object[]{name, e} );
    		return null;
    	}
    }
}