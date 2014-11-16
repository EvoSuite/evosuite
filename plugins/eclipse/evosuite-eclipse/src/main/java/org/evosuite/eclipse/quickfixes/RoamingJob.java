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
		String disabled = System.getProperty("disable.evosuite");
		if ( disabled != null && disabled.equals("1")) {
			System.out.println("RoamingJob: disabled.evosuite = 1");
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
