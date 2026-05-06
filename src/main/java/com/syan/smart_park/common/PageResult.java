package com.syan.smart_park.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 分页结果封装
 */
@Data
public class PageResult<T> {

    private List<T> records;      // 当前页数据
    private long total;           // 总记录数
    private long current;         // 当前页码
    private long size;            // 每页大小
    private long pages;           // 总页数

    public static <T> PageResult<T> empty() {
        PageResult<T> result = new PageResult<>();
        result.setRecords(Collections.emptyList());
        result.setTotal(0);
        result.setCurrent(1);
        result.setSize(10);
        result.setPages(0);
        return result;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }

    public static <T> PageResult<T> of(List<T> records, long total, long current, long size) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setCurrent(current);
        result.setSize(size);
        result.setPages(size > 0 ? (total + size - 1) / size : 0);
        return result;
    }
}
