package com.connectfour.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The main server class that handles client connections and manages game sessions.
 */
public class GameServer {
    private static final Logger logger = LoggerFactory.getLogger(GameServer.class);
    private static final int THREAD_POOL_SIZE = 50;
    
    private final int port;
    private ServerSocket serverSocket;
    private boolean running;
    private final ExecutorService threadPool;
    
    // Maps usernames to client handlers
    private final Map<String, ClientHandler> connectedClients;
    
    // Set of usernames currently in use
    private final Set<String> usernames;
    
    // Queue for matchmaking
    private final MatchmakingQueue matchmakingQueue;
    
    public GameServer(int port) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.connectedClients = new ConcurrentHashMap<>();
        this.usernames = ConcurrentHashMap.newKeySet();
        this.matchmakingQueue = new MatchmakingQueue(this);
    }
    
    /**
     * Starts the server.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            logger.info("Server started on port {}", port);
            
            // Start a separate thread for matchmaking
            Thread matchmakingThread = new Thread(matchmakingQueue);
            matchmakingThread.setDaemon(true);
            matchmakingThread.start();
            
            // Accept client connections
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("New client connected: {}", clientSocket.getInetAddress());
                    
                    // Create a new client handler for this connection
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    threadPool.execute(clientHandler);
                } catch (IOException e) {
                    if (running) {
                        logger.error("Error accepting client connection", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error starting server on port {}", port, e);
        }
    }
    
    /**
     * Stops the server.
     */
    public void stop() {
        running = false;
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket", e);
        }
        
        // Disconnect all clients
        for (ClientHandler client : connectedClients.values()) {
            client.disconnect("Server shutting down");
        }
        
        // Shutdown thread pool
        threadPool.shutdown();
        logger.info("Server stopped");
    }
    
    /**
     * Registers a client with the given username.
     * 
     * @param username The username to register
     * @param clientHandler The client handler associated with this username
     * @return true if the username was successfully registered, false if it's already in use
     */
    public synchronized boolean registerUsername(String username, ClientHandler clientHandler) {
        if (usernames.contains(username)) {
            return false;
        }
        
        usernames.add(username);
        connectedClients.put(username, clientHandler);
        logger.info("User registered: {}", username);
        return true;
    }
    
    /**
     * Unregisters a client with the given username.
     * 
     * @param username The username to unregister
     */
    public synchronized void unregisterUsername(String username) {
        usernames.remove(username);
        connectedClients.remove(username);
        logger.info("User unregistered: {}", username);
    }
    
    /**
     * Adds a client to the matchmaking queue.
     * 
     * @param clientHandler The client handler to add to the queue
     */
    public void addToMatchmaking(ClientHandler clientHandler) {
        matchmakingQueue.addToQueue(clientHandler);
        logger.info("User added to matchmaking queue: {}", clientHandler.getUsername());
    }
    
    /**
     * Removes a client from the matchmaking queue.
     * 
     * @param clientHandler The client handler to remove from the queue
     */
    public void removeFromMatchmaking(ClientHandler clientHandler) {
        matchmakingQueue.removeFromQueue(clientHandler);
        logger.info("User removed from matchmaking queue: {}", clientHandler.getUsername());
    }
    
    /**
     * Gets a client handler by username.
     * 
     * @param username The username to look up
     * @return The client handler for the given username, or null if not found
     */
    public ClientHandler getClientHandler(String username) {
        return connectedClients.get(username);
    }
} 