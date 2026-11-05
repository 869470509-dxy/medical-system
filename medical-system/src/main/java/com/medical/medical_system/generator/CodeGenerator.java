package com.medical.medical_system.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

/**
 * MyBatis-Plus 代码生成器
 * <p>
 * 用法：右键运行 main 方法（或 mvn 执行），按 medical_system 库里的表结构
 * 自动生成 entity / mapper / service / serviceImpl / controller 五层骨架，
 * 省掉纯模板代码。生成的代码只是起点，业务规则（查重、校验、分页条件）
 * 仍需在 Service 里补全。
 * <p>
 * 输出目录默认 src/main/java，可用 -Dgenerator.out=... 覆盖，
 * 便于"先生成到临时目录、再人工挑选合入"的工作方式。
 */
public class CodeGenerator {

    private static final String URL =
            "jdbc:mysql://localhost:3306/medical_system?useUnicode=true&characterEncoding=utf8"
                    + "&serverTimezone=Asia/Shanghai&useSSL=false";
    private static final String USERNAME = System.getenv().getOrDefault("MYSQL_USERNAME", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("MYSQL_PASSWORD", "123456");

    public static void main(String[] args) {
        String baseDir = System.getProperty("generator.out");
        if (baseDir == null || baseDir.isBlank()) {
            baseDir = System.getProperty("user.dir");
        }
        String javaDir = baseDir + "/src/main/java";
        String xmlDir = baseDir + "/src/main/resources/mapper";

        FastAutoGenerator.create(URL, USERNAME, PASSWORD)
                // ---------- 全局配置 ----------
                .globalConfig(builder -> builder
                        .author("董欣雨")
                        .outputDir(javaDir)
                        .disableOpenDir()
                        .commentDate("yyyy-MM-dd"))
                // ---------- 包配置 ----------
                .packageConfig(builder -> builder
                        .parent("com.medical.medical_system")
                        .entity("entity")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .controller("controller")
                        .pathInfo(Collections.singletonMap(OutputFile.xml, xmlDir)))
                // ---------- 策略配置 ----------
                .strategyConfig(builder -> builder
                        // 只为这三张需要 CRUD 的表生成代码；挂号、处方本实验只做表设计
                        .addInclude("department", "doctor", "patient")
                        .entityBuilder()
                        .enableLombok()
                        .enableTableFieldAnnotation()
                        .controllerBuilder()
                        .enableRestStyle()
                        .serviceBuilder()
                        .formatServiceFileName("%sService")
                        .formatServiceImplFileName("%sServiceImpl"))
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();

        System.out.println("代码生成完成，输出目录：" + javaDir);
    }
}
