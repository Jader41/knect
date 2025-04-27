package com.connectfour.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 
// Main entry point for the Connect Four server application.
public class ServerApp {
    private static final Logger logger = LoggerFactory.getLogger(ServerApp.class);
    private static final int DEFAULT_PORT = 8080;
    
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        // Parse command line arguments if provided
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.warn("Invalid port number provided. Using default port: {}", DEFAULT_PORT);
            }
        }
        
        logger.info("Starting Connect Four server on port {}", port);
        
        // Create and start the server
        GameServer server = new GameServer(port);
        server.start();
        
        // Add shutdown hook to gracefully stop the server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down server...");
            server.stop();
        }));
    }
} 
