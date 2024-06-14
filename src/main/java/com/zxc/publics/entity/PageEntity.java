package com.zxc.publics.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "分页入参")
public class PageEntity {

    @ApiModelProperty(value = "当前页码")
    private int pageNum = 1;

    @ApiModelProperty(value = "每页条数")
    private int pageSize = 10;

    @ApiModelProperty(value = "开始索引", hidden = true)
    private long indexStart = 0L;

    public PageEntity(int pageSize, long indexStart) {
        this.pageSize = pageSize;
        this.indexStart = indexStart;
        this.pageNum = (int) (indexStart / pageSize) + 1;
    }

}
