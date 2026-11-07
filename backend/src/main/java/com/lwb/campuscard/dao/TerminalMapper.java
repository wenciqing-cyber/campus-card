package com.lwb.campuscard.dao;

import com.lwb.campuscard.model.entity.Terminal;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 终端数据访问
 */
@Mapper
public interface TerminalMapper {

    @Select("SELECT * FROM terminal WHERE terminal_no = #{terminalNo}")
    Terminal findByNo(@Param("terminalNo") String terminalNo);

    @Select("SELECT * FROM terminal ORDER BY id")
    List<Terminal> listAll();

    @Insert("INSERT INTO terminal(terminal_no, terminal_name, terminal_type, merchant, rate, status) " +
            "VALUES(#{terminalNo}, #{terminalName}, #{terminalType}, #{merchant}, #{rate}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Terminal terminal);

    @Update("UPDATE terminal SET status = #{status} WHERE terminal_no = #{terminalNo}")
    int updateStatus(@Param("terminalNo") String terminalNo, @Param("status") int status);
}
