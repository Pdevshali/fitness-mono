package com.pdev.fitnessMono.controller;

import com.pdev.fitnessMono.dtos.LoginRequest;
import com.pdev.fitnessMono.dtos.LoginResponse;
import com.pdev.fitnessMono.dtos.RegisterRequest;
import com.pdev.fitnessMono.dtos.UserResponse;
import com.pdev.fitnessMono.model.User;
import com.pdev.fitnessMono.security.JwtUtils;
import com.pdev.fitnessMono.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    //    @Autowired
//    private UserService userService;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        // Implement registration logic here
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try{
            User user = userService.authenticate(loginRequest);
            String token = jwtUtils.generateToken(user.getId(), user.getRole().name());
            return ResponseEntity.ok(new LoginResponse(
                    token, userService.mapToUserResponse(user)
            ));
        }catch (AuthenticationException e){
            e.printStackTrace();
            return ResponseEntity.status(401).build();
        }
    }
}
