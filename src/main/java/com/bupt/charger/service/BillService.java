package com.bupt.charger.service;

import com.bupt.charger.dto.response.AllBillsByDayResponse;
import com.bupt.charger.dto.response.AllBillsResponse;
import com.bupt.charger.dto.response.DetailedBillResponse;

public interface BillService {

    // 详单获取
    DetailedBillResponse getBill(long billId);

    // 账单申请
    AllBillsResponse checkBill(String carId);

    // 导出指定车辆账单
    AllBillsByDayResponse checkBillByDay(String carId);

    // 导出所有详单
    void exportAll();

}
