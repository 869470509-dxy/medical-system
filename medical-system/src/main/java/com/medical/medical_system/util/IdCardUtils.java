package com.medical.medical_system.util;

import java.time.LocalDate;
import java.time.Period;

/**
 * 18 位居民身份证工具类
 * <p>
 * 为什么不能只用正则？正则只能保证"18 位、数字形状对"，管不了最后一位校验码。
 * GB 11643-1999 规定第 18 位是前 17 位的 ISO 7064:1983 MOD 11-2 校验码，
 * 随手编的 18 位数字（例如 110101199003074518）大概率校验位是错的。
 * 医疗系统里身份证是患者唯一档案的自然键，录错一位就会把两个人的病历串在一起，
 * 所以必须在入库前做校验位计算。
 */
public final class IdCardUtils {

    /** 格式正则：6 位地区码 + 8 位出生日期 + 3 位顺序码 + 1 位校验码 */
    public static final String REGEX =
            "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$";

    /** 前 17 位加权因子 */
    private static final int[] WEIGHT = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /** 余数 0-10 对应的校验码 */
    private static final char[] CHECK_CODE = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    private IdCardUtils() {
    }

    /**
     * 逐步定位问题，返回具体原因；完全合法返回 null。
     * <p>
     * 比只回一句"格式不正确"有用得多：录入时能立刻知道是位数不对、
     * 生日不是真实日期，还是校验位算错了。
     */
    public static String diagnose(String idCard) {
        if (idCard == null || idCard.isBlank()) {
            return "身份证号不能为空";
        }
        String id = idCard.trim().toUpperCase();
        if (!id.matches(REGEX)) {
            return "身份证号必须为 18 位，且出生日期段格式正确（前 6 位地区码不得以 0 开头）";
        }
        try {
            LocalDate.of(parseYear(id), parseMonth(id), parseDay(id));
        } catch (java.time.DateTimeException e) {
            return "身份证号中的出生日期不存在，请核对第 7-14 位";
        }
        char expect = expectedCheckCode(id);
        if (expect != id.charAt(17)) {
            return "身份证号校验位错误，按 GB 11643 计算末位应为 " + expect;
        }
        return null;
    }

    /**
     * 完整校验：格式 + 出生日期真实存在 + 校验位正确
     */
    public static boolean isValid(String idCard) {
        return diagnose(idCard) == null;
    }

    /** 计算前 17 位对应的正确校验码 */
    public static char expectedCheckCode(String idCard) {
        String id = idCard.trim().toUpperCase();
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (id.charAt(i) - '0') * WEIGHT[i];
        }
        return CHECK_CODE[sum % 11];
    }

    /** 从身份证号解析出生日期；非法返回 null */
    public static LocalDate getBirthDate(String idCard) {
        if (!isValid(idCard)) {
            return null;
        }
        String id = idCard.trim().toUpperCase();
        return LocalDate.of(parseYear(id), parseMonth(id), parseDay(id));
    }

    /** 从第 17 位解析性别：奇数为男(1)，偶数为女(2)；无法解析返回 0 */
    public static int getGender(String idCard) {
        if (!isValid(idCard)) {
            return 0;
        }
        int seq = idCard.trim().charAt(16) - '0';
        return seq % 2 == 1 ? 1 : 2;
    }

    /** 按出生日期计算周岁；非法返回 0 */
    public static int getAge(String idCard) {
        LocalDate birth = getBirthDate(idCard);
        if (birth == null) {
            return 0;
        }
        return Period.between(birth, LocalDate.now()).getYears();
    }

    private static int parseYear(String id) {
        return Integer.parseInt(id.substring(6, 10));
    }

    private static int parseMonth(String id) {
        return Integer.parseInt(id.substring(10, 12));
    }

    private static int parseDay(String id) {
        return Integer.parseInt(id.substring(12, 14));
    }
}
