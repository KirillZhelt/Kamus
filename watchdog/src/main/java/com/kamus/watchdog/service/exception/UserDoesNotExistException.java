package com.kamus.watchdog.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "User with such username does not exist")
public class UserDoesNotExistException extends RuntimeException {

    public UserDoesNotExistException(String username) {
        super("User with username '" + username + "' does not exist");
    }

}
