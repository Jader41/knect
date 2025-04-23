package com.connectfour.common.messages;

/**
 * Message sent from server to client in response to a login request.
 */
public class LoginResponseMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private final boolean success;
    private final String errorMessage;
    
    public LoginResponseMessage(boolean success, String errorMessage) {
        super(MessageType.LOGIN_RESPONSE);
        this.success = success;
        this.errorMessage = errorMessage;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
} 