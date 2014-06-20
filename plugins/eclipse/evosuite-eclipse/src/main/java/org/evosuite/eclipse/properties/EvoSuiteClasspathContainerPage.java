/**
 * 
 */
package org.evosuite.eclipse.properties;

import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author Gordon Fraser
 * 
 */
public class EvoSuiteClasspathContainerPage extends org.eclipse.jface.wizard.WizardPage
        implements IClasspathContainerPage {

	private Composite container;

	/**
	 * @param pageName
	 */
	public EvoSuiteClasspathContainerPage() {
		super("EvoSuite classpath entry");
		setTitle("EvoSuite runtime library");
		setDescription("Add EvoSuite runtime library to project classpath");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		Label label1 = new Label(container, SWT.NULL);
		label1.setText("EvoSuite jar found at: ");

		Label label2 = new Label(container, SWT.NULL);
		label2.setText("" + getPath());

		// Required to avoid an error in the system
		setControl(container);
		setPageComplete(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPage#finish()
	 */
	@Override
	public boolean finish() {
		// TODO Auto-generated method stub
		return true;
	}

	@SuppressWarnings("deprecation")
	public IPath getPath() {
		URL url = org.eclipse.core.runtime.Platform.getPlugin("org.evosuite.eclipse.core").getBundle().getEntry("evosuite.jar");
		try {
			URL evosuiteLib = org.eclipse.core.runtime.Platform.resolve(url);
			System.out.println("Evosuite jar is at " + evosuiteLib.getPath());
			return new Path(evosuiteLib.getPath());
		} catch (Exception e) {
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPage#getSelection()
	 */
	@Override
	public IClasspathEntry getSelection() {
		return JavaCore.newLibraryEntry(getPath(), null, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPage#setSelection(org.eclipse.jdt.core.IClasspathEntry)
	 */
	@Override
	public void setSelection(IClasspathEntry containerEntry) {
		// TODO Auto-generated method stub

	}

}
