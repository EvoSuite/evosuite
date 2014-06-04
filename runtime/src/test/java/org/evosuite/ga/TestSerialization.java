package org.evosuite.ga;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.evosuite.localsearch.LocalSearchObjective;
import org.junit.Assert;
import org.junit.Test;

public class TestSerialization {


	public static class DummyChromosome extends Chromosome {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3352143089964383872L;

		@Override
		public Chromosome clone() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean equals(Object obj) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public int hashCode() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int compareSecondaryObjective(Chromosome o) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void mutate() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void crossOver(Chromosome other, int position1, int position2)
				throws ConstructionFailedException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean localSearch(
				LocalSearchObjective<? extends Chromosome> objective) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
	
	@Test
	public void testSerialization() throws IOException, ClassNotFoundException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		
		double fitness = 3.14;
		DummyChromosome chromosome = new DummyChromosome();
		chromosome.setFitness(fitness);		
		oos.writeObject(chromosome);
		byte [] baSerialized = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baSerialized);
		ObjectInputStream ois = new ObjectInputStream(bais);
		DummyChromosome copy = (DummyChromosome) ois.readObject();
		Assert.assertEquals(chromosome.getFitness(), copy.getFitness(), 0.0);
	}
	

}
