package com.bupt.charger.controller;
import com.bupt.charger.dto.ApiResp;
import com.bupt.charger.service.TimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 获取当前系统时间
 */

@RestController
@Tag(name = "时间服务")
@RequestMapping("/timeNow")
public class GetTimeNowController {

    @Autowired
    private TimeService timeService;

    @GetMapping("")
    @Operation(summary = "获取时间")
    public ResponseEntity<Object> getTimeNow() {
        try {
            var response = timeService.getTimeNow();
            return ResponseEntity.ok().body(new ApiResp(response));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new ApiResp(1, e.getMessage()));
        }
    }



}
