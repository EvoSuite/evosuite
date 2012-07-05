package de.unisb.cs.st.testcarver.instrument.classloader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.testcarver.capture.FieldRegistry;
import de.unisb.cs.st.testcarver.instrument.Instrumenter;
import de.unisb.cs.st.testcarver.instrument.TransformerUtil;

public class TransformingClassLoader extends ClassLoader
{
	private final Instrumenter instrumenter;
	
	private static final Logger LOG = LoggerFactory.getLogger(TransformingClassLoader.class);
	
	public TransformingClassLoader(final ClassLoader classLoader)
	{
		super(classLoader);

		this.instrumenter = new Instrumenter();
	}
	
		
	@Override
    public Class<?> loadClass(final String name) 
    {
    	LOG.debug("start loading and transforming class {}", name);
    	
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
        		LOG.warn("Could not find resource {} in classpath -> no instrumentation applied", classAsPath);
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
            LOG.debug("instrumentation of {} has been successful", name);
            
            
            final Class<?> c = defineClass(name, classBytes , 0, classBytes.length);
            
            // this is needed as it is not possible to get the right CLASS (!!!) instance
            // which leads to errors when fields are accessed via reflections. That's why
            // this step can't be done in <clinit>.
            FieldRegistry.register(c);
            
            return c;
    	}
    	catch(final Exception e)
    	{
    		LOG.error("an error occurred while loading and transforming {} -> returning null", new Object[]{name, e} );
    		return null;
    	}
    }
}