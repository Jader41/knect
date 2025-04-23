package com.connectfour.server;

import com.connectfour.common.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Handles communication with a single client.
 */
public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    
    private final Socket socket;
    private final GameServer server;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    
    private String username;
    private boolean connected;
    private GameSession currentGame;
    private boolean inMatchmaking;
    
    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
        this.connected = true;
        this.inMatchmaking = false;
    }
    
    @Override
    public void run() {
        try {
            // Set up input and output streams
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(socket.getInputStream());
            
            // Handle incoming messages
            while (connected) {
                try {
                    Object obj = inputStream.readObject();
                    
                    if (obj instanceof Message) {
                        handleMessage((Message) obj);
                    } else {
                        logger.warn("Received unknown object from client: {}", obj.getClass().getName());
                    }
                } catch (ClassNotFoundException e) {
                    logger.error("Error reading message from client", e);
                } catch (SocketException e) {
                    connected = false;
                    handleDisconnect();
                }
            }
        } catch (IOException e) {
            logger.error("Error in client handler", e);
            connected = false;
            handleDisconnect();
        } finally {
            cleanup();
        }
    }
    
    /**
     * Handles an incoming message from the client.
     * 
     * @param message The message to handle
     */
    private void handleMessage(Message message) {
        try {
            switch (message.getType()) {
                case LOGIN_REQUEST:
                    handleLoginRequest((LoginRequestMessage) message);
                    break;
                case MOVE:
                    handleMoveMessage((MoveMessage) message);
                    break;
                case CHAT_MESSAGE:
                    handleChatMessage((ChatMessage) message);
                    break;
                case PLAY_AGAIN_REQUEST:
                    handlePlayAgainRequest((PlayAgainRequestMessage) message);
                    break;
                case DISCONNECT:
                    handleDisconnectMessage((DisconnectMessage) message);
                    break;
                default:
                    logger.warn("Unhandled message type: {}", message.getType());
            }
        } catch (Exception e) {
            logger.error("Error handling message: {}", message.getType(), e);
        }
    }
    
    /**
     * Handles a login request message.
     * 
     * @param message The login request message
     */
    private void handleLoginRequest(LoginRequestMessage message) {
        String requestedUsername = message.getUsername().trim();
        
        // Validate username
        if (requestedUsername.isEmpty() || requestedUsername.length() > 20) {
            sendMessage(new LoginResponseMessage(false, "Username must be between 1 and 20 characters"));
            return;
        }
        
        // Try to register the username
        boolean success = server.registerUsername(requestedUsername, this);
        
        if (success) {
            this.username = requestedUsername;
            sendMessage(new LoginResponseMessage(true, null));
            logger.info("User logged in: {}", username);
            
            // Add the client to matchmaking queue
            inMatchmaking = true;
            server.addToMatchmaking(this);
        } else {
            sendMessage(new LoginResponseMessage(false, "Username already in use"));
            logger.info("Login failed - username already in use: {}", requestedUsername);
        }
    }
    
    /**
     * Handles a move message.
     * 
     * @param message The move message
     */
    private void handleMoveMessage(MoveMessage message) {
        if (currentGame != null) {
            currentGame.handleMove(this, message.getColumn());
        } else {
            logger.warn("Received move message from {} but not in a game", username);
        }
    }
    
    /**
     * Handles a chat message.
     * 
     * @param message The chat message
     */
    private void handleChatMessage(ChatMessage message) {
        if (currentGame != null) {
            currentGame.broadcastChat(message);
        } else {
            logger.warn("Received chat message from {} but not in a game", username);
        }
    }
    
    /**
     * Handles a play again request.
     * 
     * @param message The play again request message
     */
    private void handlePlayAgainRequest(PlayAgainRequestMessage message) {
        if (currentGame != null) {
            currentGame.handlePlayAgainRequest(this, message.wantsToPlayAgain());
        } else {
            logger.warn("Received play again request from {} but not in a game", username);
            
            // If the player wants to play again but isn't in a game, add to matchmaking
            if (message.wantsToPlayAgain()) {
                inMatchmaking = true;
                server.addToMatchmaking(this);
            }
        }
    }
    
    /**
     * Handles a disconnect message.
     * 
     * @param message The disconnect message
     */
    private void handleDisconnectMessage(DisconnectMessage message) {
        logger.info("Client {} requested disconnect: {}", username, message.getReason());
        disconnect(message.getReason());
    }
    
    /**
     * Handles a client disconnect.
     */
    private void handleDisconnect() {
        logger.info("Client disconnected: {}", username);
        
        if (username != null) {
            // Unregister the username
            server.unregisterUsername(username);
            
            // Remove from matchmaking if necessary
            if (inMatchmaking) {
                server.removeFromMatchmaking(this);
                inMatchmaking = false;
            }
            
            // Notify the current game if there is one
            if (currentGame != null) {
                currentGame.handlePlayerDisconnect(this);
                currentGame = null;
            }
        }
    }
    
    /**
     * Cleans up resources.
     */
    private void cleanup() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            logger.error("Error cleaning up client handler", e);
        }
    }
    
    /**
     * Sends a message to the client.
     * 
     * @param message The message to send
     */
    public synchronized void sendMessage(Message message) {
        if (connected && outputStream != null) {
            try {
                outputStream.writeObject(message);
                outputStream.flush();
                logger.debug("Sent message to {}: {}", username, message.getType());
            } catch (IOException e) {
                logger.error("Error sending message to client: {}", username, e);
                connected = false;
                handleDisconnect();
            }
        }
    }
    
    /**
     * Disconnects the client with a reason.
     * 
     * @param reason The reason for disconnection
     */
    public void disconnect(String reason) {
        if (connected) {
            sendMessage(new DisconnectMessage(reason));
            connected = false;
            handleDisconnect();
        }
    }
    
    /**
     * Checks if the client is connected.
     * 
     * @return true if the client is connected, false otherwise
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Gets the username of the client.
     * 
     * @return The username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Sets the current game session for this client.
     * 
     * @param gameSession The game session
     */
    public void setCurrentGame(GameSession gameSession) {
        this.currentGame = gameSession;
        this.inMatchmaking = false;
    }
    
    /**
     * Clears the current game session.
     */
    public void clearCurrentGame() {
        this.currentGame = null;
    }
} 