package com.bupt.charger.service;

import com.bupt.charger.dto.request.*;
import com.bupt.charger.dto.response.AdminLoginResponse;
import com.bupt.charger.dto.response.CarResponse;
import com.bupt.charger.dto.response.CheckChargerResponse;

import javax.security.auth.login.LoginException;
import java.util.List;

public interface AdminService {

    // 登录
    AdminLoginResponse login(String adminName, String password) throws LoginException;

    // 启动充电桩
    void startPile(StartPileRequest startPileRequest) throws ApiException;

    // 关闭充电桩
    void shutDownPile(ShutDownPileRequest shutDownPileRequest) throws ApiException;

    // 设置充电桩参数
    void setPileParameters(SetPileParametersRequest setPileParametersRequest) throws ApiException;

    // 查看充电桩状态
    CheckChargerResponse checkCharger(String pileId);

    // 查看充电桩队列
    List<CarResponse> checkChargerQueue(String pileId);

    // 充电桩故障
    void diePile(DiePileRequest diePileRequest) throws ApiException;
}
