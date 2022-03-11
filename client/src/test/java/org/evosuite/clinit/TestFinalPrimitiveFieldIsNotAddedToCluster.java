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
package org.evosuite.clinit;

import com.examples.with.different.packagename.clinit.FinalPrimitiveField;
import org.evosuite.Properties;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.InheritanceTree;
import org.evosuite.setup.TestCluster;
import org.evosuite.setup.TestClusterGenerator;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestFinalPrimitiveFieldIsNotAddedToCluster {

    /**
     * As RESET_STATIC_FINAL_FIELDS=true removes the <code>final</code> modifier
     * of static fields in the target class, the purpose of this test case is to
     * check that the TestClusterGenerator indeed does not include these fields.
     *
     * @throws ClassNotFoundException
     * @throws RuntimeException
     */
    @Test
    public void test() throws ClassNotFoundException, RuntimeException {
        Properties.TARGET_CLASS = FinalPrimitiveField.class.getCanonicalName();
        Properties.RESET_STATIC_FINAL_FIELDS = true;

        ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
        String cp = ClassPathHandler.getInstance().getTargetProjectClasspath();
        DependencyAnalysis.analyzeClass(Properties.TARGET_CLASS, Arrays.asList(cp.split(File.pathSeparator)));
        InheritanceTree tree = DependencyAnalysis.getInheritanceTree();
        TestClusterGenerator gen = new TestClusterGenerator(tree);
        assertNotNull(gen);
        TestCluster cluster = TestCluster.getInstance();
        List<GenericAccessibleObject<?>> testCalls = cluster.getTestCalls();
        assertEquals("Unexpected number of TestCalls", 2, testCalls.size());
    }

}
