package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.repositories.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var dbUser = userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new EntityNotFoundException("User with name %s not found".formatted(username))
                );

        var roles = getRoles(dbUser);

        return new User(
                dbUser.getUsername(),
                dbUser.getPassword(),
                roles
        );
    }

    private List<SimpleGrantedAuthority> getRoles(ru.otus.hw.models.User dbUser) {
        return dbUser.getRoles().stream()
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
