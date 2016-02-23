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
package org.evosuite.eclipse.quickfixes;

import java.io.File;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.evosuite.eclipse.popup.actions.TestGenerationAction;
import org.evosuite.eclipse.popup.actions.TestGenerationJob;


/**
 * @author Thomas White, extended from Gordon Fraser's
 *         org.evosuite.eclipse.popup.actions.TestGenerationAction.java
 * 
 */
public class TestGenerationTrigger extends TestGenerationAction {
	private IResource res;
	private TestGenerationJob job;
	
	public TestGenerationTrigger(IResource r) {
		res = r;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject proj = res.getProject();
		fixJUnitClassPath(JavaCore.create(proj));
		generateTests(res);
		return null;
	}

	/**
	 * Add a new test generation job to the job queue
	 * 
	 * @param target
	 */
	@Override
	protected void addTestJob(final IResource target) {
        //TODO: Must do a merging instead of over writing entire test suite
		IJavaElement element = JavaCore.create(target);
		if (element == null) {
			return;
		}
		IJavaElement packageElement = element.getParent();

		String packageName = packageElement.getElementName();

		final String targetClass = (!packageName.isEmpty() ? packageName + "."
				: "")
				+ target.getName().replace(".java", "")
						.replace(File.separator, ".");
		System.out.println("Building new job for " + targetClass);
		job = new TestGenerationJob(shell, target, targetClass);
		job.setPriority(Job.DECORATE);
		job.schedule();
	}

	public boolean isRunning() {
		boolean running = job != null && job.isRunning();
		return running;
	}

	public void stop() {
		if (job != null) {
			job.cancel();
		}
	}

}
