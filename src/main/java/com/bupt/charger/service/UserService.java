package com.bupt.charger.service;

import com.bupt.charger.dto.request.UserRegistrationRequest;
import com.bupt.charger.dto.response.UserLoginResponse;

import javax.security.auth.login.LoginException;

public interface UserService {

    // 用户注册
    void registerUser(UserRegistrationRequest registrationRequest) throws ApiException;

    // 用户登录
    UserLoginResponse login(String username, String password) throws LoginException;
}
