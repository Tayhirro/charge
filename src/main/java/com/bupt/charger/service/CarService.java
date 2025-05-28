package com.bupt.charger.service;

import com.bupt.charger.dto.response.CarChargingResponse;
import com.bupt.charger.dto.response.CarStatusResponse;

public interface CarService {
    // 更新充电完成量
    double updateDoneAmount(String carId);

    // 获取车辆状态
    CarStatusResponse getCarStatus(String carId);

    // 获取车辆充电信息
    CarChargingResponse getCarCharging(String carId);
}

