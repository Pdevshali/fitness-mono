package com.pdev.fitnessMono.service;

import com.pdev.fitnessMono.dtos.LoginRequest;
import com.pdev.fitnessMono.dtos.RegisterRequest;
import com.pdev.fitnessMono.dtos.UserResponse;
import com.pdev.fitnessMono.model.User;

public interface UserService {

    UserResponse register(RegisterRequest request);

    UserResponse mapToUserResponse(User savedUser);

    User authenticate(LoginRequest loginRequest);
}
