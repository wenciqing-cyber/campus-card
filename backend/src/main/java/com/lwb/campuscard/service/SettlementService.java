package com.lwb.campuscard.service;

import com.lwb.campuscard.dao.SettlementMapper;
import com.lwb.campuscard.dao.TradeMapper;
import com.lwb.campuscard.model.entity.Settlement;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日终结账：把当天每个终端的消费额和退款额轧一遍，结果落 settlement 表
 * 同一终端同一天重复结账走 upsert，不会产生重复行
 */
@Service
public class SettlementService {

    private final TradeMapper tradeMapper;
    private final SettlementMapper settlementMapper;

    public SettlementService(TradeMapper tradeMapper, SettlementMapper settlementMapper) {
        this.tradeMapper = tradeMapper;
        this.settlementMapper = settlementMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Settlement> settle(LocalDate date) {
        Map<String, BigDecimal> refundMap = new HashMap<>();
        for (Map<String, Object> row : tradeMapper.refundByTerminal(date)) {
            refundMap.put(String.valueOf(row.get("terminal_no")),
                    new BigDecimal(String.valueOf(row.get("total"))));
        }

        List<Settlement> result = new ArrayList<>();
        for (Map<String, Object> row : tradeMapper.settleByTerminal(date)) {
            String terminalNo = String.valueOf(row.get("terminal_no"));
            Settlement s = new Settlement();
            s.setSettleDate(date);
            s.setTerminalNo(terminalNo);
            s.setTradeCount(Integer.parseInt(String.valueOf(row.get("cnt"))));
            s.setTotalAmount(new BigDecimal(String.valueOf(row.get("total"))));
            s.setRefundAmount(refundMap.getOrDefault(terminalNo, BigDecimal.ZERO));
            s.setStatus(1);
            settlementMapper.upsert(s);
            result.add(s);
        }
        return result;
    }

    public List<Settlement> query(LocalDate date) {
        return settlementMapper.findByDate(date);
    }
}
