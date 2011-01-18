/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */


package de.unisb.cs.st.evosuite.testcase;

import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.OUM.OUMTestFactory;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;

/**
 * Chromosome representation of test cases
 * @author Gordon Fraser
 *
 */
public class TestChromosome extends Chromosome {
	
	private final static boolean RANK_LENGTH = Properties.getPropertyOrDefault("check_rank_length", true);  
	
	private final static boolean CHECK_LENGTH = Properties.getPropertyOrDefault("check_max_length", true);
	
	static {
		if(RANK_LENGTH)
			logger.debug("Using rank check");
		else
			logger.debug("Not using rank check");
	}
	
	/** The test case encoded in this chromosome */
	public TestCase test = new TestCase();
	
	/** Factory to manipulate and generate method sequences */
	private static AbstractTestFactory test_factory = null;

	/** True if this leads to an exception */
	private boolean has_exception = false;

	public TestChromosome() {
		if(test_factory == null) {
			String factory_name = Properties.getPropertyOrDefault("test_factory", "Random");
			if(factory_name.equals("OUM"))
				test_factory = OUMTestFactory.getInstance();
			else
				test_factory = DefaultTestFactory.getInstance();
		}
	}
	
	public ExecutionResult last_result = null;
	
	/*
	public TestChromosome(TestFactory test_factory, TestMutationFactory mutation_factory) {
		this.test_factory = test_factory;
		this.mutation_factory = mutation_factory;
	}
	*/
	
	/**
	 * Create a deep copy of the chromosome
	 */
	public Chromosome clone() {
		TestChromosome c = new TestChromosome();
		c.test = test.clone();
		
		c.setFitness(getFitness());
		c.solution = solution;
		c.setChanged(isChanged());
		if(last_result != null)
			c.last_result = last_result.clone(); // TODO: Clone?
		
		return c;
	}
	
	
	/**
	 * Single point cross over
	 * @throws ConstructionFailedException 
	 */
	public void crossOver(Chromosome other, int position1, int position2) throws ConstructionFailedException  {
		logger.debug("Crossover starting");
		TestChromosome offspring = new TestChromosome();
		
		for(int i=0; i<position1; i++) {
			offspring.test.addStatement(test.getStatement(i).clone());
		}
		for(int i=position2; i<other.size(); i++) {
			test_factory.appendStatement(offspring.test, ((TestChromosome)other).test.getStatement(i));
		}
		if(!CHECK_LENGTH || offspring.test.size() <= Properties.CHROMOSOME_LENGTH) {
			test = offspring.test;
		}
			//logger.warn("Size exceeded!");
		setChanged(true);
	}

	
	/**
	 * Two chromosomes are equal if their tests are equal
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		TestChromosome other = (TestChromosome) obj;
		if (test == null) {
			if (other.test != null)
				return false;
		} else if (!test.equals(other.test))
			return false;
		return true;
	}
	
	/**
	 * Determine relative ordering of this chromosome to another chromosome
	 * If fitness is equal, the shorter chromosome comes first
	 */
	public int compareTo(Chromosome o) {
		if(RANK_LENGTH && getFitness() == o.getFitness()) {
			return (int) Math.signum((size() - ((TestChromosome)o).size()));
		}
		else
			return (int) Math.signum(getFitness() - o.getFitness());
	}
	
	/**
	 * Each statement is mutated with probability 1/l 
	 */
	public void mutate() {
		boolean changed = false;
		final double P = 1d/3d;
		
		// Delete
		if(randomness.nextDouble() <= P) {
			changed = mutationDelete();
		}
		
		// Change
		if(randomness.nextDouble() <= P) {
			changed = changed || mutationChange();
		//	changed = true;
		}
		
		// Insert
		if(randomness.nextDouble() <= P) {
			changed = changed || mutationInsert();
			//changed = true;
		}
		
		if(changed) {
			setChanged(true);
			last_result = null;
		}
	}
	
	/**
	 * Each statement is deleted with probability 1/length
	 * @return
	 */
	private boolean mutationDelete() {
		boolean changed = false;
		double pl = 1d/test.size();
		for(int num = test.size() - 1; num >= 0; num--) {

			// Each statement is deleted with probability 1/l
			if(randomness.nextDouble() <= pl) {
				//if(!test.hasReferences(test.getStatement(num).getReturnValue())) {
					try {
						TestCase copy = test.clone();
//						test_factory.deleteStatement(test, num);
						changed = true;
						test_factory.deleteStatementGracefully(copy, num);
						test = copy;

					} catch (ConstructionFailedException e) {
						logger.warn("Deletion of statement failed: "+test.getStatement(num).getCode());
						logger.warn(test.toCode());
					}
				//}
			}
		}
		
		return changed;
	}

	/**
	 * Each statement is replaced with probability 1/length
	 * @return
	 */
	private boolean mutationChange() {
		boolean changed = false;
		double pl = 1d/test.size();

		for(Statement statement : test.getStatements()) {
			if(randomness.nextDouble() <= pl) {

				if(statement instanceof PrimitiveStatement<?>) {
					//  do some mutation of values with what probability?
					//logger.info("Old statement: "+statement.getCode());
					((PrimitiveStatement<?>)statement).delta();
					
					//int position = statement.retval.statement;
					//test.setStatement(statement, position);
					//logger.info("Changed test: "+test.toCode());
					changed = true;	
				} else if(statement instanceof AssignmentStatement) {
					//logger.info("Before change at:");
					//logger.info(test.toCode());
					AssignmentStatement as = (AssignmentStatement) statement;
					if(randomness.nextDouble() < 0.5) {
						List<VariableReference> objects = test.getObjects(statement.retval.getType(), statement.retval.statement);
						objects.remove(statement.retval);
						objects.remove(as.parameter);
						if(!objects.isEmpty()) {
							as.parameter = randomness.choice(objects);
							changed = true;
						}
					} else if(as.retval.array_length > 0) {
						as.retval.array_index = randomness.nextInt(as.retval.array_length);
						changed = true;
					}
					//logger.info("After change:");
					//logger.info(test.toCode());
				} else if (statement.retval.isArray()) {
					
				} else {
					changed = test_factory.changeRandomCall(test, statement);
				}
			}
		}
		
		return changed;
	}
	
	/**
	 * With exponentially decreasing probability, insert statements at random position
	 * @return
	 */
	private boolean mutationInsert() {
		boolean changed = false;
		final double ALPHA = 0.5;
		int count = 1;
		
		while(randomness.nextDouble() <= Math.pow(ALPHA, count) && (!CHECK_LENGTH || size() < Properties.CHROMOSOME_LENGTH))
		{
			count++;				
			// Insert at position as during initialization (i.e., using helper sequences)
			test_factory.insertRandomStatement(test);
			changed = true;
		}
		return changed;
	}
	


	/**
	 * The size of a chromosome is the length of its test case
	 */
	public int size() {
		return test.size();
	}

	public String toString() {
		return test.toCode();
	}
	
	public boolean hasException() {
		return has_exception;
	}
}
