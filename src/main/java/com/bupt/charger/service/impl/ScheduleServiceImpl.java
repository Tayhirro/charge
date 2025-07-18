package com.bupt.charger.service.impl;

import com.bupt.charger.config.AppConfig;
import com.bupt.charger.entity.*;
import com.bupt.charger.service.ApiException;
import com.bupt.charger.repository.*;
import com.bupt.charger.service.TaskService;
import com.bupt.charger.util.Calculator;
import com.bupt.charger.util.Estimator;
import com.bupt.charger.util.FormatUtils;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Log
public class ScheduleServiceImpl implements com.bupt.charger.service.ScheduleService {
    // note: 将所有充电区的队列放在pile的queue里面，等候区的才放在ChargingQueue里面
    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ChargingQueueRepository chargingQueueRepository;

    @Autowired
    private ChargeReqRepository chargeReqRepository;

    @Autowired
    private PilesRepository pilesRepository;

    @Autowired
    private Estimator estimator;

    @Autowired
    TaskService taskService;

    @Autowired
    Calculator calculator;
    @Autowired
    BillRepository billRepository;

    @Autowired
    private AppConfig appConfig;

    static int fSumNumber = 0;
    static int tSumNumber = 0;

    // 是否是否等候区叫号服务
    static boolean isStopWaitArea = false;

    // 添加到等候区队列，返回分配的号码
    @Override
    public String moveToWaitingQueue(Car car) {
        // 根据类型查询等待区的队列
        ChargeRequest carRequest = chargeReqRepository.findTopByCarIdAndStatusOrderByCreatedAtDesc(car.getCarId(), ChargeRequest.Status.DOING);
        // 查看充电类型
        String queueId;
        String res;
        if (carRequest.getRequestMode() == ChargeRequest.RequestMode.SLOW) {
            queueId = "T";
            res = queueId + (++fSumNumber);

        } else if (carRequest.getRequestMode() == ChargeRequest.RequestMode.FAST) {
            queueId = "F";
            res = queueId + (++tSumNumber);
        } else {
            log.info("充电请求的充电类型错误");
            // 错误处理
            return null;
        }
        ChargingQueue waitQueue = chargingQueueRepository.findByQueueId(queueId);
        // 添加到相应的位置,
        if (waitQueue.addWaitingCar(car.getCarId())) {
            // 设置车辆状态,车辆的queueNo状态也需要更改
            car.setStatus(Car.Status.waiting);
            car.setArea(Car.Area.WAITING);
            car.setQueueNo(waitQueue.getQueueId());
            car.setEnWaitingQTime(FormatUtils.getNowLocalDateTime());
            // 保存到数据库
            chargingQueueRepository.save(waitQueue);
            carRepository.save(car);
        } else {
            throw new ApiException("等候区已经爆满不可进入");
        }
        // 调用调度函数
        trySchedule();
        return res;
    }

    // 获得有空余的充电队列
    @Override
    public List<Pile> getChargingNotFullQueue(Pile.Mode mode) {
        // 根据模式检测相应充电区的充电队列是否有空余，有则添加
        List<Pile> res = new ArrayList<>();

        // 只匹配相应充电模式即可
        List<Pile> piles = pilesRepository.findAll();
        for (Pile pile : piles) {
            //    如果没有达到容量就是有空余
            // 充电桩必须是正常运行的
            if (pile.isON() && pile.getMode().equals(mode) && pile.getQCnt() < pile.getCapacity()) {
                res.add(pile);
            }
        }
        return res;
    }

    // 提醒车辆开始充电函数
    @Override
    public boolean remindCarStartCharge(String pileId) {
        // 调用这个函数的情况：1. 空队列进来了新车(即最短时间调度函数检测到这个事件) 2. 有车辆结束充电，通知下一个。

        //     如果队列里面没有车辆，那么就不需要提醒；不然提醒指定队列的第一辆车开始充电
        Pile pile = pilesRepository.findByPile(pileId);
        if (pile.getQCnt() == 0) {
            return false;
        }
        String carId = pile.getQList().get(0);
        //    TODO:通知前端指定车辆

        return true;
    }

