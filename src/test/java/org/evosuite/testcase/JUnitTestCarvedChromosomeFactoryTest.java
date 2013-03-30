package org.evosuite.testcase;


import org.evosuite.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JUnitTestCarvedChromosomeFactoryTest {

	private static final String defaultSelectedJUnit = Properties.SELECTED_JUNIT; 
	private static final int defaultSeedMutations = Properties.SEED_MUTATIONS;
	private static final double defaultSeedClone = Properties.SEED_CLONE;
	
	@Before
	public void reset(){
		Properties.SELECTED_JUNIT = defaultSelectedJUnit;
		Properties.SEED_MUTATIONS = defaultSeedMutations;
		Properties.SEED_CLONE = defaultSeedClone;
	}
	
	@Test
	public void testDefaultEmptySetting(){
		/*
		 * by default, no seeded test should be selected
		 */
		try{
			JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(null);
			Assert.fail("Expected IllegalStateException");
		} catch(IllegalStateException e){
			//expected
		}
	}
	
	@Test
	public void testSimpleTest(){
		Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.SimpleTest.class.getCanonicalName();
		Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.Simple.class.getCanonicalName();
		
		
		Properties.SEED_MUTATIONS = 1;
		Properties.SEED_CLONE = 1;
		
		JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(null);
		TestChromosome carved = factory.getChromosome();
		
		Assert.assertNotNull(carved);
		Assert.assertEquals("Shouble be constructor, method, 2 variables, method, 1 variable, method", 7 , carved.test.size());
	}
}
