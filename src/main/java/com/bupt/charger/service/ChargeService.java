package com.bupt.charger.service;

import com.bupt.charger.dto.request.ChargeReqRequest;
import com.bupt.charger.dto.request.ModifyChargeAmountRequest;
import com.bupt.charger.dto.request.ModifyChargeModeRequest;
import com.bupt.charger.dto.response.ChargeReqResponse;
import com.bupt.charger.entity.ChargingQueue;

public interface ChargeService {

    // 用户充电请求
    ChargeReqResponse chargeRequest(ChargeReqRequest chargeReqRequest);

    // 修改充电金额
    void ModifyRequestAmount(ModifyChargeAmountRequest request);

    // 修改充电模式
    void ModifyRequestMode(ModifyChargeModeRequest request);

    // 开始充电
    void startCharging(String carId);

    // 停止充电
    void stopCharging(String carId);
}
