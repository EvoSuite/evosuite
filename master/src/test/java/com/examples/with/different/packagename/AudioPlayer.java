package com.examples.with.different.packagename;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;

public class AudioPlayer {

    private final AudioInputStream audioInputStream;

    public AudioPlayer(AudioInputStream audioInputStream) {
        this.audioInputStream = audioInputStream;
    }

    public AudioInputStream getAudioInputStream(){return this.audioInputStream;}

    public void play(AudioInputStream sound, Clip clip) {
        try {
            // load the sound into memory (a Clip)
            clip.open(sound);

            System.out.println("Playing audio...");
            clip.start();

            // Wait for the audio to finish playing
            clip.drain();
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}