    @Override
    public void trySchedule() {
        //  以下情况会调用这个函数：1. 有车辆发送结束充电请求 2. 刚进入等候区（因为这个时候可能有多个充电队列空余） 3. 提交故障请求时，因为可能其他多个充电队列空余，但故障仅发生在有车充电的充电桩里面了 4. 充电桩故障完恢复上线时(暂时没有处理) 故障恢复需要完成的调度：恢复当时就执行该函数,充电桩有多少空余就先执行多少次
        //  schedule-type: BASIC # 调度算法，BASIC 、 SINGLE_SHORTEST_TIME 或 BATCH_SHORTEST_TIME
        if (appConfig.SCHEDULE_TYPE.equals("BASIC")){
            // 获取两个模式下的空余队列
            List<Pile> fastPiles = getChargingNotFullQueue(Pile.Mode.F);
            List<Pile> slowPiles = getChargingNotFullQueue(Pile.Mode.T);
            // 如果两个故障队列都空了，那么恢复叫号服务
            ChargingQueue errorF = chargingQueueRepository.findByQueueId("ErrorF");
            ChargingQueue errorT = chargingQueueRepository.findByQueueId("ErrorT");
            if (errorT.getWaitingCarCnt() == 0 && errorF.getWaitingCarCnt() == 0) {
                isStopWaitArea = false;
            }
            if (fastPiles != null && !fastPiles.isEmpty()) {
                basicSchedule(fastPiles, Pile.Mode.F);
            }

            if (slowPiles != null && !slowPiles.isEmpty()) {
                basicSchedule(slowPiles, Pile.Mode.T);
            }
        } else if (appConfig.SCHEDULE_TYPE.equals("SINGLE_SHORTEST_TIME")) {
            //不用考虑客户修改充电请求以及充电桩出现故障
            //a)	单次调度总充电时长最短 todo

        } else if(appConfig.SCHEDULE_TYPE.equals("BATCH_SHORTEST_TIME")){
            //不用考虑客户修改充电请求以及充电桩出现故障
            //b)	批量调度总充电时长最短： todo
        }

    }

    // 将指定车辆从等候区移除
    @Override
    public void removeFromWaitingQueue(String carId, ChargeRequest.RequestMode oldMode) {
        String oldQueueId;
        if (oldMode.equals(ChargeRequest.RequestMode.FAST)) {
            oldQueueId = "F";
        } else if (oldMode.equals(ChargeRequest.RequestMode.SLOW)) {
            oldQueueId = "T";
        } else {
            log.info("移除等候区错误");
            return;
        }
        ChargingQueue chargingQueue = chargingQueueRepository.findByQueueId(oldQueueId);
        chargingQueue.removeWaitingCar(carId);
        chargingQueueRepository.save(chargingQueue);
    }

