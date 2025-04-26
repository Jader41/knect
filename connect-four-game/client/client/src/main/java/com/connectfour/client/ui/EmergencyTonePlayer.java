package com.connectfour.client.ui;

import javax.sound.sampled.*;

/**
 * A simple tone generator that creates audio directly.
 * Use this as a fallback when other audio methods fail.
 */
public class EmergencyTonePlayer {
    
    private boolean isPlaying = false;
    private Thread audioThread;
    private final Object lock = new Object();
    
    /**
     * Start playing a continuous background tone
     */
    public void start() {
        if (isPlaying) {
            return;
        }
        
        synchronized (lock) {
            isPlaying = true;
            
            audioThread = new Thread(() -> {
                try {
                    playTone();
                } catch (Exception e) {
                    System.err.println("Error in audio thread: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    synchronized (lock) {
                        isPlaying = false;
                    }
                }
            });
            
            audioThread.setDaemon(true);
            audioThread.start();
        }
    }
    
    /**
     * Stop playing the tone
     */
    public void stop() {
        synchronized (lock) {
            isPlaying = false;
            if (audioThread != null) {
                audioThread.interrupt();
                audioThread = null;
            }
        }
    }
    
    /**
     * Generate and play a continuous tone
     */
    private void playTone() {
        // Audio format parameters
        float sampleRate = 44100.0f;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        
        try {
            AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
            
            // Create a data line
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Line not supported!");
                return;
            }
            
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(format);
                line.start();
                
                // Generate a low-volume background music tone (C major chord)
                float[] frequencies = {261.63f, 329.63f, 392.00f}; // C, E, G
                int bufferSize = (int) (sampleRate / 10); // 1/10th of a second buffer
                byte[] buffer = new byte[bufferSize];
                double angle1 = 0, angle2 = 0, angle3 = 0;
                
                System.out.println("Starting emergency tone playback");
                
                while (isPlaying) {
                    // Mix three frequencies with different volumes
                    for (int i = 0; i < bufferSize; i += 2) {
                        // Calculate sample (3 superimposed sine waves)
                        double sample = Math.sin(angle1) * 0.03 + // C note (very quiet)
                                      Math.sin(angle2) * 0.02 + // E note (quieter)
                                      Math.sin(angle3) * 0.01;  // G note (quietest)
                        
                        // Scale to 16-bit range and convert to bytes
                        short val = (short) (sample * 32767);
                        buffer[i] = (byte) (val & 0xFF);
                        buffer[i+1] = (byte) ((val >> 8) & 0xFF);
                        
                        // Update angles for next sample
                        angle1 += 2 * Math.PI * frequencies[0] / sampleRate;
                        angle2 += 2 * Math.PI * frequencies[1] / sampleRate;
                        angle3 += 2 * Math.PI * frequencies[2] / sampleRate;
                        
                        // Keep angles in reasonable range to prevent floating point errors
                        if (angle1 > 2 * Math.PI) angle1 -= 2 * Math.PI;
                        if (angle2 > 2 * Math.PI) angle2 -= 2 * Math.PI;
                        if (angle3 > 2 * Math.PI) angle3 -= 2 * Math.PI;
                    }
                    
                    // Check if we've been interrupted
                    if (!isPlaying) break;
                    
                    // Write the buffer to the line
                    line.write(buffer, 0, buffer.length);
                }
                
                System.out.println("Emergency tone playback stopped");
                
                // Drain the buffer and close the line
                line.drain();
            }
            
        } catch (Exception e) {
            System.err.println("Error playing tone: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Test method
    public static void main(String[] args) throws Exception {
        EmergencyTonePlayer player = new EmergencyTonePlayer();
        player.start();
        
        // Play for 5 seconds
        Thread.sleep(5000);
        
        player.stop();
        System.out.println("Test complete");
    }
} 