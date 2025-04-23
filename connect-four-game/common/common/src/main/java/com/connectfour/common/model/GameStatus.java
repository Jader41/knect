package com.connectfour.common.model;

import java.io.Serializable;

/**
 * Represents the status of a Connect Four game.
 */
public enum GameStatus implements Serializable {
    IN_PROGRESS,
    RED_WINS,
    YELLOW_WINS,
    DRAW
} 