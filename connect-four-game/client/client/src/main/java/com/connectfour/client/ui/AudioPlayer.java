package com.connectfour.client.ui;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Handles audio playback for the Connect Four game using Java Sound API.
 */
public class AudioPlayer {
    private Clip clip;
    private MediaPlayer mediaPlayer;
    private EmergencyTonePlayer emergencyPlayer;
    private boolean isPlaying = false;
    private String currentFile = null;
    private float volume = 1.0f;

    static {
        // Debug information about audio system
        printAudioSystemInfo();
    }
    
    /**
     * Prints information about the audio system to help with debugging.
     */
    private static void printAudioSystemInfo() {
        try {
            System.out.println("---- Audio System Information ----");
            
            // Print mixer info
            Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
            System.out.println("Available audio mixers: " + mixerInfos.length);
            
            for (int i = 0; i < mixerInfos.length; i++) {
                Mixer.Info info = mixerInfos[i];
                System.out.println((i + 1) + ". " + info.getName() + " - " + info.getDescription());
                
                try {
                    Mixer mixer = AudioSystem.getMixer(info);
                    Line.Info[] sourceLines = mixer.getSourceLineInfo();
                    Line.Info[] targetLines = mixer.getTargetLineInfo();
                    
                    System.out.println("   Source lines: " + sourceLines.length);
                    System.out.println("   Target lines: " + targetLines.length);
                } catch (Exception e) {
                    System.out.println("   Error getting mixer details: " + e.getMessage());
                }
            }
            
            // JavaFX Media availability
            try {
                Class<?> mediaClass = Class.forName("javafx.scene.media.Media");
                System.out.println("JavaFX Media class is available");
            } catch (ClassNotFoundException e) {
                System.out.println("JavaFX Media class is NOT available: " + e.getMessage());
            }
            
            System.out.println("--------------------------------");
        } catch (Exception e) {
            System.err.println("Error checking audio system: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initializes the audio player with the specified sound file.
     * 
     * @param soundFileName The name of the sound file in the resources directory
     */
    public AudioPlayer(String soundFileName) {
        try {
            System.out.println("Attempting to load sound file: " + soundFileName);
            
            // Use the requested sound file (don't hardcode)
            this.currentFile = soundFileName;
            
            // Initialize emergency tone generator
            this.emergencyPlayer = new EmergencyTonePlayer();
            
            // Try to load the sound file
            boolean success = loadSound(soundFileName);
            
            if (!success) {
                // If requested file failed, try falling back to a known working MP3
                System.out.println("Falling back to somber_background.mp3");
                success = loadSound("somber_background.mp3");
                
                // If still no success, we'll fall back to the emergency tone generator when play() is called
                if (!success) {
                    System.out.println("All audio loading methods failed. Will use emergency tone generator.");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error initializing audio player: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load sound from various sources, trying different approaches
     */
    private boolean loadSound(String soundFileName) {
        // First try loading as a media file (better for MP3)
        if (soundFileName.toLowerCase().endsWith(".mp3")) {
            return loadMediaFile(soundFileName);
        } else {
            // Try loading as a clip (for WAV, etc.)
            return loadClip(soundFileName);
        }
    }
    
    /**
     * Loads MP3 files using Media API
     */
    private boolean loadMediaFile(String soundFileName) {
        try {
            // Try several methods to find the audio file
            URL resourceUrl = null;
            
            // Method 1: Try to get the resource URL directly from classpath
            resourceUrl = getClass().getClassLoader().getResource("sounds/" + soundFileName);
            if (resourceUrl != null) {
                System.out.println("Resource URL from classpath: " + resourceUrl);
                return createMediaPlayerFromURL(resourceUrl);
            }
            
            // Method 2: Try to get the resource from the class
            resourceUrl = getClass().getResource("/sounds/" + soundFileName);
            if (resourceUrl != null) {
                System.out.println("Resource URL from class: " + resourceUrl);
                return createMediaPlayerFromURL(resourceUrl);
            }
            
            // Method 3: Try absolute file path
            File soundFile = new File("src/main/resources/sounds/" + soundFileName);
            if (soundFile.exists()) {
                System.out.println("Found sound file at: " + soundFile.toURI());
                
                try {
                    Media media = new Media(soundFile.toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                    mediaPlayer.setVolume(1.0);
                    
                    // Add error listener
                    mediaPlayer.setOnError(() -> {
                        System.err.println("Media player error: " + mediaPlayer.getError());
                    });
                    
                    System.out.println("Sound initialized successfully from file system");
                    return true;
                } catch (Exception e) {
                    System.err.println("Error creating MediaPlayer from file: " + e.getMessage());
                }
            } else {
                System.out.println("File not found at: " + soundFile.getAbsolutePath());
                System.out.println("Current working directory: " + System.getProperty("user.dir"));
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Error loading media file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean createMediaPlayerFromURL(URL resourceUrl) {
        try {
            System.out.println("Loading sound from resources: " + resourceUrl);
            
            // Create a Media object with the URL
            Media media = new Media(resourceUrl.toString());
            
            // Create a new MediaPlayer
            mediaPlayer = new MediaPlayer(media);
            
            // Set up looping
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            
            // Set volume to maximum
            mediaPlayer.setVolume(1.0);
            
            // Add error listener
            mediaPlayer.setOnError(() -> {
                System.err.println("Media player error: " + mediaPlayer.getError());
            });
            
            System.out.println("Sound initialized successfully from resources");
            return true;
        } catch (Exception e) {
            System.err.println("Error creating MediaPlayer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Loads WAV files using the Clip API
     */
    private boolean loadClip(String soundFileName) {
        try {
            // Try to get the resource URL
            URL resourceUrl = getClass().getClassLoader().getResource("sounds/" + soundFileName);
            if (resourceUrl != null) {
                System.out.println("Resource URL from class: " + resourceUrl);
                System.out.println("Loading sound from resources: " + resourceUrl);
                
                try {
                    // Get AudioInputStream from the URL
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(resourceUrl);
                    
                    // Get a clip resource
                    clip = AudioSystem.getClip();
                    
                    // Open audio clip and load samples
                    clip.open(audioStream);
                    
                    // Set up auto-repeat
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            if (isPlaying) {
                                clip.setFramePosition(0);
                                clip.start();
                                System.out.println("Restarting audio for looping");
                            }
                        }
                    });
                    
                    // Set volume to maximum
                    setVolume(1.0); 
                    
                    System.out.println("Successfully initialized background music");
                    return true;
                } catch (Exception e) {
                    System.err.println("Error loading sound from resources: " + e.getMessage());
                }
            }
            
            // Try loading directly from the file system if resource approach failed
            String filePath = "src/main/resources/sounds/" + soundFileName;
            File soundFile = new File(filePath);
            System.out.println("Trying file path: " + soundFile.getAbsolutePath());
            
            if (soundFile.exists()) {
                System.out.println("Found sound file: " + soundFile.getAbsolutePath());
                
                try {
                    // Get AudioInputStream from the file
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                    
                    // Get a clip resource
                    clip = AudioSystem.getClip();
                    
                    // Open audio clip and load samples
                    clip.open(audioStream);
                    
                    // Set up auto-repeat
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            if (isPlaying) {
                                clip.setFramePosition(0);
                                clip.start();
                                System.out.println("Restarting audio for looping");
                            }
                        }
                    });
                    
                    // Set volume to maximum
                    setVolume(1.0); 
                    
                    System.out.println("Successfully initialized background music");
                    return true;
                } catch (Exception e) {
                    System.err.println("Error loading sound from absolute path: " + e.getMessage());
                }
            } else {
                System.err.println("Sound file not found: " + soundFile.getAbsolutePath());
                System.out.println("Current working directory: " + System.getProperty("user.dir"));
            }
            
            System.out.println("Failed to load sound file using any method");
            return false;
        } catch (Exception e) {
            System.err.println("Error loading sound: " + e.getMessage());
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
            
            // Reset to the beginning and set high volume
            clip.setFramePosition(0);
            setVolume(1.0);
            
            clip.start();
            isPlaying = true;
            
            System.out.println("Playback started successfully");
        } else if (mediaPlayer != null && !isPlaying) {
            System.out.println("Setting volume to " + volume);
            mediaPlayer.setVolume(volume);
            
            System.out.println("Starting background music playback");
            mediaPlayer.play();
            isPlaying = true;
        } else {
            System.out.println("Cannot play sound: " + 
                              (clip != null ? "clip=initialized" : "clip=null") + ", " +
                              (mediaPlayer != null ? "mediaPlayer=initialized" : "mediaPlayer=null") + ", " +
                              "isPlaying=" + isPlaying);
            
            // Use emergency tone generator as last resort
            if (!isPlaying) {
                System.out.println("Falling back to emergency tone generator");
                emergencyPlayer.start();
                isPlaying = true;
            }
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
            System.out.println("Playback stopped");
        } else if (mediaPlayer != null && isPlaying) {
            System.out.println("Stopping background music playback");
            mediaPlayer.stop();
            isPlaying = false;
        } else if (isPlaying) {
            // Must be emergency player
            System.out.println("Stopping emergency tone playback");
            emergencyPlayer.stop();
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
            System.out.println("Playback paused");
        } else if (mediaPlayer != null && isPlaying) {
            System.out.println("Pausing background music playback");
            mediaPlayer.pause();
            isPlaying = false;
        } else if (isPlaying) {
            // Must be emergency player
            System.out.println("Pausing emergency tone playback");
            emergencyPlayer.stop();
            isPlaying = false;
        }
    }

    /**
     * Sets the volume of the audio player.
     * 
     * @param volume Volume level between 0.0 and 10.0
     */
    public void setVolume(double volumeLevel) {
        this.volume = (float)volumeLevel;
        
        if (clip != null) {
            try {
                System.out.println("Setting volume to " + volume);
                
                // Get control for changing volume
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                
                // Convert linear volume to gain in dB
                // For volume > 1.0, boost the gain above 0dB
                // Range is typically -80.0 dB (silent) to +6.0 dB (loudest)
                float dB;
                if (volumeLevel <= 1.0) {
                    // Normal range: map 0.0-1.0 to -80.0-0.0 dB
                    dB = (float) (Math.log10(Math.max(0.0001, volumeLevel)) * 20.0);
                } else {
                    // Boosted range: map 1.0-10.0 to 0.0-6.0 dB (amplification)
                    dB = (float) (Math.min(6.0, (volumeLevel - 1.0) * 0.67));
                }
                
                // Make sure dB is within the control's range
                float min = gainControl.getMinimum();
                float max = gainControl.getMaximum();
                dB = Math.max(min, Math.min(max, dB));
                
                // Set the volume
                gainControl.setValue(dB);
                System.out.println("Volume set to: " + dB + " dB (control range: " + min + " to " + max + " dB)");
            } catch (Exception e) {
                System.err.println("Error setting volume: " + e.getMessage());
            }
        } else if (mediaPlayer != null) {
            // MediaPlayer volume range is only 0.0 to 1.0
            double clampedVolume = Math.min(1.0, volume);
            System.out.println("Setting media player volume to " + clampedVolume + 
                              (volume > 1.0 ? " (capped from " + volume + ")" : ""));
            mediaPlayer.setVolume(clampedVolume);
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
            System.out.println("Audio resources disposed");
        }
        
        if (mediaPlayer != null) {
            System.out.println("Disposing media player resources");
            mediaPlayer.stop();
            mediaPlayer.dispose();
            isPlaying = false;
        }
        
        if (emergencyPlayer != null) {
            System.out.println("Disposing emergency tone resources");
            emergencyPlayer.stop();
            isPlaying = false;
        }
    }
} 