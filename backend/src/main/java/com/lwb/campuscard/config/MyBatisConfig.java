package com.lwb.campuscard.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 没用 starter，SqlSessionFactory 自己装配
 */
@Configuration
@MapperScan("com.lwb.campuscard.dao")
public class MyBatisConfig {

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);

        org.apache.ibatis.session.Configuration cfg = new org.apache.ibatis.session.Configuration();
        // 数据库下划线列名自动映射到驼峰属性
        cfg.setMapUnderscoreToCamelCase(true);
        cfg.setCacheEnabled(false);
        factory.setConfiguration(cfg);
        return factory.getObject();
    }
}
