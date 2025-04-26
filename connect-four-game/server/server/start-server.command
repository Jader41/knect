#!/bin/bash

# Move to the directory containing this script
cd "$(dirname "$0")"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not found in PATH"
    echo "Please install Java (JDK 17 or later) and try again"
    exit 1
fi

# Run the server application
echo "Starting Connect Four Server Application..."
java -jar target/server-1.0-SNAPSHOT.jar

# Keep the terminal window open if there was an error
if [ $? -ne 0 ]; then
    echo "Server failed to start. See error messages above."
    echo "Press Enter to close this window..."
    read
fi 