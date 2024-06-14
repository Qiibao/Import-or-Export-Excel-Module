package com.zxc.publics.constant;

/**
 * excel导出组件相关常量
 */
public class ExcelExportConstant {

    // excel导出单个文件的最大数据量
    public static final int EXCEL_MAX_EXPORT_COUNT = 10000;

    // excel导出单次查询结果集的最大数据量
    public static final int EXCEL_EVERY_QUERY_DATA_NUM = 1000;

    // excel导出文件地址
    public static final String EXCEL_EXPORT_FILE_PATH = "/home/files/exportExcelFiles/";

    // 默认导出的excel文件写入的sheet名
    public static final String SHEET_NAME= "Sheet1";

    // 默认导出的文件名前缀
    public static final String EXCEL_FILE_PREFIX = "exportData_";

    // 默认导出的excel文件名后缀
    public static final String EXCEL_FILE_SUFFIX = ".xlsx";

    // 默认导出的zip文件名后缀
    public static final String ZIP_FILE_SUFFIX = ".zip";

}
