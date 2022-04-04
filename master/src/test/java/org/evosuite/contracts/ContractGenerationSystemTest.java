/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.contracts;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
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

public class ContractGenerationSystemTest extends SystemTestBase {

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

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

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

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        evosuite.parseCommandLine(command);

        // TODO: No executor in master, so this will fail
//		for(TestCase test : FailingTestSet.getFailingTests()) {
//			System.out.println(test.toCode());
//		}

        // 1 Undeclared contract
        // 1 JCrasher
        //Assert.assertEquals(2, FailingTestSet.getNumberOfUniqueViolations());
        Assert.assertEquals(1,
                FailingTestSet.getNumberOfViolations(ToStringReturnsNormallyContract.class));
    }

    @Test
    public void testHashCodeReturnsNormally() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = HashcodeException.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CHECK_CONTRACTS = true;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        evosuite.parseCommandLine(command);


        // Also reported by JCrasher
        // Assert.assertEquals(2, FailingTestSet.getNumberOfUniqueViolations());
        Assert.assertEquals(1,
                FailingTestSet.getNumberOfViolations(HashCodeReturnsNormallyContract.class));
    }

    @Test
    public void testEqualsSelfContract() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = EqualsSelf.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CHECK_CONTRACTS = true;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        evosuite.parseCommandLine(command);

        // Assert.assertEquals(2, FailingTestSet.getNumberOfUniqueViolations());
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

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

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

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        evosuite.parseCommandLine(command);

        Assert.assertTrue(FailingTestSet.getNumberOfViolations(EqualsHashcodeContract.class) > 0);
        Assert.assertTrue(FailingTestSet.getNumberOfViolations(EqualsSymmetricContract.class) > 0);
    }

    @Test
    public void testEqualsSymmetricContract() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = EqualsSymmetric.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CHECK_CONTRACTS = true;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        evosuite.parseCommandLine(command);
        // TODO: No executor in master, so this will fail
        //for(TestCase test : FailingTestSet.getFailingTests()) {
        //	System.out.println(test.toString());
        //}

        // Assert.assertEquals(1, FailingTestSet.getNumberOfUniqueViolations());
        Assert.assertEquals(1,
                FailingTestSet.getNumberOfViolations(EqualsSymmetricContract.class));
    }

    @Test
    public void testNullPointerExceptionContract() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = RaiseNullPointerException.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CHECK_CONTRACTS = true;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        evosuite.parseCommandLine(command);

        // This is reported by the NullPointer contract but also by the undeclared exception contract
        // and the JCrasher contract
        //Assert.assertEquals(3, FailingTestSet.getNumberOfUniqueViolations());
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

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        evosuite.parseCommandLine(command);

        // This is reported by the NullPointer contract but also by the undeclared exception contract
        Assert.assertEquals(1, FailingTestSet.getNumberOfUniqueViolations());
        Assert.assertEquals(1,
                FailingTestSet.getNumberOfViolations(JUnitTheoryContract.class));
    }

}
