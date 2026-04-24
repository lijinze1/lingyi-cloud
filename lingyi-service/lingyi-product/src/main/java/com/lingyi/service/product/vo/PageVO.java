package com.lingyi.service.product.vo;

import java.util.List;
import lombok.Data;

@Data
public class PageVO<T> {
    private long pageNo;
    private long pageSize;
    private long total;
    private List<T> records;

    public static <T> PageVO<T> of(long pageNo, long pageSize, long total, List<T> records) {
        PageVO<T> vo = new PageVO<>();
        vo.setPageNo(pageNo);
        vo.setPageSize(pageSize);
        vo.setTotal(total);
        vo.setRecords(records);
        return vo;
    }
}
