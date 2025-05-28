package com.bupt.charger.controller;

import com.bupt.charger.dto.ApiResp;
import com.bupt.charger.service.ApiException;
import com.bupt.charger.dto.request.CarRequest;
import com.bupt.charger.dto.request.ChargeReqRequest;
import com.bupt.charger.dto.request.ModifyChargeAmountRequest;
import com.bupt.charger.dto.request.ModifyChargeModeRequest;
import com.bupt.charger.dto.response.Resp;
import com.bupt.charger.service.ChargeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户充电管理
 * 包括充电请求、修改充电金额、修改充电模式、开始充电、停止充电
 */
@RestController
@Tag(name = "充电请求管理")
@RequestMapping("/user")
public class UserChargeController {

    @Autowired
    private ChargeService chargeService;

    @PostMapping("/request")
    public ResponseEntity<Object> reqCharge(@RequestBody ChargeReqRequest chargeReqRequest) {
        try {
            Resp resp = chargeService.chargeRequest(chargeReqRequest);
            return ResponseEntity.ok().body(new ApiResp(resp));
        } catch (ApiException e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }

    @PostMapping("modifyAmount")
    public ResponseEntity<Object> modifyAmount(@RequestBody ModifyChargeAmountRequest request) {
        try {
            chargeService.ModifyRequestAmount(request);
            return ResponseEntity.ok().body(new ApiResp(0, "请求成功"));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }

    @PostMapping("chMode")
    public ResponseEntity<Object> modifyMode(@RequestBody ModifyChargeModeRequest request) {
        try {
            chargeService.ModifyRequestMode(request);
            return ResponseEntity.ok().body(new ApiResp(0, "请求成功"));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }

    @PostMapping("/startCharging")
    public ResponseEntity<Object> startCharging(@RequestBody CarRequest request) {
        String carId = request.getCarId();
        try {chargeService.startCharging(carId);
            return ResponseEntity.ok().body(new ApiResp(0, "请求成功"));
        } catch (ApiException e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }

    @PostMapping("/stopCharging")
    public ResponseEntity<Object> stopCharging(@RequestBody CarRequest request) {
        String carId = request.getCarId();
        try {
            chargeService.stopCharging(carId);
            return ResponseEntity.ok().body(new ApiResp(0, "请求成功"));
        } catch (ApiException e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }

}