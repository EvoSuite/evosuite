package org.evosuite.junit.examples;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.examples.with.different.packagename.FlagExample1;

@RunWith(Parameterized.class)
public class JUnit4ParameterizedTest {

	private Integer input;
	private Boolean output;
	private FlagExample1 cut;

	@Before
	public void initialize() {
		this.cut = new FlagExample1();
	}

	public JUnit4ParameterizedTest(Integer a, Boolean b) {
		this.input = a;
		this.output = b;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> primeNumbers() {
		return Arrays.asList(new Object[][] { {7, false}, {28241, true} });
	}

	@Test
	public void test() {
		assertEquals(output, cut.testMe(input));
	}
}
