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

import javax.sound.sampled.AudioFormat;

import static org.evosuite.runtime.mock.java.audio.MockAudioUtils.generateChannels;
import static org.evosuite.runtime.mock.java.audio.MockAudioUtils.generateSampleRate;


/**
 * Mock implementation of Java's AudioFormat for testing purposes.
 * This ensures audio format is generated with more realistic values.
 *
 * <p>All objects are created in memory, and no access to disk is ever done.</p>
 *
 * @author Pedro-PFerreira
 */
public class MockAudioFormat extends AudioFormat implements OverrideMock {

    /**
     * Constructor: Initializes the audio format with specific parameters.
     *
     * @param encoding        the encoding of the audio data
     * @param sampleRate      the sample rate of the audio data
     * @param sampleSizeInBits the size of each sample in bits
     * @param channels        the number of channels in the audio data
     * @param frameSize       the size of each frame in bytes
     * @param frameRate       the frame rate of the audio data
     * @param bigEndian       whether the audio data is in big-endian format
     */
    public MockAudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian) {
        super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
    }

    /**
     * Default constructor: Generates random audio properties.
     */
    public MockAudioFormat(){
        super(generateSampleRate(), 16, generateChannels(), true, false);
    }
}
