package com.pdev.fitnessMono.service;

import com.pdev.fitnessMono.dtos.RegisterRequest;
import com.pdev.fitnessMono.dtos.UserResponse;

public interface UserService {

    UserResponse register(RegisterRequest request);
}
