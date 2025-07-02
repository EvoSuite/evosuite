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
import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.mock.StaticReplacementMock;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of Java's Line for testing purposes.
 * This ensures audio line is generated with random data.
 *
 * @author Pedro-PFerreira
 */
public class MockDataLine implements DataLine, StaticReplacementMock {

    private boolean isOpen;
    private boolean isRunning;
    private boolean isActive;
    private byte[] dataBuffer;
    private final List<LineListener> lineListeners = new ArrayList<>();

    /**
     * Default constructor for MockDataLine.
     * Initializes the line as closed, not running, and active.
     * Generates a random data buffer for audio content.
     */
    public MockDataLine(){

        this.isOpen = false;
        this.isRunning = false;
        this.isActive = true;
        this.dataBuffer = MockAudioUtils.generateRandomContent(MockAudioUtils.generateSampleRate(), Randomness.nextInt(1, 2));
    }

    static{
        String seed = System.getenv("SEED_FOR_MOCKS");

        if (seed != null) {
            Randomness.setSeed(Long.parseLong(seed));
        }
    }

    @Override
    public Info getLineInfo() {
        return new MockInfo(MockDataLine.class, new MockAudioFormat());
    }

    @Override
    public void open() throws LineUnavailableException {

        if (isOpen) {
            throw new LineUnavailableException("Line is already open");
        }

        isOpen = true;
    }

    @Override
    public void close() {

        isOpen = false;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public Control[] getControls() {

        int size = Randomness.nextInt(1, Integer.MAX_VALUE);

        Control[] controls = new MockControl[size];

        for (int i = 0; i < size; i++) {
            controls[i] = new MockControl();
        }

        return controls;
    }

    @Override
    public boolean isControlSupported(Control.Type type) {

        if (type == null) {
            throw new NullPointerException();
        }

        return true;
    }

    @Override
    public Control getControl(Control.Type type) {

        if (type == null) {
            throw new NullPointerException();
        }

        if (isControlSupported(type)) {
            return new MockControl();
        }

        return null;
    }

    @Override
    public void addLineListener(LineListener lineListener) {

        if (lineListener == null) {
            throw new NullPointerException();
        }

        lineListeners.add(lineListener);
    }

    @Override
    public void removeLineListener(LineListener lineListener) {

        if (lineListener == null) {
            throw new NullPointerException();
        }

        lineListeners.remove(lineListener);
    }

    @Override
    public void drain() {
        if (this.isOpen() && this.isActive) {
            if (!this.isRunning) {
                throw new IllegalStateException("Line is not running");
            } else {
                this.dataBuffer = null;
            }
        }
    }

    @Override
    public void flush() {
        if (this.isOpen() && this.isActive) {
            if (!this.isRunning) {
                throw new IllegalStateException("Line is not running");
            } else {
                this.dataBuffer = null;
            }
        }
    }

    @Override
    public void start() {
        if (this.isOpen() && this.isActive) {
            this.isRunning = true;
            this.dataBuffer = new byte[0];

            for(LineListener lineListener : this.lineListeners) {
                lineListener.update(new LineEvent(this, LineEvent.Type.START, 0L));
            }
        }
    }

    @Override
    public void stop() {
        if (this.isOpen() && this.isActive) {
            if (!this.isRunning) {
                throw new IllegalStateException("Line is not running");
            } else {
                this.isRunning = false;
            }
        }
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public AudioFormat getFormat() {
        return new MockAudioFormat();
    }

    @Override
    public int getBufferSize() {
        return this.dataBuffer.length;
    }

    @Override
    public int available() {
        return 0;
    }

    @Override
    public int getFramePosition() {
        return 0;
    }

    @Override
    public long getLongFramePosition() {
        return 0L;
    }

    @Override
    public long getMicrosecondPosition() {
        return 0L;
    }

    @Override
    public float getLevel() {
        return 0L;
    }

    @Override
    public String getMockedClassName() {
        return DataLine.class.getName();
    }

    public static class MockInfo extends DataLine.Info implements OverrideMock {

        public MockInfo(Class<?> lineClass, AudioFormat[] formats, int minBufferSize, int maxBufferSize) {
            super(lineClass, formats, minBufferSize, maxBufferSize);
        }

        public MockInfo(Class<?> lineClass, AudioFormat format, int bufferSize) {
            super(lineClass, format, bufferSize);
        }

        public MockInfo(Class<? extends Line> lineClass, AudioFormat audioFormat) {
            super(lineClass, audioFormat);
        }

        @Override
        public boolean isFormatSupported(AudioFormat format) {
            return format instanceof MockAudioFormat;
        }

        @Override
        public AudioFormat[] getFormats(){
            return new AudioFormat[] { new MockAudioFormat() };
        }

        @Override
        public boolean matches(Line.Info info) {
            return info instanceof MockInfo;
        }

        @Override
        public String toString() {
            return "MockInfo for " + getLineClass().getName();
        }
    }
}
