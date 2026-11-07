package com.lwb.campuscard.controller;

import com.lwb.campuscard.common.Result;
import com.lwb.campuscard.model.entity.DiscountRule;
import com.lwb.campuscard.model.entity.Settlement;
import com.lwb.campuscard.model.entity.Terminal;
import com.lwb.campuscard.pattern.singleton.SystemConfig;
import com.lwb.campuscard.service.QueryService;
import com.lwb.campuscard.service.SettlementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基础资料、系统参数、日终结账
 */
@RestController
@RequestMapping("/api")
public class MetaController {

    private final QueryService queryService;
    private final SettlementService settlementService;

    public MetaController(QueryService queryService, SettlementService settlementService) {
        this.queryService = queryService;
        this.settlementService = settlementService;
    }

    @GetMapping("/terminals")
    public Result<List<Terminal>> terminals() {
        return Result.ok(queryService.terminals());
    }

    @GetMapping("/rules")
    public Result<List<DiscountRule>> rules() {
        return Result.ok(queryService.rules());
    }

    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        SystemConfig cfg = SystemConfig.getInstance();
        Map<String, Object> map = new HashMap<>();
        map.put("singleLimit", cfg.getSingleLimit());
        map.put("dailyLimit", cfg.getDailyLimit());
        map.put("pointsPerYuan", cfg.getPointsPerYuan());
        map.put("notifyEnabled", cfg.isNotifyEnabled());
        return Result.ok(map);
    }

    @PostMapping("/settlement/run")
    public Result<List<Settlement>> runSettlement(@RequestParam(required = false) String date) {
        LocalDate day = (date == null || date.isEmpty()) ? LocalDate.now() : LocalDate.parse(date);
        return Result.ok(settlementService.settle(day));
    }

    @GetMapping("/settlement")
    public Result<List<Settlement>> settlement(@RequestParam(required = false) String date) {
        LocalDate day = (date == null || date.isEmpty()) ? LocalDate.now() : LocalDate.parse(date);
        return Result.ok(settlementService.query(day));
    }
}
