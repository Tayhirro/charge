package com.bupt.charger.service;

import org.springframework.scheduling.annotation.Async;

public interface WsService {

    // WebSocket服务接口，发送消息给特定用户
    @Async
    void sendToUser(String username, String message);

    // 发送给所有用户
    @Async
    void sendInfo(String message);
}
