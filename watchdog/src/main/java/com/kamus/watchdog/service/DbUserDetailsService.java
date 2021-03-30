package com.kamus.watchdog.service;

import com.google.common.base.Preconditions;
import com.kamus.core.db.User;
import com.kamus.watchdog.db.repository.UsersRepository;
import com.kamus.watchdog.service.exception.UserAlreadyExistsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
public class DbUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public DbUserDetailsService(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = usersRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return new DbUserPrincipal(user);
    }

    @Transactional
    public User signInUser(String username, String password) {
        User user = new User(username, passwordEncoder.encode(password));

        if (usernameExists(user.getUsername())) {
            throw new UserAlreadyExistsException(user.getUsername());
        }

        return usersRepository.save(user);
    }

    public Optional<User> findUser(String username) {
        return usersRepository.findByUsername(username);
    }

    public boolean usernameExists(String username) {
        return usersRepository.existsByUsername(username);
    }

    public static class DbUserPrincipal implements UserDetails {

        private final User user;

        public DbUserPrincipal(User user) {
            Preconditions.checkNotNull(user);

            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.emptyList();
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

}
