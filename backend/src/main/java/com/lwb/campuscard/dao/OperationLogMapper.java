package com.lwb.campuscard.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 操作日志数据访问，代理模式统一落库
 */
@Mapper
public interface OperationLogMapper {

    @Insert("INSERT INTO operation_log(op_type, target, detail, cost_ms) " +
            "VALUES(#{opType}, #{target}, #{detail}, #{costMs})")
    int insert(@Param("opType") String opType,
               @Param("target") String target,
               @Param("detail") String detail,
               @Param("costMs") Integer costMs);

    @Select("SELECT * FROM operation_log ORDER BY id DESC LIMIT #{limit}")
    List<Map<String, Object>> recent(@Param("limit") int limit);
}
