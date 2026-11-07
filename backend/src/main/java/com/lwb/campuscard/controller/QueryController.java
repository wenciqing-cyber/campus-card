package com.lwb.campuscard.controller;

import com.lwb.campuscard.common.Result;
import com.lwb.campuscard.service.QueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 查询与统计
 */
@RestController
@RequestMapping("/api")
public class QueryController {

    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/trades")
    public Result<Map<String, Object>> trades(@RequestParam(required = false) String cardNo,
                                             @RequestParam(required = false) String tradeType,
                                             @RequestParam(required = false) String terminalNo,
                                             @RequestParam(required = false) String from,
                                             @RequestParam(required = false) String to,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.ok(queryService.pageTrades(cardNo, tradeType, terminalNo, from, to, page, size));
    }

    @GetMapping("/stats/overview")
    public Result<Map<String, Object>> overview() {
        return Result.ok(queryService.overview());
    }

    @GetMapping("/stats/trend")
    public Result<List<Map<String, Object>>> trend(@RequestParam(defaultValue = "7") int days) {
        return Result.ok(queryService.trend(days));
    }

    @GetMapping("/stats/type")
    public Result<List<Map<String, Object>>> byType() {
        return Result.ok(queryService.byType());
    }

    @GetMapping("/stats/terminal")
    public Result<List<Map<String, Object>>> byTerminal() {
        return Result.ok(queryService.terminalRank());
    }

    @GetMapping("/notifications")
    public Result<List<Map<String, Object>>> notifications() {
        return Result.ok(queryService.notifications());
    }

    @GetMapping("/reconciliation")
    public Result<Map<String, Object>> reconciliation() {
        return Result.ok(queryService.reconciliation());
    }

    @GetMapping("/logs")
    public Result<List<Map<String, Object>>> logs(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(queryService.operationLogs(limit));
    }
}
