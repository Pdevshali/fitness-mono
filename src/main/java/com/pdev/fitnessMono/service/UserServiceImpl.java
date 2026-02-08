package com.pdev.fitnessMono.service;

import com.pdev.fitnessMono.dtos.RegisterRequest;
import com.pdev.fitnessMono.dtos.UserResponse;
import com.pdev.fitnessMono.model.User;
import com.pdev.fitnessMono.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserResponse register(RegisterRequest request) {
        User user = new User(); // we can also use builder pattern here to create user object
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
       User savedUser =  userRepository.save(user);
       return mapToUserResponse(savedUser);
    }

    private UserResponse mapToUserResponse(User savedUser) {
       UserResponse res = new UserResponse();
       res.setId(savedUser.getId());
       res.setEmail(savedUser.getEmail());
       res.setFirstName(savedUser.getFirstName());
       res.setLastName(savedUser.getLastName());
       res.setCreatedAt(savedUser.getCreatedAt());
       res.setUpdatedAt(savedUser.getUpdatedAt());
       return res;
    }
}
