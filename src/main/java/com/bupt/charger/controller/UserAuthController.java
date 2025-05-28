package com.bupt.charger.controller;

import com.bupt.charger.dto.ApiResp;
import com.bupt.charger.dto.request.*;
import com.bupt.charger.dto.response.UserLoginResponse;
import com.bupt.charger.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "用户认证")
@RequestMapping("/")
public class UserAuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "用户注册")
    public ResponseEntity<Object> signup(@RequestBody UserRegistrationRequest registrationRequest) {
        try {
            userService.registerUser(registrationRequest);
            return ResponseEntity.ok().body(new ApiResp(0, "注册成功"));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ResponseEntity<Object> login(@RequestBody LoginRequest loginRequest) {
        try {
            UserLoginResponse loginResponse = userService.login(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );
            return ResponseEntity.ok().body(new ApiResp(0, "登录成功", loginResponse));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }
}