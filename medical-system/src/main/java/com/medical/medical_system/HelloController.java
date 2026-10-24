package com.medical.medical_system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/test/hello")
    public String hello(){
        return "医疗系统测试接口访问成功！";
    }
}
