package de.unisb.cs.st.evosuite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.*;

import com.examples.with.different.packagename.PrintingThatShouldBeMuted;


import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

public class TestSUTPrintingThatShouldBeMuted extends SystemTest{

	public static final int defaultTimeout = Properties.TIMEOUT;
	public static final boolean defaultPrintToSystem = Properties.PRINT_TO_SYSTEM;
	
	public static final PrintStream defaultOut = System.out;
	
	@After
	public void resetProperties(){
		Properties.TIMEOUT = defaultTimeout;
		Properties.PRINT_TO_SYSTEM = defaultPrintToSystem;
		
		System.setOut(defaultOut);
	}
	
	@Ignore
	@Test
	public void testMuted() throws IOException{
		
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		PrintStream byteOut = new PrintStream(byteStream);
		System.setOut(byteOut);
		
		EvoSuite evosuite = new EvoSuite();
				
		String targetClass = PrintingThatShouldBeMuted.class.getCanonicalName();
		
		Properties.TARGET_CLASS = targetClass;
		
		Properties.TIMEOUT = 300;
		Properties.PRINT_TO_SYSTEM = false;
		
		String[] command = new String[]{				
				"-generateSuite",
				"-class",
				targetClass
		};
		
		Object result = evosuite.parseCommandLine(command);
		
		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :"+result.getClass(), result instanceof GeneticAlgorithm);
		
		GeneticAlgorithm ga = (GeneticAlgorithm) result;
		Assert.assertEquals("Wrong number of generations: ", 0, ga.getAge());

		int bytesUsedByEvoWhenSUTIsMuted = byteStream.size();
		byteStream.reset();
		
		Properties.PRINT_TO_SYSTEM = true;
		evosuite.parseCommandLine(command);
		int bytesUnMuted = byteStream.size();
		byteStream.reset();
		Assert.assertTrue("No difference between muted/unmuted",bytesUnMuted > bytesUsedByEvoWhenSUTIsMuted);	
		
		/*
		 * we do it again, just to be sure
		 */
		Properties.PRINT_TO_SYSTEM = false;
		evosuite.parseCommandLine(command);
		bytesUsedByEvoWhenSUTIsMuted = byteStream.size();
		byteStream.reset();
		Assert.assertTrue("No difference between muted/unmuted",bytesUnMuted > bytesUsedByEvoWhenSUTIsMuted);	
	}

}
