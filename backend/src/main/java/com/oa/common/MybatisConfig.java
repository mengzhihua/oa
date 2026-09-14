package com.oa.common;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.*;
import org.mybatis.spring.annotation.MapperScan;

@Configuration
@MapperScan("com.oa.**.mapper")
public class MybatisConfig {
    @Bean public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor i = new MybatisPlusInterceptor(); i.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); return i;
    }
}
