package com.medical.medical_system.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

/**
 * 日期序列化配置
 * <p>
 * 说明一个常见的坑：application.yaml 里的 spring.jackson.date-format 只对 java.util.Date 生效，
 * 对 JDK8 的 LocalDateTime 不起作用，不额外配置的话返回体里会出现
 * "2026-11-05T20:30:51" 这种带 T 的 ISO 格式。这里统一成 yyyy-MM-dd HH:mm:ss。
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer javaTimeCustomizer() {
        return builder -> {
            builder.serializers(new LocalDateTimeSerializer(DATE_TIME));
            builder.deserializers(new LocalDateTimeDeserializer(DATE_TIME));
            builder.serializers(new LocalDateSerializer(DATE));
            builder.deserializers(new LocalDateDeserializer(DATE));
        };
    }
}
