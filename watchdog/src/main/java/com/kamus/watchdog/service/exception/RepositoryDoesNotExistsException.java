package com.kamus.watchdog.service.exception;

import com.kamus.watchdog.http.model.RepositoryDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Cannot find given repository")
public class RepositoryDoesNotExistsException extends RuntimeException {

    public RepositoryDoesNotExistsException(RepositoryDto repository) {
        super(String.format("Given repository doesn't exists: %s", repository));
    }

}
