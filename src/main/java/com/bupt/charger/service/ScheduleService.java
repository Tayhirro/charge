package com.bupt.charger.service;

import com.bupt.charger.entity.Car;
import com.bupt.charger.entity.ChargeRequest;
import com.bupt.charger.entity.Pile;

import java.util.List;

public interface ScheduleService {
    // 添加到等候区队列，返回分配的号码（用于新车到来
    String moveToWaitingQueue(Car car);

    // 获得有空余的充电队列
    List<Pile> getChargingNotFullQueue(Pile.Mode mode);

    // 提醒车辆开始充电函数（充电队列出现空余
    boolean remindCarStartCharge(String pileId);

    // 尝试调度
    void trySchedule();

    // 将指定车辆从等候区移除（用于取消充电和修改充电模式）
    void removeFromWaitingQueue(String carId, ChargeRequest.RequestMode oldMode);

    // 故障调度的统一接口，在配置文件中选择
    void errorMoveQueue(String pileId);
}
