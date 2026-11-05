package com.medical.medical_system.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 分页结果封装
 * <p>
 * 不直接把 MyBatis-Plus 的 IPage 抛给前端，避免把 orders/optimizeCountSql 等内部字段暴露出去，
 * 只保留前端真正需要的四项：当前页数据、总条数、当前页、每页条数，外加总页数。
 */
@Data
public class PageResult<T> {

    /** 当前页数据 */
    private List<T> records;
    /** 总记录数 */
    private long total;
    /** 当前页码，从 1 开始 */
    private long pageNum;
    /** 每页条数 */
    private long pageSize;
    /** 总页数 */
    private long pages;

    public static <T> PageResult<T> of(IPage<T> page) {
        return of(page, page.getRecords());
    }

    /**
     * 实体分页转 VO 分页：分页元信息沿用原 page，数据换成转换后的列表。
     */
    public static <T> PageResult<T> of(IPage<?> page, List<T> records) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(records);
        r.setTotal(page.getTotal());
        r.setPageNum(page.getCurrent());
        r.setPageSize(page.getSize());
        r.setPages(page.getPages());
        return r;
    }

    /** 条件命中不到任何数据时返回空页，避免前端拿到 null 报错 */
    public static <T> PageResult<T> empty(long pageNum, long pageSize) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(Collections.emptyList());
        r.setTotal(0);
        r.setPageNum(pageNum);
        r.setPageSize(pageSize);
        r.setPages(0);
        return r;
    }
}
