package com.lwb.campuscard.dao;

import com.lwb.campuscard.model.entity.DiscountRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 优惠规则数据访问
 */
@Mapper
public interface DiscountRuleMapper {

    @Select("SELECT * FROM discount_rule WHERE code = #{code}")
    DiscountRule findByCode(@Param("code") String code);

    @Select("SELECT * FROM discount_rule WHERE enabled = 1 ORDER BY priority")
    List<DiscountRule> listEnabled();

    @Select("SELECT * FROM discount_rule ORDER BY priority")
    List<DiscountRule> listAll();

    @Update("UPDATE discount_rule SET enabled = #{enabled} WHERE code = #{code}")
    int updateEnabled(@Param("code") String code, @Param("enabled") int enabled);
}
