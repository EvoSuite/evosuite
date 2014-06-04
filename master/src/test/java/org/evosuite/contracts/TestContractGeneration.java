package org.evosuite.contracts;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.examples.with.different.packagename.contracts.AssertionException;
import com.examples.with.different.packagename.contracts.EqualsHashCode;
import com.examples.with.different.packagename.contracts.EqualsNull;
import com.examples.with.different.packagename.contracts.EqualsSelf;
import com.examples.with.different.packagename.contracts.EqualsSymmetric;
import com.examples.with.different.packagename.contracts.Foo;
import com.examples.with.different.packagename.contracts.FooTheories;
import com.examples.with.different.packagename.contracts.HashcodeException;
import com.examples.with.different.packagename.contracts.RaiseNullPointerException;
import com.examples.with.different.packagename.contracts.ToStringException;

public class TestContractGeneration extends SystemTest {

	private boolean checkContracts = false;

	private String junitTheories = "";

	@Before
	public void storeCheckContracts() {
		checkContracts = Properties.CHECK_CONTRACTS;
		junitTheories = Properties.JUNIT_THEORIES;
		FailingTestSet.clear();
	}

	@After
	public void restoreCheckContracts() {
		Properties.CHECK_CONTRACTS = checkContracts;
		Properties.JUNIT_THEORIES = junitTheories;
	}

	@Test
	public void testEqualsNull() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = EqualsNull.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.CHECK_CONTRACTS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);

		Assert.assertEquals(1, FailingTestSet.getNumberOfUniqueViolations());
		Assert.assertEquals(1,
		                    FailingTestSet.getNumberOfViolations(EqualsNullContract.class));
	}

	@Test
	public void testToStringException() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ToStringException.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.CHECK_CONTRACTS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);

		Assert.assertEquals(1, FailingTestSet.getNumberOfUniqueViolations());
		Assert.assertEquals(1,
		                    FailingTestSet.getNumberOfViolations(ToStringReturnsNormallyContract.class));
	}

	@Test
	public void testHashCodeReturnsNormally() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = HashcodeException.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.CHECK_CONTRACTS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);

		Assert.assertEquals(1, FailingTestSet.getNumberOfUniqueViolations());
		Assert.assertEquals(1,
		                    FailingTestSet.getNumberOfViolations(HashCodeReturnsNormallyContract.class));
	}

	@Test
	public void testEqualsSelfContract() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = EqualsSelf.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.CHECK_CONTRACTS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);

		Assert.assertEquals(1, FailingTestSet.getNumberOfUniqueViolations());
		Assert.assertEquals(1, FailingTestSet.getNumberOfViolations(EqualsContract.class));
	}

	// TODO: How to activate assertions when running with client on thread?
	@Ignore
	@Test
	public void testAssertionContract() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = AssertionException.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.CHECK_CONTRACTS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);

		Assert.assertEquals(1, FailingTestSet.getNumberOfUniqueViolations());
		Assert.assertEquals(1,
		                    FailingTestSet.getNumberOfViolations(AssertionErrorContract.class));
	}

	@Test
	public void testEqualsHashcodeContract() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = EqualsHashCode.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.CHECK_CONTRACTS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		evosuite.parseCommandLine(command);

		Assert.assertEquals(4, FailingTestSet.getNumberOfUniqueViolations());
		Assert.assertTrue(FailingTestSet.getNumberOfViolations(EqualsHashcodeContract.class) > 0);
	}

	@Test
	public void testEqualsSymmetricContract() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = EqualsSymmetric.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.CHECK_CONTRACTS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);

		Assert.assertEquals(1, FailingTestSet.getNumberOfUniqueViolations());
		Assert.assertEquals(1,
		                    FailingTestSet.getNumberOfViolations(EqualsSymmetricContract.class));
	}

	@Test
	public void testNullPointerExceptionContract() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = RaiseNullPointerException.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.CHECK_CONTRACTS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);

		// This is reported by the NullPointer contract but also by the undeclared exception contract
		Assert.assertEquals(2, FailingTestSet.getNumberOfUniqueViolations());
		Assert.assertEquals(1,
		                    FailingTestSet.getNumberOfViolations(UndeclaredExceptionContract.class));
	}

	@Test
	public void testJUnitTheoryContract() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Foo.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.CHECK_CONTRACTS = true;
		Properties.JUNIT_THEORIES = FooTheories.class.getCanonicalName();

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);

		// This is reported by the NullPointer contract but also by the undeclared exception contract
		Assert.assertEquals(1, FailingTestSet.getNumberOfUniqueViolations());
		Assert.assertEquals(1,
		                    FailingTestSet.getNumberOfViolations(JUnitTheoryContract.class));
	}

}
