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

import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.OverrideMock;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;

import static org.evosuite.runtime.mock.java.audio.MockAudioUtils.*;

/**
 * Mock implementation of Java's AudioInputStream for testing purposes.
 * This ensures audio data is generated in memory without disk access.
 *
 * <p>All objects are created in memory, and no access to disk is ever done.</p>
 *
 * @author Pedro-PFerreira
 */
public class MockAudioInputStream extends AudioInputStream implements OverrideMock {

    private final float sampleRate;
    private final int channels;
    private byte[] audioData;
    private int position = 0;
    private int markPosition = 0;

    /**
     * Default constructor: Generates random audio properties and mock audio data.
     */
    public MockAudioInputStream() {
        super(generateInputStream(generateSampleRate(), generateChannels()),
                new MockAudioFormat(),
                0);

        this.sampleRate = generateSampleRate();
        this.channels = generateChannels();
        this.audioData = generateRandomContent(this.sampleRate, this.channels);
    }

    /**
     * Constructor using a TargetDataLine.
     */
    public MockAudioInputStream(TargetDataLine line) {
        super(line);
        this.sampleRate = line.getFormat().getSampleRate();
        this.channels = line.getFormat().getChannels();
        this.frameSize = line.getFormat().getFrameSize();
    }

    @Override
    public long getFrameLength() {
        return audioData.length / frameSize;
    }


    @Override
    public AudioFormat getFormat(){
        return new MockAudioFormat();
    }

    @Override
    public int read() throws IOException {
        if (position >= audioData.length) return -1;
        return audioData[position++] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {

        if (!MockFramework.isEnabled()){
            return super.read(b, off, len);
        }

        if (position >= audioData.length) return -1;
        int bytesToRead = Math.min(len, audioData.length - position);
        System.arraycopy(audioData, position, b, off, bytesToRead);
        position += bytesToRead;
        return bytesToRead;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public long skip(long n) throws IOException {
        if (position >= audioData.length) return 0;

        long bytesToSkip = Math.min(n, audioData.length - position);
        position += (int) bytesToSkip;

        return bytesToSkip;
    }

    @Override
    public int available() throws IOException {
        return audioData.length - position;
    }

    @Override
    public void close() throws IOException {

        if (!MockFramework.isEnabled()) {
            super.close();
            return;
        }
        audioData = null;
        position = 0;
        markPosition = 0;
    }

    @Override
    public void mark(int readLimit) {

        if (!MockFramework.isEnabled()) {
            super.mark(readLimit);
            return;
        }
        if (readLimit < 0) {
            throw new IllegalArgumentException("Read limit must be non-negative");
        }
        markPosition = position;
    }

    @Override
    public void reset() throws IOException {
        if (!MockFramework.isEnabled()) {
            super.reset();
            return;
        }
        position = markPosition;
    }

    @Override
    public boolean markSupported() {
        return super.markSupported();
    }

}
