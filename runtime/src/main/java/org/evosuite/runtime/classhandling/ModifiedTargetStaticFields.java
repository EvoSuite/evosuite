package org.evosuite.runtime.classhandling;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the singleton containing those static fields whose
 * <code>final</code> modifier was removed during our instrumentation
 * 
 * @author galeotti
 *
 */
public class ModifiedTargetStaticFields {

	private static Logger logger = LoggerFactory.getLogger(ModifiedTargetStaticFields.class);

	/**
	 * gets the current set of modified target static fields
	 * 
	 * @return
	 */
	public static ModifiedTargetStaticFields getInstance() {
		if (instance == null) {
			instance = new ModifiedTargetStaticFields();
		}
		return instance;
	}

	/**
	 * Resets the singleton.
	 */
	public static void resetSingleton() {
		instance = null;
	}

	private static ModifiedTargetStaticFields instance;

	private ModifiedTargetStaticFields() {

	}

	private final ArrayList<String> finalFields = new ArrayList<>();

	/**
	 * Adds a collection of final fields whose final modifier was removed by our
	 * instrumentation
	 * 
	 * @param newFinalFields
	 */
	public void addFinalFields(Collection<String> newFinalFields) {
		for (String finalField : newFinalFields) {
			if (!finalFields.contains(finalField)) {
				logger.debug("Adding new field to ModifiedTargetStaticFields:" + newFinalFields);
				finalFields.add(finalField);
			}
		}
	}

	/**
	 * Checks if a given field is contained or not in this collection
	 * 
	 * @param name
	 * @return
	 */
	public boolean containsField(String name) {
		logger.debug("Checking if a static field was modified or not:" + name);
		return finalFields.contains(name);
	}

}
