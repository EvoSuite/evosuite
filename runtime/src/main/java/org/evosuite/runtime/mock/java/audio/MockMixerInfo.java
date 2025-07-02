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
package org.evosuite.runtime.mock.java.audio;

import org.evosuite.runtime.mock.OverrideMock;

import javax.sound.sampled.Mixer;

/**
 * Mock implementation of Java Mixer's Info for testing purposes.
 *
 * @author Pedro-PFerreira
 */
public class MockMixerInfo extends Mixer.Info implements OverrideMock {

    /**
     * Default constructor: Initializes the mixer info with default values.
     */
    public MockMixerInfo() {
        this("Mock Mixer", "EvoSuite", "Mock Mixer", "1.0");
    }

    /**
     * Constructor: Initializes the mixer info with specific values.
     * @param name the name of the mixer
     * @param vendor the vendor of the mixer
     * @param description the description of the mixer
     * @param version the version of the mixer
     */
    public MockMixerInfo(String name, String vendor, String description, String version) {
        super(name, vendor, description, version);
    }
}
