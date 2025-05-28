package com.bupt.charger.controller;

import com.bupt.charger.dto.ApiResp;
import com.bupt.charger.service.BillService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户账单查看
 * 包括获取账单、查看当前账单、导出所有账单
 */
@RestController
@Tag(name = "账单服务")
@RequestMapping("/user")
public class UserBillController {

    @Autowired
    BillService bilService;


    @GetMapping("/getBill")
    public ResponseEntity<?> getBill(@RequestParam("bill_id") long billId) {
        try {
            var resp = bilService.getBill(billId);
            return ResponseEntity.ok().body(new ApiResp(resp));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }

    @GetMapping("/checkBill")
    public ResponseEntity<?> checkBill(@RequestParam("car_id") String carId) {
        try {
            var resp = bilService.checkBill(carId);
            return ResponseEntity.ok().body(new ApiResp(resp));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }

    @GetMapping("/exportAllBill")
    public ResponseEntity<?> exportAllBill() {
        try {
            bilService.exportAll(); // 这个函数没有返回值
            return ResponseEntity.ok().body(new ApiResp());
        } catch (Exception e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }
}
