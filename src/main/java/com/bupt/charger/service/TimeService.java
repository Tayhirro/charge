package com.bupt.charger.service;

import com.bupt.charger.dto.response.GetTimeNowResponse;

public interface TimeService {
    // 获取当前时间
    GetTimeNowResponse getTimeNow() throws ApiException;
}
