package com.medical.medical_system.util;

import java.time.LocalDate;

/**
 * 快速自检：直接运行 main 方法可验证校验算法是否正确
 * <p>
 * 常用测试样本：
 * 110101199003074514 -> 合法（校验位 4）
 * 110101199003074518 -> 非法（校验位应为 4）
 * 110101199002304511 -> 非法（2 月 30 日不存在）
 */
public final class IdCardDemo {

    public static void main(String[] args) {
        String[] samples = {
                "110101199003074514",
                "110101199003074518",
                "110101199002304511",
                "310101199205126623"
        };
        for (String s : samples) {
            System.out.printf("%s  合法=%s  出生=%s  性别=%d  周岁=%d%n",
                    s, IdCardUtils.isValid(s), IdCardUtils.getBirthDate(s),
                    IdCardUtils.getGender(s), IdCardUtils.getAge(s));
        }
        System.out.println("今天是 " + LocalDate.now());
    }
}
