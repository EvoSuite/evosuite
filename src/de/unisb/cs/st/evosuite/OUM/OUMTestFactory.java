/**
 * 
 */
package de.unisb.cs.st.evosuite.OUM;

import java.io.File;

import org.softevo.jadet.sca.EventPair;
import org.softevo.jadet.sca.Method;
import org.softevo.jadet.sca.Pattern;
import org.softevo.jadet.sca.PatternsList;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.Statement;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestFactory;
import de.unisb.cs.st.ga.Randomness;

/**
 * @author Gordon Fraser
 *
 */
public class OUMTestFactory  {
	
	private Randomness randomness = Randomness.getInstance();
	
	private TestFactory test_factory = TestFactory.getInstance();
	
	public OUMTestFactory() {
		readOUMs(Properties.getProperty("OUM.file"));
	}
	
	private void readOUMs(String filename) {
		PatternsList patterns = PatternsList.readFromXML(new File("test.xml"));
		for(Pattern pattern : patterns) {
			System.out.println("EventPairs:");
			for(EventPair pair : pattern.getProperties()) {
				System.out.println(pair);
			}
			System.out.println("Methods:");
			for(Method method : pattern.getObjects()) {
				System.out.println(method);
			}
			
		}
	}
	
	/**
	 * Insert a random statement at a random position in the test
	 * @param test
	 */
	public void insertRandomStatement(TestCase test) {
		final double P = 1d/2d;
		
		double r = randomness.nextDouble();
		
		if(r <= P) {
			test_factory.insertRandomStatement(test);
		} 
		else if( r <= 2*P)
		{
			insertRandomPattern(test);
		}	
	}
	
	public void insertRandomPattern(TestCase test) {
		int position = randomness.nextInt(test.size() + 1);
		Statement s = test.getStatement(position);

		// Find patterns this statement if part of
		// Randomly select one pattern and insert statements
		// If none match, insert a new pattern
	}
	
}
