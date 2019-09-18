package org.evosuite.testcase.mutation;

import org.evosuite.Properties;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.setup.TestCluster;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.utils.Randomness;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * This class implements a "guided" insertion strategy. That is, it uses static and dynamic
 * information to make an informed decision about which statements are most likely to cover new
 * targets
 *
 * @author Sebastian Schweikl
 */
public abstract class GuidedInsertionOld extends AbstractInsertion {

    /**
     * Inserts statements in the given test after the given position. This method attempts
     * to perform an informed insertion, that is, it tries to insert statements which are most
     * likely to increase coverage. To this end, it only considers goals with a direct control
     * dependency to one of the goals already covered in the given test.
     *
     * @param test the test case in which to insert
     * @param lastPosition the position in the sequence after which to insert
     * @return the position of the last statement inserted by this method
     */
    @Override
    public int insertStatement(final TestCase test, final int lastPosition) {
//        final Set<FitnessFunction<?>> goals = new MultiCriteriaManager(Collections.emptyList()).getCurrentGoals();
        final TestFactory factory = TestFactory.getInstance();
        final TestCluster cluster = TestCluster.getInstance();

        // The goals already covered by the current test.
        final Set<TestFitnessFunction> coveredTargets = test.getCoveredGoals();

        // Uncovered goals with direct control dependencies to goals in the "coveredGoals" list.
        // TODO: need a function that takes coveredTargets and returns all targets with a direct
        //  control dependency to one of the targets in coveredTargets.
        // TODO: somehow need to get the set of currently targeted goals...
        //  gaolsManager in class DynaMOSA knows them...
        final Set<TestFitnessFunction> currentTargets = null;

        /* ****************************************************************************************
         * (1) Target Selection
         * ****************************************************************************************/

        // Randomly select a target to cover from the set of uncovered goals.
        final TestFitnessFunction target = Randomness.choice(currentTargets);

        // The method and class containing the target.
        final String targetMethodName = target.getTargetMethod();
        final String targetClassName = target.getTargetClass();
        final Type type = null;


        /* ****************************************************************************************
         * (2) Instantiation of the containing class.
         * ****************************************************************************************/


        boolean instantiate = false;

        if (instantiate) {
            // TODO: welche Factory muss ich aufrufen, damit ich eine neue Instanz erzeugen kann?
            try {
//                Folgendes STatement auskommentiert, weil es die Methode nicht mehr gibt
//                factory.createObject(test, Class.forName(targetClassName), lastPosition,
//                        Properties.MAX_RECURSION, null);
                factory.attemptGeneration(test, type, -1);
            } catch (ConstructionFailedException e) {
                e.printStackTrace();
            }

//            GenericClass.getClass(targetClassName);
//            cluster.getGenerators(
        } else {
//            var = test.getRandomObject(null);
        }

        /*
         * (3) Attempt to cover the target
         */

        throw new UnsupportedOperationException("not yet implemented");
//        return -1;
    }

    private boolean isNonStaticMethod(String methodName) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
