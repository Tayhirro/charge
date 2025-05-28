package com.bupt.charger.service.impl;

import com.bupt.charger.config.AppConfig;
import com.bupt.charger.dto.request.*;
import com.bupt.charger.dto.response.*;
import com.bupt.charger.entity.Admin;
import com.bupt.charger.entity.Car;
import com.bupt.charger.entity.ChargeRequest;
import com.bupt.charger.entity.Pile;
import com.bupt.charger.service.ApiException;
import com.bupt.charger.repository.AdminRepository;
import com.bupt.charger.repository.CarRepository;
import com.bupt.charger.repository.ChargeReqRepository;
import com.bupt.charger.repository.PilesRepository;
import com.bupt.charger.service.ScheduleService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author wyf （ created: 2023-05-26 13:27 )
 */
@Service
@Log
public class AdminServiceImpl implements com.bupt.charger.service.AdminService {
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ChargeReqRepository chargeReqRepository;

    @Override
    public AdminLoginResponse login(String adminName, String password) throws LoginException {
        log.info("Admin try to login: " + adminName);
        Admin admin = adminRepository.findByAdminNameAndPassword(adminName, password);
        if (admin == null) {
            throw new LoginException("用户名或密码错误");
        }

        AdminLoginResponse loginResponse = new AdminLoginResponse();
        loginResponse.setAdminName(admin.getAdminName());

        return loginResponse;
    }

    @Autowired
    private PilesRepository pilesRepository;

    @Autowired
    private AppConfig appConfig;

    @Override
    public void startPile(StartPileRequest startPileRequest) throws ApiException {
        log.info("Admin try to start pile: " + startPileRequest.getPileId());
        var pileId = startPileRequest.getPileId();
        Pile pile = pilesRepository.findByPile(pileId);
        if (pile == null) {
            throw new ApiException("不存在这个充电桩！");
        }
        if (pile.isON()) {
            throw new ApiException("充电桩已经是开启状态");
        }
        pile.setStatus(Pile.Status.FREE);
        // 有几个空位就调度几次，防止有空余的没有被调度
        for (int i = 0; i < pile.getCapacity(); i++) {
            scheduleService.trySchedule();
        }
        pilesRepository.save(pile);

    }

    @Override
    public void shutDownPile(ShutDownPileRequest shutDownPileRequest) throws ApiException {
        if(!appConfig.SCHEDULE_TYPE.equals("BASIC")){
            throw new ApiException("不是普通调度，禁止关闭充电桩");
        }
        log.info("Admin try to shut down pile: " + shutDownPileRequest.getPileId());
        var pileId = shutDownPileRequest.getPileId();
        Pile pile = pilesRepository.findByPile(pileId);
        if (pile == null) {
            throw new ApiException("不存在这个充电桩！");
        }

        if (!pile.isON()) {
            throw new ApiException("充电桩并没有开");
        }

        if (pile.getStatus() == Pile.Status.CHARGING) {
            throw new ApiException("充电桩正在充电，请等待用户充电完成");
        }

        pile.setStatus(Pile.Status.OFF);

        pilesRepository.save(pile);

    }


    @Override
    public void setPileParameters(SetPileParametersRequest setPileParametersRequest) throws ApiException {
        log.info("Admin try to set pile parameters: " + setPileParametersRequest.getPileId());
        var pileId = setPileParametersRequest.getPileId();
        Pile pile = pilesRepository.findByPile(pileId);
        if (pile == null) {
            throw new ApiException("不存在这个充电桩！");
        }
        if (pile.getStatus() != Pile.Status.OFF) {
            throw new ApiException("充电桩不是关闭状态，不能操作");
        }

        var feePattern = setPileParametersRequest.getRule();
        pile.setFeePattern(feePattern);

        double peakPrice = setPileParametersRequest.getPeakUp();
        pile.setPeakPrice(peakPrice);

        double usualPrice = setPileParametersRequest.getUsualUp();
        pile.setUsualPrice(usualPrice);

        double valleyPrice = setPileParametersRequest.getValleyUp();
        pile.setValleyPrice(valleyPrice);

        double servePrice = setPileParametersRequest.getServeUp();
        pile.setServePrice(servePrice);

        pilesRepository.save(pile);

    }


    @Override
    public CheckChargerResponse checkCharger(String pileId) {
        log.info("Admin try to check charger : " + pileId);
        CheckChargerResponse response = new CheckChargerResponse();
        Pile pile = pilesRepository.findByPile(pileId);
        if (pile == null) {
            throw new ApiException("未找到此充电桩");
        }
        response.setWorkingState(pile.getStatus().getValue());
        response.setTotalChargeNum(pile.getTotalChargeNum());
        response.setTotalChargeTime(pile.getTotalChargeTime());
        response.setTotalCapacity(pile.getTotalCapacity());

        return response;
    }


    @Autowired
    private CarRepository carRepository;

    @Override
    public  List<CarResponse> checkChargerQueue(String pileId) {
        log.info("Admin try to check charger queue: " + pileId);

        CheckChargerQueueResponse response = new CheckChargerQueueResponse();
        Pile pile = pilesRepository.findByPile(pileId);

        if (pile == null) {
            throw new ApiException("未找到此充电桩");
        }

        List<String> qEles = pile.getQList();
        List<Car> cars = new ArrayList<>();
        List<CarResponse> carResponseList = new ArrayList<>();
        String car_id;
        double request_amount;
        int wait_time;
        log.info("cars: " + qEles);
        for (String s : qEles) {
            Car car = carRepository.findByCarId(s);
            if (car != null) {
                cars.add(car);
                car_id=car.getCarId();
                Optional<ChargeRequest> chargeRequestOptional = chargeReqRepository.findById(car.getHandingReqId());
                if (chargeRequestOptional.isEmpty()) {
                    throw new ApiException("chargeRequestOptional is empty");
                }
                ChargeRequest chargeRequest = chargeRequestOptional.get();
                request_amount=chargeRequest.getRequestAmount();

                CarResponse carResponse=new CarResponse(22,car_id,request_amount,0);
                carResponseList.add(carResponse);
            }
        }

        return carResponseList;
    }


    @Override
    public void diePile(DiePileRequest diePileRequest) throws ApiException {
        if(!appConfig.SCHEDULE_TYPE.equals("BASIC")){
            throw new ApiException("不是普通调度，禁止故障充电桩");
        }
        log.info("Admin try to die pile: " + diePileRequest.getPileId());
        var pileId = diePileRequest.getPileId();
        Pile pile = pilesRepository.findByPile(pileId);
        if (pile == null) {
            throw new ApiException("不存在这个充电桩！");
        }

        pile.setStatus(Pile.Status.ERROR);
        pilesRepository.save(pile);

        //唤起后续调度
        scheduleService.errorMoveQueue(pileId);

    }

}
