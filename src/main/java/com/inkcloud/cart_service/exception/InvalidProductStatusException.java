package com.inkcloud.cart_service.exception;

public class InvalidProductStatusException extends RuntimeException {

    public InvalidProductStatusException(String message) {
        super(message);
    }
    
}
