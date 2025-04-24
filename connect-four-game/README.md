# Connect Four Game

A network-based implementation of the classic Connect Four game with a JavaFX GUI.

## Features

- Client-server architecture for online multiplayer
- Single-player mode against AI opponent with three difficulty levels
- Intuitive graphical user interface
- Text chat functionality
- Username creation at login
- Matchmaking system
- Play again functionality after a game ends
- Win/loss/draw detection
- Graceful handling of disconnections

## Project Structure

The project consists of three Maven modules:

1. **common**: Contains shared code used by both client and server, including:
   - Game state model
   - Communication protocol
   - Message classes

2. **client**: The client application with JavaFX GUI

3. **server**: The server application that handles client connections and game logic

## Building the Project

### Prerequisites

- Java Development Kit (JDK) 17 or later
- Maven

### Build Steps

1. Clone the repository
2. Build the project with Maven:

```
cd connect-four-game
```

Build the common module first:
```
cd common/common
mvn clean install
```

Build the server:
```
cd ../../server/server
mvn clean package
```

Build the client:
```
cd ../../client/client
mvn clean package
```

## Running the Project

### Starting the Server

Navigate to the server directory and run:

```
cd server/server
java -jar target/server-1.0-SNAPSHOT.jar
```

By default, the server runs on port 8080. You can specify a different port by passing it as a command-line argument:

```
java -jar target/server-1.0-SNAPSHOT.jar 9090
```

### Starting the Client

For macOS/Linux, navigate to the client directory and run:

```
cd client/client
mvn javafx:run
```

For Windows:
```
cd client\client
mvn javafx:run
```

**Note:** Make sure the server is running before starting the client.

## How to Play

### Online Multiplayer

1. Start the server
2. Start at least two client instances
3. Enter a username in each client
4. The server will match players automatically
5. Take turns dropping discs into the columns
6. Connect four discs of your color horizontally, vertically, or diagonally to win
7. Use the chat feature to communicate with your opponent
8. When a game ends, use the "Play Again" button to start a new game or "Return to Lobby" to go back to matchmaking

### Playing Against Computer (AI)

1. Start the server (required for authentication)
2. Start a client and enter a username
3. In the waiting screen, click "Play vs Computer"
4. Select a difficulty level (Easy, Medium, or Hard)
5. The game will start with you as the Red player and the AI as the Yellow player
6. Take turns making moves
7. After the game ends, you can use the "Play Again" button to start a new game with the AI or "Return to Lobby" to go back to matchmaking

## Game Rules

- The game is played on a 7x6 grid
- Players take turns dropping colored discs into columns
- The disc falls to the lowest available position in the selected column
- The first player to form a horizontal, vertical, or diagonal line of four discs wins
- If the grid is filled completely without a winner, the game is a draw

## Game Controls

- Click on a column to drop your disc during your turn
- Use the chat box at the bottom to send messages in online matches
- "Play Again" button appears after a game ends to start a new game
- "Return to Lobby" button allows you to exit the game and return to matchmaking

## Troubleshooting

- **Port already in use error**: If you receive a "Address already in use" error when starting the server, use the command `pkill -9 -f "java"` to kill all running Java processes and try again.
- **Client crashes**: If the client crashes with a "ConcurrentModificationException", ensure you're running the latest version by rebuilding with `mvn clean package`.
- **JavaFX runtime missing**: If you see an error about missing JavaFX runtime components, use `mvn javafx:run` instead of directly running the JAR file.

## Technology Stack

- Java 17
- JavaFX for GUI
- Maven for build management
- SLF4J and Logback for logging
- Java Sockets for network communication
- JUnit for testing 