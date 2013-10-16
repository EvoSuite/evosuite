package org.evosuite.junit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.evosuite.Properties;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.testcase.TestCase;
import org.evosuite.utils.ClassPathHacker;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is used to check if a set of test cases are 
 * valid for JUnit: ie, if they can be compiled, they do 
 * not fail, and if running them a second time produces
 * same result (ie not fail).
 * 
 * @author arcuri
 *
 */
public class JUnitAnalyzer {

	private static Logger logger = LoggerFactory.getLogger(JUnitAnalyzer.class);

	private static int dirCounter = 0;
	
	/**
	 * Try to compile each test separately, and remove the ones that
	 * cannot be compiled
	 * 
	 * @param tests
	 */
	public static void removeTestsThatDoNotCompile(List<TestCase> tests){

		logger.info("Going to execute: removeTestsThatDoNotCompile");
		
		if (tests == null || tests.isEmpty()) { //nothing to do
			return;
		}

		Iterator<TestCase> iter = tests.iterator();

		while(iter.hasNext()){
			TestCase test = iter.next();

			File dir = createNewTmpDir();
			if(dir==null){
				logger.warn("Failed to create tmp dir");
				return;
			}
			logger.debug("Created tmp folder: "+dir.getAbsolutePath());

			try{
				List<TestCase> singleList = new ArrayList<TestCase>();
				singleList.add(test);
				List<File> generated = compileTests(singleList,dir);
				if(generated==null){
					iter.remove();
					String code = test.toCode();
					logger.error("Failed to compile test case:\n"+code);
				}
			} finally {
				//let's be sure we clean up all what we wrote on disk
				if (dir != null) {
					try {
						FileUtils.deleteDirectory(dir);
						logger.debug("Deleted tmp folder: "+dir.getAbsolutePath());
					} catch (Exception e) {
						logger.error("Cannot delete tmp dir: " + dir.getAbsolutePath(), e);
					}
				}
			}

		} // end of while
	}

	/**
	 * Compile and run all the test cases, and mark as "unstable" all the
	 * ones that fail during execution (ie, unstable assertions).
	 * 
	 * <p>
	 * If a test fail due to an exception not related to a JUnit assertion,
	 * then remove such test from the input list
	 * 
	 * @param tests
	 */
	public static void handleTestsThatAreUnstable(List<TestCase> tests){		
		
		logger.info("Going to execute: handleTestsThatAreUnstable");
		
		if (tests == null || tests.isEmpty()) { //nothing to do
			return;
		}

		File dir = createNewTmpDir();
		if(dir==null){
			logger.warn("Failed to create tmp dir");
			return;
		}
		logger.debug("Created tmp folder: "+dir.getAbsolutePath());
		
		try{
			List<File> generated = compileTests(tests,dir);
			if(generated==null){
				/*
				 * Note: in theory this shouldn't really happen, as check for compilation
				 * is done before calling this method
				 */
				logger.warn("Failed to compile the test cases ");
				return;
			}

			Class<?>[] testClasses = loadTests(generated,dir);

			if (testClasses == null) {
				logger.error("Found no classes for compiled tests");
				return;
			}

			JUnitCore runner = new JUnitCore();
			Result result = runner.run(testClasses);

			if (result.wasSuccessful()){
				return; //everything is OK
			} 

			logger.error("" + result.getFailureCount() + " test cases failed");
			for (Failure failure : result.getFailures()) {
				Description des = failure.getDescription();
				String testName = des.getMethodName();//TODO check if correct

				logger.warn("Found unstable test named "+testName+" -> " + failure.getException().getClass() + ": "+ failure.getMessage() );

				boolean toRemove = !(failure.getException() instanceof java.lang.AssertionError);

				for(int i=0; i<tests.size(); i++){
					if(TestSuiteWriter.getNameOfTest(tests, i).equals(testName)){
						//we have a match. should we remove it or mark as unstable?
						if(!toRemove){
							logger.debug("Going to mark test as unstable: "+testName);
							tests.get(i).setUnstable(true);
						} else {
							logger.debug("Going to remove unstable test: "+testName);
							tests.remove(i);
						}
						break;
					}
				}
			}
		} catch (Exception e) {
			logger.error("" + e, e);			
		} finally {
			//let's be sure we clean up all what we wrote on disk
			
			if (dir != null) {
				try {
					FileUtils.deleteDirectory(dir);
					logger.debug("Deleted tmp folder: "+dir.getAbsolutePath());
				} catch (Exception e) {
					logger.warn("Cannot delete tmp dir: " + dir.getName(), e);
				}
			}
		
		}

	}

