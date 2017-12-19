package org.evosuite.ga.metaheuristics;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.Neighbourhood;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Test;

/**
 * Unit tests for Neighbourhood's functions (i.e, part of Cellular GA functions)
 * @author Nasser Albunian
 *
 */
public class NeighbourhoodTest {
	
	List<Chromosome> population = new ArrayList<Chromosome>();
	
	public void constructPopulation(){
		
		Properties.POPULATION = 16;
		
		TestSuiteChromosome[] testSuiteChromObjects = new TestSuiteChromosome[Properties.POPULATION];

		for(int i=0; i<testSuiteChromObjects.length; i++){
			testSuiteChromObjects[i] = new TestSuiteChromosome();
			DefaultTestCase testcase = new DefaultTestCase();
			StringPrimitiveStatement stringStatement = new StringPrimitiveStatement(testcase, "any string statatement for TS"+i);
			testcase.addStatement(stringStatement);
			testSuiteChromObjects[i].addTest(testcase);
		    population.add(testSuiteChromObjects[i]);
		}
		
	}

	
	@Test
	public void testRingTopology_leftNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.ringTopology(population, 2);
	    
	    Chromosome exepcted_individual = population.get(1);
	    Chromosome returned_individual = neighbors.get(0);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testRingTopology_rightNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.ringTopology(population, 2);
	    
	    Chromosome exepcted_individual = population.get(3);
	    Chromosome returned_individual = neighbors.get(2);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testRingTopology_mostLeftNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.ringTopology(population, 0);
	    
	    Chromosome exepcted_individual = population.get(15);
	    Chromosome returned_individual = neighbors.get(0);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testRingTopology_mostRightNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.ringTopology(population, 15);
	    
	    Chromosome exepcted_individual = population.get(0);
	    Chromosome returned_individual = neighbors.get(2);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testNorthNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.linearFive(population, 5);
	    
	    Chromosome exepcted_individual = population.get(1);
	    Chromosome returned_individual = neighbors.get(0);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testSouthNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.linearFive(population, 5);
	    
	    Chromosome exepcted_individual = population.get(9);
	    Chromosome returned_individual = neighbors.get(1);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testEastNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.linearFive(population, 5);
	    
	    Chromosome exepcted_individual = population.get(6);
	    Chromosome returned_individual = neighbors.get(2);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testWestNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.linearFive(population, 5);
	    
	    Chromosome exepcted_individual = population.get(4);
	    Chromosome returned_individual = neighbors.get(3);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testNorthEastNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.compactNine(population, 5);
	    
	    Chromosome exepcted_individual = population.get(2);
	    Chromosome returned_individual = neighbors.get(6);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testNorthWestNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.compactNine(population, 5);
	    
	    Chromosome exepcted_individual = population.get(0);
	    Chromosome returned_individual = neighbors.get(4);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testSouthEastNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.compactNine(population, 5);
	    
	    Chromosome exepcted_individual = population.get(10);
	    Chromosome returned_individual = neighbors.get(7);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testSouthWestNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.compactNine(population, 5);
	    
	    Chromosome exepcted_individual = population.get(8);
	    Chromosome returned_individual = neighbors.get(5);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testNorthNorthNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.CompactThirteen(population, 10);
	    
	    Chromosome exepcted_individual = population.get(2);
	    Chromosome returned_individual = neighbors.get(8);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testSouthSouthNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.CompactThirteen(population, 10);
	    
	    Chromosome exepcted_individual = population.get(2);
	    Chromosome returned_individual = neighbors.get(9);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testEastEastNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.CompactThirteen(population, 10);
	    
	    Chromosome exepcted_individual = population.get(8);
	    Chromosome returned_individual = neighbors.get(10);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testWestWestNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.CompactThirteen(population, 5);
	    
	    Chromosome exepcted_individual = population.get(7);
	    Chromosome returned_individual = neighbors.get(11);
	    	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testMostNorthWestNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.CompactThirteen(population, 0);
	    
	    Chromosome exepcted_individual = population.get(15);
	    Chromosome returned_individual = neighbors.get(4);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testMostNorthEastNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.CompactThirteen(population, 3);
	    
	    Chromosome exepcted_individual = population.get(12);
	    Chromosome returned_individual = neighbors.get(6);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testMostSouthWestNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.CompactThirteen(population, 12);
	    
	    Chromosome exepcted_individual = population.get(3);
	    Chromosome returned_individual = neighbors.get(5);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
	@Test
	public void testMostSouthEastNeighbour(){
	    
		this.constructPopulation();
		
	    Neighbourhood<Chromosome> neighbourhood = new Neighbourhood<>(Properties.POPULATION);
	    
	    List<Chromosome> neighbors = new ArrayList<>();
	    
	    neighbors = neighbourhood.CompactThirteen(population, 15);
	    
	    Chromosome exepcted_individual = population.get(0);
	    Chromosome returned_individual = neighbors.get(7);
	    
	    assertEquals(exepcted_individual, returned_individual);  
	}
	
}
