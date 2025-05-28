package com.bupt.charger.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 配置全局参数，主要是时间加速倍速
 */
@Configuration
public class AppConfig {
    // 时间加速倍速
    @Value("${app.time-scale-factor:60}")
    public int TIME_SCALE_FACTOR;

    // 快充充电桩数量
    @Value("${app.fast-pile-num:3}")
    public int FAST_PILE_NUM;

    // 慢充充电桩数量
    @Value("${app.slow-pile-num:3}")
    public int SLOW_PILE_NUM;

    // 等待区容量
    @Value("${app.waiting-area-capacity:6}")
    public int WAITING_AREA_CAPACITY;

    // 充电桩队列长度
    @Value("${app.pile-queue-length:2}")
    public int PILE_QUEUE_LENGTH;

    // 调度算法
    @Value("${app.schedule-type:BASIC}")
    public String SCHEDULE_TYPE;

    // 故障调度 TIME或  PRIORITY
    @Value("${app.error-schedule-type:PRIORITY}")
    public String ERROR_SCHEDULE_TYPE;
}