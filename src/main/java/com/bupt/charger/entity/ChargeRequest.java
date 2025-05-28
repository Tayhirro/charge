package com.bupt.charger.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 充电请求实体类
 */
@Data
@Entity
@Table(name = "requests")
public class ChargeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    private String carId;

    @Enumerated(EnumType.ORDINAL)
    private Status status = Status.INIT;

    public enum Status {
        INIT, //0, 表示这个请求尚未被处理
        DOING, //1，正在处理
        DONE,  //2，处理完了
        CANCELED  //3，被取消了
    }

    private double requestAmount;
    private double doneAmount;

    private RequestMode requestMode = RequestMode.UNSET;

    public enum RequestMode {
        UNSET, FAST, SLOW
    }



    public void setRequestMode(Object mode) {
        if(mode instanceof String){
            String stingMode= ((String) mode).trim().toLowerCase();
            if ("quick".equalsIgnoreCase(stingMode)) {
                this.setRequestMode(RequestMode.FAST);
            } else if ("slow".equalsIgnoreCase(stingMode)) {
                this.setRequestMode(RequestMode.SLOW);
            } else {
                this.setRequestMode(RequestMode.UNSET);
            }

        }
        else if (mode instanceof RequestMode) {
            this.requestMode = (RequestMode) mode;
        } else {
            throw new IllegalArgumentException("Unsupported mode type: " + mode.getClass().getName());
        }
    }



    // 用逗号分隔的字符串，存储后续请求的ID。
    // 充电请求的后续补充，主要是出现故障时，新增一个请求，这个字段记录新增的请求。
    // 比如id=1的请求要充10度电，已充2度，然后充电桩故障
    // 后面调度可以加个请求，假设新增的id=2，设置充8度电。
    // 然后id=1的nextReqs增加元素2.
    private String nextReqs;

    public boolean isSuffered() {
        return nextReqs != null && !nextReqs.isEmpty();
    }

    public List<Long> getSuccReqsList() {
        String input = nextReqs;
        List<Long> resultList = new ArrayList<>();

        if (input != null && !input.isEmpty()) {
            String[] numberStrings = input.split(",");
            for (String numberString : numberStrings) {
                try {
                    long number = Long.parseLong(numberString.trim());
                    resultList.add(number);
                } catch (NumberFormatException e) {
                    // 如果字符串无法解析为长整型数字，则忽略该项
                }
            }
        }

        return resultList;
    }

    /* 若添加成功，返回true，否则false  */
    public boolean addSuccReqs(long id) {
        if (nextReqs == null || nextReqs.isEmpty()) {
            nextReqs = String.valueOf(id);
        } else {
            nextReqs += "," + id;
        }
        return true;
    }

    private LocalDateTime startChargingTime;
    private LocalDateTime endChargingTime;

    private Long billId;
}
