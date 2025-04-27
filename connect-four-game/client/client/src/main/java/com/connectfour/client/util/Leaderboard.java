package com.connectfour.client.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

// 
// Simple file-backed leaderboard that stores total wins per username.
// Data is stored as a CSV-like text file in the user's home directory.
public class Leaderboard {

    private static final Path FILE_PATH = Paths.get(System.getProperty("user.home"), ".knect4_leaderboard.dat");
    private static final Map<String, Integer> SCORE_MAP = new HashMap<>();

    static {
        load();
    }

    private static void load() {
        if (!Files.exists(FILE_PATH)) {
            return;
        }
        try (BufferedReader br = Files.newBufferedReader(FILE_PATH)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    String user = parts[0].trim();
                    try {
                        int wins = Integer.parseInt(parts[1].trim());
                        SCORE_MAP.put(user, wins);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading leaderboard: " + e.getMessage());
        }
    }

    private static void save() {
        try (BufferedWriter bw = Files.newBufferedWriter(FILE_PATH)) {
            for (Map.Entry<String, Integer> entry : SCORE_MAP.entrySet()) {
                bw.write(entry.getKey() + "," + entry.getValue());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing leaderboard: " + e.getMessage());
        }
    }

    // 
// Increment win count for a username.
    public static void recordWin(String username) {
        if (username == null || username.isBlank()) return;
        SCORE_MAP.merge(username, 1, Integer::sum);
        save();
    }

    // 
// Returns a list of "username – wins" strings ordered by wins descending.
    public static List<String> getTopPlayers(int limit) {
        return SCORE_MAP.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(e -> e.getKey() + " – " + e.getValue() + " wins")
                .collect(Collectors.toList());
    }

    private Leaderboard() {}
} 