	/**
	 * Check if it is possible to use the Java compiler.
	 * @return
	 */
	public static boolean isJavaCompilerAvailable(){
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		return compiler != null;
	}

	private static List<File> compileTests(List<TestCase> tests, File dir){

		TestSuiteWriter suite = new TestSuiteWriter();
		suite.insertTests(tests);

		String name = Properties.TARGET_CLASS.substring(Properties.TARGET_CLASS.lastIndexOf(".") + 1);
		name += "Test"; //postfix

		try {
			//now generate the JUnit test case
			List<File> generated = suite.writeTestSuite(name, dir.getAbsolutePath());
			for (File file : generated) {
				if (!file.exists()) {
					logger.error("Supposed to generate " + file
							+ " but it does not exist");
					return null;
				}
			}

			//try to compile the test cases
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			if (compiler == null) {
				logger.error("No Java compiler is available");
				return null;
			}

			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics,
					Locale.getDefault(),
					Charset.forName("UTF-8"));
			Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(generated);

			CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null,
					null, compilationUnits);
			boolean compiled = task.call();
			fileManager.close();

			if (!compiled) {
				logger.error("Compilation failed on compilation units: "
						+ compilationUnits);
				for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
					if(diagnostic.getMessage(null).startsWith("error while writing")) {
						logger.error("Error is due to file permissions, ignoring...");
						return generated;
					}
					logger.error("Diagnostic: " + diagnostic.getMessage(null) + ": "
							+ diagnostic.getLineNumber());
				}
				for (JavaFileObject sourceFile : compilationUnits) {
					List<String> lines = FileUtils.readLines(new File(
							sourceFile.toUri().getPath()));
					logger.error(compilationUnits.iterator().next().toString());
					for (int i = 0; i < lines.size(); i++) {
						logger.error((i + 1) + ": " + lines.get(i));
					}
				}
				return null;
			}

			return generated;

		} catch (IOException e) {
			logger.error("" + e, e);
			return null;
		} 
	}


	protected static File createNewTmpDir(){
		File dir = null;
		String dirName = FileUtils.getTempDirectoryPath() + File.separator + "EvoSuite_"+(dirCounter++)+"_"+
				+ System.currentTimeMillis();

		//first create a tmp folder
		dir = new File(dirName);
		if (!dir.mkdirs()) {
			logger.error("Cannot create tmp dir: " + dirName);
			return null;
		}

		if(!dir.exists()){
			logger.error("Weird behavior: we created folder, but Java cannot determine if it exists? Folder: "+dirName);
			return null;
		}
		
		return dir;
	}


	private static Class<?>[] loadTests(List<File> tests, File dir){

		try{
			Properties.CP += File.pathSeparator + dir.getAbsolutePath();
			ClassPathHacker.addFile(dir); //FIXME need refactoring
		} catch(Exception e){
			logger.error("Failed to add folder to classpath: "+dir.getAbsolutePath());
			return null;
		}

		/*
		 * Ideally, when we run a generated test case, it
		 * will automatically use JavaAgent to instrument the CUT.
		 * But here we have already loaded the CUT by now, so that 
		 * mechanism will not work.
		 * 
		 * A simple option is to just use an instrumenting class loader,
		 * as it does exactly the same type of instrumentation.
		 * TODO: but a better idea would be to use a new 
		 * non-instrumenting classloader to re-load the CUT, and so see
		 * if the JavaAgent works properly.
		 */
		InstrumentingClassLoader loader = new InstrumentingClassLoader();
		Class<?>[] testClasses = getClassesFromFiles(tests, loader);
		return testClasses;		
	}

	/**
	 * <p>
	 * The output of EvoSuite is a set of test cases. For debugging and
	 * experiment, we usually would not write any JUnit to file. But we still
	 * want to see if test cases can compile and execute properly. As EvoSuite
	 * is supposed to only capture the current behavior of the SUT, all
	 * generated test cases should pass.
	 * </p>
	 * 
	 * <p>
	 * Here we compile to a tmp folder, load and execute the test cases, and
	 * then clean up (ie delete all generated files).
	 * </p>
	 * 
	 * @param tests
	 * @return
	 */
	public static boolean verifyCompilationAndExecution(List<TestCase> tests) {

		if (tests == null || tests.isEmpty()) {
			//nothing to compile or run
			return true;
		}

		File dir = createNewTmpDir();
		if(dir==null){
			logger.warn("Failed to create tmp dir");
			return false;
		}

		try{
			List<File> generated = compileTests(tests,dir);
			if(generated==null){
				logger.warn("Failed to compile the test cases ");
				return false;
			}


			//as last step, execute the generated/compiled test cases

			Class<?>[] testClasses = loadTests(generated,dir);

			if (testClasses == null) {
				logger.error("Found no classes for compiled tests");
				return false;
			}

			JUnitCore runner = new JUnitCore();
			Result result = runner.run(testClasses);
			if (!result.wasSuccessful()) {
				logger.error("" + result.getFailureCount() + " test cases failed");
				for (Failure failure : result.getFailures()) {
					logger.error("Failure " + failure.getException().getClass() + ": "
							+ failure.getMessage() + "\n" + failure.getTrace());
				}
				/*
				for (JavaFileObject sourceFile : compilationUnits) {
					List<String> lines = FileUtils.readLines(new File(
					        sourceFile.toUri().getPath()));
					logger.error(compilationUnits.iterator().next().toString());
					for (int i = 0; i < lines.size(); i++) {
						logger.error((i + 1) + ": " + lines.get(i));
					}
				}
				 */
				return false;
			} else {
				/*
				 * OK, it was successful, but was there any test case at all?
				 * 
				 * Here we just log (and not return false), as it might be that EvoSuite is just not able to generate
				 * any test case for this SUT
				 */
				if (result.getRunCount() == 0) {
					logger.warn("There was no test to run");
					//return false;
				}
			}

		} catch (Exception e) {
			logger.error("" + e, e);
			return false;
		} finally {
			//let's be sure we clean up all what we wrote on disk
			if (dir != null) {
				try {
					FileUtils.deleteDirectory(dir);
				} catch (IOException e) {
					logger.warn("Cannot delete tmp dir: " + dir.getName(), e);
				}
			}
		}

		logger.debug("Successfully compiled and run test cases generated for "
				+ Properties.TARGET_CLASS);
		return true;
	}

	/**
	 * Given a list of files representing .java classes, load them (it assumes
	 * the classpath to be correctly set)
	 * 
	 * @param files
	 * @return
	 */
	private static Class<?>[] getClassesFromFiles(List<File> files, ClassLoader loader) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		for (File file : files) {

			//String packagePrefix = Properties.TARGET_CLASS.substring(0,Properties.TARGET_CLASS.lastIndexOf(".")+1);
			String packagePrefix = Properties.CLASS_PREFIX;
			if (!packagePrefix.isEmpty() && !packagePrefix.endsWith(".")) {
				packagePrefix += ".";
			}

			final String JAVA = ".java";
			String name = file.getName();
			name = name.substring(0, name.length() - JAVA.length());
			String className = packagePrefix + name;

			Class<?> testClass = null;
			try {
				testClass = loader.loadClass(className);
			} catch (ClassNotFoundException e) {
				logger.error("Failed to load test case " + className + " from file "+file.getAbsolutePath()+" , error " + e, e);
				return null;
			}
			classes.add(testClass);
		}
		return classes.toArray(new Class<?>[classes.size()]);
	}


}
