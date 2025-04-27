package com.connectfour.common.messages;

import java.io.Serializable;

// 
// Enum defining the different types of messages that can be exchanged.
public enum MessageType implements Serializable {
    LOGIN_REQUEST,
    LOGIN_RESPONSE,
    GAME_START,
    MOVE,
    GAME_STATE_UPDATE,
    CHAT_MESSAGE,
    PLAY_AGAIN_REQUEST,
    PLAY_AGAIN_RESPONSE,
    DISCONNECT,
    CANCEL_MATCHMAKING,
    RETURN_TO_LOBBY
} 
