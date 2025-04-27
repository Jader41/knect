package com.connectfour.common.messages;

// 
// Message sent from client to server to request login with a username.
public class LoginRequestMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private final String username;
    
    public LoginRequestMessage(String username) {
        super(MessageType.LOGIN_REQUEST);
        this.username = username;
    }
    
    public String getUsername() {
        return username;
    }
} 
