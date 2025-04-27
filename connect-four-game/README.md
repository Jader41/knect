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
- Somber background music during gameplay

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

First, build the common module (must be done first):
```
cd /path/to/connect-four-game/common/common
mvn clean install
```

Then build the server and client modules individually.

## Running the Project

### Starting the Server

Use Maven's JavaFX plugin to run the server:

```
cd /path/to/connect-four-game/server/server
mvn javafx:run
```

Example:
```
cd /Users/wale/5/connect-four-game/server/server && mvn javafx:run
```

By default, the server runs on port 8080.

### Starting the Client

Use Maven's JavaFX plugin to run the client:

```
cd /path/to/connect-four-game/client/client
mvn javafx:run
```

Example:
```
cd /Users/wale/5/connect-four-game/client/client && mvn javafx:run
```

**Note:** Make sure the server is running before starting the client.

### Running Multiple Client Instances

To test multiplayer functionality, you can run multiple client instances in separate terminal windows using the same command.

## How to Play

### Online Multiplayer

1. Start the server
2. Start at least two client instances
3. Enter a username in each client
4. Click "Play Online" to connect to the server
5. The server will match players automatically
6. Take turns dropping discs into the columns
7. Connect four discs of your color horizontally, vertically, or diagonally to win
8. Use the chat feature to communicate with your opponent
9. When a game ends, use the "Play Again" button to start a new game or "Return to Lobby" to go back to matchmaking

### Playing Against Computer (AI)

1. Start a client and enter a username
2. Click "Play with Computer"
3. Select a difficulty level (Easy, Medium, or Hard)
4. The game will start with you as the Red player and the AI as the Yellow player
5. Take turns making moves
6. After the game ends, you can use the "Play Again" button to start a new game with the AI or "Return to Lobby" to go back to the main screen

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

- **Port already in use error**: If you receive a "Address already in use" error when starting the server, use the command `pkill -f java` to kill all running Java processes and try again.
- **Client crashes**: If the client crashes with a "ConcurrentModificationException", ensure you're running the latest version by rebuilding the common module first with `mvn clean install`.
- **Cannot find symbol errors**: If you encounter compilation errors about missing classes or symbols, make sure you've built the common module first using `mvn clean install` before building the server or client.
