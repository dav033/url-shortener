package io.github.dav033.user_service.controllers;


import io.github.dav033.user_service.dto.UserResponse;
import io.github.dav033.user_service.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public List<UserResponse> changePassword() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(user.getName(), user.getEmail()))
                .toList();
    }
}
