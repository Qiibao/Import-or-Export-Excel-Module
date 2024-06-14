package com.zxc.publics.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ExportExcelEntity {

    @ApiModelProperty("文件下载地址前缀")
    private String downloadUrlPrefix;

    @ApiModelProperty("导出的文件存储地址")
    private String filePath;

    @ApiModelProperty("导出excel文件名")
    private String excelFileName;

    @ApiModelProperty("导出zip文件名")
    private String zipFileName;

    @ApiModelProperty("excel文件表头")
    private String excelHeader;

    @ApiModelProperty("excel文件sheet页名称")
    private String sheetName;

    /**
     * 获取导出excel文件下载地址
     */
    public String getExcelDownloadUrl() {
        return downloadUrlPrefix + filePath + excelFileName;
    }

    /**
     * 获取导出zip文件下载地址
     */
    public String getZipDownloadUrl() {
        return downloadUrlPrefix + filePath + zipFileName;
    }

}
