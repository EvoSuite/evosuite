package org.evosuite.testcarver.instrument;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashSet;

import org.evosuite.testcarver.capture.Capturer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Transformer implements ClassFileTransformer
{
	private final HashSet<String> classesToBeObserved;
	
	private final Instrumenter  instrumenter;
	
	private static final Logger LOG = LoggerFactory.getLogger(Transformer.class);
	
	public Transformer(final String[] namesOfClassesToBeObserved)
	{
		LOG.debug("initialized transformer with namesOfClassesToBeObserved={}", new Object[]{ Arrays.toString(namesOfClassesToBeObserved)});
		if(namesOfClassesToBeObserved == null)
		{
			throw new NullPointerException("Given array of names of classes to be observed must not be null");
		}
		
		this.classesToBeObserved = new HashSet<String>();
		
		final int size = namesOfClassesToBeObserved.length;
		for(int i = 0; i < size; i++)
		{
			this.classesToBeObserved.add(namesOfClassesToBeObserved[i].replace('.', '/'));
		}
		
		this.instrumenter = new Instrumenter();
	}


	@Override
	public byte[] transform(final ClassLoader     loader, 
							final String           className,
							final Class<?>         classBeingRedefined, 
							final ProtectionDomain protectionDomain,
							final byte[]           classFileBuffer) 
	throws IllegalClassFormatException 
	{
		LOG.debug("transforming {}", className);
		
		if(! Capturer.isCapturing())
		{
			LOG.debug("class {} has not been transformed because Capturer is not active", className);
			return classFileBuffer;
		}
		
		if(! TransformerUtil.isClassConsideredForInstrumenetation(className))
		{
			LOG.debug("class {} has not been instrumented because its name is on the blacklist", className);
			return classFileBuffer;
		}
		
		final ClassReader cr = new ClassReader(classFileBuffer);
		final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		final ClassNode   cn = new ClassNode();
		cr.accept(cn, ClassReader.SKIP_DEBUG);
		
		this.instrumenter.instrument(className, cn);
		
		cn.accept(cw);
		
		return cw.toByteArray();
	}
}
