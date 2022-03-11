/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.runtime.classhandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class represents the singleton containing those static fields whose
 * <code>final</code> modifier was removed during our instrumentation
 *
 * @author galeotti
 */
public class ModifiedTargetStaticFields {

    private static final Logger logger = LoggerFactory.getLogger(ModifiedTargetStaticFields.class);

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
                // logger.debug("Adding new field to ModifiedTargetStaticFields:" + newFinalFields);
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
        // logger.debug("Checking if a static field was modified or not:" + name);
        return finalFields.contains(name);
    }

}
