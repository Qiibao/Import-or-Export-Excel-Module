package com.zxc.publics.constant;

import java.util.UUID;

/**
 * @Description: excel导入组件相关常量
 */
public class ExcelImportConstant {

    // excel的字段表头行索引
    public static final int EXCEL_HEAD_ROW_INDEX = 1;

    // excel单个文件的最大数据量
    public static final int EXCEL_MAX_ALLOW_DATA_NUM = 50000;

    // 读取excel数据时，单次写入内存的最大数据量
    public static final int EXCEL_EVERY_ADD_DATA_NUM = 2000;

    // 导入excel过程中的非法数据导出文件路径
    public static final String EXCEL_ILLEGAL_DATA_EXPORT_PATH = "/home/files/illegalDataExcel/";

    // 导入excel过程中的非法数据导出文件名称
    public static final String EXCEL_ILLEGAL_DATA_EXPORT_NAME = UUID.randomUUID().toString().replace("-", "") + ".xlsx";

    // 默认导出的excel文件写入的sheet名
    public static final String SHEET_NAME= "Sheet1";

    // 默认导出导入失败数据的excel文件名后缀
    public static final String EXCEL_FILE_SUFFIX = ".xlsx";

}
