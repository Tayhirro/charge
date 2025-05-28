package com.bupt.charger.service.impl;

import com.bupt.charger.entity.Car;
import com.bupt.charger.repository.CarRepository;
import com.bupt.charger.dto.response.UserLoginResponse;
import com.bupt.charger.entity.User;
import com.bupt.charger.service.ApiException;
import com.bupt.charger.repository.UserRepository;
import com.bupt.charger.dto.request.UserRegistrationRequest;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;

/**
 * @author ll （ created: 2023-05-26 19:38 )
 */
@Service
@Log
public class UserServiceImpl implements com.bupt.charger.service.UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRepository carRepository;

    @Override
    public void registerUser(UserRegistrationRequest registrationRequest) throws ApiException {
        log.info("User: register request" + registrationRequest);
        if (userRepository.existsByUsername(registrationRequest.getUsername())) {
            throw new ApiException("用户名已存在");
        }
        if (carRepository.existsByCarId(registrationRequest.getCarId())) {
            throw new ApiException("车已被其他用户绑定");
        }

        User user = new User();
        user.setUsername(registrationRequest.getUsername());
        user.setPassword(registrationRequest.getPassword());
        user.setCarId(registrationRequest.getCarId());

        Car car = new Car();
        car.setCarId(registrationRequest.getCarId());

        userRepository.save(user);
        carRepository.save(car);
    }

    @Override
    public UserLoginResponse login(String username, String password) throws LoginException {
        log.info("User try to login:" + username);
        User user = userRepository.findByUsernameAndPassword(username, password);
        if (user == null) {
            throw new LoginException("用户名或密码错误");
        }

        UserLoginResponse loginResponse = new UserLoginResponse();
        loginResponse.setUsername(user.getUsername());
        loginResponse.setCarId(user.getCarId());

        return loginResponse;
    }
}
