package de.unisb.cs.st.evosuite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.*;

import com.examples.with.different.packagename.InfiniteLoops;
import com.examples.with.different.packagename.PrintingThatShouldBeMuted;
import com.examples.with.different.packagename.StaticPrinting;


import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.utils.LoggingUtils;

public class TestSUTPrintingThatShouldBeMuted extends SystemTest{

	public static final int defaultTimeout = Properties.TIMEOUT;
	public static final boolean defaultPrintToSystem = Properties.PRINT_TO_SYSTEM;
	
	public static final PrintStream defaultOut = System.out;
	
	@After
	public void resetProperties(){
		Properties.TIMEOUT = defaultTimeout;
		Properties.PRINT_TO_SYSTEM = defaultPrintToSystem;
		
		System.setOut(defaultOut);
		Properties.CLIENT_ON_THREAD = true;
	}
	
	
	
	public void checkIfMuted(String targetClass, String msgSUT){
		Properties.CLIENT_ON_THREAD = false;
		
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		PrintStream byteOut = new PrintStream(byteStream);
		System.setOut(byteOut);
		
		EvoSuite evosuite = new EvoSuite();
				
		Properties.TARGET_CLASS = targetClass;
		
		Properties.TIMEOUT = 300;
		
		String[] command = new String[]{				
				"-generateSuite",
				"-class",
				targetClass,
				"-Dprint_to_system=true"
		};
		
		Object result = evosuite.parseCommandLine(command);
			
		String printed = byteStream.toString();
		Assert.assertTrue("PRINTED:\n"+printed,printed.contains("Starting client"));
		Assert.assertTrue("PRINTED:\n"+printed,printed.contains(msgSUT));		

		
		command = new String[]{				
				"-generateSuite",
				"-class",
				targetClass,
				"-Dprint_to_system=false"
		};
		
		byteStream.reset();
		result = evosuite.parseCommandLine(command);
			
		printed = byteStream.toString();
		Assert.assertTrue("PRINTED:\n"+printed,printed.contains("Starting client"));
		Assert.assertFalse("PRINTED:\n"+printed,printed.contains(msgSUT));			
	}
	
	
	@Test
	public void testBase() throws IOException{		
		checkIfMuted(PrintingThatShouldBeMuted.class.getCanonicalName(),"Greater");
	}

	@Test
	public void testStatic() throws IOException{
		checkIfMuted(StaticPrinting.class.getCanonicalName(),"this should not be printed");
	}

	/**
	 * This has quite a few side effects on other test cases
	 * @throws IOException
	 */
	@Ignore
	@Test
	public void testInfiniteLoops() throws IOException{
		checkIfMuted(InfiniteLoops.class.getCanonicalName(),"This should not be printed");
	}
}
