package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.User;
import ru.otus.hw.repositories.UserRepository;
import ru.otus.hw.util.MessageProvider;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final MessageProvider messageProvider;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("user.not_found.username", username)
                ));

        System.out.println("=== LOADING USER DETAILS ===");
        System.out.println("Username: " + username);
        System.out.println("User roles from DB: " + user.getRoles());

        // ДОБАВЛЯЕМ ПРЕФИКС "ROLE_" для Spring Security
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> {
                    String authority = "ROLE_" + role;
                    System.out.println("Converting role: " + role + " -> " + authority);
                    return new SimpleGrantedAuthority(authority);
                })
                .collect(Collectors.toList());

        System.out.println("Granted authorities for Spring Security: " + authorities);

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}