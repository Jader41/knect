#!/bin/bash

# Kill any existing Java processes to avoid port conflicts
pkill -9 -f "java" || true
echo "Terminated any existing Java processes"
sleep 1

# Go to the correct server directory
cd /Users/wale/5/connect-four-game/server/server

echo "Starting Connect Four Server..."
echo "-------------------------------"

# Run the server with JavaFX explicitly using the ServerLauncher class
mvn javafx:run -Djavafx.mainClass=com.connectfour.server.ServerLauncher

# Keep the terminal open if there was an error
if [ $? -ne 0 ]; then
    echo "Server failed to start. See error messages above."
    echo "Press Enter to close this window..."
    read
fi 