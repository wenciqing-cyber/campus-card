package com.lwb.campuscard.dao;

import com.lwb.campuscard.model.entity.Account;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户数据访问
 */
@Mapper
public interface AccountMapper {

    @Select("SELECT * FROM account WHERE id = #{id}")
    Account findById(@Param("id") Long id);

    @Select("SELECT * FROM account WHERE student_no = #{studentNo}")
    Account findByStudentNo(@Param("studentNo") String studentNo);

    @Select("SELECT a.* FROM account a JOIN card c ON c.account_id = a.id WHERE c.card_no = #{cardNo}")
    Account findByCardNo(@Param("cardNo") String cardNo);

    @Select("SELECT * FROM account ORDER BY id")
    List<Account> listAll();

    @Select("SELECT COUNT(*) FROM account")
    int countAll();

    /** 原子扣减：余额不足时影响行数为 0，靠这个判并发安全 */
    @Update("UPDATE account SET balance = balance - #{amount}, version = version + 1 " +
            "WHERE id = #{id} AND balance >= #{amount}")
    int deduct(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE account SET balance = balance + #{amount}, version = version + 1 WHERE id = #{id}")
    int addBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    /** 积分变动，delta 可为负；扣到负会失败 */
    @Update("UPDATE account SET points = points + #{delta} WHERE id = #{id} AND points + #{delta} >= 0")
    int addPoints(@Param("id") Long id, @Param("delta") int delta);

    @Insert("INSERT INTO account(student_no, student_name, dept, balance, points, status) " +
            "VALUES(#{studentNo}, #{studentName}, #{dept}, #{balance}, #{points}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Account account);

    @Update("UPDATE account SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
