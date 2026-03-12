package com.pdev.fitnessMono.service;

import com.pdev.fitnessMono.dtos.LoginRequest;
import com.pdev.fitnessMono.dtos.RegisterRequest;
import com.pdev.fitnessMono.dtos.UserResponse;
import com.pdev.fitnessMono.model.FitnessLevel;
import com.pdev.fitnessMono.model.User;
import com.pdev.fitnessMono.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.pdev.fitnessMono.model.FitnessGoal.GENERAL_FITNESS;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(RegisterRequest request) {
        User user = new User(); // we can also use builder pattern here to create user object
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        // Set optional fitness profile fields
        user.setWeight(request.getWeight());
        user.setHeight(request.getHeight());
        user.setFitnessLevel(request.getFitnessLevel() != null ? request.getFitnessLevel() : FitnessLevel.BEGINNER);
        user.setPrimaryGoal(request.getPrimaryGoal() != null ? request.getPrimaryGoal() : GENERAL_FITNESS);
        
       User savedUser =  userRepository.save(user);
       return mapToUserResponse(savedUser);
    }

    @Override
    public UserResponse mapToUserResponse(User savedUser) {
       UserResponse res = new UserResponse();
       res.setId(savedUser.getId());
       res.setEmail(savedUser.getEmail());
       res.setFirstName(savedUser.getFirstName());
       res.setLastName(savedUser.getLastName());
       
       // Set fitness profile fields
       res.setWeight(savedUser.getWeight());
       res.setHeight(savedUser.getHeight());
       res.setFitnessLevel(savedUser.getFitnessLevel());
       res.setPrimaryGoal(savedUser.getPrimaryGoal());
       
       res.setCreatedAt(savedUser.getCreatedAt());
       res.setUpdatedAt(savedUser.getUpdatedAt());
       return res;
    }

    @Override
    public User authenticate(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail());
        if(user == null) {
            log.info("User not found with email {}", loginRequest.getEmail());
            throw new RuntimeException("Invalid email or password");
        }

        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            log.info("Invalid password for email {}", loginRequest.getEmail());
            throw new RuntimeException("Invalid email or password");
        }
        return user;
    }
}
