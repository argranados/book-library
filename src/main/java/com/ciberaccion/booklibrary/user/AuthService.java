package com.ciberaccion.booklibrary.user;

import com.ciberaccion.booklibrary.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthPayload register(RegisterInput input) {
        if (userRepository.findByUsername(input.username()).isPresent()) {
            throw new RuntimeException("Username already taken: " + input.username());
        }

        User user = User.builder()
                .username(input.username())
                .password(passwordEncoder.encode(input.password())) // nunca guardar plain text
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthPayload(token, user.getUsername());
    }

    public AuthPayload login(LoginInput input) {
        // authenticate() verifica username + password contra DB automáticamente
        // lanza AuthenticationException si son incorrectos
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.username(), input.password()));

        User user = userRepository.findByUsername(input.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user);
        return new AuthPayload(token, user.getUsername());
    }
}