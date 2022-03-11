/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.evosuite.Properties.Criterion;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Test;
import com.examples.with.different.packagename.TargetMethod;

public class TargetMethodSystemTest extends SystemTestBase {

    private void test(Criterion criterion, String methodName, String[] methodsNotTested) {
        EvoSuite evosuite = new EvoSuite();

        Properties.ASSERTIONS = false; // some undesirable methods may end up in assertions

        String targetClass = TargetMethod.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.TARGET_METHOD = methodName;
        Properties.CRITERION = new Properties.Criterion[]{criterion};
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best.toString());
        assertTrue(best.toString().contains(methodName.substring(0, methodName.indexOf("("))));
        for (String methodNotTested : methodsNotTested) {
            assertFalse(best.toString().contains(methodNotTested));
        }
    }

    @Test
    public void testTargetMethodWithTRYCATCH() {
        Properties.EXCEPTION_BRANCHES = true;
        this.test(Properties.Criterion.TRYCATCH, "boo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }

    @Test
    public void testExceptionBranches() {
//    Properties.ERROR_BRANCHES = true;
        Properties.EXCEPTION_BRANCHES = true;
        this.test(Properties.Criterion.EXCEPTION, "boo(Ljava/lang/Integer;)Z", new String[]{"foo", "bar"});
    }

    @Test
    public void testTargetMethodWithALLDEFS() {
        this.test(Properties.Criterion.ALLDEFS, "getY()I", new String[]{"foo", "bar"});
    }

    @Test
    public void testTargetMethodWithAMBIGUITY() {
        this.test(Properties.Criterion.AMBIGUITY, "foo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithBRANCH() {
        this.test(Properties.Criterion.BRANCH, "foo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithCBRANCH() {
        this.test(Properties.Criterion.CBRANCH, "foo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithDEFUSE() {
        this.test(Properties.Criterion.DEFUSE, "foo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithEXCEPTION() {
        this.test(Properties.Criterion.EXCEPTION, "foo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithIBRANCH() {
        this.test(Properties.Criterion.IBRANCH, "foo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithINPUT() {
        this.test(Properties.Criterion.INPUT, "foo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithLINE() {
        this.test(Properties.Criterion.LINE, "foo(Ljava/lang/Integer;)Z", new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithMETHOD() {
        this.test(Properties.Criterion.METHOD, "foo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithMETHODNOEXCEPTION() {
        this.test(Properties.Criterion.METHODNOEXCEPTION, "foo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithMETHODTRACE() {
        this.test(Properties.Criterion.METHODTRACE, "foo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithMUTATION() {
        this.test(Properties.Criterion.MUTATION, "foo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithONLYBRANCH() {
        this.test(Properties.Criterion.ONLYBRANCH, "foo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithONLYLINE() {
        this.test(Properties.Criterion.ONLYLINE, "foo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithONLYMUTATION() {
        this.test(Properties.Criterion.ONLYMUTATION, "foo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithOUTPUT() {
        this.test(Properties.Criterion.OUTPUT, "foo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithRHO() {
        this.test(Properties.Criterion.RHO, "foo(Ljava/lang/Integer;)Z", new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithSTATEMENT() {
        this.test(Properties.Criterion.STATEMENT, "foo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithSTRONGMUTATION() {
        this.test(Properties.Criterion.STRONGMUTATION, "foo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }

    @Test
    public void testTargetMethodWithWEAKMUTATION() {
        this.test(Properties.Criterion.WEAKMUTATION, "foo(Ljava/lang/Integer;)Z",
                new String[]{"bar", "getY"});
    }
}
