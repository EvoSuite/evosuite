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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.evosuite.eclipse.Activator;

public class RoamingJob extends Job {

	private IProject project = null;

	public RoamingJob(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		Boolean disabled = System.getProperty("evosuite.disable") != null; //  && System.getProperty("evosuite.disable").equals("1")
		if ( disabled ) {
			System.out.println("RoamingJob: The EvoSuite plugin is disabled :(");
			return Status.OK_STATUS;
		}
		int delay = Activator.getDefault().getPreferenceStore()
				.getInt("roamtime") * 1000;
		if (delay > 0 && Activator.FILE_QUEUE.getSize() == 0 && project != null) {
			try {
				project.accept(new IResourceVisitor() {

					@Override
					public boolean visit(IResource resource)
							throws CoreException {
						if (Activator.FILE_QUEUE.getSize() == 0
								&& resource.getType() == IResource.FILE
								&& resource.getName().toLowerCase().endsWith("java")
								&& !resource.getName().endsWith(Activator.JUNIT_IDENTIFIER)
								&& !resource.getName().endsWith(Activator.SCAFFOLDING_IDENTIFIER)) {
							String filepath = resource.getProjectRelativePath().toOSString();
							IFile file = project.getFolder(
									Activator.DATA_FOLDER).getFile(filepath + ".gadata");
							if (!file.exists()) {
								Activator.FILE_QUEUE.addFile(resource);
								Activator.FILE_QUEUE.update();
							}

						}
						return true;
					}

				});
			} catch (CoreException e) {
				e.printStackTrace();
			}
			this.schedule(delay);
		}
		return Status.OK_STATUS;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

}
