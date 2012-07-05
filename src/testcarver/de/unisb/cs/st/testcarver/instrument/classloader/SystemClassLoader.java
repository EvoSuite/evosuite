package de.unisb.cs.st.testcarver.instrument.classloader;

import de.unisb.cs.st.testcarver.capture.Capturer;
import de.unisb.cs.st.testcarver.configuration.Configuration;
import de.unisb.cs.st.testcarver.instrument.TransformerUtil;

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