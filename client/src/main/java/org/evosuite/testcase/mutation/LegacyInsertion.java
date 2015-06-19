package org.evosuite.testcase.mutation;

import org.evosuite.Properties;
import org.evosuite.setup.TestCluster;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyInsertion implements InsertionStrategy {

	private static final Logger logger = LoggerFactory.getLogger(LegacyInsertion.class);

	@Override
	public int insertStatement(TestCase test, int lastPosition) {
		//final double P = Properties.INSERTION_SCORE_UUT
		//        + Properties.INSERTION_SCORE_OBJECT
		//        + Properties.INSERTION_SCORE_PARAMETER;
		// final double P_UUT = Properties.INSERTION_SCORE_UUT / P;
		// final double P_OBJECT = P_UUT + Properties.INSERTION_SCORE_OBJECT / P;

		int oldSize = test.size();
		double r = Randomness.nextDouble();
		//		int position = Randomness.nextInt(test.size() + 1);
		int max = lastPosition;
		if (max == test.size())
			max += 1;

		if (max <= 0)
			max = 1;

		int position = Randomness.nextInt(max);

		if (logger.isDebugEnabled()) {
			//for (int i = 0; i < test.size(); i++) {
			//	logger.debug(test.getStatement(i).getCode() + ": Distance = "
			//	        + test.getStatement(i).getReturnValue().getDistance());
			//}
			logger.debug(test.toCode());
		}

		//		if (r <= P_UUT) {
		boolean success = false;
		if (r <= Properties.INSERTION_UUT && TestCluster.getInstance().getNumTestCalls() > 0) {
			// add new call of the UUT - only declared in UUT!
			logger.debug("Adding new call on UUT");
			success = TestFactory.getInstance().insertRandomCall(test, position);
			if (test.size() - oldSize > 1) {
				position += (test.size() - oldSize - 1);
			}
		} else { // if (r <= P_OBJECT) {
			logger.debug("Adding new call on existing object");
			success = TestFactory.getInstance().insertRandomCallOnObject(test, position);
			if (test.size() - oldSize > 1) {
				position += (test.size() - oldSize - 1);
			}
			//		} else {
			//			logger.debug("Adding new call with existing object as parameter");
			// insertRandomCallWithObject(test, position);
		}
		if (success)
			return position;
		else
			return -1;
	}

}
