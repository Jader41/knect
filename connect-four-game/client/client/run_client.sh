#!/bin/bash
java --module-path "${JAVAFX_HOME}/lib" --add-modules javafx.controls,javafx.fxml -jar target/client-1.0-SNAPSHOT.jar "$@" 