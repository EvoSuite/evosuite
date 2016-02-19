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
package org.evosuite.eclipse;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.evosuite.Properties;
import org.evosuite.eclipse.properties.EvoSuitePreferencePage;
import org.evosuite.eclipse.quickfixes.FileQueue;
import org.evosuite.eclipse.quickfixes.RoamingJob;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements
		IResourceChangeListener, IResourceDeltaVisitor, IStartup {

	// The plug-in ID
	public static final String PLUGIN_ID = "evosuite-eclipse"; //$NON-NLS-1$
	public static final String EVOSUITE_CORE_BUNDLE = "org.evosuite.plugins.eclipse.deps";
	public static final String EVOSUITE_JAR = "lib/evosuite.jar";
	public static final String JUNIT_IDENTIFIER = Properties.JUNIT_SUFFIX + ".java";
	public static final String SCAFFOLDING_IDENTIFIER = Properties.SCAFFOLDING_SUFFIX + ".java";
	public static final String DATA_FOLDER = "evosuite-qfdata";
	
	public static IResource CURRENT_WRITING_FILE = null;

	// set max running time to 30 seconds
	public static final int MAX_RUNNING_TIME = 30;

	// The shared instance
	private static Activator plugin;

	private RoamingJob roamingJob = null;

	public static final FileQueue FILE_QUEUE = new FileQueue();

	protected Shell shell;
	// private IResourceChangeEvent event;

	/**
	 * The constructor
	 */
	public Activator() {
		EvoSuitePreferencePage page = new EvoSuitePreferencePage();
		page.init(getWorkbench());
	}

	@Override
	public PreferenceStore getPreferenceStore() {
		return EvoSuitePreferencePage.PREFERENCE_STORE;
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		// IPath containerPath = new IPath(JavaCore.J);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		roamingJob = new RoamingJob("EvoSuite Roaming Job");
		// rj.schedule(DELAY_BEFORE_ROAM);
		plugin = this;

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent evnt) {
		// this.event = evnt;
		if (evnt.getType() == IResourceChangeEvent.POST_CHANGE) {
			try {
				evnt.getDelta().accept(this);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		boolean isAutomatic = getPreferenceStore().getBoolean(EvoSuitePreferencePage.AUTOMATIC_TEST_ON_SAVE);
		if (isAutomatic // && markersEnabled() 
				&& delta.getKind() == IResourceDelta.CHANGED 
				&& delta.getFlags() != IResourceDelta.MARKERS 
				&& delta.getResource() != null) {
			final IResource res = delta.getResource();
			
			if (res.getType() == IResource.FILE
					&& res.getName().toLowerCase().endsWith("java")
					&& !res.getName().endsWith(JUNIT_IDENTIFIER)
					&& !res.getName().endsWith(SCAFFOLDING_IDENTIFIER)) {
				System.out.println("Resetting Roaming Job for " + res.getName());
				FILE_QUEUE.addFile(res);
				FILE_QUEUE.update();
				resetRoamingJob(res);
			}
		}
		return true;
	}

	public static boolean markersEnabled(){
		return Activator.getDefault().getPreferenceStore().getBoolean(EvoSuitePreferencePage.MARKERS_ENABLED);
	}

	public static boolean organizeImports(){
		return Activator.getDefault().getPreferenceStore().getBoolean(EvoSuitePreferencePage.ORGANIZE_IMPORTS);
	}
	
	public void resetRoamingJob(IResource res) {
		roamingJob.cancel();
		int time = getPreferenceStore().getInt(EvoSuitePreferencePage.ROAMTIME) * 1000;
		if (time > 0) {
			roamingJob.schedule(time);
			roamingJob.setProject(res.getProject());
		}
	}

	@Override
	public void earlyStartup() {
		// TODO Auto-generated method stub
	}
}
