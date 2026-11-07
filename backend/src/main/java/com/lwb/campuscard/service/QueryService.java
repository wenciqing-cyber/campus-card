package com.lwb.campuscard.service;

import com.lwb.campuscard.dao.AccountMapper;
import com.lwb.campuscard.dao.DiscountRuleMapper;
import com.lwb.campuscard.dao.OperationLogMapper;
import com.lwb.campuscard.dao.TerminalMapper;
import com.lwb.campuscard.dao.TradeMapper;
import com.lwb.campuscard.model.entity.DiscountRule;
import com.lwb.campuscard.model.entity.Terminal;
import com.lwb.campuscard.pattern.observer.NotifyObserver;
import com.lwb.campuscard.pattern.observer.ReconciliationObserver;
import com.lwb.campuscard.pattern.observer.StatisticsObserver;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询与统计：明细、对账、趋势、实时概览
 * 实时计数不查库，直接读观察者里攒着的内存数据
 */
@Service
public class QueryService {

    private final TradeMapper tradeMapper;
    private final AccountMapper accountMapper;
    private final TerminalMapper terminalMapper;
    private final DiscountRuleMapper ruleMapper;
    private final OperationLogMapper logMapper;
    private final NotifyObserver notifyObserver;
    private final StatisticsObserver statisticsObserver;
    private final ReconciliationObserver reconciliationObserver;

    public QueryService(TradeMapper tradeMapper,
                        AccountMapper accountMapper,
                        TerminalMapper terminalMapper,
                        DiscountRuleMapper ruleMapper,
                        OperationLogMapper logMapper,
                        NotifyObserver notifyObserver,
                        StatisticsObserver statisticsObserver,
                        ReconciliationObserver reconciliationObserver) {
        this.tradeMapper = tradeMapper;
        this.accountMapper = accountMapper;
        this.terminalMapper = terminalMapper;
        this.ruleMapper = ruleMapper;
        this.logMapper = logMapper;
        this.notifyObserver = notifyObserver;
        this.statisticsObserver = statisticsObserver;
        this.reconciliationObserver = reconciliationObserver;
    }

    public Map<String, Object> pageTrades(String cardNo, String tradeType, String terminalNo,
                                          String from, String to, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        List<Map<String, Object>> rows = tradeMapper.page(cardNo, tradeType, terminalNo,
                from, to, (safePage - 1) * safeSize, safeSize);
        int total = tradeMapper.countPage(cardNo, tradeType, terminalNo, from, to);
        Map<String, Object> map = new HashMap<>();
        map.put("rows", rows);
        map.put("total", total);
        map.put("page", safePage);
        map.put("size", safeSize);
        return map;
    }

    public Map<String, Object> overview() {
        Map<String, Object> map = new HashMap<>();
        map.put("accountCount", accountMapper.countAll());
        map.put("tradeCount", tradeMapper.countSuccess());
        map.put("consumeTotal", tradeMapper.sumConsumeTotal());
        map.put("todayTrend", tradeMapper.trend(1));
        map.put("byType", tradeMapper.statByType());
        map.put("byTerminal", tradeMapper.settleByTerminal(java.time.LocalDate.now()));
        // 这些是进程内实时计数，和库里对得上才说明观察者没丢事件
        map.put("liveConsumeCount", statisticsObserver.getConsumeCount());
        map.put("liveConsumeAmount", statisticsObserver.getConsumeTotal());
        map.put("liveReconcileCount", reconciliationObserver.count());
        return map;
    }

    // 没有交易的日期也要占一格，不然趋势图只剩一根柱子
    public List<Map<String, Object>> trend(int days) {
        int span = Math.min(Math.max(days, 1), 30);
        Map<String, Map<String, Object>> byDay = new HashMap<>();
        for (Map<String, Object> row : tradeMapper.trend(span)) {
            byDay.put(String.valueOf(row.get("day")), row);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = span - 1; i >= 0; i--) {
            String day = today.minusDays(i).toString();
            Map<String, Object> row = byDay.get(day);
            if (row == null) {
                row = new LinkedHashMap<>();
                row.put("day", day);
                row.put("cnt", 0);
                row.put("total", BigDecimal.ZERO);
            }
            result.add(row);
        }
        return result;
    }

    public List<Map<String, Object>> byType() {
        return tradeMapper.statByType();
    }

    public List<Map<String, Object>> terminalRank() {
        return tradeMapper.settleByTerminal(LocalDate.now());
    }

    public List<Map<String, Object>> notifications() {
        return notifyObserver.recent();
    }

    public Map<String, Object> reconciliation() {
        Map<String, Object> map = new HashMap<>();
        map.put("byTerminal", reconciliationObserver.snapshot());
        map.put("count", reconciliationObserver.count());
        return map;
    }

    public List<Map<String, Object>> operationLogs(int limit) {
        return logMapper.recent(Math.min(Math.max(limit, 1), 200));
    }

    public List<Terminal> terminals() {
        return terminalMapper.listAll();
    }

    public List<DiscountRule> rules() {
        return ruleMapper.listAll();
    }

    public BigDecimal todayTotal() {
        List<Map<String, Object>> list = tradeMapper.trend(1);
        if (list.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Object total = list.get(0).get("total");
        return total == null ? BigDecimal.ZERO : new BigDecimal(total.toString());
    }
}
