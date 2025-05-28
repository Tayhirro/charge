package com.bupt.charger.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 充电车辆实体类
 * 包括车辆的基本信息、充电状态、充电桩信息等
 */
@Data
@Entity
@Table(name = "cars")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String carId;

    @Enumerated(EnumType.ORDINAL)
    private Status status = Status.COMPLETED;

    public enum Status { //车辆的状态
        COMPLETED, //0，表示没来充电
        waiting,  //1，表示车辆在等待充电桩
        pending,  //2，表示车辆已经被分配了充电桩，但还没有开始充电
        charging, //3，只有在车辆真正请求开始充电、开始计费时，才会进入此状态
        OTHER  //4
    }

    @Enumerated(EnumType.ORDINAL)
    private Area area = Area.COMPLETED;


    public enum Area {// 车辆所在区域
        COMPLETED, //0，表示没来充电
        WAITING,  //1，表示车辆在等待充电桩的区域
        CHARGING, //2，表示车辆在充电桩的充电区域
        OTHER;  //3

        @Override
        public String toString() {
            if (this == WAITING) {
                return "waiting area";
            }
            if (this == CHARGING) {
                return "charging area";
            }
            return super.toString();
        }
    }

    // NOTE: 这个属性暂时废弃，后面测试没有问题可以删除
    private Queue queue = Queue.UNQUEUED;

    //  这个是所在队列的号码，因为可能是故障队列/等候区队列，不一定分配到了充电桩
    private String queueNo;

    public enum Queue {
        UNQUEUED, //0,表示没来充电
        WAITING,  //1
        CHARGING, //2
        OTHER  //3
    }

    // note: 车辆进入等候区的时间，用于时间调度排序
    private LocalDateTime enWaitingQTime;

    // 对应 ChargeRequest 的 id，-1表示没有正在处理的请求
    private long handingReqId = -1;
    // 这个是被分配的充电桩的id,只有被移入充电区分配指定充电桩才可以
    private String pileId;

    public boolean canCharging() {
        // 需要位于充电去，且状态为waiting，同时是队列的第一个
        return status == Status.waiting && area == Area.CHARGING;
    }

    public boolean inChargingProcess() {
        return status != Status.OTHER && status != Status.COMPLETED;
    }

    public void releaseChargingProcess() {
        this.setStatus(Car.Status.COMPLETED);
        this.setArea(Car.Area.COMPLETED);
        this.setPileId("");
        this.setQueue(Car.Queue.UNQUEUED);
        this.setQueueNo("");
        this.setHandingReqId(-1);
        this.setEnWaitingQTime(null);
    }
}
