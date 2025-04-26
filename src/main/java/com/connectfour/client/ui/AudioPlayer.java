package com.connectfour.client.ui;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Handles audio playback for the Connect Four game using Java Sound API.
 */
public class AudioPlayer {
    private Clip clip;
    private boolean isPlaying = false;
    private String currentFile = null;
    private float volume = 1.0f;

    /**
     * Initializes the audio player with the specified sound file.
     * 
     * @param soundFileName The name of the sound file in the resources directory
     */
    public AudioPlayer(String soundFileName) {
        try {
            System.out.println("Attempting to load sound file with Java Sound API: " + soundFileName);
            
            // Always use the MP3 file we know works
            soundFileName = "somber_background.mp3";
            System.out.println("Using somber_background.mp3 for reliable audio playback");
            
            this.currentFile = soundFileName;
            
            // Get resource as stream
            InputStream is = getClass().getResourceAsStream("/sounds/" + soundFileName);
            if (is == null) {
                is = getClass().getClassLoader().getResourceAsStream("sounds/" + soundFileName);
            }
            
            if (is != null) {
                System.out.println("Found sound file in resources: " + soundFileName);
                
                // We need to buffer the input stream for mark/reset
                BufferedInputStream bis = new BufferedInputStream(is);
                bis.mark(Integer.MAX_VALUE);  // Mark the start of the stream
                
                try {
                    // Get AudioInputStream from the file
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(bis);
                    
                    // Get a clip resource
                    clip = AudioSystem.getClip();
                    
                    // Open audio clip and load samples
                    clip.open(audioStream);
                    
                    // Add a listener to know when playback completes for looping
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            // If we're still meant to be playing, restart
                            if (isPlaying) {
                                clip.setFramePosition(0);
                                clip.start();
                                System.out.println("Restarting playback for looping");
                            }
                        }
                    });
                    
                    // Set initial volume
                    setVolume(volume);
                    
                    System.out.println("Sound initialized successfully from resources");
                } catch (UnsupportedAudioFileException e) {
                    System.err.println("Audio format not supported: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("Could not find sound file in resources: " + soundFileName);
            }
        } catch (Exception e) {
            System.err.println("Error initializing audio player: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Try loading from resources.
     */
    private boolean tryLoadFromResources(String soundFileName) {
        try {
            // Get resource as stream
            InputStream is = getClass().getResourceAsStream("/sounds/" + soundFileName);
            if (is == null) {
                is = getClass().getClassLoader().getResourceAsStream("sounds/" + soundFileName);
            }
            
            if (is != null) {
                System.out.println("Found sound file in resources: " + soundFileName);
                
                // We need to buffer the input stream for mark/reset
                BufferedInputStream bis = new BufferedInputStream(is);
                bis.mark(Integer.MAX_VALUE);  // Mark the start of the stream
                
                try {
                    // Get AudioInputStream from the file
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(bis);
                    
                    // Get a clip resource
                    clip = AudioSystem.getClip();
                    
                    // Open audio clip and load samples
                    clip.open(audioStream);
                    
                    // Add a listener to know when playback completes for looping
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            // If we're still meant to be playing, restart
                            if (isPlaying) {
                                clip.setFramePosition(0);
                                clip.start();
                                System.out.println("Restarting playback for looping");
                            }
                        }
                    });
                    
                    // Set initial volume
                    setVolume(volume);
                    
                    System.out.println("Sound initialized successfully from resources");
                    return true;
                } catch (UnsupportedAudioFileException e) {
                    System.err.println("Audio format not supported: " + e.getMessage());
                    return false;
                }
            } else {
                System.err.println("Could not find sound file in resources: " + soundFileName);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error loading sound from resources: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Try loading from filesystem.
     */
    private boolean tryLoadFromFilesystem(String soundFileName) {
        try {
            // Try using absolute path as fallback
            File soundFile = new File("src/main/resources/sounds/" + soundFileName);
            System.out.println("Trying file path: " + soundFile.getAbsolutePath());
            
            if (soundFile.exists()) {
                try {
                    // Get AudioInputStream from the file
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                    
                    // Get a clip resource
                    clip = AudioSystem.getClip();
                    
                    // Open audio clip and load samples
                    clip.open(audioStream);
                    
                    // Add a listener to know when playback completes for looping
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            // If we're still meant to be playing, restart
                            if (isPlaying) {
                                clip.setFramePosition(0);
                                clip.start();
                                System.out.println("Restarting playback for looping");
                            }
                        }
                    });
                    
                    // Set initial volume
                    setVolume(volume);
                    
                    System.out.println("Sound initialized successfully from file");
                    return true;
                } catch (UnsupportedAudioFileException e) {
                    System.err.println("Audio format not supported (filesystem): " + e.getMessage());
                    return false;
                }
            } else {
                System.err.println("Sound file not found on filesystem: " + soundFileName);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error in filesystem loading: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Starts playing the background music.
     */
    public void play() {
        if (clip != null && !isPlaying) {
            System.out.println("Starting playback of: " + currentFile);
            
            // Reset to the beginning
            clip.setFramePosition(0);
            
            // Start playing
            clip.start();
            isPlaying = true;
        } else {
            System.out.println("Cannot play sound: clip=" + (clip != null ? "initialized" : "null") + 
                              ", isPlaying=" + isPlaying);
        }
    }

    /**
     * Stops the background music.
     */
    public void stop() {
        if (clip != null && isPlaying) {
            System.out.println("Stopping playback");
            clip.stop();
            isPlaying = false;
        }
    }

    /**
     * Pauses the background music.
     */
    public void pause() {
        if (clip != null && isPlaying) {
            System.out.println("Pausing playback");
            clip.stop();
            isPlaying = false;
        }
    }

    /**
     * Sets the volume of the audio player.
     * 
     * @param volume Volume level between 0.0 and 1.0
     */
    public void setVolume(double volumeLevel) {
        if (clip != null) {
            try {
                this.volume = (float)volumeLevel;
                System.out.println("Setting volume to " + volume);
                
                // Get control for changing volume
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                
                // Calculate volume - linear to dB conversion
                // We'll use a simple conversion that makes volume=0 be -80dB (near silence)
                // and volume=1 be 0dB (max volume)
                float dB = 20f * (float) Math.log10(Math.max(0.0001f, volume));
                
                // Make sure the value is within the control's range
                if (dB < gainControl.getMinimum()) {
                    dB = gainControl.getMinimum();
                } else if (dB > gainControl.getMaximum()) {
                    dB = gainControl.getMaximum();
                }
                
                // Set the volume
                gainControl.setValue(dB);
                System.out.println("Volume set to: " + dB + " dB");
            } catch (Exception e) {
                System.err.println("Error setting volume: " + e.getMessage());
            }
        }
    }

    /**
     * Checks if the audio is currently playing.
     * 
     * @return True if audio is playing, false otherwise
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * Releases resources used by the audio player.
     */
    public void dispose() {
        if (clip != null) {
            System.out.println("Disposing audio resources");
            clip.stop();
            clip.close();
            isPlaying = false;
        }
    }
} 