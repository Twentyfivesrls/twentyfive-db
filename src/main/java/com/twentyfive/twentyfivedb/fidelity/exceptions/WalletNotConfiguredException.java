package com.twentyfive.twentyfivedb.fidelity.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
public class WalletNotConfiguredException extends RuntimeException {
    public WalletNotConfiguredException(String message) {
        super(message);
    }
}