    // 基本调度策略
    public String basicSchedule(List<Pile> piles, Pile.Mode mode) {
        if (piles == null || piles.size() == 0) {
            return null;
        }
        String suffixId;
        if (mode.equals(Pile.Mode.T)) {
            suffixId = "T";
        } else if (mode.equals(Pile.Mode.F)) {
            suffixId = "F";
        } else {
            log.info("调度策略错误");
            return null;
        }

        String waitQueueId;
        if (isStopWaitArea) {
            waitQueueId = "Error" + suffixId;
        } else {
            waitQueueId = suffixId;
        }

        // 从等候区中寻找和这个充电桩充电模式匹配的队列，然后将第一个车辆调度过来
        ChargingQueue waitQueue = chargingQueueRepository.findByQueueId(waitQueueId);
        // 测试，打印等候区所有车
        System.out.println("打印等候区所有车" + waitQueueId);
        for (var carId : waitQueue.getWaitingCarsList()) {
            System.out.println(carId);
        }


        // 从等待区移走
        String topCarId = waitQueue.consumeWaitingCar();
        if (topCarId != null && topCarId.length() > 0) {
            Car car = carRepository.findByCarId(topCarId);
            // 执行调度策略
            // 无论是故障调度还是基本调度都是从一个等候队列到一个充电队列,选择总时间最短的充电队列
            // 如果只有一个空闲队列，那么就直接调度到这个队列
            Pile resPile;
            if (piles.size() == 1) {
                resPile = piles.get(0);
            } else {
                // 计算每个队列的充电总时间，选择总电量最少的那个充电桩
                ArrayList<Duration> leftTimeList = new ArrayList<>();
                for (Pile pile : piles) {
                    //    获取队列中所有车辆
                    List<String> carList = pile.getQList();
                    //    计算所有车辆充完电需要的总时间
                    Duration sumDuration = Duration.ZERO;
                    for (int i = 0; i < carList.size(); i++) {
                        if (i == 0) {
                            Car chargingCar = carRepository.findByCarId(carList.get(i));
                            //    检查这个车辆是否在充电中
                            if (chargingCar.getStatus() == Car.Status.charging) {
                                sumDuration = sumDuration.plus(estimator.estimateCarLeftChargeTime(chargingCar.getCarId()));
                            } else {
                                sumDuration = sumDuration.plus(estimator.estimateCarChargeTime(chargingCar.getCarId()));
                            }
                        } else {
                            sumDuration = sumDuration.plus(estimator.estimateCarChargeTime(carList.get(i)));
                        }
                    }
                    leftTimeList.add(sumDuration);
                }
                //    获取最小时间
                int minIndex = 0;
                Duration minDuration = leftTimeList.get(0);
                for (int i = 0; i < leftTimeList.size(); i++) {
                    if (leftTimeList.get(i).compareTo(minDuration) < 0) {
                        minIndex = i;
                        minDuration = leftTimeList.get(i);
                    }
                }
                resPile = piles.get(minIndex);
            }
            // 测试
            System.out.println("打印分配到的pile" + resPile.getPileId());
            if (!resPile.addCar(car.getCarId())) {
                System.out.println("出现异常，明明有空余，但是却无法添加");
            }

            // 设置车辆状态
            car.setStatus(Car.Status.waiting);
            car.setArea(Car.Area.CHARGING);
            car.setQueueNo(resPile.getPileId());
            car.setPileId(resPile.getPileId());

            //    保存
            pilesRepository.save(resPile);
            carRepository.save(car);
            chargingQueueRepository.save(waitQueue);

            // 如果这个队列加上新加的也只有一个，那么就通知车辆
            if (resPile.getQCnt() == 1) {
                //    通知车辆
                remindCarStartCharge(resPile.getPileId());
            }
            return resPile.getPileId();
        }
        return null;
    }

    // 这个是故障机制中，故障充电桩车辆停止充电
    public void errorStopCharging(String carId) {
        //    停止充电，但是不能更改Car的进入等候区的状态

        Car car = carRepository.findByCarId(carId);
        if (car == null) {
            throw new ApiException("车辆不存在");
        }

        if (!car.inChargingProcess()) {
            throw new ApiException("车辆并未在充电进程中");
        }

        var requestOptionalal = chargeReqRepository.findById(car.getHandingReqId());
        if (requestOptionalal.isEmpty()) {
            throw new ApiException("没有关联的充电请求，请联系客服");
        }

        LocalDateTime endTime = FormatUtils.getNowLocalDateTime();

        taskService.cancelTask(carId);

        // 标记充电请求为已完成
        ChargeRequest request = requestOptionalal.get();

        String pileNo = car.getPileId();
        Pile pile = pilesRepository.findByPile(pileNo);


        //计算实际充电量
        LocalDateTime startTime = request.getStartChargingTime();
        double amount = calculator.getChargeAmount(startTime, endTime, request.getRequestMode());
        amount = Double.min(amount, request.getRequestAmount());

        // 生成详单
        Bill bill = new Bill();
        bill.setCarId(carId);
        bill.setStartTime(startTime);
        bill.setEndTime(endTime);
        bill.setPileId(pileNo);
        bill.setChargeAmount(amount);
        double chargeFee = calculator.getChargeFee(startTime, endTime, pileNo, amount);
        bill.setChargeFee(chargeFee);
        bill.setServiceFee(amount * pile.getServePrice());
        billRepository.save(bill);

        // 设置旧订单
        request.setBillId(bill.getId());
        request.setEndChargingTime(endTime);
        request.setStatus(ChargeRequest.Status.DONE);
        request.setDoneAmount(amount);
        // 不需要设置车辆状态，因为之后进入调度队列自动设置

        // 需要为该车重建一个请求，传入没有充的电量
        ChargeRequest newChargeRequest = new ChargeRequest();
        newChargeRequest.setRequestAmount(request.getRequestAmount() - amount);
        newChargeRequest.setRequestMode(request.getRequestMode());
        newChargeRequest.setStatus(ChargeRequest.Status.DOING);
        newChargeRequest.setCarId(carId);
        chargeReqRepository.save(newChargeRequest);

        // 旧请求连接到新请求中
        request.addSuccReqs(newChargeRequest.getId());

        // 保存
        chargeReqRepository.save(request);
        car.setHandingReqId(newChargeRequest.getId());
        carRepository.save(car);
    }

