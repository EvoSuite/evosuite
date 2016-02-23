/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.utils;

import java.lang.reflect.Method;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.TimeController;
import org.evosuite.coverage.mutation.MutationObserver;
import org.evosuite.runtime.LoopCounter;
import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.classhandling.ClassResetter;
import org.evosuite.runtime.classhandling.ResetManager;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.runtime.util.AtMostOnceLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResetExecutor {

	private final static Logger logger = LoggerFactory.getLogger(ResetExecutor.class);

	private static final ResetExecutor instance = new ResetExecutor();

	private ResetExecutor() {
	}
	

	public synchronized static ResetExecutor getInstance() {
		return instance;
	}

	public void resetAllClasses() {
		List<String> classesToReset = ResetManager.getInstance().getClassResetOrder();
		resetClasses(classesToReset);
	}

	public void resetClasses(List<String> classesToReset) {
		ClassLoader loader = TestGenerationContext.getInstance().getClassLoaderForSUT();
		resetClasses(classesToReset, loader);
	}
	
	public void resetClasses(List<String> classesToReset, ClassLoader loader) {
		//try to reset each collected class

		ClassResetter.getInstance().setClassLoader(loader);

		long start = System.currentTimeMillis();

		for (String className : classesToReset) {
			//this can be expensive

			long elapsed = System.currentTimeMillis() - start;

                        /*
                        TODO: Cancelling reset leaves the system in an inconsistent state
                        
			if(!className.equals(Properties.TARGET_CLASS) && (! TimeController.getInstance().isThereStillTimeInThisPhase() || elapsed > Properties.TIMEOUT_RESET)){
				AtMostOnceLogger.warn(logger, "Stopped resetting of classes due to timeout");
				//continue; //do not "break", as need to be sure we still reset TARGET_CLASS
			}
			*/
			resetClass(className);
		}
	}

	private void resetClass(String className) {


		//className.__STATIC_RESET() exists
		logger.debug("Resetting class " + className);
		
		int mutationActive = MutationObserver.activeMutation;
		MutationObserver.deactivateMutation();

		//execute __STATIC_RESET()
		Sandbox.goingToExecuteSUTCode();
        TestGenerationContext.getInstance().goingToExecuteSUTCode();

		Runtime.getInstance().resetRuntime(); //it is important to initialize the VFS
		boolean wasLoopCheckOn = LoopCounter.getInstance().isActivated();

		try {
			Method resetMethod = ClassResetter.getInstance().getResetMethod(className);
			if (resetMethod!=null) {
				LoopCounter.getInstance().setActive(false);
				resetMethod.invoke(null, (Object[]) null);
			}
		} catch (Throwable  e) {
			ClassResetter.getInstance().logWarn(className, e.getClass() + " thrown during execution of method  __STATIC_RESET() for class " + className + ", " + e.getCause());
		}  finally {
			Sandbox.doneWithExecutingSUTCode();
            TestGenerationContext.getInstance().doneWithExecutingSUTCode();
			MutationObserver.activateMutation(mutationActive);
			LoopCounter.getInstance().setActive(wasLoopCheckOn);
		}
	}
}
