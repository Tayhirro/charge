package com.bupt.charger.service;

import com.bupt.charger.entity.Car;
import com.bupt.charger.entity.ChargeRequest;
import com.bupt.charger.entity.Pile;

import java.util.List;

public interface ScheduleService {
    // 添加到等候区队列，返回分配的号码
    String moveToWaitingQueue(Car car);

    // 获得有空余的充电队列
    List<Pile> getChargingNotFullQueue(Pile.Mode mode);

    // 提醒车辆开始充电函数
    boolean remindCarStartCharge(String pileId);

    // 进入充电区f
    void moveToChargingQueue();

    // 将指定车辆从等候区移除
    void removeFromWaitingQueue(String carId, ChargeRequest.RequestMode oldMode);

    // 基本调度策略
    String basicSchedule(List<Pile> piles, Pile.Mode mode);

    // 这个是故障机制中，故障充电桩车辆停止充电
    void errorStopCharging(String carId);

    // 故障调度的统一接口，在配置文件中选择
    void errorMoveQueue(String pileId);
}
