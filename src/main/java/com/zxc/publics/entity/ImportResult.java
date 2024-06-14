package com.zxc.publics.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "导入结果")
public class ImportResult {

    @ApiModelProperty(value = "导入成功条数")
    private int successCount;

    @ApiModelProperty(value = "校验失败条数")
    private int failCount;

    @ApiModelProperty(value = "导入总数")
    private int totalCount;

    @ApiModelProperty(value = "源数据总数")
    private int sourceCount;

    @ApiModelProperty(value = "导入进度")
    private String progress;

    @ApiModelProperty(value = "导入耗时(ms)")
    private Long timeConsuming;

    @ApiModelProperty(value = "导入失败数据文件地址")
    private String failDataFileUrl;

}
