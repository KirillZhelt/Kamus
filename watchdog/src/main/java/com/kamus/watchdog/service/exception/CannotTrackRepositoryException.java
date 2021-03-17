package com.kamus.watchdog.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Wasn't able to add a repository")
public class CannotTrackRepositoryException extends RuntimeException {

    public CannotTrackRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

}
