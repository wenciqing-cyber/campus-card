package com.lwb.campuscard.dao;

import com.lwb.campuscard.model.entity.Settlement;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 日终结账数据访问
 */
@Mapper
public interface SettlementMapper {

    @Insert("INSERT INTO settlement(settle_date, terminal_no, trade_count, total_amount, refund_amount, status) " +
            "VALUES(#{settleDate}, #{terminalNo}, #{tradeCount}, #{totalAmount}, #{refundAmount}, #{status}) " +
            "ON DUPLICATE KEY UPDATE trade_count = #{tradeCount}, total_amount = #{totalAmount}, " +
            "refund_amount = #{refundAmount}, status = #{status}")
    int upsert(Settlement settlement);

    @Select("SELECT * FROM settlement WHERE settle_date = #{date} ORDER BY terminal_no")
    List<Settlement> findByDate(@Param("date") LocalDate date);
}
