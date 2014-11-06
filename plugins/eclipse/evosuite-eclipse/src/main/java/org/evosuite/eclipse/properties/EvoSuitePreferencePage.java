/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.eclipse.properties;

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
	
	static {
		PREFERENCE_STORE = new PreferenceStore("evosuite-quickfixes-properties");	
	}
	
	public EvoSuitePreferencePage(){
		super("CATS Generation Properties", FieldEditorPreferencePage.GRID);
		setPreferenceStore(PREFERENCE_STORE);
	}

	@Override
	public PreferenceStore getPreferenceStore() {
		// TODO Auto-generated method stub
		return PREFERENCE_STORE;
	}

	@Override
	protected void createFieldEditors() {
		
		BooleanFieldEditor enabled = new BooleanFieldEditor("enabled", "Enable Markers and Quick-fixes", getFieldEditorParent());
		addField(enabled);
		
		IntegerFieldEditor runtime = new IntegerFieldEditor("runtime", "Time for EvoSuite to improve code coverage (s)", getFieldEditorParent());
		addField(runtime);
		
		IntegerFieldEditor roamtime = new IntegerFieldEditor("roamtime", "Inactive time before other classes will be tested (s)", getFieldEditorParent());
		addField(roamtime);
		
		BooleanFieldEditor uncoveredLines = new BooleanFieldEditor("uncovered", "Show lines EvoSuite couldn't cover", getFieldEditorParent());
		addField(uncoveredLines);
		
		BooleanFieldEditor removedLines = new BooleanFieldEditor("removed", "Show lines the compiler may have removed", getFieldEditorParent());
		addField(removedLines);
		
		BooleanFieldEditor auto = new BooleanFieldEditor("automatic", "Automatic test on save", getFieldEditorParent());
		addField(auto);
	}
	
	

	@Override
	protected void performDefaults() {
		getPreferenceStore().setToDefault("enabled");
		getPreferenceStore().setToDefault("runtime");
		getPreferenceStore().setToDefault("roamtime");
		getPreferenceStore().setToDefault("uncovered");
		getPreferenceStore().setToDefault("removed");
		
		super.performDefaults();
	}

	@Override
	public void init(IWorkbench arg0) {
		try {
			PreferenceStore prefStore = getPreferenceStore();
			prefStore.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		getPreferenceStore().setDefault("enabled", true);
		getPreferenceStore().setDefault("runtime", 30);
		getPreferenceStore().setDefault("roamtime", 240);
		getPreferenceStore().setDefault("uncovered", false);
		getPreferenceStore().setDefault("removed", false);
		getPreferenceStore().setDefault("automatic", true);
		
		storeDefaults();
		//getPreferenceStore().
	}
	
	public void storeDefaults(){
		if (!getPreferenceStore().contains("enabled")){
			getPreferenceStore().setValue("enabled", true);
		}
		
		if (!getPreferenceStore().contains("runtime")){
			getPreferenceStore().setValue("runtime", 30);
		}
		
		if (!getPreferenceStore().contains("roamtime")){
			getPreferenceStore().setValue("roamtime", 240);
		}
		
		if (!getPreferenceStore().contains("uncovered")){
			getPreferenceStore().setValue("uncovered", false);
		}
		
		if (!getPreferenceStore().contains("removed")){
			getPreferenceStore().setValue("removed", false);
		}
		
		if (!getPreferenceStore().contains("automatic")){
			getPreferenceStore().setValue("automatic", true);
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
