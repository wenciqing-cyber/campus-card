package com.lwb.campuscard.dao;

import com.lwb.campuscard.model.entity.Trade;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 交易流水数据访问（流水只增不改，冲正靠新增反向单）
 */
@Mapper
public interface TradeMapper {

    @Insert("INSERT INTO trade(trade_no, request_no, account_id, card_no, trade_type, terminal_no, " +
            "origin_amount, discount_amount, pay_amount, points_delta, strategy_code, status, " +
            "origin_trade_no, remark) VALUES(#{tradeNo}, #{requestNo}, #{accountId}, #{cardNo}, " +
            "#{tradeType}, #{terminalNo}, #{originAmount}, #{discountAmount}, #{payAmount}, " +
            "#{pointsDelta}, #{strategyCode}, #{status}, #{originTradeNo}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Trade trade);

    @Select("SELECT * FROM trade WHERE trade_no = #{tradeNo}")
    Trade findByTradeNo(@Param("tradeNo") String tradeNo);

    @Select("SELECT * FROM trade WHERE request_no = #{requestNo}")
    Trade findByRequestNo(@Param("requestNo") String requestNo);

    @Select("SELECT IFNULL(SUM(pay_amount), 0) FROM trade WHERE account_id = #{accountId} " +
            "AND trade_type = 'CONSUME' AND status = 1 AND DATE(created_at) = CURDATE()")
    BigDecimal sumConsumeToday(@Param("accountId") Long accountId);

    @Select("SELECT IFNULL(SUM(pay_amount), 0) FROM trade WHERE account_id = #{accountId} " +
            "AND trade_type = 'CONSUME' AND status = 1 AND terminal_no = #{terminalNo} AND DATE(created_at) = CURDATE()")
    BigDecimal sumConsumeTodayByTerminal(@Param("accountId") Long accountId,
                                         @Param("terminalNo") String terminalNo);

    @Update("UPDATE trade SET status = #{status} WHERE trade_no = #{tradeNo}")
    int updateStatus(@Param("tradeNo") String tradeNo, @Param("status") int status);

    /** 明细查询，条件都可空 */
    @Select("<script>" +
            "SELECT t.trade_no AS tradeNo, t.card_no AS cardNo, t.trade_type AS tradeType, " +
            "t.terminal_no AS terminalNo, t.origin_amount AS originAmount, " +
            "t.discount_amount AS discountAmount, t.pay_amount AS payAmount, " +
            "t.points_delta AS pointsDelta, t.strategy_code AS strategyCode, t.status, " +
            "t.origin_trade_no AS originTradeNo, t.created_at AS createdAt, " +
            "a.student_name AS studentName " +
            "FROM trade t LEFT JOIN account a ON a.id = t.account_id " +
            "WHERE 1=1 " +
            "<if test='cardNo != null and cardNo != \"\"'> AND t.card_no = #{cardNo} </if>" +
            "<if test='tradeType != null and tradeType != \"\"'> AND t.trade_type = #{tradeType} </if>" +
            "<if test='terminalNo != null and terminalNo != \"\"'> AND t.terminal_no = #{terminalNo} </if>" +
            "<if test='from != null'> AND t.created_at &gt;= #{from} </if>" +
            "<if test='to != null'> AND t.created_at &lt; #{to} </if>" +
            "ORDER BY t.id DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<Map<String, Object>> page(@Param("cardNo") String cardNo,
                                   @Param("tradeType") String tradeType,
                                   @Param("terminalNo") String terminalNo,
                                   @Param("from") String from,
                                   @Param("to") String to,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM trade t WHERE 1=1 " +
            "<if test='cardNo != null and cardNo != \"\"'> AND t.card_no = #{cardNo} </if>" +
            "<if test='tradeType != null and tradeType != \"\"'> AND t.trade_type = #{tradeType} </if>" +
            "<if test='terminalNo != null and terminalNo != \"\"'> AND t.terminal_no = #{terminalNo} </if>" +
            "<if test='from != null'> AND t.created_at &gt;= #{from} </if>" +
            "<if test='to != null'> AND t.created_at &lt; #{to} </if>" +
            "</script>")
    int countPage(@Param("cardNo") String cardNo,
                  @Param("tradeType") String tradeType,
                  @Param("terminalNo") String terminalNo,
                  @Param("from") String from,
                  @Param("to") String to);

    /** 按终端对账 */
    @Select("SELECT terminal_no, COUNT(*) AS cnt, IFNULL(SUM(pay_amount),0) AS total " +
            "FROM trade WHERE trade_type='CONSUME' AND status=1 AND DATE(created_at)=#{date} " +
            "GROUP BY terminal_no ORDER BY total DESC")
    List<Map<String, Object>> settleByTerminal(@Param("date") LocalDate date);

    /** 当日退款按终端汇总，日终结账时和消费额对照 */
    @Select("SELECT terminal_no, IFNULL(SUM(pay_amount),0) AS total FROM trade " +
            "WHERE trade_type='REFUND' AND status=1 AND DATE(created_at)=#{date} " +
            "GROUP BY terminal_no")
    List<Map<String, Object>> refundByTerminal(@Param("date") LocalDate date);

    /** 近 N 天消费趋势，只算消费单，充值和退款不算进来 */
    @Select("SELECT DATE(created_at) AS day, COUNT(*) AS cnt, IFNULL(SUM(pay_amount),0) AS total " +
            "FROM trade WHERE status=1 AND trade_type='CONSUME' " +
            "AND created_at >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(created_at) ORDER BY day")
    List<Map<String, Object>> trend(@Param("days") int days);

    @Select("SELECT trade_type AS type, COUNT(*) AS cnt, IFNULL(SUM(pay_amount),0) AS total " +
            "FROM trade WHERE status=1 GROUP BY trade_type")
    List<Map<String, Object>> statByType();

    @Select("SELECT COUNT(*) FROM trade WHERE status = 1")
    int countSuccess();

    @Select("SELECT IFNULL(SUM(pay_amount),0) FROM trade WHERE trade_type='CONSUME' AND status=1")
    BigDecimal sumConsumeTotal();
}
