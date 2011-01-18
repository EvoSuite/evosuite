/**
 * 
 */
package de.unisb.cs.st.evosuite.assertion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.XStream;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * @author Gordon Fraser
 *
 */
public class TestSuiteAsserter {

	@SuppressWarnings("unchecked")
	public void generateAssertions() {
		XStream xstream = new XStream();
		FileReader fstream;
		//TestCluster cluster = TestCluster.getInstance();
		try {
			fstream = new FileReader(Properties.TARGET_CLASS+"_tests.xml");
			BufferedReader in = new BufferedReader(fstream);

			ArrayList<TestCase> tests = (ArrayList<TestCase>)xstream.fromXML(in);
			/*
			for(TestCase test : tests) {
				System.out.println(test.toCode());
			}
			*/
			
			
			AssertionGenerator asserter = new AssertionGenerator();
			int num = 0;
			Set<Long> killed = new HashSet<Long>();
			for(TestCase test : tests) {
				System.out.println("Test "+num+"/"+tests.size());
				/*
				if(num == 11 || num == 25 || num == 26 || num ==31 || num == 37 || num == 44 || num == 49) {
					System.out.println("Skipping");
					num++;
					continue;
				}
				*/
				num++;
				System.out.println("Current test: \n"+test.toCode());
				asserter.addAssertions(test, killed);
				System.out.println("Resulting test: \n"+test.toCode());
			}
//			System.out.println("Mutants killed with assertions: "+killed.size());
			StringBuffer html = new StringBuffer();
			int num_test = 1;
			html.append("<html><head><title>Test cases with assertions</title>\n");
			html.append("<link href=\"prettify.css\" type=\"text/css\" rel=\"stylesheet\" />\n");
            html.append("<script type=\"text/javascript\" src=\"prettify.js\"></script>\n");
            html.append("</head><body  onload=\"prettyPrint()\">\n");
			for(TestCase test : tests) {
				html.append("<h2>Test case ");
				html.append(num_test);
				html.append("</h2>");
				html.append("<pre  class=\"prettyprint\" style=\"border: 1px solid #888;padding: 2px\">");
				html.append(test.toCode());
				html.append("</pre>");
				num_test++;
			}
			html.append("</body></html>");
			FileWriter fout = new FileWriter(Properties.TARGET_CLASS+"_tests.html");
			BufferedWriter out = new BufferedWriter(fout);
			out.write(html.toString());
			out.close();

		} catch(IOException e) {
			
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Creating assertions for test suite.");
		TestSuiteAsserter asserter = new TestSuiteAsserter();
		asserter.generateAssertions();	}

}
