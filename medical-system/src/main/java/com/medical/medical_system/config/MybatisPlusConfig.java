package com.medical.medical_system.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置
 * <p>
 * 关键点：分页功能不是"装上 MyBatis-Plus 就自动生效"的，必须在拦截器链里显式注册
 * PaginationInnerInterceptor，否则 Page 对象会被忽略，SQL 退化成全表查询。
 * 这是本实验故障排查表里"分页查询不分页"的根因。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 分页插件：必须指定 DbType，否则方言判断可能出错
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        // 单页最大条数上限，防止前端传 pageSize=999999 把数据库打穿
        pagination.setMaxLimit(100L);
        // 请求页码超过总页数时返回空列表，而不是回到第一页
        pagination.setOverflow(false);
        interceptor.addInnerInterceptor(pagination);

        // 2. 防全表更新/删除插件：拦截没有 where 条件的 update/delete
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }
}
