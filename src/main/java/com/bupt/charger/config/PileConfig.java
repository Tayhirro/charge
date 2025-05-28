package com.bupt.charger.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 配置充电桩参数
 */
@Configuration
@Data
public class PileConfig {
    //快充功率（度/小时）
    public final double FAST_POWER = 30;
    //慢充功率（度/小时）
    public final double SLOW_POWER = 7;
}
