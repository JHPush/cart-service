package com.inkcloud.cart_service.exception;

public class InvalidCartQuantityException extends RuntimeException {

    public InvalidCartQuantityException(String message) {
        super(message);
    }
    
}