    @Override
    public void errorMoveQueue(String pileId){
        // 故障调度的统一接口，在配置文件中选择
        //TIME 或 PRIORITY
        if (appConfig.ERROR_SCHEDULE_TYPE.equals("PRIORITY")) {
            priorityErrorMoveQueue(pileId);
        } else if (appConfig.ERROR_SCHEDULE_TYPE.equals("TIME")) {
            timeErrorMoveQueue(pileId);
        } else {
            throw new ApiException("故障调度模式错误");
        }

    }

    /*  获取故障上报请求,也可管理员端直接进行,暂停正在充电的车辆,同时转移队列到故障队列
        注意一个问题: 优先级调度就是将对应的原充电桩队列转移到相应模式的故障队列,但是时间顺序队列需要将同类型的所有没在充电的车辆全部汇集到故障队列里面,同时需要按照车辆排队号码进行排序,数字越大越靠后.汇聚之后将原来的队列清空,因为我们有实时检测是否有队列空的,自然就会从故障队列里面加回去
     */
//    故障-优先级调度移动队列
    public void priorityErrorMoveQueue(String pileId) {
        //   优先级调度就是将对应的原充电桩队列转移到相应模式的故障队列
        //    暂停正常移进充电区服务
        Pile pile = pilesRepository.findByPile(pileId);
        // 如果队列为空，则不执行
        if (pile.getQCnt() == 0) {
            return;
        }
        ScheduleServiceImpl.isStopWaitArea = true;
        // 调用故障停止充电函数，将第一个正在充电的车停止充电
        Car topCar = carRepository.findByCarId(pile.getQList().get(0));
        if (topCar.getStatus().equals(Car.Status.charging)) {
            errorStopCharging(topCar.getCarId());
        }
        // 选择指定模式的故障队列
        ChargingQueue errorQueue;
        if (pile.mode.equals(Pile.Mode.F)) {
            errorQueue = chargingQueueRepository.findByQueueId("ErrorF");
        } else {
            errorQueue = chargingQueueRepository.findByQueueId("ErrorT");
        }
        int len = pile.getQCnt();
        for (int i = 0; i < len; i++) {
            String carId = pile.consumeWaitingCar();
            // 移到故障队列里面的车辆状态还是认为处于充电区域，等待充电
            Car tmpCar = carRepository.findByCarId(carId);
            tmpCar.setStatus(Car.Status.waiting);
            tmpCar.setQueueNo(errorQueue.getQueueId());
            tmpCar.setArea(Car.Area.CHARGING);
            carRepository.save(tmpCar);
            errorQueue.addWaitingCar(carId);
        }
        //    保存数据库
        pilesRepository.save(pile);
        chargingQueueRepository.save(errorQueue);
        //    调度移到充电桩,因为可能有多个空余，宁可多调用几次，所以有几个车辆在故障队列就调用几次检测
        for (int i = 0; i < errorQueue.getWaitingCarCnt(); i++) {
            trySchedule();
        }
    }

