package com.connectfour.client;

import com.connectfour.common.messages.*;
import com.connectfour.common.model.GameState;
import com.connectfour.common.model.PlayerColor;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles network communication with the server.
 */
public class GameClient {
    private final String host;
    private final int port;
    
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private final ExecutorService executor;
    
    private boolean connected;
    private String username;
    private GameState currentGameState;
    private PlayerColor assignedColor;
    private String opponentUsername;
    
    private final List<ConnectionListener> connectionListeners;
    private final List<GameStateListener> gameStateListeners;
    private final List<ChatMessageListener> chatMessageListeners;
    
    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.executor = Executors.newSingleThreadExecutor();
        this.connected = false;
        this.connectionListeners = new CopyOnWriteArrayList<>();
        this.gameStateListeners = new CopyOnWriteArrayList<>();
        this.chatMessageListeners = new CopyOnWriteArrayList<>();
    }
    
    /**
     * Connects to the server and attempts to log in with the given username.
     * 
     * @param username The username to use for login
     */
    public void connect(String username) {
        if (connected) {
            return;
        }
        
        this.username = username;
        
        executor.execute(() -> {
            try {
                // Connect to the server
                socket = new Socket(host, port);
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.flush();
                inputStream = new ObjectInputStream(socket.getInputStream());
                
                connected = true;
                
                // Send the login request
                sendMessage(new LoginRequestMessage(username));
                
                // Start listening for messages
                startListening();
                
                // Notify listeners of the connection
                notifyConnectionEstablished();
            } catch (IOException e) {
                System.err.println("Error connecting to server: " + e.getMessage());
                notifyConnectionFailed(e.getMessage());
            }
        });
    }
    
    /**
     * Disconnects from the server with an optional reason.
     * 
     * @param reason The reason for disconnection
     */
    public void disconnect(String reason) {
        if (!connected) {
            return;
        }
        
        try {
            if (outputStream != null) {
                sendMessage(new DisconnectMessage(reason));
            }
        } catch (Exception e) {
            System.err.println("Error sending disconnect message: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    /**
     * Cleans up resources.
     */
    private void cleanup() {
        // Set connected to false first to prevent recursive cleanup calls
        boolean wasConnected = connected;
        connected = false;
        
        try {
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            System.err.println("Error cleaning up resources: " + e.getMessage());
        }
        
        // Only notify if we were connected before
        if (wasConnected) {
            notifyDisconnected("Connection closed");
        }
    }
    
    /**
     * Starts listening for messages from the server.
     */
    private void startListening() {
        executor.execute(() -> {
            try {
                while (connected) {
                    try {
                        Object obj = inputStream.readObject();
                        
                        if (obj instanceof Message) {
                            handleMessage((Message) obj);
                        } else {
                            System.err.println("Received unknown object from server: " + obj.getClass().getName());
                        }
                    } catch (ClassNotFoundException e) {
                        System.err.println("Error reading message from server: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                if (connected) {
                    System.err.println("Connection lost: " + e.getMessage());
                    cleanup();
                }
            }
        });
    }
    
    /**
     * Handles a message received from the server.
     * 
     * @param message The message to handle
     */
    private void handleMessage(Message message) {
        switch (message.getType()) {
            case LOGIN_RESPONSE:
                handleLoginResponse((LoginResponseMessage) message);
                break;
            case GAME_START:
                handleGameStart((GameStartMessage) message);
                break;
            case GAME_STATE_UPDATE:
                handleGameStateUpdate((GameStateUpdateMessage) message);
                break;
            case CHAT_MESSAGE:
                handleChatMessage((ChatMessage) message);
                break;
            case PLAY_AGAIN_RESPONSE:
                handlePlayAgainResponse((PlayAgainResponseMessage) message);
                break;
            case DISCONNECT:
                handleDisconnect((DisconnectMessage) message);
                break;
            default:
                System.err.println("Unhandled message type: " + message.getType());
        }
    }
    
    /**
     * Handles a login response message.
     * 
     * @param message The login response message
     */
    private void handleLoginResponse(LoginResponseMessage message) {
        if (message.isSuccess()) {
            notifyLoginSuccessful();
        } else {
            notifyLoginFailed(message.getErrorMessage());
        }
    }
    
    /**
     * Handles a game start message.
     * 
     * @param message The game start message
     */
    private void handleGameStart(GameStartMessage message) {
        this.currentGameState = message.getInitialState();
        this.assignedColor = message.getAssignedColor();
        this.opponentUsername = message.getOpponentUsername();
        
        notifyGameStarted(currentGameState, assignedColor, opponentUsername);
    }
    
    /**
     * Handles a game state update message.
     * 
     * @param message The game state update message
     */
    private void handleGameStateUpdate(GameStateUpdateMessage message) {
        System.out.println("Received game state update from server:");
        System.out.println("  Current turn: " + message.getGameState().getCurrentTurn());
        System.out.println("  Board state:");
        for (int row = 0; row < GameState.ROWS; row++) {
            StringBuilder rowStr = new StringBuilder("    ");
            for (int col = 0; col < GameState.COLUMNS; col++) {
                switch(message.getGameState().getCellState(row, col)) {
                    case EMPTY: rowStr.append("[ ]"); break;
                    case RED: rowStr.append("[R]"); break;
                    case YELLOW: rowStr.append("[Y]"); break;
                }
            }
            System.out.println(rowStr.toString());
        }
        
        this.currentGameState = message.getGameState();
        notifyGameStateUpdated(currentGameState);
    }
    
    /**
     * Handles a chat message.
     * 
     * @param message The chat message
     */
    private void handleChatMessage(ChatMessage message) {
        notifyChatMessageReceived(message);
    }
    
    /**
     * Handles a play again response message.
     * 
     * @param message The play again response message
     */
    private void handlePlayAgainResponse(PlayAgainResponseMessage message) {
        if (message.bothWantToPlayAgain()) {
            // A new game will be started, wait for the game start message
        } else if (message.isOpponentDisconnected()) {
            notifyOpponentDisconnected();
        } else {
            notifyOpponentDeclinedRematch();
        }
    }
    
    /**
     * Handles a disconnect message.
     * 
     * @param message The disconnect message
     */
    private void handleDisconnect(DisconnectMessage message) {
        System.out.println("Disconnected from server: " + message.getReason());
        cleanup();
    }
    
    /**
     * Sends a message to the server.
     * 
     * @param message The message to send
     */
    private void sendMessage(Message message) {
        if (connected && outputStream != null) {
            try {
                outputStream.writeObject(message);
                outputStream.flush();
            } catch (IOException e) {
                System.err.println("Error sending message to server: " + e.getMessage());
                cleanup();
            }
        }
    }
    
    /**
     * Makes a move in the specified column.
     * 
     * @param column The column in which to drop the piece (0-based index)
     */
    public void makeMove(int column) {
        sendMessage(new MoveMessage(column));
    }
    
    /**
     * Sends a chat message.
     * 
     * @param content The content of the message
     */
    public void sendChatMessage(String content) {
        sendMessage(new ChatMessage(username, content));
    }
    
    /**
     * Requests to play again.
     * 
     * @param wantToPlayAgain Whether the player wants to play again
     */
    public void requestPlayAgain(boolean wantToPlayAgain) {
        sendMessage(new PlayAgainRequestMessage(wantToPlayAgain));
    }
    
    /**
     * Interface for connection-related events.
     */
    public interface ConnectionListener {
        void onConnectionEstablished();
        void onConnectionFailed(String reason);
        void onDisconnected(String reason);
        void onLoginSuccessful();
        void onLoginFailed(String reason);
    }
    
    /**
     * Interface for game state-related events.
     */
    public interface GameStateListener {
        void onGameStarted(GameState gameState, PlayerColor assignedColor, String opponentUsername);
        void onGameStateUpdated(GameState gameState);
        void onOpponentDisconnected();
        void onOpponentDeclinedRematch();
    }
    
    /**
     * Interface for chat message-related events.
     */
    public interface ChatMessageListener {
        void onChatMessageReceived(ChatMessage message);
    }
    
    /**
     * Adds a connection listener.
     * 
     * @param listener The listener to add
     */
    public void addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }
    
    /**
     * Removes a connection listener.
     * 
     * @param listener The listener to remove
     */
    public void removeConnectionListener(ConnectionListener listener) {
        connectionListeners.remove(listener);
    }
    
    /**
     * Adds a game state listener.
     * 
     * @param listener The listener to add
     */
    public void addGameStateListener(GameStateListener listener) {
        gameStateListeners.add(listener);
    }
    
    /**
     * Removes a game state listener.
     * 
     * @param listener The listener to remove
     */
    public void removeGameStateListener(GameStateListener listener) {
        gameStateListeners.remove(listener);
    }
    
    /**
     * Adds a chat message listener.
     * 
     * @param listener The listener to add
     */
    public void addChatMessageListener(ChatMessageListener listener) {
        chatMessageListeners.add(listener);
    }
    
    /**
     * Removes a chat message listener.
     * 
     * @param listener The listener to remove
     */
    public void removeChatMessageListener(ChatMessageListener listener) {
        chatMessageListeners.remove(listener);
    }
    
    /**
     * Notifies all connection listeners that the connection was established.
     */
    private void notifyConnectionEstablished() {
        Platform.runLater(() -> {
            // CopyOnWriteArrayList is thread-safe and doesn't throw ConcurrentModificationException
            for (ConnectionListener listener : connectionListeners) {
                try {
                    listener.onConnectionEstablished();
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Notifies all connection listeners that the connection failed.
     * 
     * @param reason The reason for the failure
     */
    private void notifyConnectionFailed(String reason) {
        Platform.runLater(() -> {
            // CopyOnWriteArrayList is thread-safe and doesn't throw ConcurrentModificationException
            for (ConnectionListener listener : connectionListeners) {
                try {
                    listener.onConnectionFailed(reason);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Notifies all connection listeners that the connection was closed.
     * 
     * @param reason The reason for the disconnection
     */
    private void notifyDisconnected(String reason) {
        Platform.runLater(() -> {
            // CopyOnWriteArrayList is thread-safe and doesn't throw ConcurrentModificationException
            for (ConnectionListener listener : connectionListeners) {
                try {
                    listener.onDisconnected(reason);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Notifies all connection listeners that the login was successful.
     */
    private void notifyLoginSuccessful() {
        Platform.runLater(() -> {
            // CopyOnWriteArrayList is thread-safe and doesn't throw ConcurrentModificationException
            for (ConnectionListener listener : connectionListeners) {
                try {
                    listener.onLoginSuccessful();
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Notifies all connection listeners that the login failed.
     * 
     * @param reason The reason for the failure
     */
    private void notifyLoginFailed(String reason) {
        Platform.runLater(() -> {
            // CopyOnWriteArrayList is thread-safe and doesn't throw ConcurrentModificationException
            for (ConnectionListener listener : connectionListeners) {
                try {
                    listener.onLoginFailed(reason);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Notifies all game state listeners that a game has started.
     * 
     * @param gameState The initial game state
     * @param assignedColor The color assigned to the player
     * @param opponentUsername The username of the opponent
     */
    private void notifyGameStarted(GameState gameState, PlayerColor assignedColor, String opponentUsername) {
        Platform.runLater(() -> {
            // CopyOnWriteArrayList is thread-safe and doesn't throw ConcurrentModificationException
            for (GameStateListener listener : gameStateListeners) {
                try {
                    listener.onGameStarted(gameState, assignedColor, opponentUsername);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Notifies all game state listeners that the game state was updated.
     * 
     * @param gameState The updated game state
     */
    private void notifyGameStateUpdated(GameState gameState) {
        Platform.runLater(() -> {
            // CopyOnWriteArrayList is thread-safe and doesn't throw ConcurrentModificationException
            for (GameStateListener listener : gameStateListeners) {
                try {
                    listener.onGameStateUpdated(gameState);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Notifies all game state listeners that the opponent disconnected.
     */
    private void notifyOpponentDisconnected() {
        Platform.runLater(() -> {
            // CopyOnWriteArrayList is thread-safe and doesn't throw ConcurrentModificationException
            for (GameStateListener listener : gameStateListeners) {
                try {
                    listener.onOpponentDisconnected();
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Notifies all game state listeners that the opponent declined a rematch.
     */
    private void notifyOpponentDeclinedRematch() {
        Platform.runLater(() -> {
            // CopyOnWriteArrayList is thread-safe and doesn't throw ConcurrentModificationException
            for (GameStateListener listener : gameStateListeners) {
                try {
                    listener.onOpponentDeclinedRematch();
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Notifies all chat message listeners that a chat message was received.
     * 
     * @param message The chat message
     */
    private void notifyChatMessageReceived(ChatMessage message) {
        Platform.runLater(() -> {
            // CopyOnWriteArrayList is thread-safe and doesn't throw ConcurrentModificationException
            for (ChatMessageListener listener : chatMessageListeners) {
                try {
                    listener.onChatMessageReceived(message);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Gets the current game state.
     * 
     * @return The current game state
     */
    public GameState getCurrentGameState() {
        return currentGameState;
    }
    
    /**
     * Gets the color assigned to the player.
     * 
     * @return The assigned color
     */
    public PlayerColor getAssignedColor() {
        return assignedColor;
    }
    
    /**
     * Gets the username of the opponent.
     * 
     * @return The opponent's username
     */
    public String getOpponentUsername() {
        return opponentUsername;
    }
    
    /**
     * Returns the user's username.
     * 
     * @return The username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Returns whether the client is connected to the server.
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Cancels matchmaking with the server.
     */
    public void cancelMatchmaking() {
        if (connected) {
            try {
                sendMessage(new CancelMatchmakingMessage());
            } catch (Exception e) {
                System.err.println("Error sending cancel matchmaking message: " + e.getMessage());
            }
        }
    }
} 