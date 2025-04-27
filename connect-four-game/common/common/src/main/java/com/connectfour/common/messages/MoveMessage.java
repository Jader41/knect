package com.connectfour.common.messages;

// 
// Message sent from client to server when a player makes a move.
public class MoveMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private final int column;
    
    public MoveMessage(int column) {
        super(MessageType.MOVE);
        this.column = column;
    }
    
    public int getColumn() {
        return column;
    }
} 
