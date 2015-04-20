/**
 * 
 */
package org.evosuite.junit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.manipulation.Filter;

/**
 * <p>
 * JUnitRunner class
 * </p>
 * 
 * @author José Campos
 */
public class JUnitRunner {

	/**
	 * 
	 */
	private static HashSet<String> testNames = new HashSet<String>();

	/**
	 * 
	 */
	private List<JUnitResult> testResults;

	/**
	 * 
	 */
	public JUnitRunner() {
		this.testResults = new ArrayList<JUnitResult>();
	}

	public void run(Class<?> junitClass) {
		Request request = Request.aClass(junitClass);
		request = request.sortWith(new Comparator<Description>() {
			@Override
			public int compare(Description desc1, Description desc2) {
				if ((desc1.getMethodName() == null) && (desc2.getMethodName() == null))
					return 0;
				if (desc1.getMethodName().length() > desc2.getMethodName().length())
					return 1;
				else if (desc1.getMethodName().length() < desc2.getMethodName().length())
					return -1;
				return desc1.getMethodName().compareTo(desc2.getMethodName());
			}
		});
		request = request.filterWith(new Filter() {
			@Override
			public String describe() {
				return null;
			}

			@Override
			public boolean shouldRun(Description desc) {
				if (desc.getMethodName() == null) {
					for (String s : testNames) {
						if (s.contains(desc.getClassName()))
							return false;
					}

					testNames.add(desc.getClassName());
					return true;
				}
				else {
					if (!testNames.contains(desc.getClassName() + "#" + desc.getMethodName())) {
						testNames.add(desc.getClassName() + "#" + desc.getMethodName());
						return true;
					}
					else
						return false;
				}
			}
		});

		JUnitCore junit = new JUnitCore();
		junit.addListener(new JUnitRunListener(this));
		junit.run(request);
	}

	/**
	 * 
	 * @param testResult
	 */
	public void addResult(JUnitResult testResult) {
		this.testResults.add(testResult);
	}

	/**
	 * 
	 * @return
	 */
	public List<JUnitResult> getTestResults() {
		return this.testResults;
	}
}
