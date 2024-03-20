package com.twentyfive.twentyfivedb.fidelity.exceptions;

public class ExpiredCard extends RuntimeException{
    public ExpiredCard(String message) {
        super(message);
    }
}
