package com.medical.medical_system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 医疗系统启动类
 *
 * @MapperScan 扫描 Mapper 接口所在包，省略每个接口上的 @Mapper 注解
 */
@SpringBootApplication
@MapperScan("com.medical.medical_system.mapper")
public class MedicalSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicalSystemApplication.class, args);
    }
}
