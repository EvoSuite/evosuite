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
package org.evosuite.testcarver.codegen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.*;

import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.configuration.Configuration;
import org.evosuite.testcarver.exception.CapturerException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import gnu.trove.list.array.TIntArrayList;

public final class PostProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(PostProcessor.class);
	
	private static TIntArrayList failedRecords;
	
	private static int recentLogRecNo;
	
	private PostProcessor() {}
	
	public static void init()
	{
		failedRecords = new TIntArrayList();
		recentLogRecNo = -1;
	}
	
	public static void notifyRecentlyProcessedLogRecNo(final int logRecNo)
	{
		recentLogRecNo = logRecNo;
	}
	
	public static void captureException(final int logRecNo)
	{
		failedRecords.add(logRecNo);
	}

	
	
	private static void writeTest(final CaptureLog log, final String packageName, final String testName, final Class<?>[] observedClasses, final File targetFile,   final boolean postprocessing)
	{
		// TODO parallelize
		FileOutputStream fout = null;
		try
		{
			fout = new FileOutputStream(targetFile);
			
//			final CodeGenerator generator = new CodeGenerator(log);
			
			final ICaptureLogAnalyzer analyzer = new CaptureLogAnalyzer();
			final JUnitCodeGenerator codeGen = new JUnitCodeGenerator(testName, packageName);
			
			final String code;
			
			if(postprocessing)
			{
				codeGen.enablePostProcessingCodeGeneration();
				analyzer.analyze(log, codeGen, observedClasses);
				code = codeGen.getCode().toString();
//				code = generator.generateCodeForPostProcessing(testName, packageName, observedClasses).toString();
			}
			else
			{
				// if recent log recno is contained in failed records,
				// remove it as the very last statement has to throw an exception, if it threw one in
				// the original program run
				failedRecords.remove(recentLogRecNo)
				;
				codeGen.disablePostProcessingCodeGeneration(failedRecords);
				analyzer.analyze(log, codeGen, observedClasses);
				code = codeGen.getCode().toString();
				
//				code = generator.generateFinalCode(testName, packageName, failedRecords, observedClasses).toString();
			}
			
			// TODO not nice but how can blank line be inserted with JDT?
			fout.write(code.replaceAll("\\s+;", "\n").getBytes());
		}
		catch(final Exception e)
		{
			throw new CapturerException(e);
		}
		finally
		{
			try 
			{
				if(fout != null)
				{
					fout.close();
				}
			} 
			catch (final IOException e) 
			{
				throw new CapturerException(e);
			}
		}
	}
	
	
	/**
	 * 
	 * @param logs   logs of captured interaction
	 * @param packages  package names associated with logs. Mapping logs.get(i) belongs to packages.get(i)
	 * @throws IOException 
	 */
	public static void process(final List<CaptureLog> logs, final List<String> packages, final List<Class<?>[]> observedClasses) throws IOException
	{
		if(logs == null)
		{
			throw new NullPointerException("list of CaptureLogs must not be null");
		}
		
		if(packages == null)
		{
			throw new NullPointerException("list of package names associated with logs must not be null");
		}
		
		if(observedClasses == null)
		{
			throw new NullPointerException("list of classes to be observed must not be null");
		}
		
		final int size = logs.size();
		if(packages.size() != size || observedClasses.size() != size)
		{
			throw new IllegalArgumentException("given lists must have same size");
		}
		
		// create post-processing sources in os specific temp folder
		final File tempDir = new File(System.getProperty("java.io.tmpdir"), 
				                      "postprocessing_" + System.currentTimeMillis());
		tempDir.mkdir();
	
		CaptureLog log;
		String     packageName;
		Class<?>[] classes;
		
		String targetFolder;
		File targetFile;
		final StringBuilder testClassNameBuilder = new StringBuilder();
		String testClassName;
		
		for(int i = 0; i < size; i++)
		{
			//=============== prepare carved test for post-processing ================================================
			
			packageName  = packages.get(i);
			targetFolder = packageName.replace(".", File.separator);
			classes      = observedClasses.get(i);
			
			if(classes.length == 0)
			{
				throw new IllegalArgumentException("there must be at least one class to be observed");
			}
	
//			for(int j = 0; j < classes.length; j++)
//			{
//				testClassNameBuilder.append(classes[j].getSimpleName());
//				if(testClassNameBuilder.length() >= 10)
//				{
//					break;
//				}
//			}
			testClassNameBuilder.append("CarvedTest");
			
			
			
			log = logs.get(i);
			
			long s = System.currentTimeMillis();
			logger.debug(">>>> (postprocess) start test create ");
			
			targetFile = new File(tempDir, targetFolder);
			targetFile.mkdirs();
			testClassName = getFreeClassName(targetFile, testClassNameBuilder.toString());
			
			targetFile = new File(targetFile, testClassName + ".java");
			
			// write out test files containing post-processing statements
			writeTest(log, packageName, testClassName, classes, targetFile, true);
			
			logger.debug(">>>> (postprocess) end test creation -> " + (System.currentTimeMillis() - s) / 1000);
			
			//=============== compile generated post-processing test ================================================
			
			final JavaCompiler compiler =  new EclipseCompiler();
//			final JavaCompiler 			  compiler    = ToolProvider.getSystemJavaCompiler();
			final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
			
			Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(new File[]{targetFile}));
			
			//--- add modified bins (see Transformer and ClassPreparer.createPreparedBin()) to class path
			String classPath = Configuration.INSTANCE.getProperty(Configuration.MODIFIED_BIN_LOC);
			classPath += File.pathSeparator + System.getProperty("java.class.path");
			
			
		  	final Boolean wasCompilationSuccess = compiler.getTask(null, fileManager, null, Arrays.asList(new String[]{"-cp", classPath}), null, compilationUnits).call();
			
			if(! wasCompilationSuccess)
			{
				
				logger.error("Compilation was not not successful for " + targetFile);
				fileManager.close();
				continue;
			}
			fileManager.close();
			
			
			//=============== execute + observe post-processing test run ================================================

			final PostProcessorClassLoader cl = new PostProcessorClassLoader(tempDir);
			
			final Class<?> testClass = cl.findClass(packageName + '.' + testClassName);
			
			BlockJUnit4ClassRunner testRunner;
			try 
			{
				testRunner = new BlockJUnit4ClassRunner(testClass);
				testRunner.run(new RunNotifier());
			} 
			catch (InitializationError e) 
			{
				logger.error(""+e,e);
			}
			
			//============== generate final test file ================================================================
			
			final String targetDir = Configuration.INSTANCE.getProperty(Configuration.GEN_TESTS_LOC);
			targetFile             = new File(new File(targetDir), targetFolder);
			targetFile.mkdirs();
			testClassName = getFreeClassName(targetFile, testClassNameBuilder.toString());
			targetFile    = new File(targetFile, testClassName + ".java");
			writeTest(log, packageName, testClassName, classes, targetFile, false);
			
			// recycle StringBuilder for testClassName
			testClassNameBuilder.setLength(0);
		}
		
		// clean up post-processing stuff
		tempDir.delete();
	}
	
	
	
	private static String getFreeClassName(final File targetDir, final String className)
	{
		final String[] similarFileNames = targetDir.list(new FilenameFilter() 
		{
			final Pattern PATTERN = Pattern.compile(className + "\\d*\\.java");
			
			@Override
			public boolean accept(final File dir, final String name) 
			{
				final Matcher m = PATTERN.matcher(name);
				return m.matches();
			}
		});
		
		
		if(similarFileNames.length > 0)
		{
			return className + similarFileNames.length;
		}
		

		return className;
	}
	
	
	private static final class PostProcessorClassLoader extends ClassLoader
	{
		private final File baseDir;
		
		public PostProcessorClassLoader(final File baseDir)
		{
			this.baseDir = baseDir;
		}
		
		@Override
		public Class<?> findClass(final String name)
		{
			final String fileName = name.replace(".", File.separator);
			final File targetFile = new File(this.baseDir, fileName + ".class");
			
			try
			{
				if(! targetFile.exists())
				{
					return super.findClass(fileName);
				}
				
				
				final FileInputStream fin = new FileInputStream(targetFile);
				
				final byte[] content = new byte[(int) targetFile.length()];
				fin.read(content);
				fin.close();
				
				return super.defineClass(name, content, 0, content.length);
			}
			catch(Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
}
