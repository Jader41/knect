# Connect Four Game

A network-based implementation of the classic Connect Four game with a JavaFX GUI.

## Features

- Client-server architecture for online multiplayer
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
mvn clean install
```

Build the client:
```
cd ../../client/client
mvn clean install
```

## Running the Project

### Starting the Server

Navigate to the server directory and run:

```
cd server/server
mvn exec:java -Dexec.mainClass="com.connectfour.server.ServerApp"
```

Alternatively, you can run the packaged JAR:

```
java -jar target/server-1.0-SNAPSHOT.jar
```

By default, the server runs on port 8080. You can specify a different port by passing it as a command-line argument:

```
java -jar target/server-1.0-SNAPSHOT.jar 9090
```

### Starting the Client

Navigate to the client directory and run:

```
cd client/client
mvn javafx:run
```

Alternatively, you can run the packaged JAR:

```
java -jar target/client-1.0-SNAPSHOT.jar
```

By default, the client connects to localhost:8080. You can specify a different host and port by passing parameters:

```
java -jar target/client-1.0-SNAPSHOT.jar --host=example.com --port=9090
```

## How to Play

1. Start the server
2. Start at least two client instances
3. Enter a username in each client
4. The server will match players automatically
5. Take turns dropping discs into the columns
6. Connect four discs of your color horizontally, vertically, or diagonally to win
7. Use the chat feature to communicate with your opponent
8. Choose to play again or quit after a game ends

## Game Rules

- The game is played on a 7x6 grid
- Players take turns dropping colored discs into columns
- The disc falls to the lowest available position in the selected column
- The first player to form a horizontal, vertical, or diagonal line of four discs wins
- If the grid is filled completely without a winner, the game is a draw

## Technology Stack

- Java 17
- JavaFX for GUI
- Maven for build management
- SLF4J and Logback for logging
- Java Sockets for network communication
- JUnit for testing 