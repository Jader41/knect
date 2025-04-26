package com.connectfour.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handles matchmaking between players.
 */
public class MatchmakingQueue implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MatchmakingQueue.class);
    private static final long MATCHMAKING_CHECK_INTERVAL = 1000; // 1 second
    
    private final GameServer server;
    private final Queue<ClientHandler> queue;
    private boolean running;
    
    public MatchmakingQueue(GameServer server) {
        this.server = server;
        this.queue = new ConcurrentLinkedQueue<>();
        this.running = true;
    }
    
    /**
     * Adds a client to the matchmaking queue.
     * 
     * @param client The client handler to add
     */
    public void addToQueue(ClientHandler client) {
        if (!queue.contains(client)) {
            queue.add(client);
            logger.info("Added client to matchmaking queue: {}. Queue size: {}", client.getUsername(), queue.size());
        }
    }
    
    /**
     * Removes a client from the matchmaking queue.
     * 
     * @param client The client handler to remove
     */
    public void removeFromQueue(ClientHandler client) {
        queue.remove(client);
        logger.info("Removed client from matchmaking queue: {}. Queue size: {}", client.getUsername(), queue.size());
    }
    
    @Override
    public void run() {
        logger.info("Matchmaking queue started");
        
        while (running) {
            try {
                matchPlayers();
                Thread.sleep(MATCHMAKING_CHECK_INTERVAL);
            } catch (InterruptedException e) {
                logger.warn("Matchmaking thread interrupted", e);
                running = false;
            } catch (Exception e) {
                logger.error("Error in matchmaking", e);
            }
        }
        
        logger.info("Matchmaking queue stopped");
    }
    
    /**
     * Attempts to match waiting players.
     */
    private void matchPlayers() {
        if (queue.size() >= 2) {
            logger.info("Attempting to match players. Current queue size: {}", queue.size());
        }
        
        while (queue.size() >= 2) {
            ClientHandler player1 = queue.poll();
            ClientHandler player2 = queue.poll();
            
            if (player1 != null && player2 != null) {
                // Check if both clients are still connected
                if (player1.isConnected() && player2.isConnected()) {
                    // Create a new game session
                    GameSession gameSession = new GameSession(player1, player2, server);
                    
                    // Start the game
                    gameSession.start();
                    
                    logger.info("Matched players: {} vs {}. Remaining in queue: {}", 
                        player1.getUsername(), player2.getUsername(), queue.size());
                } else {
                    // If one of the clients is disconnected, put the connected one back in the queue
                    if (player1.isConnected()) {
                        queue.add(player1);
                        logger.info("Player {} reconnected to queue after failed match", player1.getUsername());
                    }
                    if (player2.isConnected()) {
                        queue.add(player2);
                        logger.info("Player {} reconnected to queue after failed match", player2.getUsername());
                    }
                }
            }
        }
    }
    
    /**
     * Stops the matchmaking queue.
     */
    public void stop() {
        running = false;
    }
} 