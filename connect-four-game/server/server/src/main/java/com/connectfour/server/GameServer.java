package com.connectfour.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.connectfour.common.messages.Message;

/**
 * The main server class that handles client connections and manages game sessions.
 */
public class GameServer {
    private static final Logger logger = LoggerFactory.getLogger(GameServer.class);
    private static GameServer instance;
    
    private final int port;
    private ServerSocket serverSocket;
    private boolean running;
    private final List<ClientHandler> connectedClients;
    private final MatchmakingQueue matchmakingQueue;
    private final List<GameSession> activeSessions;
    private final ExecutorService executorService;
    private final Map<String, ClientHandler> usernameMap = new ConcurrentHashMap<>();
    
    /**
     * Creates a new game server on the specified port.
     * 
     * @param port The port number to listen on
     */
    public GameServer(int port) {
        this.port = port;
        this.connectedClients = Collections.synchronizedList(new ArrayList<>());
        this.activeSessions = Collections.synchronizedList(new ArrayList<>());
        this.executorService = Executors.newCachedThreadPool();
        this.matchmakingQueue = new MatchmakingQueue(this);
        
        // Start the matchmaking queue
        Thread matchmakingThread = new Thread(matchmakingQueue);
        matchmakingThread.setDaemon(true);
        matchmakingThread.start();
        
        instance = this;
    }
    
    /**
     * Returns the singleton instance of the game server.
     * 
     * @return The game server instance
     */
    public static GameServer getInstance() {
        return instance;
    }
    
    /**
     * Starts the game server.
     */
    public void start() {
        running = true;
        
        try {
            serverSocket = new ServerSocket(port);
            logger.info("Server started on port {}", port);
            
            // Accept client connections
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("New client connected: {}", clientSocket.getInetAddress().getHostAddress());
                    
                    // Create a new client handler for the connection
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    connectedClients.add(clientHandler);
                    
                    // Notify of new client connection
                    clientConnected(clientHandler);
                    
                    // Start a new thread for the client handler
                    executorService.submit(clientHandler);
                } catch (IOException e) {
                    if (running) {
                        logger.error("Error accepting client connection", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error starting server on port {}", port, e);
        } finally {
            shutdown();
        }
    }
    
    /**
     * Stops the game server.
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
    }
    
    /**
     * Shuts down the game server and cleans up resources.
     */
    private void shutdown() {
        try {
            // Close all client connections
            for (ClientHandler client : connectedClients) {
                client.disconnect("Server shutting down");
            }
            
            // Shutdown the executor service
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("Server shutdown complete");
    }
    
    /**
     * Registers a username for a client.
     * 
     * @param username The username to register
     * @param client The client to register the username for
     * @return true if the username was successfully registered, false otherwise
     */
    public boolean registerUsername(String username, ClientHandler client) {
        if (usernameMap.containsKey(username)) {
            return false;
        }
        usernameMap.put(username, client);
        return true;
    }
    
    /**
     * Unregisters a username.
     * 
     * @param username The username to unregister
     */
    public void unregisterUsername(String username) {
        usernameMap.remove(username);
    }
    
    /**
     * Removes a client from the matchmaking queue.
     * 
     * @param client The client to remove
     */
    public void removeFromMatchmaking(ClientHandler client) {
        matchmakingQueue.removeFromQueue(client);
    }
    
    /**
     * Removes a client from the connected clients list.
     * 
     * @param client The client to remove
     */
    public void removeClient(ClientHandler client) {
        connectedClients.remove(client);
        matchmakingQueue.removeFromQueue(client);
        logger.info("Client removed: {}", client.getUsername());
        clientDisconnected(client);
    }
    
    /**
     * Called when a client connects to the server.
     * Can be overridden by subclasses to handle client connections.
     * 
     * @param client The client that connected
     */
    public void clientConnected(ClientHandler client) {
        // Default implementation does nothing
    }
    
    /**
     * Called when a client disconnects from the server.
     * Can be overridden by subclasses to handle client disconnections.
     * 
     * @param client The client that disconnected
     */
    public void clientDisconnected(ClientHandler client) {
        // Default implementation does nothing
    }
    
    /**
     * Adds a client to the matchmaking queue.
     * 
     * @param client The client to add to matchmaking
     */
    public void addToMatchmaking(ClientHandler client) {
        matchmakingQueue.addToQueue(client);
    }
    
    /**
     * Ends a game session.
     * 
     * @param session The game session to end
     */
    public void endGameSession(GameSession session) {
        activeSessions.remove(session);
        logger.info("Game session ended");
    }
    
    /**
     * Broadcasts a message to all connected clients.
     * 
     * @param message The message to broadcast
     */
    public void broadcastMessage(Message message) {
        for (ClientHandler client : connectedClients) {
            client.sendMessage(message);
        }
    }
    
    /**
     * Returns a list of online users.
     * 
     * @return A list of usernames of connected clients
     */
    public List<String> getOnlineUsers() {
        List<String> onlineUsers = new ArrayList<>();
        
        for (ClientHandler client : connectedClients) {
            if (client.isConnected() && client.getUsername() != null) {
                onlineUsers.add(client.getUsername());
            }
        }
        
        return onlineUsers;
    }
    
    /**
     * The main method to start the server.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        int port = 8080; // Default port
        
        // Check if a port number was provided as a command line argument
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.warn("Invalid port number: {}. Using default port: {}", args[0], port);
            }
        }
        
        // Create and start the server
        GameServer server = new GameServer(port);
        server.start();
    }
} 