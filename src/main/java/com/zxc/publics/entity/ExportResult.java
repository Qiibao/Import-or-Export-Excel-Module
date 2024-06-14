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
@ApiModel(value = "导出结果")
public class ExportResult {

    @ApiModelProperty(value = "已导出总数")
    private int exportCount;

    @ApiModelProperty(value = "已扫描的源数据总数")
    private int sourceCount;

    @ApiModelProperty(value = "导出进度")
    private String progress;

    @ApiModelProperty(value = "导出耗时(ms)")
    private Long timeConsuming;

    @ApiModelProperty(value = "导出是否结束")
    private boolean endFlag;

    @ApiModelProperty(value = "导出文件的下载地址")
    private String downloadUrl;

}
