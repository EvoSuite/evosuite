package org.evosuite.testsuite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.PrimitiveStatement;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.junit.Assert;
import org.junit.Test;

public class TestSerialization {

	@Test
	public void testSerializationNonEmptySuite() throws IOException, ClassNotFoundException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);

		double fitness = 0.9950513142057124d;
		TestSuiteChromosome chromosome = new TestSuiteChromosome();
		TestChromosome testChromosome = new TestChromosome();
		TestCase test = new DefaultTestCase();
		PrimitiveStatement<?> statement = PrimitiveStatement.getPrimitiveStatement(test, int.class);
		test.addStatement(statement);
		testChromosome.setTestCase(test);
		testChromosome.setFitness(3.14d);		
		chromosome.setFitness(fitness);
		chromosome.setCoverage(0.5);
		chromosome.updateAge(24);
		chromosome.setChanged(true);
		chromosome.setSolution(true);
		chromosome.addTest(testChromosome);
		oos.writeObject(chromosome);
		byte [] baSerialized = baos.toByteArray();

		ByteArrayInputStream bais = new ByteArrayInputStream(baSerialized);
		ObjectInputStream ois = new ObjectInputStream(bais);
		TestSuiteChromosome copy = (TestSuiteChromosome) ois.readObject();
		Assert.assertEquals(chromosome.getFitness(), copy.getFitness(), 0.0);
		Assert.assertEquals(chromosome.getAge(), copy.getAge());
		Assert.assertEquals(chromosome.getCoverage(), copy.getCoverage(), 0.0);
		Assert.assertEquals(chromosome.getCoveredGoals(), copy.getCoveredGoals());
		Assert.assertEquals(chromosome.isChanged(), copy.isChanged());
		Assert.assertEquals(chromosome.isSolution(), copy.isSolution());
		
		Assert.assertEquals(chromosome.getTestChromosome(0).getFitness(), copy.getTestChromosome(0).getFitness(), 0.0);
	}
}
