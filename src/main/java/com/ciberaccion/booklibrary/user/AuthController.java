package com.ciberaccion.booklibrary.user;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @MutationMapping
    public AuthPayload register(@Argument RegisterInput input) {
        return authService.register(input);
    }

    @MutationMapping
    public AuthPayload login(@Argument LoginInput input) {
        return authService.login(input);
    }
}