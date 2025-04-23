package com.connectfour.common.model;

import java.io.Serializable;

/**
 * Represents the state of a cell in the Connect Four board.
 */
public enum CellState implements Serializable {
    EMPTY,
    RED,
    YELLOW
} 