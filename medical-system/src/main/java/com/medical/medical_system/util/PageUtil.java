package com.medical.medical_system.util;

/**
 * 分页参数收敛
 * <p>
 * 前端传什么都不能直接进 SQL：pageNum 传 0 或负数会算出负偏移量，
 * pageSize 传 100000 会让一次查询把整张表拉进内存。统一在这里夹到安全区间。
 */
public final class PageUtil {

    private static final long MAX_PAGE_SIZE = 100L;

    private PageUtil() {
    }

    public static long pageNum(Long pageNum) {
        return pageNum == null || pageNum < 1 ? 1L : pageNum;
    }

    public static long pageSize(Long pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10L;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
