/**
 * 
 */
package de.unisb.cs.st.evosuite.ma.gui;

import javax.swing.JFrame;

import de.unisb.cs.st.evosuite.ma.Editor;

/**
 * @author Yury Pavlov
 * 
 */
public interface TestEditorGUI {

	public void createMainWindow(final Editor editor);

	public JFrame getMainFrame();
	
}