    //    故障-时间顺序调度移动队列
    public void timeErrorMoveQueue(String pileId) {
        Pile pile = pilesRepository.findByPile(pileId);

        if (pile.getQCnt() == 0) {
            return;
        }


        ScheduleServiceImpl.isStopWaitArea = true;
        // 调用故障停止充电函数，将第一个正在充电的车停止充电
        Car topCar = carRepository.findByCarId(pile.getQList().get(0));
        if (topCar.getStatus().equals(Car.Status.charging)) {
            errorStopCharging(topCar.getCarId());
        }
        // 选择指定模式的故障队列
        ChargingQueue errorQueue;
        if (pile.mode.equals(Pile.Mode.F)) {
            errorQueue = chargingQueueRepository.findByQueueId("ErrorF");
        } else {
            errorQueue = chargingQueueRepository.findByQueueId("ErrorT");
        }

        List<Car> cars = new ArrayList<>();
        // 先把该充电桩的所有车辆进来
        int len = pile.getQCnt();
        for (int i = 0; i < len; i++) {
            String carId = pile.consumeWaitingCar();
            cars.add(carRepository.findByCarId(carId));
        }
        // 保存
        pilesRepository.save(pile);

        // 时间顺序队列需要将同类型的所有没在充电的车辆全部汇集到故障队列里面,同时需要按照车辆排队号码进行排序,数字越大越靠后.汇聚之后将原来的队列清空,因为我们有实时检测是否有队列空的,自然就会从故障队列里面加回去
        // 筛选所有同类型的充电桩
        List<Pile> allPiles = pilesRepository.findAll();
        for (Pile tmpPile : allPiles) {
            if (!tmpPile.getPileId().equals(pile.getPileId()) && tmpPile.getMode().equals(pile.getMode())) {
                // 把这个同类型的充电桩(不能包括自己)所有没有在充电的车辆放到cars数组
                int tmpLen = tmpPile.getQCnt();
                // 避免引用传递，需要额外开辟空间复制
                List<String> tmpCarIdList = new ArrayList<>(tmpPile.getQList());
                // 清空pile中所有车辆
                tmpPile.setCarQueue("");
                //  单独检测第一个
                if (tmpLen > 0) {
                    Car tmpCar = carRepository.findByCarId(tmpCarIdList.get(0));
                    // 不在充电中才加入
                    if (!tmpCar.getStatus().equals(Car.Status.charging)) {
                        cars.add(tmpCar);
                    } else {
                        //    第一个在充电，那么加回去
                        tmpPile.addCar(tmpCar.getCarId());
                    }
                }
                //  从第二个开始
                for (int i = 1; i < tmpLen; i++) {
                    Car tmpCar = carRepository.findByCarId(tmpCarIdList.get(i));
                    cars.add(tmpCar);
                }
                //  保存充电桩队列
                pilesRepository.save(tmpPile);
            }
        }

        //    按照Car类里面enWaitingQTime进入等候区时间进行排序
        cars.sort(new Comparator<Car>() {
            @Override
            public int compare(Car o1, Car o2) {
                return o2.getEnWaitingQTime().compareTo(o1.getEnWaitingQTime());
            }
        });

        //    按顺序写入到故障队列
        for (Car car : cars) {
            errorQueue.addWaitingCar(car.getCarId());
        }

        // 更改车辆状态
        for (Car tmpCar : cars) {
            tmpCar.setStatus(Car.Status.waiting);
            tmpCar.setQueueNo(errorQueue.getQueueId());
            tmpCar.setArea(Car.Area.CHARGING);
            carRepository.save(tmpCar);
        }
        //    保存
        chargingQueueRepository.save(errorQueue);

        //    调度移到充电桩
        for (int i = 0; i < len; i++) {
            String carId = pile.consumeWaitingCar();
            cars.add(carRepository.findByCarId(carId));
        }
    }
}
