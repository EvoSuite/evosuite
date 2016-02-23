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
package org.evosuite.eclipse.properties;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class EvoSuitePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final PreferenceStore PREFERENCE_STORE;
	
	public static final String MARKERS_ENABLED = "markersEnabled";
	public static final String RUNTIME = "runtime";
	public static final String ROAMTIME = "roamtime";
	public static final String UNCOVERED_MARKER = "uncovered";
	public static final String REMOVED_MARKER = "removed";
	public static final String AUTOMATIC_TEST_ON_SAVE = "automatic";
	public static final String ORGANIZE_IMPORTS = "organizeImports";
	public static final String TEST_COMMENTS = "testComments";

	static {
		PREFERENCE_STORE = new PreferenceStore("evosuite-properties");	
	}
	
	public EvoSuitePreferencePage(){
		super("CATS Generation Properties", FieldEditorPreferencePage.GRID);
		setPreferenceStore(PREFERENCE_STORE);
	}

	@Override
	public PreferenceStore getPreferenceStore() {
		return PREFERENCE_STORE;
	}

	@Override
	protected void createFieldEditors() {
		
		BooleanFieldEditor markersEnabled = new BooleanFieldEditor(MARKERS_ENABLED, "Enable Markers and Quick-fixes", getFieldEditorParent());
		addField(markersEnabled);
		
		IntegerFieldEditor runtime = new IntegerFieldEditor(RUNTIME, "Time for EvoSuite to improve code coverage (s)", getFieldEditorParent());
		runtime.setEmptyStringAllowed(false);
		runtime.setValidRange(0, Integer.MAX_VALUE);
		addField(runtime);
		
		IntegerFieldEditor roamtime = new IntegerFieldEditor(ROAMTIME, "Inactive time before other classes will be tested (s)", getFieldEditorParent());
		addField(roamtime);
		
		BooleanFieldEditor uncoveredLines = new BooleanFieldEditor(UNCOVERED_MARKER, "Show lines EvoSuite couldn't cover", getFieldEditorParent());
		addField(uncoveredLines);
		
		BooleanFieldEditor removedLines = new BooleanFieldEditor(REMOVED_MARKER, "Show lines the compiler may have removed", getFieldEditorParent());
		addField(removedLines);
		
		BooleanFieldEditor auto = new BooleanFieldEditor(AUTOMATIC_TEST_ON_SAVE, "Automatic test on save", getFieldEditorParent());
		addField(auto);
		
		BooleanFieldEditor cleanupImports = new BooleanFieldEditor(ORGANIZE_IMPORTS, "Organize imports", getFieldEditorParent());
		addField(cleanupImports);
		
		BooleanFieldEditor printComments = new BooleanFieldEditor(TEST_COMMENTS, "Print test comments", getFieldEditorParent());
		addField(printComments);

	}
	
	

	@Override
	protected void performDefaults() {
		getPreferenceStore().setToDefault(MARKERS_ENABLED);
		getPreferenceStore().setToDefault(RUNTIME);
		getPreferenceStore().setToDefault(ROAMTIME);
		getPreferenceStore().setToDefault(UNCOVERED_MARKER);
		getPreferenceStore().setToDefault(REMOVED_MARKER);
		getPreferenceStore().setToDefault(AUTOMATIC_TEST_ON_SAVE);
		getPreferenceStore().setToDefault(ORGANIZE_IMPORTS);
		getPreferenceStore().setToDefault(TEST_COMMENTS);

		super.performDefaults();
	}

	@Override
	public void init(IWorkbench arg0) {
		try {
			PreferenceStore prefStore = getPreferenceStore();
			prefStore.load();
		} catch (FileNotFoundException e){
			// ignore if there is no file
		} catch (IOException e) {
			e.printStackTrace();
		}
		getPreferenceStore().setDefault(MARKERS_ENABLED, false);
		getPreferenceStore().setDefault(RUNTIME, 30);
		getPreferenceStore().setDefault(ROAMTIME, 240);
		getPreferenceStore().setDefault(UNCOVERED_MARKER, false);
		getPreferenceStore().setDefault(REMOVED_MARKER, false);
		getPreferenceStore().setDefault(AUTOMATIC_TEST_ON_SAVE, false);
		getPreferenceStore().setDefault(ORGANIZE_IMPORTS, true);
		getPreferenceStore().setDefault(TEST_COMMENTS, false);
		
		storeDefaults();
		//getPreferenceStore().
	}
	
	public void storeDefaults(){
		if (!getPreferenceStore().contains(MARKERS_ENABLED)){
			getPreferenceStore().setValue(MARKERS_ENABLED, false);
		}
		
		if (!getPreferenceStore().contains(RUNTIME)){
			getPreferenceStore().setValue(RUNTIME, 30);
		}
		
		if (!getPreferenceStore().contains(ROAMTIME)){
			getPreferenceStore().setValue(ROAMTIME, 240);
		}
		
		if (!getPreferenceStore().contains(UNCOVERED_MARKER)){
			getPreferenceStore().setValue(UNCOVERED_MARKER, false);
		}
		
		if (!getPreferenceStore().contains(REMOVED_MARKER)){
			getPreferenceStore().setValue(REMOVED_MARKER, false);
		}
		
		if (!getPreferenceStore().contains(AUTOMATIC_TEST_ON_SAVE)){
			getPreferenceStore().setValue(AUTOMATIC_TEST_ON_SAVE, false);
		}
		
		if (!getPreferenceStore().contains(ORGANIZE_IMPORTS)){
			getPreferenceStore().setValue(ORGANIZE_IMPORTS, true);
		}

		if (!getPreferenceStore().contains(TEST_COMMENTS)){
			getPreferenceStore().setValue(TEST_COMMENTS, false);
		}
		
		try {
			getPreferenceStore().save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		try {
			getPreferenceStore().save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
