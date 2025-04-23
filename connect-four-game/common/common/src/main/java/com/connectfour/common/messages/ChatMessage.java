package com.connectfour.common.messages;

/**
 * Message sent between client and server for chat functionality.
 */
public class ChatMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private final String sender;
    private final String content;
    
    public ChatMessage(String sender, String content) {
        super(MessageType.CHAT_MESSAGE);
        this.sender = sender;
        this.content = content;
    }
    
    public String getSender() {
        return sender;
    }
    
    public String getContent() {
        return content;
    }
} 