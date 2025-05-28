package com.bupt.charger.service.impl;

import com.bupt.charger.util.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class WsServiceImpl implements com.bupt.charger.service.WsService {

    @Autowired
    public WebSocketServer webSocketServer;

    @Override
    @Async
    public void sendToUser(String username, String message) {
       webSocketServer.sendToUser(username, message);
    }

    @Override
    @Async
    public void sendInfo(String message) {
        webSocketServer.sendInfo(message);
    }
}
