package com.bupt.charger.controller;

import com.bupt.charger.dto.ApiResp;
import com.bupt.charger.service.CarService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户车辆信息查看
 * 包括查看车辆排队状态、查看充电状态
 */
@RestController
@Tag(name = "车辆信息")
@RequestMapping("/user")
public class UserCarController {

    @Autowired
    CarService carService;

    @GetMapping("/checkWaitQueue")
    public ResponseEntity<?> checkWaitQueue(@RequestParam("car_id") String carId) {
        try {
            var resp = carService.getCarStatus(carId);
            return ResponseEntity.ok().body(new ApiResp(resp));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }

    @GetMapping("/checkStatus")
    public ResponseEntity<?> checkStatus(@RequestParam("car_id") String carId) {
        try {
            var resp = carService.getCarCharging(carId);
            return ResponseEntity.ok().body(new ApiResp(resp));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }

}
