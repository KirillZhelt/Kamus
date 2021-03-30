package com.kamus.watchdog.http;

import com.kamus.core.db.User;
import com.kamus.watchdog.service.DbUserDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sign-in")
public class SignInController {

    private final DbUserDetailsService userService;

    public SignInController(DbUserDetailsService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> signIn(@RequestParam String username, @RequestParam String password) {
        return new ResponseEntity<>(userService.signInUser(username, password), HttpStatus.CREATED);
    }

    @GetMapping("/username-exists")
    public boolean usernameExists(@RequestParam String username) {
        return userService.usernameExists(username);
    }

}
