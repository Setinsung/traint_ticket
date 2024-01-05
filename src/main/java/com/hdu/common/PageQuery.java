package com.hdu.common;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import java.lang.ref.PhantomReference;

public class PageQuery {
    @Getter
    @Setter
    @Min(value = 1, message = "当前页不合法")
    private int pageNo = 1;
    @Getter
    @Setter
    @Min(value = 1, message = "每页展示数量不合法")
    private int pageSize = 10;
    @Setter
    private int offset;

    public int getOffset() {
        return (pageNo - 1) * pageSize;
    }
}
