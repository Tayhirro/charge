package com.bupt.charger.service;

import com.bupt.charger.entity.User;
import com.bupt.charger.service.impl.TaskServiceImpl;

import java.time.Duration;
import java.time.LocalDateTime;

public interface TaskService {
    // 首次调用是加入，再次调用是覆盖。也可以直接cancelTask取消。
    void scheduleTask(String carId, LocalDateTime scheduledTime, String message);

    // 首次调用是加入，再次调用是覆盖。也可以直接cancelTask取消。
    // Duration是一个时间段，设置的执行时间是当前.plus(duration)
    void scheduleTask(String carId, Duration duration, String message);

    void cancelTask(String carId);

}
