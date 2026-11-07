package com.lwb.campuscard.dao;

import com.lwb.campuscard.model.entity.Card;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 卡片数据访问
 */
@Mapper
public interface CardMapper {

    @Select("SELECT * FROM card WHERE card_no = #{cardNo}")
    Card findByCardNo(@Param("cardNo") String cardNo);

    @Select("SELECT * FROM card WHERE account_id = #{accountId}")
    Card findByAccountId(@Param("accountId") Long accountId);

    // Map 结果不会走驼峰转换，这里直接起好别名，前端拿去就能用
    @Select("SELECT c.id, c.card_no AS cardNo, c.card_status AS cardStatus, c.issued_at AS issuedAt, " +
            "a.student_no AS studentNo, a.student_name AS studentName, a.dept, a.balance, a.points, " +
            "a.status AS accountStatus " +
            "FROM card c JOIN account a ON a.id = c.account_id ORDER BY c.id")
    List<Map<String, Object>> listWithAccount();

    @Insert("INSERT INTO card(card_no, account_id, card_status, pay_pwd) " +
            "VALUES(#{cardNo}, #{accountId}, #{cardStatus}, #{payPwd})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Card card);

    @Update("UPDATE card SET card_status = #{status} WHERE card_no = #{cardNo}")
    int updateStatus(@Param("cardNo") String cardNo, @Param("status") int status);

    @Update("UPDATE card SET pay_pwd = #{pwd} WHERE card_no = #{cardNo}")
    int updatePwd(@Param("cardNo") String cardNo, @Param("pwd") String pwd);
}
