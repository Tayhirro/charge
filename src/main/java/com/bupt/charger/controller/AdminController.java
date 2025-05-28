package com.bupt.charger.controller;


import com.bupt.charger.dto.ApiResp;
import com.bupt.charger.dto.request.*;
import com.bupt.charger.dto.response.AdminLoginResponse;
import com.bupt.charger.service.AdminService;
import com.bupt.charger.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员充电桩操作
 * 包括启动充电桩、关闭充电桩、修改充电桩状态、查看充电桩状态、查看充电桩队列状态
 */


@RestController
@Tag(name = "充电桩管理")
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    private ScheduleService scheduleService;

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public ResponseEntity<Object> adminLogin(@RequestBody AdminLoginRequest adminLoginRequest) {
        try {
            AdminLoginResponse adminLoginResponse = adminService.login(adminLoginRequest.getAdminName(), adminLoginRequest.getPassword());
            return ResponseEntity.ok().body(new ApiResp(0, "请求成功", adminLoginResponse));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }

    @PostMapping("/startCharger")
    @Operation(summary = "管理员启动充电桩")
    public ResponseEntity<Object> startPile(@RequestBody StartPileRequest request) {
        try {
            adminService.startPile(request);
            return ResponseEntity.ok().body(new ApiResp(0, "请求成功"));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }

    @PostMapping("/offCharger")
    @Operation(summary = "管理员关闭充电桩")
    public ResponseEntity<Object> shutDownPile(@RequestBody ShutDownPileRequest request) {
        try {
            adminService.shutDownPile(request);
            return ResponseEntity.ok().body(new ApiResp(0, "请求成功"));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }

    @PostMapping("/setParameters")
    @Operation(summary = "管理员修改充电桩状态")
    public ResponseEntity<Object> setPileParameters(@RequestBody SetPileParametersRequest request) {
        try {
            adminService.setPileParameters(request);
            return ResponseEntity.ok().body(new ApiResp(0, "请求成功"));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }

    @GetMapping("/checkCharger")
    @Operation(summary = "管理员查看充电桩状态")
    public ResponseEntity<?> checkCharger(@RequestParam("pile_id") String pileId) {
        try {
            var response = adminService.checkCharger(pileId);
            return ResponseEntity.ok().body(new ApiResp(response));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }

    @GetMapping("/checkChargerQueue")
    @Operation(summary = "管理员查看指定充电桩队列状态")
    public ResponseEntity<?> checkChargerQueue(@RequestParam("pile_id") String pileId) {
        try {
            var response = adminService.checkChargerQueue(pileId);
            return ResponseEntity.ok().body(new ApiResp(response));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }

    @PostMapping("/offCharger")
    @Operation(summary = "使充电桩故障")
    public ResponseEntity<Object> diePile(@RequestBody DiePileRequest request) {
        try {
            adminService.diePile(request);
            return ResponseEntity.ok().body(new ApiResp(0, "请求成功"));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }

}


