package com.connectfour.client.ui;

import com.connectfour.client.util.Leaderboard;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LeaderboardDialog {
    public static void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Leaderboard – Top 5 Players");

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #F7F5F2;");

        Label header = new Label("All-Time Wins");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        root.getChildren().add(header);

        java.util.List<String> top = Leaderboard.getTopPlayers(5);
        if (top.isEmpty()) {
            Label none = new Label("No wins recorded yet. Play some games!");
            none.setStyle("-fx-font-size: 16px;");
            root.getChildren().add(none);
        } else {
            top.forEach(entry -> {
                Label lbl = new Label(entry);
                lbl.setStyle("-fx-font-size: 16px;");
                root.getChildren().add(lbl);
            });
        }

        Button close = new Button("Close");
        close.setOnAction(e -> dialog.close());
        root.getChildren().add(close);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
} 