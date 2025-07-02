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

import org.evosuite.runtime.Randomness;
import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.StaticReplacementMock;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;

/**
 * Mock implementation of Java's AudioSystem for testing purposes.
 * This ensures audio System instance is generated, using the other mock instances.
 *
 * @author Pedro-PFerreira
 */
public class MockAudioSystem implements StaticReplacementMock {

    private static final MockClip mockClipInstance = new MockClip();

    static {
        String seed = System.getenv("SEED_FOR_MOCKS");

        if (seed != null) {
            Randomness.setSeed(Long.parseLong(seed));
        }
    }

    /**
     * Returns an array of Mixer.Info objects representing the available mixers.
     * If MockFramework is enabled, it generates mock mixers instead of querying the system.
     *
     * @return an array of Mixer.Info objects
     */
    public static Mixer.Info[] getMixerInfo() {

        if (!MockFramework.isEnabled()) {
            return AudioSystem.getMixerInfo();
        }

        int size = Randomness.nextInt();

        Mixer.Info[] mixerInfo = new Mixer.Info[size];

        for (int i = 0; i < size; i++) {
            mixerInfo[i] = new MockMixerInfo();
        }

        return mixerInfo;
    }

    /**
     * Returns a Mixer instance for the specified Mixer.Info.
     * If MockFramework is enabled, it returns a mock mixer.
     *
     * @param info the Mixer.Info object
     * @return a Mixer instance
     * @throws NullPointerException if info is null
     */
    public static Mixer getMixer(Mixer.Info info) {
        if (info == null) {
            throw new NullPointerException();
        }

        if (!MockFramework.isEnabled()) {
            return AudioSystem.getMixer(info);
        }

        return new MockMixer();
    }

    public static Line.Info[] getSourceLineInfo(Line.Info info) {

        if (info == null) {
            throw new NullPointerException();
        }

        if (!MockFramework.isEnabled()){
            return AudioSystem.getSourceLineInfo(info);
        }

        int size = Randomness.nextInt();

        Line.Info[] lines = new MockLineInfo[size];

        for (int i = 0; i < size; i++) {
            lines[i] = new MockLineInfo(MockMixer.class);
        }

        return lines;
    }

    public static Line.Info[] getTargetLineInfo(Line.Info info) {

        if (info == null) {
            throw new NullPointerException();
        }

        if (!MockFramework.isEnabled()){
            return AudioSystem.getSourceLineInfo(info);
        }

        int size = Randomness.nextInt();

        Line.Info[] lines = new MockLineInfo[size];

        for (int i = 0; i < size; i++) {
            lines[i] = new MockLineInfo(MockMixer.class);
        }

        return lines;
    }

    public static boolean isLineSupported(Line.Info info) {

        if (!MockFramework.isEnabled()){
            return AudioSystem.isLineSupported(info);
        }

        return new MockMixer().isLineSupported(info);
    }

    public static Line getLine(Line.Info info) throws LineUnavailableException {

        if (info == null) {
            throw new NullPointerException();
        }
        if (info.getLineClass().equals(SourceDataLine.class))
            return new MockSourceDataLine();
        else if (info.getLineClass().equals(TargetDataLine.class))
            return new MockTargetDataLine();
        else if (info.getLineClass().equals(Clip.class))
            return mockClipInstance;
        else if (info.getLineClass().equals(MockLine.class))
            return new MockDataLine();
        else
            throw new LineUnavailableException();
    }


    public static Clip getClip() {
        return mockClipInstance;
    }

    public Clip getClip(Mixer.Info info){
        if (info == null) {
            throw new NullPointerException();
        }
        return mockClipInstance;
    }

    public static SourceDataLine getSourceDataLine(AudioFormat format) {
        if (format == null) {
            throw new NullPointerException();
        }
        return new MockSourceDataLine();
    }

    public static AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {

        if (!MockFramework.isEnabled()){
            return AudioSystem.getAudioInputStream(file);
        }
        return new MockAudioInputStream();
    }

    public static AudioInputStream getAudioInputStream(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        if (!MockFramework.isEnabled())
            return AudioSystem.getAudioInputStream(inputStream);
        return new MockAudioInputStream();
    }

    public static AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        if (!MockFramework.isEnabled())
            return AudioSystem.getAudioInputStream(url);
        return new MockAudioInputStream();
    }

    public static AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream sourceStream) {

        if (!MockFramework.isEnabled())
            return AudioSystem.getAudioInputStream(targetFormat, sourceStream);

        return new MockAudioInputStream();
    }

    @Override
    public String getMockedClassName() {
        return "javax.sound.sampled.AudioSystem";
    }
}
